package room;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Room {
    private String name;
    private Set<Session> users = Collections.synchronizedSet(new HashSet<>());

    public Room() {
    }

    public Room(String name, Set<Session> users) {
        this.name = name;
        this.users = users;
    }

    public void join(Session session) {
        users.add(session);
    }

    public void leave(Session session) {
        users.remove(session);
    }

    public void sendMessage(String message) {
        users.forEach(user -> {
            try {
                user.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void displayMemberInRoom() {
        users.forEach(user -> {
            System.out.println(user.getUserProperties().get("name"));
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Session> getUsers() {
        return users;
    }

    public void setUsers(Set<Session> users) {
        this.users = users;
    }
}
