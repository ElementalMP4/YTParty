function showMessage(message) {
    document.getElementById("message").style.display = "block";
    document.getElementById("subtitle").style.display = "none";
    document.getElementById("message").innerHTML = message;
}

function getToken() {
    return localStorage.getItem('token');
}

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
                "Go to <a href='https://ytparty.voidtech.de'>YTParty</a> and log in, then press the YTParty logo to link your account.");
            else window.location.href = "createroom.html";
        } else showMessage("Navigate to a video then press the YTParty logo to open the room creation menu!")
    }
});