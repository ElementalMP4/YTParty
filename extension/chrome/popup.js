const GatewayServerURL = "wss://ytparty.voidtech.de/gateway";
var Gateway = new WebSocket(GatewayServerURL);

Gateway.onopen = function() {
    console.log("Connected To Gateway");
    startPopup();
}

Gateway.onclose = function() {
    console.log("Connection Lost");
}

Gateway.onmessage = function(message) {
    const data = JSON.parse(message.data);
    const roomID = data.response.roomID;

    if (data.success) {
        if (roomID == "none") window.location.href = "createroom.html";
        else window.location.href = "queuevideo.html?roomID=" + roomID;
    } else showMessage("Something went wrong: " + data.response);
}

function showMessage(message) {
    document.getElementById("message").style.display = "block";
    document.getElementById("subtitle").style.display = "none";
    document.getElementById("message").innerHTML = message;
}

function getToken() {
    return localStorage.getItem('token');
}

function createRoomOrQueueVideos() {
    Gateway.send(JSON.stringify({ "type": "user-getroom", "data": { "token": localStorage.getItem("token") } }));
}

function startPopup() {
    chrome.tabs.query({ active: true, currentWindow: true }, function(tabs) {
        var tab = tabs[0];
        const url = new URL(tab.url);
        if (url.host == "ytparty.voidtech.de") {
            chrome.scripting.executeScript({ target: { tabId: tab.id }, func: getToken }, result => {
                const token = result[0].result;
                console.log(token);
                if (token == null) showMessage("You are not signed in!");
                else {
                    localStorage.setItem("token", token);
                    showMessage("Your account has been linked!");
                }
            });
        } else if (url.host == "www.youtube.com") {
            if (url.pathname == "/watch") {
                if (localStorage.getItem("token") == null) showMessage("Your account is not linked! " +
                    "Go to <a href='https://ytparty.voidtech.de' target='blank' rel='noreferrer noopener'>YTParty</a> and log in, " +
                    "then press the YTParty logo to link your account.");
                else createRoomOrQueueVideos();
            } else showMessage("Navigate to a video then press the YTParty logo to open the room creation menu!");
        }
    });
}