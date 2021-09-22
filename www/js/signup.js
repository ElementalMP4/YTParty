function showUserMessage(message) {
    document.getElementById("user-message").style.display = "block";
    document.getElementById("user-message").innerHTML = message;
}

function hideUserMessage() {
    document.getElementById("user-message").style.display = "none";
}

function reactToAuth(data) {
    const success = data.success;

    if (success) {
        showUserMessage("Account Created!");
        document.getElementById("signup-button").disabled = true;
    } else {
        showUserMessage(data.message);
    }
}

function handleResponse(response) {
    console.log(response);
    if (response.success) {
        showUserMessage("Account Created!");
        window.localStorage.setItem("token", response.token);
        window.location.href = location.protocol + "//" + location.host + "/home.html";
    } else {
        showUserMessage(response.message);
    }
}

function createSignupPayload(username, password) {
    const payload = {
        "type": "user-signup",
        "data": {
            "username": username,
            "password": password
        }
    }
    return JSON.stringify(payload);
}

function sendSignupData() {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/signup");
    xhr.setRequestHeader('Content-Type', 'application/json;');
    xhr.onload = function(event) {
        let response = JSON.parse(event.target.response);
        if (response)
            handleResponse(response);
    };
    var formData = new FormData(document.getElementById("signup-form"));
    var values = [];
    formData.forEach(item => values.push(item));
    finalData = {
        "username": values[0],
        "password": values[1],
        "password-confirm": values[2],
        "h-captcha": values[3]
    }
    xhr.send(JSON.stringify(finalData));
}