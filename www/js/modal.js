"use strict";

let modal = document.getElementById("modal");
let closeButton = document.getElementById("close");

function showModalMessage(title, message) {
    document.getElementById("modal-title").innerHTML = title;
    document.getElementById("modal-message").innerHTML = message;
    showModal();
}

function hideModal() {
    modal.style.display = "none";
}

function showModal() {
    modal.style.display = "block";
}

closeButton.onclick = function() {
    hideModal();
}

window.onclick = function(event) {
    if (event.target == modal) {
        hideModal();
    }
}

document.addEventListener("keydown", function(event) {
    if (event.code == "Escape") {
        hideModal();
    }
});