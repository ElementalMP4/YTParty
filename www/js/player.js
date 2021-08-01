const GatewayServerURL = "ws://" + location.host + "/gateway";
var Gateway = new WebSocket(GatewayServerURL);

var USER_PROPERTIES;
var TOKEN;

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
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
    }
}