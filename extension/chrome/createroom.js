function showMessage(message) {
    document.getElementById("message").style.display = "block";
    document.getElementById("subtitle").style.display = "none";
    document.getElementById("message").innerHTML = message;
}

const GatewayServerURL = "wss://ytparty.voidtech.de/gateway"
var Gateway = new WebSocket(GatewayServerURL);

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    if (response.success) window.open("https://ytparty.voidtech.de/player.html?roomID=" + response.response.partyID);
    else showMessage("Error: " + response.response);
}

function createRoom() {
    chrome.tabs.query({ active: true, currentWindow: true }, function(tabs) {
        var tab = tabs[0];
        const url = new URL(tab.url);
        const ownerControlsOnly = !document.getElementById("owner-controls-checkbox").checked;
        const theme = document.getElementById("room-colour-picker").value;
        const payload = {
            "type": "party-createparty",
            "data": {
                "token": window.localStorage.getItem("token"),
                "ownerControlsOnly": ownerControlsOnly,
                "theme": theme,
                "videoID": url.searchParams.get("v")
            }
        }
        Gateway.send(JSON.stringify(payload));
    });
}

document.getElementById("create-button").addEventListener("click", createRoom);