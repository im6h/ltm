import com.google.gson.Gson;
import message.MessageReceive;
import message.MessageResponse;
import message.MessageType;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@ServerEndpoint(value = "/socket")
public class Endpoint {

    private static Set<Session> users = Collections.synchronizedSet(new HashSet<Session>());

    /* hàm kết nối*/
    @OnOpen
    public void onOpen(Session session){
        users.add(session);
    }

    /* hàm nhận tin nhắn từ phía client và gửi trả tin nhắn cho client */
    @OnMessage
    public void onMessage(Session session, String msgJson) throws IOException {
        Gson gson = new Gson();
        MessageReceive messageReceive = gson.fromJson(msgJson,MessageReceive.class);
        if (messageReceive.getMessageType() == MessageType.LOGIN){
            handleLoginMessage(messageReceive,session);
        }else if (messageReceive.getMessageType() == MessageType.MESSAGE){
            handleMessageMessage(messageReceive,session);
        }else if (messageReceive.getMessageType() == MessageType.LISTUSER){
            handleMessageListUser(messageReceive,session);
        }
    }

    /* chức năng lấy về danh sách user đang online trên hệ thống */
    private void handleMessageListUser(MessageReceive messageReceive, Session session) {
        MessageResponse messageResponse = new MessageResponse();
        Gson gson = new Gson();
        MessageType type = messageReceive.getMessageType();
        String content = gson.toJson(getUserOnline());
        messageResponse.setMessageType(type);
        messageResponse.setContent(content);
        String msg = gson.toJson(messageResponse);
        sendRequestUser(session,msg);
    }

    /* chức năng online vào hệ thống */
    private void handleLoginMessage(MessageReceive messageReceive,Session session) {
        MessageResponse messageResponse = new MessageResponse();
        Gson gson = new Gson();
        String name = messageReceive.getContent();
        MessageType type = messageReceive.getMessageType();
        if (session.getUserProperties().get("name")== null){
            session.getUserProperties().put("name",name);
            String content = gson.toJson(getUserOnline());
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
        messageResponse.setContent(session.getUserProperties().get("name") + ":" +content);
        String msg = gson.toJson(messageResponse);
        sendMessageText(msg);
    }

    /* hàm trả về toàn bộ tên của user đang online*/
    private List<String> getUserOnline (){
        List<String> userOnline = new ArrayList<>();
        users.forEach(u->{
            String name = (String) u.getUserProperties().get("name");
            userOnline.add(name);
        });
        return userOnline;
    }
    private void sendRequestUser(Session session, String msg){
        users.forEach(u->{
            if (u.equals(session)){
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
    public void onClose(Session session){
        users.remove(session);
        Gson gson = new Gson();
        String content = gson.toJson(getUserOnline());
        MessageResponse messageResponse = new MessageResponse(MessageType.LISTUSER,content);
        String msg = gson.toJson(messageResponse);
        sendMessageText(msg);
    }

    /* xử lý lỗi liên quan khi kết lối ...*/
    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }

    /* hàm cho phép gửi 1 tin nhắn đến toàn bộ người dùng trong hệ thống */
    private void sendMessageText(String msg){
        users.forEach(u->{
            try {
                u.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
