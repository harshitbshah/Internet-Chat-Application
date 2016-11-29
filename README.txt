Internet Chat Application: a Java-based chat application using Socket Programming 
Prepared by: Harshit Shah (UFID: 1211-6976) and Arnav Upadhyaya (UFID: 1111-4048)

There are three files in total for Internet Chat Application:
Server.java  : Everything related to the Server is present in this file
Client.java  : Everything related to the Client is present in this file
Message.java : This class defines the different type of messages that will be exchanged between the Clients and the Server.


Following commands can be used:

1. Broadcast message [message] 

   Any client is able to send a text to the server, which will relay it to all other clients for display.

2. Broadcast file [file_path]
   
   Any client is able to send a file of any type to the group via the server.

3. Unicast message [client_username] [message] 

   Any client is able to send a private message to a specific other client via the server.

4. Unicast file [client_username] [file_path] 

   Any client is able to send a private file of any type to a specific other client via the server.

5. Blockcast message [client_username] [message]
 
   Any client is able to send a text to all other clients except for one via the sever.