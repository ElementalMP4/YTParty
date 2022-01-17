"use strict";
let modal = document.getElementById("modal");
let openButton = document.getElementById("open-modal");
let closeButton = document.getElementById("close");

function showModalMessage(title, message) {
    document.getElementById("modal-title").innerHTML = title;
    document.getElementById("modal-message").innerHTML = message;
    document.getElementById("open-modal").click();
}

function hideModal() {
    modal.style.display = "none";
}

function showModal() {
    modal.style.display = "block";
}

function showModalMenu() {
    document.getElementById("open-modal").click();
}

openButton.onclick = function() {
    showModal();
}

closeButton.onclick = function() {
    hideModal();
}

//Close modal when click detected off of modal
window.onclick = function(event) {
    if (event.target == modal) {
        hideModal();
    }
}

//Close modal on escape key
document.addEventListener("keydown", function(event) {
    if (event.code == "Escape") {
        hideModal();
    }
});