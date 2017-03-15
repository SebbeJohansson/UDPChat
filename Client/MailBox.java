package Client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;
import MsgData.*;

public class MailBox extends Thread {

	public MsgData lastUnAck = null;

	private ServerConnection m_serverConnection = null;
	private DatagramSocket m_socket = null;

	private int messageSize = 1024;
	
	private int failedMessages = 0;

	public MailBox(ServerConnection serverConnection, DatagramSocket socket) {
		m_serverConnection = serverConnection;
		m_socket = socket;

		
	}
	
	public void run() {
		do {

			long timediff;
			if(lastUnAck != null){
				
				// Checks how long ago message was sent.
				timediff = (System.currentTimeMillis() - lastUnAck.m_timestamp);
				System.out.println("inMailbox - Timediff is " + timediff);
				// If message was sent more then 10 milliseconds ago we resend the message.
				if (timediff > 10) {
					System.out.println("Sends messages again in mailbox");
					m_serverConnection.sendChatMessage(lastUnAck.m_message);
					
					System.out.println(failedMessages);
					
					// Increments the number of failed sends to check if the server has crashed.
					failedMessages = failedMessages + 1;
					
					// If the message has failed 10 times, something bad has happened.
					if(failedMessages > 10){
						// Lets assume the server has crashed.
						m_serverConnection.displayMessage("Teh server has crashed. Srry.");
						m_socket.close();
					}
				}
			}
			
			
			
			// TODO:
			// * receive message from server
			byte[] recieveBytes = new byte[messageSize];
			// * receive response message from server
			DatagramPacket packetToRecieve = new DatagramPacket(recieveBytes, recieveBytes.length);

			//String received = new String();
			MsgData msg = null;

			System.out.println("Before recieve in mailbox");
			// System.out.println("It was "+successful);
			try {
				m_socket.setSoTimeout(500);
				m_socket.receive(packetToRecieve);
				recieveBytes = packetToRecieve.getData();
				ByteArrayInputStream in = new ByteArrayInputStream(recieveBytes);
				ObjectInputStream instream = new ObjectInputStream(in);
				
				try {
					msg = (MsgData)instream.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				//received = new String(recieveBytes, 0, packetToRecieve.getLength());// String(packetToRecieve.getData());
				System.out.println("We recieved a message with the text: " + msg.m_message);
			} catch (SocketTimeoutException e) {
				System.out.println("SocketException at " + e.getMessage() + " in ServerConnection in receivechatmessage");
				//received = null;
				msg = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("IOException at " + e.getMessage());
			} finally {
				try {
					m_socket.setSoTimeout(0);
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
			
			//System.out.println(received);
			if (msg != null) {

				String[] splitArray = null;
				try {
					splitArray = msg.m_message.split("\\s+");
				} catch (PatternSyntaxException e) {
					//
					System.out.println(e.getMessage());
				}

				if (splitArray[0].equals("/handshake")) {
					lastUnAck = null;
					if (splitArray[1].equals("true")) {
						m_serverConnection.handshakeStatus(true);
					} else {
						m_serverConnection.handshakeStatus(false);
					}
				}else if (splitArray[0].equals("/yo")) {
					if(lastUnAck != null){
						splitArray = Arrays.copyOfRange(splitArray, 1, splitArray.length);
						String localmessage;
						localmessage = String.join(" ", splitArray);
						System.out.println(lastUnAck.m_message);
						System.out.println(localmessage);
						System.out.println("The msg says: "+ localmessage +" And the stored ack says: "+lastUnAck.m_message);
						if(lastUnAck != null){
							if(localmessage.equals(lastUnAck.m_message)){
								lastUnAck = null;
								failedMessages = 0;
							}
						}
						// String cleanMessage = getCleanMessage(received);
					}
					
					
				}else if (msg != null || msg.m_message != "") {
					m_serverConnection.displayMessage(msg.m_message);
				}

				System.out.println("After recieve in mailbox");
			}
		} while (!m_socket.isClosed());
		// Note that the main thread can block on receive here without
		// problems, since the GUI runs in a separate thread
		// return received;
	}

	public void listenForChatMessages() {

		
	}

	private String getCleanMessage(String message) {
		String[] splitArray = null;
		String cleanMessage = new String();

		try {
			splitArray = message.split("\\s+");
		} catch (PatternSyntaxException e) {
			//
			System.out.println(e.getMessage());
		}

		// Without timestamp.
		splitArray = Arrays.copyOfRange(splitArray, 1, splitArray.length);

		if (splitArray[0].equals("/yo")) {
			splitArray = Arrays.copyOfRange(splitArray, 1, splitArray.length);
			cleanMessage = String.join(" ", splitArray);
		} else {
			cleanMessage = String.join(" ", splitArray);
		}

		System.out.println("InMailbox - without timestamp and command: " + cleanMessage);
		return cleanMessage;

	}

	public void setUnAckedMessage(MsgData msg) {
		lastUnAck = msg;
	}

	public MsgData getLastUnAck() {
		return lastUnAck;
	}

}
