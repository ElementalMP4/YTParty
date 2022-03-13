"use strict"; //Strict mode promotes good code practices

let modal = document.getElementById("modal"); //Store the modal object
let closeButton = document.getElementById("close"); //Store the close button

//Show a modal message
function showModalMessage(title, message) {
    document.getElementById("modal-title").innerHTML = title; //Set the title
    document.getElementById("modal-message").innerHTML = message; //Set the content
    showModal(); //Show the modal
}

function hideModal() {
    modal.style.display = "none";
}

function showModal() {
    modal.style.display = "block";
}

//Modal menus are modals with customised HTML. We should not interfere with these.
function showModalMenu() {
    showModal();
}

//Close modal when the close button is clicked
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