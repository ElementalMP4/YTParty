const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var USER_PROPERTIES;
var TOKEN;
var PLAYER;
var CURRENT_VIDEO_ID;
var ROOM_ID;

var LAST_RECEIVED_MESSAGE;
var LAST_SENT_MESSAGE;

var LAST_MESSAGE_AUTHOR;

var PLAYER_READY = false;

function showTypingMessage() {
    document.getElementById("typing-message").style.display = "block";
}

function hideTypingMessage() {
    document.getElementById("typing-message").style.display = "none";
}

function sendGatewayMessage(message) {
    if (message.type == LAST_SENT_MESSAGE && message.type !== "party-chatmessage") return;
    if (message.type !== "party-chatmessage") LAST_SENT_MESSAGE = message.type;
    Gateway.send(JSON.stringify(message));
}

function sendPlayingMessage() {
    let time = PLAYER.getCurrentTime();
    sendGatewayMessage({ "type": "party-playvideo", "data": { "token": TOKEN, "roomID": ROOM_ID, "timestamp": time } });
}

function sendPausedMessage() {
    sendGatewayMessage({ "type": "party-pausevideo", "data": { "token": TOKEN, "roomID": ROOM_ID } });
}

function onYouTubeIframeAPIReady() {
    PLAYER = new YT.Player('player', {
        height: '100%',
        width: '80%',
        videoId: CURRENT_VIDEO_ID,
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        }
    });
}

function onPlayerReady() {
    PLAYER_READY = true;
}

function onPlayerStateChange(event) {
    let playerState = event.data;

    switch (playerState) {
        case 1:
            sendPlayingMessage();
            break;
        case 2:
            sendPausedMessage();
            break;
    }
}

function loadVideo(youTubeVideoID) {
    CURRENT_VIDEO_ID = youTubeVideoID;
    if (PLAYER_READY) PLAYER.loadVideoById(youTubeVideoID, 0);
}

function startVideo(data) {
    PLAYER.seekTo(data.time, true);
    PLAYER.playVideo();
}

function pauseVideo() {
    PLAYER.pauseVideo();
}

function handleChatMessage(data) {
    let author = data.author;
    let colour = data.colour;
    let content = data.content;
    let modifiers = data.modifiers !== "" ? `class="${data.modifiers}"` : "";

    let newMessage = `<div class="chat-message">`;
    if (LAST_MESSAGE_AUTHOR !== author) newMessage += `<p class="msg-nickname" style="color:${colour}">${author}</p><br>`;
    newMessage += `<p ${modifiers}>${content}</p></div>`;
    if (LAST_MESSAGE_AUTHOR !== author) newMessage += "<br>";

    LAST_MESSAGE_AUTHOR = author;

    $("#chat-history").prepend(newMessage);
    $('#chat-history').scrollTop($('#chat-history')[0].scrollHeight);
}

function handleSystemMessage(data) {
    if (data.type == LAST_RECEIVED_MESSAGE || data.type == LAST_SENT_MESSAGE) return;
    LAST_RECEIVED_MESSAGE = data.type;

    switch (data.type) {
        case "playvideo":
            startVideo(data.data);
            break;
        case "pausevideo":
            pauseVideo();
            break;
    }
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

    if (response.origin == "party-joinparty") loadVideo(JSON.parse(response.response).video);
    if (response.origin == "user-getprofile") USER_PROPERTIES = JSON.parse(response.response);

    switch (response.type) {
        case "party-chatmessage":
            handleChatMessage(response.data);
            break;
        case "party-systemmessage":
            handleSystemMessage(response.data);
            break;
    }
}

function getToken() {
    if (document.cookie == "") {
        window.location.href = "http://" + location.host + "/login.html";
    } else {
        const cookie = JSON.parse(document.cookie);
        return cookie.token;
    }
}

function embedPlayer() {
    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
}

Gateway.onopen = function() {
    hideTypingMessage();
    const selfURL = new URL(location.href);
    TOKEN = getToken();

    if (!selfURL.searchParams.get("roomID")) {
        window.location.href = "http://" + location.host + "/login.html";
    } else {
        ROOM_ID = selfURL.searchParams.get("roomID");
        embedPlayer();
        console.log("Ready");
        sendGatewayMessage({ "type": "party-joinparty", "data": { "token": TOKEN, "roomID": ROOM_ID } });
        sendGatewayMessage({ "type": "user-getprofile", "data": { "token": TOKEN } });
    }
}

document.getElementById("chat-input").addEventListener("keyup", function(event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        let message = document.getElementById("chat-input").value;
        sendGatewayMessage({
            "type": "party-chatmessage",
            "data": {
                "token": TOKEN,
                "roomID": ROOM_ID,
                "content": message,
                "colour": USER_PROPERTIES.colour,
                "author": USER_PROPERTIES.name,
                "modifiers": ""
            }
        });
        document.getElementById("chat-input").value = "";
    }
});