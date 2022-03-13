"use strict"; //Strict mode promotes good code practice
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway"; //Automatically determine the WS URL
let Gateway = new WebSocket(GatewayServerURL); //Create a new websocket connection

Gateway.onopen = function() { //Log open connections
    console.log("Connected To Gateway");
}

Gateway.onclose = function() { //Log connection losses
    console.log("Connection Lost");
}

Gateway.onmessage = function(message) { //Handle messages
    const response = JSON.parse(message.data);
    console.log(response);
    //We are only expecting one kind of message type. As such, we don't need to add extra checking logic here
    if (response.success) showModalMessage("Success!", "Password reset! You can now log back into your account.");
    else {
        showModalMessage("Error", response.response);
        grecaptcha.reset(); //Reset the captcha if the password reset request failed and show an error message
    }
}

function sendResetData() {
    let formData = new FormData(document.getElementById("reset-form")); //Get the reset form data 
    let values = [];

    let url = new URL(location.href);
    let resetToken = url.searchParams.get("token"); //Get the reset token from the URL
    let username = url.searchParams.get("user"); //Get the username of the account to reset from the URL

    formData.forEach(item => values.push(item)); //Iterate through the reset form data
    let finalData = {
        "type": "user-resetpassword", //Set the message type
        "data": {
            "password": values[0], //Set the password
            "password-confirm": values[1], //Set the confirmation password
            "captcha-token": values[2], //Set the captcha token
            "reset-token": resetToken, //Set the reset token
            "username": username //Set the username
        }
    }
    Gateway.send(JSON.stringify(finalData)); //Send the data
}