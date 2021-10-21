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
    showUserMessage(response.response);
    if (!response.success) grecaptcha.reset();
}

function sendResetRequestData() {
    var formData = new FormData(document.getElementById("reset-form"));
    var values = [];
    formData.forEach(item => values.push(item));
    finalData = {
        "type": "user-forgottenpassword",
        "data": {
            "username": values[0],
            "captcha-token": values[1]
        }
    }
    Gateway.send(JSON.stringify(finalData));
}