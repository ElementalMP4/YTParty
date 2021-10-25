var modal = document.getElementById("modal");
var btn = document.getElementById("open-modal");
var span = document.getElementById("close");

function showModalMessage(title, message) {
    document.getElementById("modal-title").innerHTML = title;
    document.getElementById("modal-message").innerHTML = message;
    document.getElementById("open-modal").click();
}

btn.onclick = function() {
    modal.style.display = "block";
}

span.onclick = function() {
    modal.style.display = "none";
}

window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}