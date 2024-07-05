"use strict";
//Connect to the gateway
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

//When we click the logout button, run this function
function logout() {
    window.localStorage.removeItem("token");
    window.location.href = location.protocol + "//" + location.host;
}

//When we lose connection to the gateway, log it here
Gateway.onclose = function() {
    console.log("Connection Lost");
}

//Handle a profile response
function handleProfileResponse(response) {
    let userProfile = response.response;
    document.getElementById("welcome-banner").innerHTML = "Good to see you, " + userProfile.username + "!";
    Gateway.close(); //We no longer need to maintain a connection
}

//Handle gateway responses
Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response); //Log the message
    handleProfileResponse(response); //Handle the message
}

//Get the user's token
function getToken() {
    let token = window.localStorage.getItem("token");
    //Redirect them to the login if they are not logged in
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/html/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

//Get the profile of the user so we can welcome them
function getUserProfile() {
    let payload = {
        "type": "user-getprofile",
        "data": {
            "token": getToken()
        }
    }
    Gateway.send(JSON.stringify(payload));
}

//When we are connected, get the profile.
Gateway.onopen = function() {
    console.log("Connected To Gateway");
    getUserProfile();
}