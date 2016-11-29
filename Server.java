import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

	private ArrayList <Handler> clients;
	private boolean flag; 	// the boolean that will be turned of to stop the server
	private int portNo; // the port number to listen for connection
	private static int uniId; // a unique ID for each connection

	public Server(int port) {
		clients = new ArrayList<Handler>();
		this.portNo = port;
	}

	public void start() {
		flag = true;
		// create socket server and wait for connection requests
		try 
		{
			// the socket used by the server
			ServerSocket serverSock = new ServerSocket(portNo);

			// infinite loop to wait for connections
			while(flag) 
			{
				display("Server waiting for Clients on port " + portNo + ".");				

				// accept connection
				Socket socket = serverSock.accept();  	

				// if server was asked to stop
				if(!flag)
					break;
				Handler t = new Handler(socket);  
				clients.add(t);	
				t.start();
			}
			// Server was asked to stop
			try {
				serverSock.close();
				for(int i = 0; i < clients.size(); ++i) {
					Handler tc = clients.get(i);
					try {
						tc.in.close();
						tc.out.close();
						tc.connection.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			String msg = " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}	

	private void display(String msg) {
		System.out.println(msg);
	}

	/*
	 *  To broadcast a message / file to all Clients
	 */
	private synchronized void broadcast(String username, String message, Handler curr) {
		boolean found1 = false;
		boolean found2 = false;
		String []con = message.split(" ",2);
		if (con.length < 2) {
			curr.writeMsg("Not enough args passed. Please try again.");
			return;
		}
		else if (con.length > 2) {
			curr.writeMsg("Too many args passed. Please try again.");
			return;
		}
		if(con[0].equalsIgnoreCase("message")){
			// Display message on console
			//			System.out.println(curr.username + " broadcasted message");

			// we loop in reverse order in case we would have to remove a Client
			// because it has disconnected
			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);

				if(ct.no == curr.no){
					continue;
				}

				if(!ct.writeMsg(username + " : " + con[1])) {
					clients.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
				else{
					found2 = true;
				}
			}
			if(found2 == true){
				System.out.println(curr.username + " broadcasted message");
			}
		}
		else if(con[0].equalsIgnoreCase("file")){
			File srcFolder = new File(con[1]);

			InputStream filChk = null; 

			try{
				filChk = new FileInputStream(srcFolder);
				filChk.close();
			}
			catch(IOException e){

				System.out.println(curr.username + " broadcasted incorrect file");
				for(int i = clients.size(); --i >= 0;) {
					Handler ct = clients.get(i);

					if(ct.no == curr.no){
						// Try to write to the Client if it fails remove it from the list
						if(!ct.writeMsg("Input file doesn't exist ")) {
							clients.remove(i);
							display("Disconnected Client " + ct.username + " removed from list.");
						}
						return;
					}


				}

			}

			//			System.out.println(curr.username + " broadcasted file");

			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);

				if(ct.no == curr.no){
					continue;
				}

				boolean success = new File("..\\" + ct.getUsername()).mkdir();
				File dstFolder = new File("..\\" + ct.getUsername() + "\\" + con[1].substring(con[1].lastIndexOf('/') + 1));

				// Try to write to the Client if it fails remove it from the list
				if(!ct.writeMsg("File: " + con[1].substring(con[1].lastIndexOf('/') + 1) + " was sent by " + username) || !ct.copyFile(srcFolder,dstFolder)) {
					clients.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
				else{
					found1 = true;
				}	
			}

			if(found1 == true){
				System.out.println(curr.username + " broadcasted file");

			}	
		}
		else{
			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);
				// Try to write to the Client if it fails remove it from the list

				if((ct.no == curr.no)){	
					ct.writeMsg("Incorrect command entered. Please try again.");
				}
			}

		}

	}


	/*
	 *  To unicast a message / file to all Clients
	 */
	private synchronized void unicast(String username, String message, Handler curr) {
		String []con = message.split(" ",3);
		boolean found1 = false;
		boolean found2 = false;
		
		if (con.length < 3) {
			curr.writeMsg("Not enough args passed. Please try again.");
			return;
		}
		else if (con.length > 3) {
			curr.writeMsg("Too many args passed. Please try again.");
			return;
		}

		if(con[0].equalsIgnoreCase("message")){
			// Display message on console
			
			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);
				// Try to write to the Client if it fails remove it from the list

				if(ct.getUsername().equalsIgnoreCase(con[1]) && !(ct.no == curr.no)){
					if(!ct.writeMsg(username + " : " + con[2])) {
						clients.remove(i);
						display("Disconnected Client " + ct.username + " removed from list.");
					}
					else{
						found1 = true;
						break;
					}
				}

				else
					continue;
			}
			if(found1 == false){
				curr.writeMsg("User does not exist. Please try again.");
			}
			else{
				System.out.println(curr.username + " unicasted message to " + con[1]);
			}
		}
		else if(con[0].equalsIgnoreCase("file")){			
			File srcFolder = new File(con[2]);

			InputStream filChk = null; 

			try{
				filChk = new FileInputStream(srcFolder);
				filChk.close();
			}
			catch(IOException e){

				System.out.println(curr.username + " unicasted incorrect file");
				for(int i = clients.size(); --i >= 0;) {
					Handler ct = clients.get(i);

					if(ct.no == curr.no){
						// Try to write to the Client if it fails remove it from the list
						if(!ct.writeMsg("Input file doesn't exist ")) {
							clients.remove(i);
							display("Disconnected Client " + ct.username + " removed from list.");
						}
						return;
					}


				}

			}

			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);

				if(ct.no == curr.no || (!(ct.getUsername().equalsIgnoreCase(con[1])))){
					continue;
				}

				boolean success = new File("..\\" + ct.getUsername()).mkdir();
				File dstFolder = new File("..\\" + ct.getUsername() + "\\" + con[2].substring(con[2].lastIndexOf('/') + 1));

				// Try to write to the Client if it fails remove it from the list
				if(!ct.writeMsg("File: " + con[2].substring(con[2].lastIndexOf('/') + 1) + " was sent by " + username) || !ct.copyFile(srcFolder,dstFolder)) {
					curr.writeMsg("User does not exist. Please try again.");
					clients.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
				else{
					found2 = true;
				}
			}
			if(found2 == false){
				curr.writeMsg("User does not exist. Please try again.");
			}
			else{
				System.out.println(curr.username + " unicasted file to " + con[1]);
			}			
		}
		else{
			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);
				// Try to write to the Client if it fails remove it from the list

				if((ct.no == curr.no)){	
					ct.writeMsg("Incorrect command entered. Please try again.");
				}
			}
		}
	}
	private synchronized void blockcast(String username, String message, Handler curr) {
		String []con = message.split(" ",3);
		if (con.length < 3) {
			curr.writeMsg("Not enough args passed. Please try again.");
			return;
		}
		else if (con.length > 3) {
			curr.writeMsg("Too many args passed. Please try again.");
			return;
		}
		if(con[0].equalsIgnoreCase("message")){
			// Display message on console
			System.out.println(curr.username + " blockcasted message");

			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);
				// Try to write to the Client if it fails remove it from the list

				if(ct.getUsername().equalsIgnoreCase(con[1]) || (ct.no == curr.no)){
					continue;
				}

				else{
					if(!ct.writeMsg(username + " : " + con[2])) {
						clients.remove(i);
						display("Disconnected Client " + ct.username + " removed from list.");
					}
				}
			}
		}
		else
		{
			for(int i = clients.size(); --i >= 0;) {
				Handler ct = clients.get(i);
				// Try to write to the Client if it fails remove it from the list

				if((ct.no == curr.no)){	
					ct.writeMsg("Incorrect command entered. Please try again.");
				}
			}
		}
	}
	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < clients.size(); ++i) {
			Handler ct = clients.get(i);
			// found it
			if(ct.no == id) {
				clients.remove(i);
				return;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// start server on port 1500 unless a PortNumber is specified 
		int port = 1500;
		System.out.println("The server is running.");  

		switch(args.length) {
		case 1:
			try {
				port = Integer.parseInt(args[0]);
			}
			catch(Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;

		}
		// create a server object and start it
		Server server = new Server(port);
		server.start();

	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	class Handler extends Thread {
		private Socket connection;
		private ObjectInputStream in;	//stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client
		String username; // the Username of the Client
		Message cm; // the only type of message a will receive
		String date; // the date I connect



		public String getUsername() {
			return username;
		}

		public Handler(Socket connection) {
			this.connection = connection;

			// a unique id
			no = ++uniId;
			this.connection = connection;
			/* Creating both Data Stream */
			try
			{
				// create output first
				out = new ObjectOutputStream(connection.getOutputStream());
				in  = new ObjectInputStream(connection.getInputStream());
				// read the username
				username = (String) in.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}

		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (Message) in.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();

				// Switch on the type of message receive
				switch(cm.getType()) {

				case Message.BROADCASTMESSAGE:
					broadcast(username, message, this);
					break;
				case Message.UNICASTMESSAGE:
					unicast(username, message, this);
					break;
				case Message.BLOCKCASTMESSAGE:
					blockcast(username, message, this);
					break;
				case Message.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(no);
			close();
		}

		//send a message to the output stream
		public void sendMessage(String msg)
		{
			try{

				for(int i = clients.size(); --i >= 0;) {
					Handler ct = clients.get(i);
					// try to write to the Client if it fails remove it from the list
					if(!ct.writeMsg(msg)) {
						clients.remove(i);
						display("Disconnected Client " + ct.username + " removed from list.");
					}
				}

			}
			catch(Exception ioException){
				ioException.printStackTrace();
			}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!connection.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				out.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}

		/*
		 *  to copy a file from one location to another
		 */
		private boolean copyFile(File source, File dest) {
			InputStream input = null;
			OutputStream output = null;
			try {
				input = new FileInputStream(source);
				output = new FileOutputStream(dest);
				byte[] buf = new byte[1024];
				int bytesRead;
				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}

				input.close();
				output.close();
			} 
			catch(IOException e){
				e.printStackTrace();
			}

			return true;
		}

		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(out != null) out.close();
			}
			catch(Exception e) {}
			try {
				if(in != null) in.close();
			}
			catch(Exception e) {};
			try {
				if(connection != null) connection.close();
			}
			catch (Exception e) {}
		}

	}

}
