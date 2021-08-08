const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var USER_PROPERTIES;
var TOKEN;
var PLAYER;
var CURRENT_VIDEO_ID;

var PLAYER_READY = false;

function onYouTubeIframeAPIReady() {
    PLAYER = new YT.Player('player', {
        height: '100%',
        width: '80%',
        videoId: CURRENT_VIDEO_ID,
        playerVars: {
            'playsinline': 1
        },
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        }
    });
}

function onPlayerReady() {
    PLAYER_READY = true;
}

function onPlayerStateChange() {

}

function loadVideo(youTubeVideoID) {
    CURRENT_VIDEO_ID = youTubeVideoID;
    if (PLAYER_READY) PLAYER.loadVideoById(youTubeVideoID, 0);
}

function handleChatMessage() {

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

    switch (response.type) {
        case "party-chatmessage":
            handleChatMessage(response.data);
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

Gateway.onopen = function() {
    const selfURL = new URL(location.href);
    TOKEN = getToken();
    if (!selfURL.searchParams.get("roomID")) {
        window.location.href = "http://" + location.host + "/login.html";
    } else {
        console.log("Ready");
        Gateway.send(JSON.stringify({ "type": "party-joinparty", "data": { "token": TOKEN, "roomID": selfURL.searchParams.get("roomID") } }));
    }
}

function embedPlayer() {
    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
}
embedPlayer();