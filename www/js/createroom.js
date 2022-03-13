"use strict"; //Strict mode promotes good code practice

//Connect to the gateway
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

//Log opened connections
Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

//Log closed connections
Gateway.onclose = function() {
    console.log("Connection Lost");
}

//Handle messages
Gateway.onmessage = function(message) {
    const serverMessage = JSON.parse(message.data);
    console.log(serverMessage); //Log the message
    if (serverMessage.success) { //If the request was a success...
        //We can redirect the user to their room
        window.location.href = location.protocol + "//" + location.host + "/player.html?roomID=" + serverMessage.response.partyID;
    } else showModalMessage("Error", serverMessage.response); //Otherwise, show an error
}

//Get the user's token from storage. If it is not available, then lead them to the signin page and then redirect them back here.
function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

//Send room creation data
function sendRoomPayload(videoID) {
    const ownerControlsOnly = !document.getElementById("owner-only-checkbox").checked; //Get permissions setting
    const theme = document.getElementById("room-colour-picker").value; //Get room theme colour
    const payload = {
            "type": "party-createparty",
            "data": {
                "token": getToken(), //Get the user's token
                "ownerControlsOnly": ownerControlsOnly, //Set the permissions
                "theme": theme, //Set the theme
                "videoID": videoID //Set the initial video
            }
        }
        //Send the payload
    Gateway.send(JSON.stringify(payload));
}

//Create room function
function createRoom() {
    try {
        const videoURL = document.getElementById("video-entry").value; //Get the entered URL
        const urlObject = new URL(videoURL); //Try and parse it as a URL
        const videoID = urlObject.searchParams.get("v"); //Get the "v" parameter (video ID)
        sendRoomPayload(videoID); //Send the room payload
    } catch { //If URL parsing fails, show an error modal because the URL entered is not valid.
        showModalMessage("Error", "You supplied an invalid link!");
    }
}