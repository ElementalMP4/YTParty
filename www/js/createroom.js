"use strict";
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var TOKEN;

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

Gateway.onmessage = function(message) {
    const serverMessage = JSON.parse(message.data);
    console.log(serverMessage);
    if (serverMessage.success) {
        window.location.href = location.protocol + "//" + location.host + "/player.html?roomID=" + serverMessage.response.partyID;
    } else {
        showModalMessage("Error", serverMessage.response);
    }
}

function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

function sendRoomPayload(videoID) {
    const ownerControlsOnly = !document.getElementById("owner-only-checkbox").checked;
    const theme = document.getElementById("room-colour-picker").value;
    const payload = {
        "type": "party-createparty",
        "data": {
            "token": TOKEN,
            "ownerControlsOnly": ownerControlsOnly,
            "theme": theme,
            "videoID": videoID
        }
    }
    Gateway.send(JSON.stringify(payload));
}

function createRoom() {
    document.getElementById("video-message").innerHTML = "";
    try {
        const videoURL = document.getElementById("video-entry").value;
        const urlObject = new URL(videoURL);
        const videoID = urlObject.searchParams.get("v");
        sendRoomPayload(videoID);
    } catch {
        showModalMessage("Error", "You supplied an invalid link!");
    }
}

TOKEN = getToken();