const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

function showUserMessage(message) {
    document.getElementById("user-message").style.display = "block";
    document.getElementById("user-message").innerHTML = message;
}

function hideUserMessage() {
    document.getElementById("user-message").style.display = "none";
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    if (response.success) {
        showUserMessage("Logging you in!");
        document.cookie = JSON.stringify({ "token": response.response });
        window.location.href = location.protocol + "//" + location.host + "/home.html";
    } else {
        showUserMessage(response.response);
    }
}

function createLoginPayload(username, password) {
    const payload = {
        "type": "user-signin",
        "data": {
            "username": username,
            "password": password
        }
    }
    return JSON.stringify(payload);
}

function sendLoginData() {
    const username = document.getElementById("username-input").value;
    const password = document.getElementById("password-input").value;

    Gateway.send(createLoginPayload(username, password));
}