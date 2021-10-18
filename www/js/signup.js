const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    if (response.success) {
        showUserMessage("Account Created!");
        window.localStorage.setItem("token", response.response);
        window.location.href = location.protocol + "//" + location.host + "/home.html";
    } else {
        grecaptcha.reset();
        showUserMessage(response.response);
    }
}

function showUserMessage(message) {
    document.getElementById("user-message").style.display = "block";
    document.getElementById("user-message").innerHTML = message;
}

function hideUserMessage() {
    document.getElementById("user-message").style.display = "none";
}

function sendSignupData() {
    var formData = new FormData(document.getElementById("signup-form"));
    var values = [];
    formData.forEach(item => values.push(item));
    finalData = {
        "type": "user-signup",
        "data": {
            "username": values[0],
            "password": values[1],
            "password-confirm": values[2],
            "captcha-token": values[3]
        }
    }
    Gateway.send(JSON.stringify(finalData));
}