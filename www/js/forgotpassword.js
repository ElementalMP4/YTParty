"use strict";
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

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

function sendResetRequestData() {
    let formData = new FormData(document.getElementById("reset-form"));
    let values = [];
    formData.forEach(item => values.push(item));
    let finalData = {
        "type": "user-forgottenpassword",
        "data": {
            "username": values[0],
            "captcha-token": values[1]
        }
    }
    Gateway.send(JSON.stringify(finalData));
}