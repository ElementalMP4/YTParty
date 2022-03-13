"use strict"; //Strict mode promotes good code practice

//Connect to gateway
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

//Log opened connections
Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

//Log closed connections
Gateway.onclose = function() {
    console.log("Connection Lost");
}

//Handle messages
Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response);
    //Show a modal to tell the user if the request was a success or not
    showModalMessage(response.success ? "Success!" : "Error", response.response);
    //If it was not a success, reset the captcha
    if (!response.success) grecaptcha.reset();
}

//Send reset data
function sendResetRequestData() {
    let formData = new FormData(document.getElementById("reset-form")); //Get the form data
    let values = []; //We need somewhere to store the form values
    formData.forEach(item => values.push(item)); //Load the form data
    let finalData = {
            "type": "user-forgottenpassword",
            "data": {
                "username": values[0], //Set the username to be reset
                "captcha-token": values[1] //Set the captcha token
            }
        }
        //Send the data
    Gateway.send(JSON.stringify(finalData));
}