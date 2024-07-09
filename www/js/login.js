"use strict";

const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway;

if (window.localStorage.getItem("token") !== null) window.location.href = location.protocol + "//" + location.host + "/html/home.html";
else Gateway = new WebSocket(GatewayServerURL);

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
        let url = new URL(location.href);
        let redirect = url.searchParams.get("redirect");
        window.localStorage.setItem("token", response.response.token);
        window.location.href = location.protocol + "//" + location.host + (redirect == null ? "/html/home.html" : redirect);
    } else {
        grecaptcha.reset();
        showModalMessage("Error", response.response);
    }
}

function sendLoginData() {
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    let loginData = {
        "type": "user-signin",
        "data": {
            "username": username,
            "password": password,
            "captcha-token": grecaptcha.getResponse()
        }
    }
    Gateway.send(JSON.stringify(loginData));
}