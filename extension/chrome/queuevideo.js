const GatewayServerURL = "wss://ytparty.voidtech.de/gateway"
var Gateway = new WebSocket(GatewayServerURL);

function showMessage(message) {
    document.getElementById("message").style.display = "block";
    document.getElementById("subtitle").style.display = "none";
    document.getElementById("message").innerHTML = message;
}

Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    let button = document.getElementById("queue-button");
    if (response.success) {
        showMessage("Added video successfully!");
        button.classList.add("action-complete");
    } else showMessage("Something went wrong: " + response.response);
    button.disabled = true;
}

function queueVideo() {
    chrome.tabs.query({ active: true, currentWindow: true }, function(tabs) {
        var tab = tabs[0];
        const tabUrl = new URL(tab.url);
        const popupUrl = new URL(window.location.href);
        const roomID = popupUrl.searchParams.get("roomID");
        const videoID = tabUrl.searchParams.get("v");
        const payload = {
            "type": "party-queuevideo",
            "data": {
                "token": window.localStorage.getItem("token"),
                "video": videoID,
                "roomID": roomID
            }
        }
        Gateway.send(JSON.stringify(payload));
    });
}

document.getElementById("queue-button").addEventListener("click", queueVideo);