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
    if (response.success) {
        window.localStorage.setItem("token", response.response.token);
        window.location.href = location.protocol + "//" + location.host + "/html/home.html";
    } else {
        grecaptcha.reset();
        showModalMessage("Error", response.response);
    }
}

function sendSignupData() {
    let formData = new FormData(document.getElementById("signup-form"));
    let avatar = document.getElementById("avatar-selector").value;
    let values = [];
    formData.forEach(item => values.push(item));
    let finalData = {
            "type": "user-signup",
            "data": {
                "username": values[0],
                "password": values[1],
                "avatar": avatar,
                "password-confirm": values[2],
                "captcha-token": values[3]
            }
        }
    Gateway.send(JSON.stringify(finalData));
}

function setAvatarUrl(avatar) {
    document.getElementById("avatar-preview").src = "/img/avatars/" + avatar + ".png";
}

document.getElementById("avatar-selector").onchange = function() {
    setAvatarUrl(document.getElementById("avatar-selector").value);
}