"use strict";

const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

function logout() {
    window.localStorage.removeItem("token");
    window.location.href = location.protocol + "//" + location.host;
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

function handleProfileResponse(response) {
    let userProfile = response.response;
    document.getElementById("welcome-banner").innerHTML = "Good to see you, " + userProfile.username + "!";
    Gateway.close();
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    handleProfileResponse(response);
}

function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/html/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

function getUserProfile() {
    let payload = {
        "type": "user-getprofile",
        "data": {
            "token": getToken()
        }
    }
    Gateway.send(JSON.stringify(payload));
}

Gateway.onopen = function() {
    console.log("Connected To Gateway");
    getUserProfile();
}