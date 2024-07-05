"use strict";

const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

function setAvatarUrl(avatar) {
    document.getElementById("avatar-preview").src = "/img/avatars/" + avatar + ".png";
} 

function handleColourChange(response) {
    if (response.success) showModalMessage("Success!", "Colour changed!");
    else showModalMessage("Error", response.response);
}

function handleNicknameChange(response) {
    if (response.success) showModalMessage("Success!", "Nickname changed!");
    else showModalMessage("Error", response.response);
}

function handleProfileResponse(response) {
    if (response.success) {
        let userProfile = response.response;
        document.getElementById("name-colour-picker").value = userProfile.colour;
        document.getElementById("nickname-entry").value = userProfile.effectiveName;
        document.getElementById("avatar-selector").value = userProfile.avatar;
        setAvatarUrl(userProfile.avatar);
    } else {
        window.location.href = location.protocol + "//" + location.host + "/html/login.html";
    }
}

function handlePasswordChange(response) {
    if (response.success) {
        showModalMessage("Success!", "Password changed!");
        window.localStorage.setItem("token", response.response);
    } else showModalMessage("Error", response.response);
}

function handleAccountDeleteResponse(response) {
    if (response.success) {
        window.localStorage.removeItem("token");
        window.location.href = location.protocol + "//" + location.host;
    } else showModalMessage("Error", response.response);
}

function handleAvatarChangeResponse(response) {
    if (response.success) showModalMessage("Success!", "Avatar changed!");
    else showModalMessage("Error", response.response);
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response); 
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

function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/html/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

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

function deleteAccount() {
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

function getUserProfile() {
    let payload = {
        "type": "user-getprofile",
        "data": {
            "token": getToken()
        }
    }
    Gateway.send(JSON.stringify(payload));
}

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

document.getElementById("avatar-selector").onchange = function() {
    setAvatarUrl(document.getElementById("avatar-selector").value);
}

Gateway.onopen = function() {
    getUserProfile();
}