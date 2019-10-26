import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import message.MessageReceive;
import message.MessageResponse;
import message.MessageType;
import room.Room;
import room.RoomReceive;
import room.RoomResponse;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

@ServerEndpoint(value = "/socket")
public class Endpoint {

    private static Set<Session> users = Collections.synchronizedSet(new HashSet<Session>());

    /* hàm kết nối*/
    @OnOpen
    public void onOpen(Session session) {
        users.add(session);
    }

    /* hàm nhận tin nhắn từ phía client và gửi trả tin nhắn cho client */
    @OnMessage
    public void onMessage(Session session, String msgJson) throws IOException {
        Gson gson = new Gson();
        if (msgJson.contains("CREATE_ROOM")) {
            RoomReceive roomReceive = gson.fromJson(msgJson, RoomReceive.class);
            handleMessageCreateRoom(roomReceive, session);

        } else {
            MessageReceive messageReceive = gson.fromJson(msgJson, MessageReceive.class);
            if (messageReceive.getMessageType() == MessageType.LOGIN) {
                handleLoginMessage(messageReceive, session);
            } else if (messageReceive.getMessageType() == MessageType.MESSAGE) {
                handleMessageMessage(messageReceive, session);
            } else if (messageReceive.getMessageType() == MessageType.LIST_USER) {
                handleMessageListUser(messageReceive, session);
            }
        }
    }
    /* hàm xử lý tạo room */
    private void handleMessageCreateRoom(RoomReceive messageReceive, Session session) {
        Gson gson = new Gson();
        String roomName = messageReceive.getRoomName();
        String names = messageReceive.getUsers();
        Room room = new Room();
        room.setName(roomName);
        Type list = new TypeToken<List<String>>() {
        }.getType();
        List<String> nameOfUser = gson.fromJson(names, list);
        users.forEach(user -> {
            nameOfUser.forEach(name -> {
                if (user.getUserProperties().get("name").equals(name)) {
                    room.join(user);
                }
            });
        });
        room.join(session);
        RoomResponse roomResponse = new RoomResponse();
        roomResponse.setMessage("You are joined the new room");
        roomResponse.addRoom(room.getName());
        String content = gson.toJson(roomResponse);
        MessageResponse messageResponse = new MessageResponse(MessageType.SHOW_ROOM, content);
        String msg = gson.toJson(messageResponse);
        room.sendMessage(msg);


    }

    /* chức năng lấy về danh sách user đang online trên hệ thống */
    private void handleMessageListUser(MessageReceive messageReceive, Session session) {
        MessageResponse messageResponse = new MessageResponse();
        Gson gson = new Gson();
        MessageType type = messageReceive.getMessageType();
        String content = gson.toJson(getUserOnline(session));
        messageResponse.setMessageType(type);
        messageResponse.setContent(content);
        String msg = gson.toJson(messageResponse);
        sendRequestUser(session, msg);
    }

    /* chức năng online vào hệ thống */
    private void handleLoginMessage(MessageReceive messageReceive, Session session) {
        MessageResponse messageResponse = new MessageResponse();
        Gson gson = new Gson();
        String name = messageReceive.getContent();
        MessageType type = messageReceive.getMessageType();
        if (session.getUserProperties().get("name") == null) {
            session.getUserProperties().put("name", name);
            String content = gson.toJson(getUserOnline(session));
            messageResponse.setMessageType(type);
            messageResponse.setContent(content);
            String msg = gson.toJson(messageResponse);
            sendMessageText(msg);
        }
    }

    /* chức năng gửi tin nhắn cho client */
    private void handleMessageMessage(MessageReceive messageReceive, Session session) {
        MessageResponse messageResponse = new MessageResponse();
        Gson gson = new Gson();
        MessageType type = messageReceive.getMessageType();
        String content = messageReceive.getContent();
        messageResponse.setMessageType(type);
        messageResponse.setContent(session.getUserProperties().get("name") + ":" + content);
        String msg = gson.toJson(messageResponse);
        sendMessageText(msg);
    }

    /* hàm trả về toàn bộ tên của user đang online*/
    private List<String> getUserOnline(Session session) {
        List<String> userOnline = new ArrayList<>();
        users.forEach(u -> {
            if (u.getId() != session.getId()) {
                userOnline.add((String) u.getUserProperties().get("name"));
            }
        });
        return userOnline;
    }

    private void sendRequestUser(Session session, String msg) {
        users.forEach(u -> {
            if (u.equals(session)) {
                try {
                    u.getBasicRemote().sendText(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* đóng kết nối, dĩ nhiên rồi .... */
    @OnClose
    public void onClose(Session session) {
        users.remove(session);
        Gson gson = new Gson();
        String content = gson.toJson(getUserOnline(session));
        MessageResponse messageResponse = new MessageResponse(MessageType.LIST_USER, content);
        String msg = gson.toJson(messageResponse);
        sendMessageText(msg);
    }

    /* xử lý lỗi liên quan khi kết lối ...*/
    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    /* hàm cho phép gửi 1 tin nhắn đến toàn bộ người dùng trong hệ thống */
    private void sendMessageText(String msg) {
        users.forEach(u -> {
            try {
                u.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
