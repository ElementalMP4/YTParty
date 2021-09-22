const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var USER_PROPERTIES;
var TOKEN;
var PLAYER;
var CURRENT_VIDEO_ID;
var ROOM_ID;
var LAST_MESSAGE_AUTHOR;
var CAN_CONTROL_PLAYER;
var ROOM_COLOUR;

var TYPING_COUNT = 0;

var TYPING = false;
var PLAYER_READY = false;

function showTypingMessage() {
    document.getElementById("typing-message").style.display = "block";
}

function hideTypingMessage() {
    document.getElementById("typing-message").style.display = "none";
}

function updateTyping(data) {
    if (data.user == USER_PROPERTIES.username) return;
    if (data.mode == "start") TYPING_COUNT = TYPING_COUNT + 1;
    else TYPING_COUNT = TYPING_COUNT - 1;

    if (TYPING_COUNT > 0) showTypingMessage();
    else hideTypingMessage();
};

function sendGatewayMessage(message) {
    Gateway.send(JSON.stringify(message));
}

function addChatMessage(data) {
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

function displayLocalMessage(message) {
    addChatMessage({ "author": "System", "colour": ROOM_COLOUR, "content": message, "modifiers": "system" });
}

function sendPlayingMessage() {
    let time = PLAYER.getCurrentTime();
    sendGatewayMessage({ "type": "party-playvideo", "data": { "token": TOKEN, "roomID": ROOM_ID, "timestamp": time } });
    displayLocalMessage("Video playing at " + new Date(time * 1000).toISOString().substr(11, 8));
}

function sendPausedMessage() {
    sendGatewayMessage({ "type": "party-pausevideo", "data": { "token": TOKEN, "roomID": ROOM_ID } });
    displayLocalMessage("Video paused");
}

function onYouTubeIframeAPIReady() {
    PLAYER = new YT.Player('player', {
        height: '100%',
        width: '80%',
        playerVars: { 'controls': CAN_CONTROL_PLAYER ? 1 : 0 },
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
    console.log(event);
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
    if (PLAYER.getPlayerState() !== YT.PlayerState.PLAYING) {
        PLAYER.seekTo(data.time, true);
        PLAYER.playVideo();
    }
}

function pauseVideo() {
    if (PLAYER.getPlayerState() !== YT.PlayerState.PAUSED) {
        PLAYER.pauseVideo();
    }
}

function handleChatMessage(data) {
    addChatMessage(data);
}

function handleSystemMessage(data) {
    switch (data.type) {
        case "playvideo":
            startVideo(data.data);
            break;
        case "pausevideo":
            pauseVideo();
            break;
        case "changevideo":
            loadVideo(data.data.video);
            break;
        case "typingupdate":
            updateTyping(data.data);
            break;
    }
}

function initialiseParty(response) {
    let options = JSON.parse(response);
    loadVideo(options.video);
    CAN_CONTROL_PLAYER = options.canControl;
    ROOM_COLOUR = options.theme;

    var chatInput = document.getElementById("chat-input");
    chatInput.addEventListener("focus", function() {
        this.style.borderBottom = "2px solid " + ROOM_COLOUR;
    });

    chatInput.addEventListener("blur", function() {
        this.style.borderBottom = "2px solid grey";
    });

    displayLocalMessage("This party's Room ID is " + ROOM_ID + "<br> Use /help to see some chat commands!");
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

    if (!response.success) {
        switch (response.response) {
            case "An invalid token was provided":
                window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
                break;
            case "An invalid room ID was provided":
                displayLocalMessage("This room does not exist! Check the room ID and try again!");
                break;
        }
    }

    switch (response.origin) {
        case "party-joinparty":
            initialiseParty(response.response);
            break;
        case "user-getprofile":
            USER_PROPERTIES = JSON.parse(response.response);
            break;
        case "party-chatmessage":
            if (!response.success) displayLocalMessage(response.response);
            break;

    }

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
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
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
        window.location.href = location.protocol + "//" + location.host + "/home.html";
    } else {
        ROOM_ID = selfURL.searchParams.get("roomID");
        embedPlayer();
        console.log("Ready");
        sendGatewayMessage({ "type": "party-joinparty", "data": { "token": TOKEN, "roomID": ROOM_ID } });
        sendGatewayMessage({ "type": "user-getprofile", "data": { "token": TOKEN } });
    }
}

Gateway.onclose = function() {
    displayLocalMessage("You lost connection to the server! Use /r to reconnect");
}

function handleSetVideoCommand(args) {
    let videoURL = new URL(args[0]);
    let videoID = videoURL.searchParams.get("v");
    if (!videoID) return;
    else sendGatewayMessage({ "type": "party-changevideo", "data": { "token": TOKEN, "roomID": ROOM_ID, "video": videoID } });
}

function handleHelpCommand() {
    displayLocalMessage(`
    ~ ~ YTParty Help ~ ~<br>
    /help - shows this message<br><br>
    /setvideo [video URL] - changes the video<br><br>
    /id - Shows you the room ID<br><br>
    /i [message] - changes your message to italics<br><br>
    /u [message] - changes your message to underline<br><br>
    /b [message] - makes your message bold<br><br>
    /s [message] - changes your message to strikethrough<br><br>
    /c [message] - changes your message to cursive<br><br>
    /big [message] - makes your message big<br><br>
    /r - reloads your session
    `);
}

function sendTypingStop() {
    if (TYPING) {
        TYPING = false;
        sendGatewayMessage({ "type": "party-typingupdate", "data": { "token": TOKEN, "roomID": ROOM_ID, "mode": "stop", "user": USER_PROPERTIES.username } });
    }
}

function sendTypingStart() {
    if (!TYPING) {
        TYPING = true;
        sendGatewayMessage({ "type": "party-typingupdate", "data": { "token": TOKEN, "roomID": ROOM_ID, "mode": "start", "user": USER_PROPERTIES.username } });
    }
}

document.getElementById("chat-input").addEventListener("keyup", function(event) {
    if (event.keyCode === 13) {
        sendTypingStop();
        event.preventDefault();
        let message = document.getElementById("chat-input").value.trim();
        if (message == "") return;
        if (message.length > 800) {
            displayLocalMessage("Your message is too long! Messages cannot be longer than 800 characters.");
            return;
        }

        let sendChatMessage = true;
        let modifiers = "";

        if (message.startsWith("/")) {
            const args = message.slice(1).split(/ +/);
            const command = args.shift().toLowerCase();

            switch (command) {
                case "help":
                    handleHelpCommand();
                    sendChatMessage = false;
                    break;
                case "id":
                    displayLocalMessage("This Party's Room ID is " + ROOM_ID);
                    sendChatMessage = false;
                    break;
                case "setvideo":
                    handleSetVideoCommand(args);
                    break;
                case "i":
                    modifiers = "italic";
                    message = args.join(" ");
                    break;
                case "u":
                    modifiers = "underline";
                    message = args.join(" ");
                    break;
                case "b":
                    modifiers = "bold";
                    message = args.join(" ");
                    break;
                case "s":
                    modifiers = "strikethrough";
                    message = args.join(" ");
                    break;
                case "c":
                    modifiers = "cursive";
                    message = args.join(" ");
                    break;
                case "big":
                    modifiers = "big";
                    message = args.join(" ");
                    break;
                case "r":
                    sendChatMessage = false;
                    location.reload();
                    break;
            }
        }
        if (sendChatMessage) {
            sendGatewayMessage({
                "type": "party-chatmessage",
                "data": {
                    "token": TOKEN,
                    "roomID": ROOM_ID,
                    "content": message,
                    "colour": USER_PROPERTIES.colour,
                    "author": USER_PROPERTIES.effectiveName,
                    "modifiers": modifiers
                }
            });
        }
        document.getElementById("chat-input").value = "";
    } else {
        let message = document.getElementById("chat-input").value.trim();
        if (message == "") sendTypingStop();
        else sendTypingStart();
    }
});