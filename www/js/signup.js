const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
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
    if (response.success) {
        window.localStorage.setItem("token", response.response);
        window.location.href = location.protocol + "//" + location.host + "/home.html";
    } else {
        grecaptcha.reset();
        showModalMessage("Error", response.response);
    }
}

function sendSignupData() {
    var formData = new FormData(document.getElementById("signup-form"));
    var values = [];
    formData.forEach(item => values.push(item));
    finalData = {
        "type": "user-signup",
        "data": {
            "username": values[0],
            "email": values[1],
            "password": values[2],
            "password-confirm": values[3],
            "captcha-token": values[4]
        }
    }
    Gateway.send(JSON.stringify(finalData));
}