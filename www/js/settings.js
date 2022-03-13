"use strict"; //Strict mode promotes good code practices

//Connect to the gateway
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

//Log opened connections
Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

//Log closed conenctions
Gateway.onclose = function() {
    console.log("Connection Lost");
}

//Update the avatar preview image
function setAvatarUrl(avatar) {
    document.getElementById("avatar-preview").src = "/avatar/" + avatar;
}

//Handle a colour change response. Show an appropriate message depending on the success of the request
function handleColourChange(response) {
    if (response.success) showModalMessage("Success!", "Colour changed!");
    else showModalMessage("Error", response.response);
}

//Handle a nickname change response. Show an appropriate message depending on the success of the request
function handleNicknameChange(response) {
    if (response.success) showModalMessage("Success!", "Nickname changed!");
    else showModalMessage("Error", response.response);
}

//Load the user's data
function handleProfileResponse(response) {
    if (response.success) {
        let userProfile = response.response;
        document.getElementById("name-colour-picker").value = userProfile.colour;
        document.getElementById("nickname-entry").value = userProfile.effectiveName;
        document.getElementById("avatar-selector").value = userProfile.avatar;
        setAvatarUrl(userProfile.avatar);
    } else { //If the profile request failed prompt the user to log in
        window.location.href = location.protocol + "//" + location.host + "/login.html";
    }
}

//Handle a password change response. Show an appropriate message depending on the success of the request
function handlePasswordChange(response) {
    if (response.success) {
        showModalMessage("Success!", "Password changed!");
        window.localStorage.setItem("token", response.response);
    } else showModalMessage("Error", response.response);
}

//Handle an account response. Delete the user's stored token and send them to the homepage
function handleAccountDeleteResponse(response) {
    if (response.success) {
        window.localStorage.removeItem("token");
        window.location.href = location.protocol + "//" + location.host;
    } else showModalMessage("Error", response.response);
}

//Handle an avatar change response. Show an appropriate message depending on the success of the request
function handleAvatarChangeResponse(response) {
    if (response.success) showModalMessage("Success!", "Avatar changed!");
    else showModalMessage("Error", response.response);
}

//Handle gateway messages
Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response); //Log messages
    switch (response.type) {
        case "user-changecolour":
            handleColourChange(response);
            break;
        case "user-changenickname":
            handleNicknameChange(response);
            break;
        case "user-changepassword":
            handlePasswordChange(response);
            break;
        case "user-getprofile":
            handleProfileResponse(response);
            break;
        case "user-deleteaccount":
            handleAccountDeleteResponse(response);
            break;
        case "user-changeavatar":
            handleAvatarChangeResponse(response);
            break;
    }
}

//Get the user's token. If there isn't one, send them to the login page and then redirect them back here
function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

//Send a colour update payload
function updateColour() {
    let hexColour = document.getElementById("name-colour-picker").value;
    let payload = {
        "type": "user-changecolour",
        "data": {
            "colour": hexColour,
            "token": getToken()
        }
    }
    Gateway.send(JSON.stringify(payload));
}

//Send a nickname update payload
function updateNickname() {
    let nickname = document.getElementById("nickname-entry").value;
    if (nickname == "") showModalMessage("Error", "That nickname is too short!");
    else {
        let payload = {
            "type": "user-changenickname",
            "data": {
                "nickname": nickname,
                "token": getToken()
            }
        }
        Gateway.send(JSON.stringify(payload));
    }
}

//Send a password update payload
function updatePassword() {
    let password = document.getElementById("new-password-entry").value;
    let passwordMatch = document.getElementById("password-match-entry").value;
    let originalPassword = document.getElementById("original-password-entry").value;
    let payload = {
        "type": "user-changepassword",
        "data": {
            "new-password": password,
            "original-password": originalPassword,
            "password-match": passwordMatch,
            "token": getToken()
        }
    }
    Gateway.send(JSON.stringify(payload));
}

//Send a delete account payload
function deleteAccount() {
    //Double check with the user
    let deleteMessageAccepted = window.confirm("Are you sure you want to delete your account? This action cannot be undone!");
    let password = document.getElementById("delete-password-entry").value;
    if (deleteMessageAccepted) {
        let payload = {
            "type": "user-deleteaccount",
            "data": {
                "password": password,
                "token": getToken()
            }
        }
        Gateway.send(JSON.stringify(payload));
    }
}

//Send a profile request
function getUserProfile() {
    let payload = {
        "type": "user-getprofile",
        "data": {
            "token": getToken()
        }
    }
    Gateway.send(JSON.stringify(payload));
}

//Send an avatar update payload
function updateAvatar() {
    let avatar = document.getElementById("avatar-selector").value;
    let payload = {
        "type": "user-changeavatar",
        "data": {
            "avatar": avatar,
            "token": getToken()
        }
    }
    Gateway.send(JSON.stringify(payload));
}

//Update the user's avatar preview
document.getElementById("avatar-selector").onchange = function() {
    setAvatarUrl(document.getElementById("avatar-selector").value);
}

//When the gateway is connected, get the user profile
Gateway.onopen = function() {
    getUserProfile();
}