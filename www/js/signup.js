const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function(content) {
    console.log("Connection Lost");
}

function showUserMessage(message) {
    document.getElementById("user-message").style.display = "block";
    document.getElementById("user-message").innerHTML = message;
}

function hideUserMessage() {
    document.getElementById("user-message").style.display = "none";
}

function reactToAuth(data) {
    const success = data.success;

    if (success) {
        showUserMessage("Account Created!");
        document.getElementById("signup-button").disabled = true;
    } else {
        showUserMessage(data.message);
    }
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    if (response.success) {
        showUserMessage("Account Created!");
        document.cookie = JSON.stringify({ "token": response.response });
        window.location.href = location.protocol + "//" + location.host + "/home.html";
    } else {
        showUserMessage(response.response);
    }
}

function createSignupPayload(username, password) {
    const payload = {
        "type": "user-signup",
        "data": {
            "username": username,
            "password": password
        }
    }
    return JSON.stringify(payload);
}

function sendSignupData() {

    const integerTest = /\d/;

    const username = document.getElementById("username-input").value;
    const password = document.getElementById("password-input").value;
    const passwordRepeat = document.getElementById("password-input-repeat").value;

    if (password !== passwordRepeat) {
        showUserMessage("Your passwords do not match!");
    } else if (!integerTest.test(password)) {
        showUserMessage("Your password needs to contain a number!");
    } else {
        Gateway.send(createSignupPayload(username, password));
        hideUserMessage();
    }
}