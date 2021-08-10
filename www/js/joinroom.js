const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var globalRoomID;

function showUserMessage(message) {
    document.getElementById("ID-message").style.display = "block";
    document.getElementById("ID-message").innerHTML = message;
}

function hideUserMessage() {
    document.getElementById("ID-message").style.display = "none";
}

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
        showUserMessage("Redirecting to room");
        window.location.href = location.protocol + "//" + location.host + "/player.html?roomID=" + globalRoomID;
    } else {
        showUserMessage(response.response);
    }
}

function getToken() {
    if (document.cookie == "") {
        window.location.href = location.protocol + "//" + location.host + "/login.html";
    } else {
        const cookie = JSON.parse(document.cookie);
        return cookie.token;
    }
}

function sendRoomPayload(roomID) {
    globalRoomID = roomID;
    const payload = {
        "type": "party-joinparty",
        "data": {
            "token": getToken(),
            "roomID": roomID
        }
    }
    Gateway.send(JSON.stringify(payload));
}

function joinRoom() {
    const roomID = document.getElementById("ID-entry").value;
    if (roomID == "") {
        showUserMessage("You need to provide an ID!");
    } else {
        sendRoomPayload(roomID);
    }
}