package room;

import message.MessageType;

import java.util.ArrayList;
import java.util.List;

public class RoomResponse {
    private String message;
    private String nameRoom;

    public RoomResponse() {
    }

    public RoomResponse(String message, String nameRoom) {
        this.message = message;
        this.nameRoom = nameRoom;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNameRoom() {
        return nameRoom;
    }

    public void setNameRoom(String nameRoom) {
        this.nameRoom = nameRoom;
    }
}
