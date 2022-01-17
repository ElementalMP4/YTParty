"use strict";
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
    showModalMessage(response.success ? "Success!" : "Error", response.response);
    if (!response.success) grecaptcha.reset();
}

function sendResetData() {
    var formData = new FormData(document.getElementById("reset-form"));
    var values = [];

    let url = new URL(location.href);
    let resetToken = url.searchParams.get("token");
    let username = url.searchParams.get("user");

    formData.forEach(item => values.push(item));
    finalData = {
        "type": "user-resetpassword",
        "data": {
            "password": values[0],
            "password-confirm": values[1],
            "captcha-token": values[2],
            "reset-token": resetToken,
            "username": username
        }
    }
    Gateway.send(JSON.stringify(finalData));
}