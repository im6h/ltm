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
    private static Gson gson = new Gson();
    // get session and websocket connection
    @OnOpen
    public void onOpen(Session session){
        users.add(session);
    }

    // handle receive message from client
    @OnMessage
    public void onMessage(Session session, String msgJson) throws IOException {
        MessageReceive messageReceive = gson.fromJson(msgJson,MessageReceive.class);
        MessageResponse messageResponse = new MessageResponse();
        String content = messageReceive.getContent();
        MessageType type = messageReceive.getMessageType();
        if (type == MessageType.LOGIN){
            if (session.getUserProperties().get("name") == null){
                session.getUserProperties().put("name",content);
                messageResponse.setMessageType(type);
                messageResponse.setContent(content + " login");
                String msg = gson.toJson(messageResponse);
                sendText(msg);
            }
        }
        if (type == MessageType.MESSAGE){
            messageResponse.setMessageType(type);
            messageResponse.setContent(session.getUserProperties().get("name") + ":" +content);
            String msg = gson.toJson(messageResponse);
            sendText(msg);
        }
        if (type == MessageType.LISTUSER){
            List<String> userOnline = new ArrayList<>();
            users.forEach(u->{
                String name = (String) u.getUserProperties().get("name");
                userOnline.add(name);
            });
            messageResponse.setMessageType(type);
            messageResponse.setContent(gson.toJson(userOnline));
            String msg = gson.toJson(messageResponse);
            users.forEach(u->{
                if (session.equals(u)){
                    try {
                        u.getBasicRemote().sendText(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    // close connect
    @OnClose
    public void onClose(Session session){
        users.remove(session);
    }

    // handle error
    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }

    public void sendText(String msg){
        users.forEach(u->{
            try {
                u.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
