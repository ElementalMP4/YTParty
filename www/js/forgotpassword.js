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

function sendResetData() {
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    let passwordConfirm = document.getElementById("password-confirm").value;
    let otpCode = document.getElementById("otp-code").value;
    let finalData = {
            "type": "user-resetpassword",
            "data": {
                "username": username,
                "captcha-token": grecaptcha.getResponse(),
                "otp": otpCode,
                "password": password,
                "password-confirm": passwordConfirm
            }
        }
    Gateway.send(JSON.stringify(finalData));
}