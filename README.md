## Webchat realtime with java
 - Sử dụng lib websocket của java
    - Có 4 method chính được sử dụng:
        - OnOpen (quản lý việc kết nối từ các session)
        - OnMessage (quản lý việc gửi tin nhắn đến session từ phía server)
        - OnClose  ( quản lý việc 1 session ngắt kết nối)
        - OnError (quản lý việc kết nối xảy ra lỗi)
    - Tất cả client sẽ ở trong 1 tập hợp sesssion để quản lý (mối client là 1 session)
    - Khi client gửi tin nhắn đến server, tin nhắn đó được chuyển thành 1 chuỗi string, server giải mã chuối string đó 
    - Tin nhắn gửi từ client có nhiều dạng (MessageType) và nội dung (Content)
    - Server sau khi xử lý sẽ gửi lại một String với (MessageType) và nội dung (Content) để phía client xử lý
## File Structure
```
.
├── chat
│   ├── src
│   ├──  ├── main
├   ├──  ├──  ├── java 
├   ├──  ├──  ├── ├── message 
├   ├──  ├──  ├── ├──  ├── MessageReceive.java
├   ├──  ├──  ├── ├──  ├── MessageResponse.java
├   ├──  ├──  ├── ├──  ├── MessageType.java
├   ├──  ├──  ├── ├── room 
├   ├──  ├──  ├── ├──  ├── Room.java
├   ├──  ├──  ├── ├──  ├── RoomReceive.java
├   ├──  ├──  ├── ├──  ├── RoomResponse.java
├   ├──  ├──  ├── ├── Endpoint.java 
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
├── README.md
├── setting.gradle
```