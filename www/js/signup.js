"use strict"; //Strict mode promotes good code practice

//Connect to the gateway
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway = new WebSocket(GatewayServerURL);

//Log an opened connection
Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

//Log connection closures
Gateway.onclose = function() {
    console.log("Connection Lost");
}

//Handle messages
Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data);
    console.log(response); //Log the message
    if (response.success) { //If the signup was successful, store the user's token and go to their home page
        window.localStorage.setItem("token", response.response.token);
        window.location.href = location.protocol + "//" + location.host + "/home.html";
    } else { //If the signup failed, display an error message and reset the captcha
        grecaptcha.reset();
        showModalMessage("Error", response.response);
    }
}

//Function to send signup data to the server
function sendSignupData() {
    let formData = new FormData(document.getElementById("signup-form")); //Get the data from the signup form
    let avatar = document.getElementById("avatar-selector").value; //Get the avatar selection
    let values = []; //Store the form values
    formData.forEach(item => values.push(item));
    let finalData = {
            "type": "user-signup",
            "data": {
                "username": values[0], //Set the username
                "email": values[1], //Email (optional)
                "password": values[2], //Password
                "avatar": avatar, //Avatar
                "password-confirm": values[3], //Confirm password
                "captcha-token": values[4] //Captcha token
            }
        }
        //Send to Gateway
    Gateway.send(JSON.stringify(finalData));
}

//Automatically update the avatar preview
function setAvatarUrl(avatar) {
    document.getElementById("avatar-preview").src = "/avatar/" + avatar;
}

document.getElementById("avatar-selector").onchange = function() {
    setAvatarUrl(document.getElementById("avatar-selector").value);
}