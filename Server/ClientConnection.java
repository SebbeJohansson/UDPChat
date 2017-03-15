/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import MsgData.MsgData;

/**
 * 
 * @author brom
 */
public class ClientConnection {

	static double TRANSMISSION_FAILURE_RATE = 0.3;

	private final String m_name;
	private final InetAddress m_address;
	private final int m_port;

	//private int messageSize = 1024;
	
	public MsgData lastUnAck = null;
	
	public int failedMessages = 0;

	public ClientConnection(String name, InetAddress address, int port) {
		m_name = name;
		m_address = address;
		m_port = port;
	}

	public void sendMessage(String message, DatagramSocket socket) {
		
		Random generator = new Random();
		double failure = generator.nextDouble();
		boolean successful = false;

		// TODO:
		// * marshal message if necessary

		byte[] messageBytes = new byte[message.length()];// ];
		//message = modifyMessage(message);
		
		MsgData msg = new MsgData(message);
		lastUnAck = msg;
		
		System.out.println(message);
		//m_mailbox.setUnAckedMessage(msg);

		
		
		
		
		// * send a chat message to the server
		
		try {
			// Serialize to a byte array
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bStream); 
			oo.writeObject(msg);
			
			messageBytes = bStream.toByteArray();
			DatagramPacket packetToSend = new DatagramPacket(messageBytes, messageBytes.length, m_address,
					m_port);
			
			failure = generator.nextDouble();
			if (failure > TRANSMISSION_FAILURE_RATE) {
				socket.send(packetToSend);
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

		
	

	}
	
	static public void sendAck(String message, DatagramSocket socket, InetAddress ip, int port){
		Random generator = new Random();
		double failure = generator.nextDouble();
		boolean successful = false;

		// TODO:
		// * marshal message if necessary

		byte[] messageBytes = new byte[message.length()];// ];
		//message = modifyMessage(message);
		
		MsgData msg = new MsgData(message);
		
		System.out.println(message);
		//m_mailbox.setUnAckedMessage(msg);

		
		
		
		
		// * send a chat message to the server
		
		try {
			// Serialize to a byte array
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bStream); 
			oo.writeObject(msg);
			
			messageBytes = bStream.toByteArray();
			DatagramPacket packetToSend = new DatagramPacket(messageBytes, messageBytes.length, ip,
					port);
			
			failure = generator.nextDouble();
			if (failure > TRANSMISSION_FAILURE_RATE) {
				socket.send(packetToSend);
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
	}

	public boolean hasName(String testName) {
		return testName.equals(m_name);
	}

	public String getName() {
		return m_name;
	}

}
