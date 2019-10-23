var head = document.getElementById("head")
var wrap = document.getElementById("wrap")
var websocket = new WebSocket("ws://localhost:8080/chat/socket");
websocket.onopen = function (message) {
    textAreaMessage.value += "Server connect... \n";
};
websocket.onmessage = function (message) {
    console.log(message);
    textAreaMessage.value += message.data + " \n";
};
websocket.onclose = function (message) {
    textAreaMessage.value += "Server Disconnect... \n";

};
websocket.onerror = function (message) {
    textAreaMessage.value += "Error... " + message + " \n";

};

function sendUsername() {
    if (typeof websocket != 'undefined' && websocket.readyState == WebSocket.OPEN) {
        websocket.send(textUsername.value)
        head.style.visibility = "hidden"
        wrap.style.visibility = "visible"
    }
}

function sendMessage() {
    if (typeof websocket != 'undefined' && websocket.readyState == WebSocket.OPEN) {
        websocket.send(textMessage.value);
        textMessage.value = "";
    }
}