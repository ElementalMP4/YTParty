const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var TOKEN;

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

function showUserMessage(message, location) {
    document.getElementById(location).style.display = "block";
    document.getElementById(location).innerHTML = message;
}

function hideUserMessage(location) {
    document.getElementById(location).style.display = "none";
}

function handleColourChange(response) {
    if (response.success) showUserMessage("Colour changed!", "colour-message");
    else showUserMessage(response.response, "colour-message");
}

function handleNicknameChange(response) {
    if (response.success) showUserMessage("Nickname changed!", "nickname-message");
    else showUserMessage(response.response, "nickname-message");
}

function handleProfileResponse(response) {
    if (response.success) {
        var userProfile = JSON.parse(response.response);
        document.getElementById("name-colour-picker").value = userProfile.colour;
        document.getElementById("nickname-entry").value = userProfile.effectiveName;
    } else {
        window.location.href = location.protocol + "//" + location.host + "/login.html";
    }
}

function handlePasswordChange(response) {
    if (response.success) showUserMessage("Password changed! You may need to log in again", "password-message");
    else showUserMessage(response.response, "password-message");
}

function handleAccountDeleteResponse(response) {
    if (response.success) showUserMessage("Your account has been deleted!", "delete-message");
    else showUserMessage(response.response, "delete-message");
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    switch (response.origin) {
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
    }
}

function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
}

function updateColour() {
    var hexColour = document.getElementById("name-colour-picker").value;
    var payload = {
        "type": "user-changecolour",
        "data": {
            "colour": hexColour,
            "token": TOKEN
        }
    }
    Gateway.send(JSON.stringify(payload));
}

function updateNickname() {
    var nickname = document.getElementById("nickname-entry").value;
    var payload = {
        "type": "user-changenickname",
        "data": {
            "nickname": nickname,
            "token": TOKEN
        }
    }
    Gateway.send(JSON.stringify(payload));
}

function updatePassword() {
    var password = document.getElementById("password-entry").value;
    var passwordMatch = document.getElementById("password-match-entry").value;

    if (password !== passwordMatch) {
        showUserMessage("Your passwords do not match!", "password-message");
    } else {
        var payload = {
            "type": "user-changepassword",
            "data": {
                "password": password,
                "token": TOKEN
            }
        }
        Gateway.send(JSON.stringify(payload));
    }
}

function deleteAccount() {
    var deleteMessageAccepted = window.confirm("Are you wure you want to delete your account? This action cannot be undone!");
    if (deleteMessageAccepted) {
        var payload = {
            "type": "user-deleteaccount",
            "data": {
                "token": TOKEN
            }
        }
        Gateway.send(JSON.stringify(payload));
    }
}

function getUserProfile() {
    var payload = {
        "type": "user-getprofile",
        "data": {
            "token": TOKEN
        }
    }
    Gateway.send(JSON.stringify(payload));
}

Gateway.onopen = function() {
    TOKEN = getToken();
    getUserProfile();
}