/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.PatternSyntaxException;
import java.nio.charset.StandardCharsets;
import MsgData.*;

/**
 *
 * @author brom
 */
public class ServerConnection {

	// Artificial failure rate of 30% packet loss
	static double TRANSMISSION_FAILURE_RATE = 0.3;

	public DatagramSocket m_socket = null;
	private InetAddress m_serverAddress = null;
	private int m_serverPort = -1;
	private Client m_client = null;
	private MailBox m_mailbox = null;

	private int messageSize = 1024;
	
	public boolean handshaked = false;
	
	public boolean disconnect = false;

	public ServerConnection(String hostName, int port, Client c) {
		m_serverPort = port;
		m_client = c;
		// TODO:
		// * get address of host based on parameters and assign it to
		// m_serverAddress
		try {
			m_serverAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			System.out.println("Unknown host at " + port);
			System.out.println("UnknownHostException at " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println(port);
		System.out.println(hostName);

		// * set up socket and assign it to m_socket
		try {
			// Client shouldnt need port in socket declaration.
			m_socket = new DatagramSocket();
//			m_socket.connect(m_serverAddress, m_serverPort);
			// Creates a new mailbox.
			m_mailbox = new MailBox(this, m_socket);
			m_mailbox.start();
			
			sendChatMessage("/handshake " + m_client.m_name);
			//m_mailbox.listenForChatMessages();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("SocketException at " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println(m_serverPort + "" + m_serverAddress + "");

	}

	

	public void sendChatMessage(String message) {
		Random generator = new Random();
		double failure = generator.nextDouble();
		boolean successful = false;

		// TODO:
		// * marshal message if necessary

		byte[] messageBytes = new byte[message.length()];// ];
		message = modifyMessage(message);
		
		MsgData msg = new MsgData(message, m_client.m_name);
		
		System.out.println("In  sendchatmessage, we send: " + message);
		m_mailbox.setUnAckedMessage(msg);

		
		
		
		
		// * send a chat message to the server
		
		try {
			// Serialize to a byte array
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bStream); 
			oo.writeObject(msg);
			
			messageBytes = bStream.toByteArray();
			DatagramPacket packetToSend = new DatagramPacket(messageBytes, messageBytes.length, m_serverAddress,
					m_serverPort);
			
			failure = generator.nextDouble();
			if (failure > TRANSMISSION_FAILURE_RATE) {
				m_socket.send(packetToSend);
			} else {
				// Message got lost
				// Though fucking luck.
				System.out.println("Message got lost.");
				// sendChatMessage(message);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOException at " + e.getMessage());
		}
		
		// If we are going to disconnect:
		if(disconnect){
			// CLoses socket.
			m_socket.close();
			// Sends disconnect message.
			m_client.disconnectMessage();
		}
		

	}

	public String modifyMessage(String message) {
		String[] splitArray = null;
		String cleanMessage = new String();

		try {
			splitArray = message.split("\\s+");
		} catch (PatternSyntaxException e) {
			//
			System.out.println(e.getMessage());
		}

		// If its a leave/disconnect we need to disconnect the client.
		if (splitArray[0].equals("/leave") || splitArray[0].equals("/disconnect")) {
			splitArray = Arrays.copyOfRange(splitArray, 1, splitArray.length);
			cleanMessage = String.join(" ", splitArray);
			cleanMessage = splitArray[0] + m_client.m_name + cleanMessage;
			
			// Sets disconnect to true to disconnect after sending message.
			disconnect = true;
			
			
		} else {
			cleanMessage = String.join(" ", splitArray);
		}
		
		//cleanMessage = cleanMessage;

		return cleanMessage;
	}
	
	public void displayMessage(String message){
		m_client.displayMessage(message);
	}
	
	public void handshakeStatus(boolean status){
		handshaked = status;
		if(status == true){
			m_client.displayMessage("Successfully joined server!");
		}else{
			m_client.displayMessage("Username taken.");
			m_client.disconnectMessage();
		}
	}

}
