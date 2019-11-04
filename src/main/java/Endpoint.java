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
import java.nio.ByteBuffer;
import java.util.*;

@ServerEndpoint(value = "/socket")
public class Endpoint {

    private static Set<Session> users = Collections.synchronizedSet(new HashSet<Session>());
    private static Set<Room> rooms = Collections.synchronizedSet(new HashSet<>());

    /* hàm kết nối*/
    @OnOpen
    public void onOpen(Session session) {
        users.add(session);
        sendAllUser();
    }

    /* hàm nhận tin nhắn từ phía client và gửi trả tin nhắn cho client */
    @OnMessage
    public void onMessage(Session session, String msgJson) throws IOException {
        Gson gson = new Gson();
        List<String> userName = new ArrayList<>();
        if (msgJson.contains("CREATE_ROOM")) {
            RoomReceive roomReceive = gson.fromJson(msgJson, RoomReceive.class);
            handleMessageCreateRoom(roomReceive, session);
        } else if (msgJson.contains("SEND_MESSAGE")) {
            RoomReceive roomReceive = gson.fromJson(msgJson, RoomReceive.class);
            handleSendMessage(roomReceive, session);
        } else if (msgJson.contains("SEND_FILE")) {
            RoomReceive roomReceive = gson.fromJson(msgJson, RoomReceive.class);
            handleSendFile(roomReceive, session);

        } else {
            MessageReceive messageReceive = gson.fromJson(msgJson, MessageReceive.class);
            if (messageReceive.getMessageType() == MessageType.LOGIN) {
                handleLoginMessage(messageReceive, session);
            } else if (messageReceive.getMessageType() == MessageType.LIST_USER) {
                handleMessageListUser(messageReceive, session);
            } else if (messageReceive.getMessageType() == MessageType.GET_MESSAGE) {
                handleGetMessageRequest(messageReceive, session);
            }
        }
    }
    @OnMessage(maxMessageSize = 20000000)
    public void messageBinary(Session session, ByteBuffer byteBuffer){
        users.forEach(u->{
            try {
                u.getBasicRemote().sendBinary(byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleSendFile(RoomReceive roomReceive, Session session) {
        Gson gson = new Gson();

        String nameRoom = roomReceive.getRoomName();
        String message = roomReceive.getUsers();
        Set<Session> user = new HashSet<>();
        for (Room room : rooms) {
            if (room.getName().equals(nameRoom)) {
                user = room.getUsers();
            }
        }
        MessageResponse messageResponse = new MessageResponse(MessageType.RESPONSE_FILE, message);
        String json = gson.toJson(messageResponse);
        user.forEach(u -> {
            try {
                u.getBasicRemote().sendText(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleSendMessage(RoomReceive messageReceive, Session set) {
        Gson gson = new Gson();

        String nameRoom = messageReceive.getRoomName();
        String message = messageReceive.getUsers();
        String nameUser = null;
        Set<Session> user = new HashSet<>();
        for (Room room : rooms) {
            if (room.getName().equals(nameRoom)) {
                user = room.getUsers();
            }
        }
        for (Session u : user) {
            if (u.getId().equals(set.getId())) {
                nameUser = (String) u.getUserProperties().get("name");
            }
        }
        String msg = nameUser + " said: " + message;
        MessageResponse messageResponse = new MessageResponse(MessageType.RESPONSE_MESSAGE, msg);
        String json = gson.toJson(messageResponse);
        user.forEach(u -> {
            try {
                u.getBasicRemote().sendText(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleGetMessageRequest(MessageReceive messageReceive, Session ses) {
        Gson gson = new Gson();
        Set<Session> user = new HashSet<>();
        String content = messageReceive.getContent();

        for (Room room : rooms) {
            if (room.getName().equals(content)) {
                user = room.getUsers();
            }
        }
        /* gui message den cac user  co trong nhom*/
        List<String> msgs = new ArrayList<>();
        for (Session session : users) {
            for (Session session1 : user) {
                if (session1.getId().equals(session.getId())) {
//                    String msg = (String) session1.getUserProperties().get("name") + " login";
//                    msgs.add(msg);
                }
            }
        }
        String listMsg = gson.toJson(msgs);
        MessageResponse messageResponse = new MessageResponse(MessageType.GET_MESSAGE, listMsg);
        String json = gson.toJson(messageResponse);
        try {
            ses.getBasicRemote().sendText(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* hàm xử lý tạo room */
    private void handleMessageCreateRoom(RoomReceive messageReceive, Session session) {
        Gson gson = new Gson();
        String roomName = messageReceive.getRoomName();
        String names = messageReceive.getUsers();
        Type list = new TypeToken<List<String>>() {
        }.getType();
        List<String> nameOfUser = gson.fromJson(names, list);

        Set<Session> userRoom = Collections.synchronizedSet(new HashSet<>());
        users.forEach(user -> {
            nameOfUser.forEach(name -> {
                if (user.getUserProperties().get("name").equals(name)) {
                    userRoom.add(user);
                }
            });
        });
        userRoom.add(session);
        Room room = new Room(roomName, userRoom);
        rooms.add(room);
        /* trả về hai 2 message, 1 cho session hiện tại và 1 cho những session khác*/

        RoomResponse roomResponse = new RoomResponse();
        userRoom.forEach(user -> {
            if (user.equals(session)) {
                roomResponse.setMessage("Ban vua tao phong");
                roomResponse.setNameRoom(roomName);
                String json = gson.toJson(roomResponse);
                MessageResponse messageResponse = new MessageResponse(MessageType.LIST_ROOM, json);
                String content = gson.toJson(messageResponse);
                try {
                    user.getBasicRemote().sendText(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                roomResponse.setMessage("Ban vua duoc them vao phong");
                roomResponse.setNameRoom(roomName);
                String json = gson.toJson(roomResponse);
                MessageResponse messageResponse = new MessageResponse(MessageType.JOIN_ROOM, json);
                String content = gson.toJson(messageResponse);
                try {
                    user.getBasicRemote().sendText(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* chức năng login vào hệ thống */
    private void handleLoginMessage(MessageReceive messageReceive, Session session) {
        String name = messageReceive.getContent();
        if (session.getUserProperties().get("name") == null) {
            session.getUserProperties().put("name", name);
        }
        sendAllUser();
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
        users.forEach(user -> {
            if (user.getId() != session.getId()) {
                String name = (String) user.getUserProperties().get("name");
                userOnline.add(name);
            }
        });
        return userOnline;
    }

    private void sendAllUser() {
        Gson gson = new Gson();
        for (Session ss : users) {
            List<String> userName = new ArrayList<>();
            for (Session sess1 : users) {
                if (ss.equals(sess1)) {
                    continue;
                } else {
                    userName.add((String) sess1.getUserProperties().get("name"));
                }
            }
            String json = gson.toJson(userName);
            MessageResponse messageResponse = new MessageResponse(MessageType.LIST_USER, json);
            String msg = gson.toJson(messageResponse);
            try {
                ss.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* đóng kết nối, dĩ nhiên rồi .... */
    @OnClose
    public void onClose(Session session) {
        users.remove(session);
        sendAllUser();
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
