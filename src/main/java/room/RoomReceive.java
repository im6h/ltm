package room;

import message.MessageType;

import java.util.List;

public class RoomReceive {
    private MessageType messageType;
    private String roomName;
    private String users;
    public RoomReceive(){

    }

    public RoomReceive(MessageType messageType, String roomName, String users) {
        this.messageType = messageType;
        this.roomName = roomName;
        this.users = users;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }
}
