"use strict"; //Strict mode creates better code practices

const YOUTUBE_URL = "https://youtube.com/watch?v="; //This URL is the base of all videos. We use this to reconstruct URLs from IDs
//We automatically choose the correct protocol (WSS or WS) depending on whether the server is running in dev or prod.
const GATEWAY_URL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
//Create the WebSocket gateway connection
let Gateway = new WebSocket(GATEWAY_URL);

let Globals = {
    USER_PROPERTIES: {}, //Store the user properties (username etc)
    TOKEN: "", //Store the user's token
    PLAYER: {}, //Store the YouTube iFrame player
    CURRENT_VIDEO_ID: "", //Keep track of the current video ID
    ROOM_ID: "", //Store the room ID
    LAST_MESSAGE_AUTHOR: "", //Store the author of the last message (to keep the chat clean)
    CAN_CONTROL_PLAYER: false, //Permissions are set here
    ROOM_COLOUR: "", //The room theme is stored here
    TYPING_COUNT: 0, //We keep track of all the people typing
    TYPING: false, //We keep track of whether we are typing
    PLAYER_READY: false //We need to know if the player is ready so we can dissolve the loading animation
}

function sendGatewayMessage(message) {
    Gateway.send(JSON.stringify(message)); //This method makes sending messages a little more convenient
}

function data(params) { //This function automatically creates the `data` object for our messages
    const defaultParams = { token: Globals.TOKEN, roomID: Globals.ROOM_ID }; //We have some default data parameters
    return {...defaultParams, ...params }; //Add the default parameters to the supplied parameters
}

function showTypingMessage() { //Show the typing message
    document.getElementById("typing-message").style.display = "block";
}

function hideTypingMessage() { //Hide the typing message
    document.getElementById("typing-message").style.display = "none";
}

function updateTyping(data) { //Decide whether we should show or hide the typing message
    if (data.user == Globals.USER_PROPERTIES.username) return; //If we receive a message saying we are typing, ignore it.
    if (data.mode == "start") Globals.TYPING_COUNT = Globals.TYPING_COUNT + 1; //If someone has started typing, increment the typing count
    else Globals.TYPING_COUNT = Globals.TYPING_COUNT - 1; //Otherwise, decrement it.

    if (Globals.TYPING_COUNT > 0) showTypingMessage(); //If people are typing, show the typing message
    else hideTypingMessage(); //Otherwise, hide it.
};

function addChatMessage(data) { //Add a chat message to the message history div
    //Get some parameters from the supplied object
    const author = data.author;
    const colour = data.colour;
    const content = data.content;
    const modifiers = data.modifiers !== "" ? `class="${data.modifiers}"` : ""; //Modifiers are classes applied to the `p` tag
    const avatar = data.avatar;

    let newMessage = `<div class="chat-message">`; //Create a new message div
    if (Globals.LAST_MESSAGE_AUTHOR !== author) { //If this message is not from the same person as last time, add their nickname and avatar to the message.
        newMessage += `<img class="user-image" src="${modifiers.includes("system") ? avatar : ("/avatar/" + avatar)}">`;
        newMessage += `<p class="msg-nickname" style="color:${colour}">${author}</p><br>`;
    }
    //Add the message content
    newMessage += `<p ${modifiers}>${content}</p></div>`;
    if (Globals.LAST_MESSAGE_AUTHOR !== author) newMessage += "<br>";

    //Store the last author as the author of this message
    Globals.LAST_MESSAGE_AUTHOR = author;

    //Append this message to the history and scroll to the bottom of the chat
    let chatHistory = document.getElementById("chat-history");
    chatHistory.insertAdjacentHTML('afterbegin', newMessage);
    chatHistory.scrollTop = chatHistory.scrollHeight;
}

//If we receive a Text To Speech message, we can use the browser's TTS feature to speak it here:
function speakMessage(message) {
    let tts = new SpeechSynthesisUtterance();
    tts.text = message;
    window.speechSynthesis.speak(tts);
}

//If we want to show a message for the help menu, we can use this method to quickly display some text. 
//All the necessary fields are pre-filled
function displayLocalMessage(message) {
    addChatMessage({ "author": "System", "colour": Globals.ROOM_COLOUR, "content": message, "modifiers": "system", "avatar": "/favicon.png" });
}

//Send a playing message to the server
function sendPlayingMessage() {
    const time = Globals.PLAYER.getCurrentTime(); //Get the current time
    sendGatewayMessage({ "type": "party-playvideo", "data": data({ "timestamp": time }) }); //Send the time and message type to the server
    displayLocalMessage("Video playing at " + new Date(time * 1000).toISOString().substr(11, 8)); //Show a message saying the video is playing
}

//Send a paused message to the server
function sendPausedMessage() {
    sendGatewayMessage({ "type": "party-pausevideo", "data": data() }); //Send a pause message with the default data parameters
    displayLocalMessage("Video paused"); //Tell the user the video has been paused
}

//Send a message when the video finishes
function sendVideoEndedMessage() {
    sendGatewayMessage({ "type": "party-videoend", "data": data() });
    displayLocalMessage("Video ended!");
}

//This function is executed when the player API is ready, but not the player itself. This function is provided by Google.
function onYouTubeIframeAPIReady() {
    Globals.PLAYER = new YT.Player('player', { //Create a new player
        height: '100%', //Set the height
        width: '80%', //And width
        //Disable the controls if the permissions disallow it
        playerVars: { 'controls': Globals.CAN_CONTROL_PLAYER ? 1 : 0, 'disablekb': Globals.CAN_CONTROL_PLAYER ? 0 : 1 },
        videoId: Globals.CURRENT_VIDEO_ID, //Set the video ID
        events: { //Register events
            'onReady': onPlayerReady, //This function runs when the player is ready
            'onStateChange': onPlayerStateChange //This function runs when the player does something eg: play/pause
        }
    });
}

//When the player is ready, set the global player ready value to true
function onPlayerReady() {
    Globals.PLAYER_READY = true;
}

function onPlayerStateChange(event) {
    let playerState = event.data;
    switch (playerState) {
        case 0: //0 = video ended
            sendVideoEndedMessage();
            break;
        case 1: //1 = video playing
            sendPlayingMessage();
            break;
        case 2: //2 = video paused
            sendPausedMessage();
            break;
    }
}

//This function loads a new video into the player
function loadVideo(youTubeVideoID) {
    Globals.CURRENT_VIDEO_ID = youTubeVideoID;
    if (Globals.PLAYER_READY) Globals.PLAYER.loadVideoById(youTubeVideoID, 0);
}

//This function plays the video that has been loaded.
function startVideo(data) {
    if (Globals.PLAYER.getPlayerState() !== YT.PlayerState.PLAYING) { //If it's already palying, ignore this event.
        Globals.PLAYER.seekTo(data.time, true); //Move to the correct timestamp
        Globals.PLAYER.playVideo(); //Play the video
    }
}

//Pause the video
function pauseVideo() {
    if (Globals.PLAYER.getPlayerState() !== YT.PlayerState.PAUSED) { //If the video is already paused, ignore this event.
        Globals.PLAYER.pauseVideo(); //Pause the video
    }
}

//This method converts a list of video IDs to a list of video anchors for the player menu
function convertVideoList(videos) {
    if (videos.length == 0) return "No videos queued!"; //Set the message if there are no videos
    else {
        let videosFormatted = []; //Store the formatted videos
        videos.forEach(video => { //Iterate through each ID
            videosFormatted.push("<a href='" + YOUTUBE_URL + video + "'>" + video + "</a>"); //Build an HTML anchor element with the ID and youtube URL
        });
        return videosFormatted.join("<br>"); //turn the array into a list of elements in a string
    }
}

//Handle chat messages here
function handleChatMessage(data) {
    if (data.modifiers.includes("tts")) speakMessage(data.content); //If it is a TTS message, speak it
    addChatMessage(data); //Add the message to the history
}

//Show the queued items to the user
function refreshModalQueueData(videos) {
    let message = convertVideoList(videos);
    document.getElementById("queue-items").innerHTML = message + "<br><br>"; //Show the list here
    document.getElementById("queue-title").innerHTML = "Queued Items (" + videos.length + ")"; //Show the number of queued items here
}

//Initialise the room
function initialiseParty(options) {
    loadVideo(options.video); //Load the current video
    Globals.CAN_CONTROL_PLAYER = options.canControl; //Set the permissions
    Globals.ROOM_COLOUR = options.theme; //Set the theme

    document.getElementsByTagName("title")[0].text = options.owner + "'s room!"; //Set the room title

    //An event listener to change the colour of the chat input box when it is clicked
    let chatInput = document.getElementById("chat-input");
    chatInput.addEventListener("focus", function() {
        this.style.borderBottom = "2px solid " + Globals.ROOM_COLOUR;
    });

    //An event listener to change the colour of the chat input box when it is left
    chatInput.addEventListener("blur", function() {
        this.style.borderBottom = "2px solid grey";
    });

    //Tell the user where to find commands and the player menu
    displayLocalMessage("Use /help to see some chat commands! Use ctrl + m to open the player menu!");
}

//Slowly fade away the loading screen
function hideLoadingScreen() {
    let screen = document.getElementById("loading-screen");
    screen.classList.add("loaded");
    setTimeout(() => { screen.style.display = "none" }, 500);
}

//Handle messages from the server
function handleGatewayMessage(packet) {
    switch (packet.type) {
        case "party-partyready": //If the party is ready, hide the loading screen
            hideLoadingScreen();
            break;
        case "party-chatmessage": //If we receive a chat message, use the handler
            handleChatMessage(packet.data);
            break;
        case "party-joinparty":
            initialiseParty(packet.response); //When we join the party, handle the join party response
            break;
        case "user-getprofile": //When we request our user profile, store the values we receive
            Globals.USER_PROPERTIES = packet.response;
            break;
        case "party-playvideo": //When the video starts playing for other people, start the player in our client
            startVideo(packet.data);
            break;
        case "party-pausevideo": //When the video is paused by other people, pause the video in our client
            pauseVideo();
            break;
        case "party-changevideo": //When the video is changed, load the new video
            loadVideo(packet.data.video);
            break;
        case "party-typingupdate": //When a typing update is received, handle it
            updateTyping(packet.data);
            break;
        case "party-getqueue": //When we receive queue data, handle it
            refreshModalQueueData(packet.response.videos);
            break;
        case "system-ping": //When we receive a server ping reply, show the ping time
            displayLocalMessage("API response time: " + (new Date().getTime() - packet.response.start) + "ms");
            break;
    }
}

function getToken() { //Get the token of the user. If they are not logged in (have no token) then we can tell them to log in
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

//Add the YouTube iFrame player API to the document
function embedPlayer() {
    let tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    let firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
}

//Show command help
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

//Send a typing stopped message
function sendTypingStop() {
    if (Globals.TYPING) {
        Globals.TYPING = false;
        sendGatewayMessage({ "type": "party-typingupdate", "data": data({ "mode": "stop", "user": Globals.USER_PROPERTIES.username }) });
    }
}

//Send a typing started message
function sendTypingStart() {
    if (!Globals.TYPING) {
        Globals.TYPING = true;
        sendGatewayMessage({ "type": "party-typingupdate", "data": data({ "mode": "start", "user": Globals.USER_PROPERTIES.username }) });
    }
}

//Chat Listener
document.getElementById("chat-input").addEventListener("keyup", function(event) {
    if (event.key == "Enter") { //If the enter key is pressed
        sendTypingStop();
        event.preventDefault(); //Prevent the default action
        let message = document.getElementById("chat-input").value.trim(); //Get the chat input value
        if (message == "") return; //Ignore a blank message
        if (message.length > 2000) { //Warn that a message is too long
            displayLocalMessage("Your message is too long! Messages cannot be longer than 2000 characters.");
            return;
        }

        let sendChatMessage = true; //We use this to tell if we should send this message later
        let modifiers = ""; //We store the modifiers here

        if (message.startsWith("/")) { //The command prefix is / to prevent accidental command usage
            const args = message.slice(1).split(/ +/); //Create an array of arguments
            const command = args.shift().toLowerCase(); //Get the command from the arguments list

            switch (command) {
                case "help":
                    handleHelpCommand();
                    sendChatMessage = false; //Do NOT send a chat message
                    break;
                case "cc":
                    message = toCrazyCase(args.join(" ")); //Convert the message to Crazy Case
                    break;
                case "i":
                    modifiers = "italic";
                    message = args.join(" "); //Convert the message to italics
                    break;
                case "u":
                    modifiers = "underline";
                    message = args.join(" "); //Underline the message
                    break;
                case "b":
                    modifiers = "bold";
                    message = args.join(" "); //Bold the message
                    break;
                case "s":
                    modifiers = "strikethrough";
                    message = args.join(" "); //Strike through the message
                    break;
                case "c":
                    modifiers = "cursive";
                    message = args.join(" "); //Put the message in cursive
                    break;
                case "big":
                    modifiers = "big";
                    message = args.join(" "); //Make the text larger
                    break;
                case "r":
                    sendChatMessage = false; //Rejoin the room if connection is lost
                    location.reload();
                    break;
                case "tts":
                    modifiers = "tts";
                    message = args.join(" "); //Send a TTS message
                    break;
                case "ping":
                    handlePingCommand(); //Ping the server
                    sendChatMessage = false; //Do NOT send a chat message
                    break;
            }
        }
        if (sendChatMessage) {
            sendGatewayMessage({ //Send a new chat message
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
        document.getElementById("chat-input").value = ""; //Clear the input box
    } else { //If enter isn't pressed...
        let message = document.getElementById("chat-input").value.trim(); //Get the chat value
        if (message == "") sendTypingStop(); //Determine whether we should send a typing stop
        else sendTypingStart(); //Or a typing start
    }
});

//GUI FUNCTIONS

//Get the current queue
function refreshQueue() {
    sendGatewayMessage({ "type": "party-getqueue", "data": data() });
}

//Open Menu
window.addEventListener("keydown", function(event) {
    if (event.code == "KeyM" && event.ctrlKey) { //Ctrl + M
        refreshQueue(); //Refresh the queue
        let copyButton = document.getElementById("copy-button");
        if (copyButton.classList.contains("action-complete")) copyButton.classList.remove("action-complete"); //Make the copy link button look unpressed
        showModalMenu(); //Open the menu
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

//Set a new video
function setVideo(video) {
    let videoURL = new URL(video); //Create a URL object
    let videoID = videoURL.searchParams.get("v"); //ensure it has the correct URL elements
    if (videoID) sendGatewayMessage({ "type": "party-changevideo", "data": data({ "video": videoID }) }); //Send a change video message
}

//Skip the current video
function skipVideo() {
    sendGatewayMessage({ "type": "party-skipvideo", "data": data() });
    refreshQueue();
}

//Clear the queue
function clearQueue() {
    sendGatewayMessage({ "type": "party-clearqueue", "data": data() });
    refreshQueue();
}

//Do this when the copy button is pressed
function copyRoomURL() {
    if (document.getElementById("copy-button").classList.contains("action-complete")) return;
    navigator.clipboard.writeText(location.href).then(function() {
        console.log('Copied room URL');
    }, function(err) {
        console.error('Could not copy room URL: ', err);
    });
    document.getElementById("copy-button").classList.add("action-complete");
}

//Handle a gateway connection
Gateway.onopen = function() {
    console.log("Connected To Gateway");
    hideTypingMessage();
    const selfURL = new URL(location.href);
    Globals.TOKEN = getToken(); //Get token function will automatically redirect a user to the login page

    //If no room ID is present, redirect the user to their home page
    if (!selfURL.searchParams.get("roomID")) window.location.href = location.protocol + "//" + location.host + "/home.html";
    else {
        Globals.ROOM_ID = selfURL.searchParams.get("roomID"); //Set the room ID
        embedPlayer(); //Embed YouTube API
        sendGatewayMessage({ "type": "party-joinparty", "data": data() }); //Join the party to receive party messages
        sendGatewayMessage({ "type": "user-getprofile", "data": { "token": Globals.TOKEN } }); //Get the user's profile for the chat
    }
}

//Handle gateway closure
Gateway.onclose = function(event) {
    console.log(`Gateway Disconnected\n\nCode: ${event.code}\nReason: ${event.reason}\nClean?: ${event.wasClean}`);
    displayLocalMessage("You lost connection to the server! Use /r to reconnect");
}

//Handle gateway messages
Gateway.onmessage = function(message) {
    const packet = JSON.parse(message.data);
    console.log(packet);

    //We need to handle both Type/Data messages and Type/Data/Success messages
    if (packet.hasOwnProperty("success")) {
        if (!packet.success) displayLocalMessage(packet.response);
        else handleGatewayMessage(packet);
    } else handleGatewayMessage(packet);
}