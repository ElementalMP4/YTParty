"use strict";
//Connect to the gateway
const GatewayServerURL = (location.protocol == "https:" ? "wss://" : "ws://") + location.host + "/gateway";
let Gateway;

//If they are logged in, take them to the home page
if (window.localStorage.getItem("token") !== null) window.location.href = location.protocol + "//" + location.host + "/html/home.html";
//If not, connect to the gateway and continue with login procedures.
else Gateway = new WebSocket(GatewayServerURL);

//When we are connected, log the connection
Gateway.onopen = function() {
    console.log("Connected To Gateway");
}

//If the connection is lost, log it
Gateway.onclose = function() {
    console.log("Connection Lost");
}

//When we receive a message, handle it
Gateway.onmessage = function(message) {
    const response = JSON.parse(message.data); //Parse the message
    console.log(response); //Log the message
    if (response.success) { //If the message is a success, we know the login worked.
        let url = new URL(location.href);
        let redirect = url.searchParams.get("redirect"); //Get the redirect
        window.localStorage.setItem("token", response.response.token); //Store the token
        window.location.href = location.protocol + "//" + location.host + (redirect == null ? "/html/home.html" : redirect); //Go to the redirect if there is one
    } else {
        grecaptcha.reset(); //If the request failed, reset the captcha and show a message
        showModalMessage("Error", response.response);
    }
}

//Send the login data
function sendLoginData() {
    //Get the data from the login form
    let formData = new FormData(document.getElementById("signin-form"));
    //Store the values here
    let values = [];
    formData.forEach(item => values.push(item));
    let finalData = {
        "type": "user-signin",
        "data": {
            "username": values[0], //Get the username, password and captcha token
            "password": values[1],
            "captcha-token": values[2]
        }
    }
    Gateway.send(JSON.stringify(finalData)); //Send these values to the server
}