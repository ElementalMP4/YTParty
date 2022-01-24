"use strict";
const YOUTUBE_URL = "https://youtube.com/watch?v=";
const GATEWAY_URL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GATEWAY_URL);

let Globals = {
    USER_PROPERTIES: {},
    TOKEN: "",
    PLAYER: {},
    CURRENT_VIDEO_ID: "",
    ROOM_ID: "",
    LAST_MESSAGE_AUTHOR: "",
    CAN_CONTROL_PLAYER: false,
    ROOM_COLOUR: "",
    TYPING_COUNT: 0,
    TYPING: false,
    PLAYER_READY: false
}

function data(params) {
    const defaultParams = { token: Globals.TOKEN, roomID: Globals.ROOM_ID };
    return {...defaultParams, ...params };
}

function showTypingMessage() {
    document.getElementById("typing-message").style.display = "block";
}

function hideTypingMessage() {
    document.getElementById("typing-message").style.display = "none";
}

function updateTyping(data) {
    if (data.user == Globals.USER_PROPERTIES.username) return;
    if (data.mode == "start") Globals.TYPING_COUNT = Globals.TYPING_COUNT + 1;
    else Globals.TYPING_COUNT = Globals.TYPING_COUNT - 1;

    if (Globals.TYPING_COUNT > 0) showTypingMessage();
    else hideTypingMessage();
};

function sendGatewayMessage(message) {
    Gateway.send(JSON.stringify(message));
}

function addChatMessage(data) {
    const author = data.author;
    const colour = data.colour;
    const content = data.content;
    const modifiers = data.modifiers !== "" ? `class="${data.modifiers}"` : "";
    const avatar = data.avatar;

    let newMessage = `<div class="chat-message">`;
    if (Globals.LAST_MESSAGE_AUTHOR !== author) {
        newMessage += `<img class="user-image" src="${modifiers.includes("system") ? avatar : ("/avatar/" + avatar)}">`;
        newMessage += `<p class="msg-nickname" style="color:${colour}">${author}</p><br>`;
    }
    newMessage += `<p ${modifiers}>${content}</p></div>`;
    if (Globals.LAST_MESSAGE_AUTHOR !== author) newMessage += "<br>";

    Globals.LAST_MESSAGE_AUTHOR = author;

    let chatHistory = document.getElementById("chat-history");
    chatHistory.insertAdjacentHTML('afterbegin', newMessage);
    chatHistory.scrollTop = chatHistory.scrollHeight;
}

function speakMessage(message) {
    let tts = new SpeechSynthesisUtterance();
    tts.text = message;
    window.speechSynthesis.speak(tts);
}

function displayLocalMessage(message) {
    addChatMessage({ "author": "System", "colour": Globals.ROOM_COLOUR, "content": message, "modifiers": "system", "avatar": "/favicon.png" });
}

function sendPlayingMessage() {
    const time = Globals.PLAYER.getCurrentTime();
    sendGatewayMessage({ "type": "party-playvideo", "data": data({ "timestamp": time }) });
    displayLocalMessage("Video playing at " + new Date(time * 1000).toISOString().substr(11, 8));
}

function sendPausedMessage() {
    sendGatewayMessage({ "type": "party-pausevideo", "data": data() });
    displayLocalMessage("Video paused");
}

function sendVideoEndedMessage() {
    sendGatewayMessage({ "type": "party-videoend", "data": data() });
    displayLocalMessage("Video ended!");
}

function onYouTubeIframeAPIReady() {
    Globals.PLAYER = new YT.Player('player', {
        height: '100%',
        width: '80%',
        playerVars: { 'controls': Globals.CAN_CONTROL_PLAYER ? 1 : 0 },
        videoId: Globals.CURRENT_VIDEO_ID,
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        }
    });
}

function onPlayerReady() {
    Globals.PLAYER_READY = true;
}

function onPlayerStateChange(event) {
    let playerState = event.data;
    switch (playerState) {
        case 0:
            sendVideoEndedMessage();
            break;
        case 1:
            sendPlayingMessage();
            break;
        case 2:
            sendPausedMessage();
            break;
    }
}

function loadVideo(youTubeVideoID) {
    Globals.CURRENT_VIDEO_ID = youTubeVideoID;
    if (Globals.PLAYER_READY) Globals.PLAYER.loadVideoById(youTubeVideoID, 0);
}

function startVideo(data) {
    if (Globals.PLAYER.getPlayerState() !== YT.PlayerState.PLAYING) {
        Globals.PLAYER.seekTo(data.time, true);
        Globals.PLAYER.playVideo();
    }
}

function pauseVideo() {
    if (Globals.PLAYER.getPlayerState() !== YT.PlayerState.PAUSED) {
        Globals.PLAYER.pauseVideo();
    }
}

function convertVideoList(videos) {
    if (videos.length == 0) return "No videos queued!";
    else {
        let videosFormatted = [];
        videos.forEach(video => {
            videosFormatted.push("<a href='" + YOUTUBE_URL + video + "'>" + video + "</a>");
        });
        return videosFormatted.join("<br>");
    }
}

function handleChatMessage(data) {
    if (data.modifiers.includes("tts")) speakMessage(data.content);
    addChatMessage(data);
}

function refreshModalQueueData(videos) {
    let message = convertVideoList(videos);
    document.getElementById("queue-items").innerHTML = message + "<br><br>";
    document.getElementById("queue-title").innerHTML = "Queued Items (" + videos.length + ")";
}

function initialiseParty(options) {
    loadVideo(options.video);
    Globals.CAN_CONTROL_PLAYER = options.canControl;
    Globals.ROOM_COLOUR = options.theme;

    document.getElementsByTagName("title")[0].text = options.owner + "'s room!";

    let chatInput = document.getElementById("chat-input");
    chatInput.addEventListener("focus", function() {
        this.style.borderBottom = "2px solid " + Globals.ROOM_COLOUR;
    });

    chatInput.addEventListener("blur", function() {
        this.style.borderBottom = "2px solid grey";
    });

    displayLocalMessage("Use /help to see some chat commands! Use ctrl + m to open the player menu!");
}



function handleGatewayMessage(packet) {
    switch (packet.type) {
        case "party-chatmessage":
            handleChatMessage(packet.data);
            break;
        case "party-joinparty":
            initialiseParty(packet.response);
            break;
        case "user-getprofile":
            Globals.USER_PROPERTIES = packet.response;
            break;
        case "party-chatmessage":
            displayLocalMessage(packet.response);
            break;
        case "party-playvideo":
            startVideo(packet.data);
            break;
        case "party-pausevideo":
            pauseVideo();
            break;
        case "party-changevideo":
            if (packet.hasOwnProperty("response")) loadVideo(packet.response.video);
            else loadVideo(packet.data.video);
            break;
        case "party-typingupdate":
            updateTyping(packet.data);
            break;
        case "party-getqueue":
            refreshModalQueueData(packet.response.videos);
            break;
        case "system-ping":
            displayLocalMessage("API response time: " + (new Date().getTime() - packet.response.start) + "ms");
            break;
    }
}

function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

function embedPlayer() {
    let tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    let firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
}

function handleHelpCommand() {
    displayLocalMessage(`Chat Command Help:<br>
/help - shows this message<br><br>
/i [message] - changes your message to italics<br><br>
/u [message] - changes your message to underline<br><br>
/b [message] - makes your message bold<br><br>
/s [message] - changes your message to strikethrough<br><br>
/c [message] - changes your message to cursive<br><br>
/cc [message] - cHaNgEs YoUr TeXt LiKe ThIs<br><br>
/big [message] - makes your message big<br><br>
/r - reloads your session<br><br>
/tts - send a text-to-speech message<br><br>
/ping - get the API response time`);
}

function toCrazyCase(body) {
    let toUpper = Math.round(Math.random()) == 1 ? true : false;
    let messageLetters = body.split("");
    let final = "";

    for (let i = 0; i < messageLetters.length; i++) {
        if (messageLetters[i].replace(/[A-Za-z]+/g, " ") !== "") {
            if (toUpper) final += messageLetters[i].toLowerCase();
            else final += messageLetters[i].toUpperCase();
            toUpper = !toUpper;
        } else final += messageLetters[i];
    }
    return final;
}

function handlePingCommand() {
    let requestData = {
        "type": "system-ping",
        "data": {
            "start": new Date().getTime()
        }
    }
    sendGatewayMessage(requestData);
}

function sendTypingStop() {
    if (Globals.TYPING) {
        Globals.TYPING = false;
        sendGatewayMessage({ "type": "party-typingupdate", "data": data({ "mode": "stop", "user": Globals.USER_PROPERTIES.username }) });
    }
}

function sendTypingStart() {
    if (!Globals.TYPING) {
        Globals.TYPING = true;
        sendGatewayMessage({ "type": "party-typingupdate", "data": data({ "mode": "start", "user": Globals.USER_PROPERTIES.username }) });
    }
}

//Chat Listener
document.getElementById("chat-input").addEventListener("keyup", function(event) {
    if (event.key == "Enter") {
        sendTypingStop();
        event.preventDefault();
        let message = document.getElementById("chat-input").value.trim();
        if (message == "") return;
        if (message.length > 2000) {
            displayLocalMessage("Your message is too long! Messages cannot be longer than 2000 characters.");
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
                case "cc":
                    message = toCrazyCase(args.join(" "));
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
                case "tts":
                    modifiers = "tts";
                    message = args.join(" ");
                    break;
                case "ping":
                    handlePingCommand();
                    sendChatMessage = false;
                    break;
            }
        }
        if (sendChatMessage) {
            sendGatewayMessage({
                "type": "party-chatmessage",
                "data": {
                    "token": Globals.TOKEN,
                    "roomID": Globals.ROOM_ID,
                    "content": message,
                    "colour": Globals.USER_PROPERTIES.colour,
                    "author": Globals.USER_PROPERTIES.effectiveName,
                    "avatar": Globals.USER_PROPERTIES.avatar,
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

//GUI FUNCTIONS

function refreshQueue() {
    sendGatewayMessage({ "type": "party-getqueue", "data": data() });
}

//Open Menu
window.addEventListener("keydown", function(event) {
    if (event.code == "KeyM" && event.ctrlKey) {
        refreshQueue();
        let copyButton = document.getElementById("copy-button");
        if (copyButton.classList.contains("action-complete")) copyButton.classList.remove("action-complete");
        showModalMenu();
    }
});

//Change current video
document.getElementById("current-video-input").addEventListener("keyup", function(event) {
    if (event.key == "Enter") {
        event.preventDefault();
        let videoURL = document.getElementById("current-video-input").value.trim();
        document.getElementById("current-video-input").value = "";
        if (videoURL == "") return;
        setVideo(videoURL);
    }
});

//Add to queue
document.getElementById("queue-input").addEventListener("keyup", function(event) {
    if (event.key == "Enter") {
        event.preventDefault();
        let videoURL = document.getElementById("queue-input").value.trim();
        document.getElementById("queue-input").value = "";
        if (videoURL == "") return;
        let videoURLClass = new URL(videoURL);
        let videoID = videoURLClass.searchParams.get("v");
        if (videoID) {
            sendGatewayMessage({ "type": "party-queuevideo", "data": data({ "video": videoID }) });
            refreshQueue();
        }
    }
});

function setVideo(video) {
    let videoURL = new URL(video);
    let videoID = videoURL.searchParams.get("v");
    if (videoID) sendGatewayMessage({ "type": "party-changevideo", "data": data({ "video": videoID }) });
}

function skipVideo() {
    sendGatewayMessage({ "type": "party-skipvideo", "data": data() });
    refreshQueue();
}

function clearQueue() {
    sendGatewayMessage({ "type": "party-clearqueue", "data": data() });
    refreshQueue();
}

function copyRoomURL() {
    if (document.getElementById("copy-button").classList.contains("action-complete")) return;
    navigator.clipboard.writeText(location.href).then(function() {
        console.log('Copied room URL');
    }, function(err) {
        console.error('Could not copy room URL: ', err);
    });
    document.getElementById("copy-button").classList.add("action-complete");
}

Gateway.onopen = function() {
    console.log("Connected To Gateway");
    hideTypingMessage();
    const selfURL = new URL(location.href);
    Globals.TOKEN = getToken();

    if (!selfURL.searchParams.get("roomID")) window.location.href = location.protocol + "//" + location.host + "/home.html";
    else {
        Globals.ROOM_ID = selfURL.searchParams.get("roomID");
        embedPlayer();
        console.log("Ready");
        sendGatewayMessage({ "type": "party-joinparty", "data": data() });
        sendGatewayMessage({ "type": "user-getprofile", "data": { "token": Globals.TOKEN } });
    }
}

Gateway.onclose = function(event) {
    console.log(`Gateway Disconnected\n\nCode: ${event.code}\nReason: ${event.reason}\nClean?: ${event.wasClean}`);
    displayLocalMessage("You lost connection to the server! Use /r to reconnect");
}

Gateway.onmessage = function(message) {
    const packet = JSON.parse(message.data);
    console.log(packet);

    if (packet.hasOwnProperty("success")) {
        if (!packet.success) displayLocalMessage(packet.response);
        else handleGatewayMessage(packet);
    } else handleGatewayMessage(packet);
}