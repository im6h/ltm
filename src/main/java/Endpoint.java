import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/socket")
public class Endpoint {

    private static Set<Session> users = Collections.synchronizedSet(new HashSet<Session>());
    // get session and websocket connection
    @OnOpen
    public void onOpen(Session session){
        users.add(session);
    }

    // handle receive message from client
    @OnMessage
    public void onMessage(String msg,Session session) throws IOException {
//        Gson gson = new Gson();
        String username = (String)session.getUserProperties().get("username");
        if (username == null){
            session.getUserProperties().put("username",msg);
            session.getBasicRemote().sendText("Welcome to chat : "+ msg);
        }
        else{
            users.forEach(user->{
                try {
                    user.getBasicRemote().sendText(username + " said: " + msg);
                } catch (IOException e) {
                    e.printStackTrace();
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
}
