package Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

//
// Source file for the server side. 
//
// Created by Sanny Syberfeldt
// Maintained by Marcus Brohede
//

import java.net.*;
//import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

import javax.swing.JOptionPane;

import MsgData.*;

public class Server {

	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private DatagramSocket m_socket;

	private int messageSize = 1024;

	// private MsgData lastUnAck = null;

	public static void main(String[] args) {
		String port = JOptionPane.showInputDialog("Please input the port for the server!");
		try {
			Server instance = new Server(Integer.parseInt(port));
			instance.listenForClientMessages();
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Server(int portNumber) {
		// TODO: create a socket, attach it to port based on portNumber, and
		// assign it to m_socket
		try {
			m_socket = new DatagramSocket(portNumber);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("SocketException at " + e.getMessage());
		}
	}

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");

		do {

			for (int i = 0; i < m_connectedClients.size(); i++) {

				long timediff;
				if (m_connectedClients.get(i).lastUnAck != null) {

					// Checks how long ago message was sent.
					timediff = (System.currentTimeMillis() - m_connectedClients.get(i).lastUnAck.m_timestamp);
					System.out.println("inMailbox - Timediff is " + timediff);
					// If message was sent more then 10 milliseconds ago we
					// resend the message.
					if (timediff > 10) {
						System.out.println("Sends messages again in mailbox");
						m_connectedClients.get(i).sendMessage(m_connectedClients.get(i).lastUnAck.m_message, m_socket);

						System.out.println(m_connectedClients.get(i).failedMessages);

						// Increments the number of failed sends to check if the
						// client has crashed.
						m_connectedClients.get(i).failedMessages = m_connectedClients.get(i).failedMessages + 1;

						// If the message has failed 10 times, something bad has
						// happened.
						if (m_connectedClients.get(i).failedMessages > 10) {
							// Lets assume the client has crashed.
							System.out.println(
									"Client with name " + m_connectedClients.get(i).getName() + " has crashed.");
							m_connectedClients.remove(i);
						}
					}
				}
			}

			String cleanMessage = new String();
			int messageType;
			// String message = new String();
			String target = new String();

			MsgData msg = null;

			// TODO: Listen for client messages.
			// On reception of message, do the following:
			// recieve message
			byte[] recieveBytes = new byte[messageSize * 2];
			DatagramPacket packetToRecieve = new DatagramPacket(recieveBytes, recieveBytes.length);
			System.out.println("Wiating for a client message");
			try {
				// m_socket.setSoTimeout(5000);
				m_socket.receive(packetToRecieve);
				recieveBytes = packetToRecieve.getData();
				ByteArrayInputStream in = new ByteArrayInputStream(recieveBytes);
				ObjectInputStream instream = new ObjectInputStream(in);

				try {
					msg = (MsgData) instream.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// received = new String(recieveBytes, 0,
				// packetToRecieve.getLength());//
				// String(packetToRecieve.getData());
				System.out.println("We recieved a message with the text: " + msg.m_message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("IOException at " + e.getMessage());
			}
			// target = new String(recieveBytes, 1, 256);

			// * Unmarshal message
			// String recieved = new String(recieveBytes, 0,
			// packetToRecieve.getLength());

			// * Depending on message type, either
			// - Try to create a new ClientConnection using addClient(), send
			// response message to client detailing whether it was successful

			// Splitting the string into parts where there is a space.
			String[] splitArray = null;
			try {
				splitArray = msg.m_message.split("\\s+");
			} catch (PatternSyntaxException e) {
				//
				System.out.println(e.getMessage());
			}

			System.out.println("The full string is: " + msg.m_message + "end thingy");
			System.out.println("Type: " + splitArray[0]);

			if (splitArray[0].equals("/handshake") || splitArray[0].equals("/join")) {
				messageType = 0;
			} else if (splitArray[0].equals("/disconnect") || splitArray[0].equals("/leave")) {
				messageType = 1;
			} else if (splitArray[0].equals("/list")) {
				messageType = 2;
			} else if (splitArray[0].equals("/pm")) {
				messageType = 3;
			} else if (splitArray[0].equals("/yo")) {
				messageType = -1;
			} else {
				messageType = 4;
			}

			System.out.println("The type number is: " + messageType);

			// Get clean message.
			if (messageType < 4) {
				// cleanMessage = splitArray.toString();
				if (messageType == 0) {
					splitArray = Arrays.copyOfRange(splitArray, 1, splitArray.length);
					cleanMessage = String.join(" ", splitArray);
				} else if (messageType == 1) {

					cleanMessage = String.join(" ", splitArray);
				} else if (messageType == 2) {
					cleanMessage = String.join(" ", splitArray);
				} else if (messageType == 3) {
					if (splitArray.length > 2) {
						target = splitArray[1];
						splitArray = Arrays.copyOfRange(splitArray, 2, splitArray.length);
						cleanMessage = String.join(" ", splitArray);
					} else {
						messageType = 3;
						cleanMessage = "It's /pm [name] [message], not /pm whateveryoudid.";
						target = msg.m_name;
					}
				}

			} else {
				cleanMessage = msg.m_message;
			}

			// Handshake
			if (messageType == -1) {
				System.out.println("The message is a confirmation.");
				for (int i = 0; i < m_connectedClients.size(); i++) {
					if (m_connectedClients.get(i).lastUnAck != null) {
						splitArray = Arrays.copyOfRange(splitArray, 1, splitArray.length);
						String localmessage;
						localmessage = String.join(" ", splitArray);
						System.out.println(m_connectedClients.get(i).lastUnAck.m_message);
						System.out.println(localmessage);
						System.out.println(
								"The msg says: " + localmessage + " And the stored ack says: " + m_connectedClients.get(i).lastUnAck.m_message);
						if (m_connectedClients.get(i).lastUnAck != null) {
							if (localmessage.equals(m_connectedClients.get(i).lastUnAck.m_message)) {
								m_connectedClients.get(i).lastUnAck = null;
								m_connectedClients.get(i).failedMessages = 0;
							}
						}
						// String cleanMessage = getCleanMessage(received);
					}
				}
			} else if (messageType == 0) {
				System.out.println("CLEAN: " + cleanMessage);
				if (addClient(cleanMessage, packetToRecieve.getAddress(), packetToRecieve.getPort())) {
					System.out.println("User has been created");
					String testMessage = "/handshake true " + cleanMessage;
					sendPrivateMessage(testMessage, cleanMessage);
					// broadcast(cleanMessage + " just joined the server!
					// Welcome!");
				} else {
					System.out.println("Username is already taken");
					String testMessage = "/handshake false";
					sendPrivateMessage(testMessage, cleanMessage);
				}
				

			} // Disconnect
			else if (messageType == 1) {
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				if (removeClient(target)) {
					broadcast(cleanMessage);
				} else {
					sendPrivateMessage("You do not exist on the server. What you doin'?", target);
				}
			} // List
			else if (messageType == 2) {
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				list();
			} // - Send a private message to a user using sendPrivateMessage()
			else if (messageType == 3) {
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				sendPrivateMessage(cleanMessage, target);
			} // - Broadcast the message to all connected users using
				// broadcast()
			else if (messageType == 4) {
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				ClientConnection.sendAck("/yo " + msg.m_message, m_socket, packetToRecieve.getAddress(),
						packetToRecieve.getPort());
				broadcast(cleanMessage);
			} else {
				System.out.println("When did this even happen?");
			}

		} while (true);
	}

	public boolean addClient(String name, InetAddress address, int port) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return false; // Already exists a client with this name
			}
		}
		m_connectedClients.add(new ClientConnection(name, address, port));
		return true;
	}

	public boolean removeClient(String name) {

		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				m_connectedClients.remove(m_connectedClients.indexOf(c));
				broadcast(name + " just left the server. Say goodbye.");
				return true; // client existed and it is now removed.
			}
		}

		// returns false if removing wasnt possible (ie client didnt exist)
		return false;
	}

	public void sendPrivateMessage(String message, String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.sendMessage(message, m_socket);
			}
		}
	}

	public void broadcast(String message) {
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message, m_socket);
		}
	}

	public void list() {
		String message = "List of connected clients:\n\n";
		for (int i = 0; i < m_connectedClients.size(); i++) {
			message += (i + 1) + ": " + m_connectedClients.get(i).getName() + "\n";

		}
		System.out.println(message);
		broadcast(message);
	}
}
