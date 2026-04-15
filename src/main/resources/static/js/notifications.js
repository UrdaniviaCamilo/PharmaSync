function playNotificationSound() {
    const audio = new Audio('/sounds/notification.mp3');
    audio.play().catch(e => console.log("Error sonido:", e));
}


function nuevaNotificacion(msg) {
    playNotificationSound();
    alert(msg);
}