"use strict";
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

Gateway.onopen = function() {
    console.log("Connected To Gateway");
    if (window.localStorage.getItem("token") !== null)
        window.location.href = location.protocol + "//" + location.host + "/home.html";
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
        window.location.href = location.protocol + "//" + location.host + (redirect == null ? "/home.html" : redirect);
    } else {
        grecaptcha.reset();
        showModalMessage("Error", response.response);
    }
}

function sendLoginData() {
    var formData = new FormData(document.getElementById("signin-form"));
    var values = [];
    formData.forEach(item => values.push(item));
    var finalData = {
        "type": "user-signin",
        "data": {
            "username": values[0],
            "password": values[1],
            "captcha-token": values[2]
        }
    }
    Gateway.send(JSON.stringify(finalData));
}