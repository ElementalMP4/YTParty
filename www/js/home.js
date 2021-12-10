const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var TOKEN;

function logout() {
    window.localStorage.removeItem("token");
    window.location.href = location.protocol + "//" + location.host;
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

function handleProfileResponse(response) {
    var userProfile = JSON.parse(response.response);
    document.getElementById("welcome-banner").innerHTML = "Good to see you, " + userProfile.username + "!";
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    handleProfileResponse(response);
}

function getToken() {
    let token = window.localStorage.getItem("token");
    if (token == null) window.location.href = location.protocol + "//" + location.host + "/login.html?redirect=" + location.pathname + location.search;
    else return token;
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
    console.log("Connected To Gateway");
    TOKEN = getToken();
    getUserProfile();
}