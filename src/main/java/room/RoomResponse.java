package room;

import message.MessageType;

import java.util.ArrayList;
import java.util.List;

public class RoomResponse {
    private String message;
    private List<String> rooms = new ArrayList<>();

    public RoomResponse() {
    }

    public RoomResponse(String message, List<String> rooms) {
        this.message = message;
        this.rooms = rooms;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }

    public void addRoom(String roomName){
        rooms.add(roomName);
    }
}
