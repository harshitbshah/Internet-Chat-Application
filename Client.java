import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
	private Socket requestSocket;           //socket connect to the server
	private ObjectOutputStream out;         //stream write to the socket
	private ObjectInputStream in;          //stream read from the socket
	// the server, the port and the username
	private String server, username;
	private int port;

	public void Client() {}
	
	/*
	 *  Constructor called by console mode
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	
	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
			System.out.println(msg);      // println in console mode
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(Message msg) {
		try {
			out.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try { 
			if(in != null) in.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(out != null) out.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(requestSocket != null) requestSocket.close();
		}
		catch(Exception e) {} // not much else I can do
			
	}
	
	/*
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the server
		try {
			requestSocket = new Socket(server, port);
		} 
		// if it failed not much I can so
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + requestSocket.getInetAddress() + ":" + requestSocket.getPort();
		display(msg);
	
		String format = "\nFollowing commands can be used:\n\nBroadcast message [message] \nBroadcast file [file_path] \nUnicast message [client_username] [message] \nUnicast file [client_username] [file_path] \nBlockcast message [client_username] [message]\n";
		display(format);
		/* Creating both Data Stream */
		try
		{
			in  = new ObjectInputStream(requestSocket.getInputStream());
			out = new ObjectOutputStream(requestSocket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			out.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}
	
	
	/*
	 * To start the Client in console mode use one of the following command
	 * > java Client
	 * > java Client username
	 * > java Client username portNumber
	 * > java Client username portNumber serverAddress
	 * at the console prompt
	 * If the portNumber is not specified 1500 is used
	 * If the serverAddress is not specified "localHost" is used
	 * If the username is not specified "Anonymous" is used
	 * > java Client 
	 * is equivalent to
	 * > java Client Anonymous 1500 localhost 
	 * are eqquivalent
	 * 
	 * In console mode, if an error occurs the program simply stops
	 * when a GUI id used, the GUI is informed of the disconnection
	 */
	public static void main(String[] args) {
		// default values
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";

		// depending of the number of arguments provided we fall through
		switch(args.length) {
			// > javac Client username portNumber serverAddr
			case 3:
				serverAddress = args[2];
			// > javac Client username portNumber
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber]");
					return;
				}
			// > javac Client username
			case 1: 
				userName = args[0];
			// > java Client
			case 0:
				break;
			// invalid number of arguments
			default:
				System.out.println("Usage is: > java Client [username] [portNumber]");
			return;
		}
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
		// test if we can start the connection to the Server
		// if it failed nothing we can do
		if(!client.start())
			return;
		
		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
		while(true) {
			System.out.print("> ");
			// read message from user
			String []wholeMsg = scan.nextLine().split(" ",2);
			String con = "";
			
			String msg = wholeMsg[0];
			if(wholeMsg.length > 1)
				con = wholeMsg[1];
					
			
			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("logout")) {
				client.sendMessage(new Message(Message.LOGOUT, ""));
				// break to do the disconnect
				break;
			}
			// broadcast message
			else if(msg.equalsIgnoreCase("broadcast")){				
				client.sendMessage(new Message(Message.BROADCASTMESSAGE, con));
			}
			// unicast message
			else if(msg.equalsIgnoreCase("unicast")){
				client.sendMessage(new Message(Message.UNICASTMESSAGE, con));
			}
			else if(msg.equalsIgnoreCase("blockcast")){
				client.sendMessage(new Message(Message.BLOCKCASTMESSAGE, con));
			}
		}
		// done disconnect
		client.disconnect();	
	}
	
	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) in.readObject();
					// if console mode print the message and add back the prompt
						System.out.println(msg);
						System.out.print("> ");
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}


}
