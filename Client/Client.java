package Client;

import java.awt.event.*;
//import java.io.*;

import javax.swing.JOptionPane;

public class Client implements ActionListener {

	public String m_name = null;
	private final ChatGUI m_GUI;
	private ServerConnection m_connection = null;

	public static void main(String[] args) {
		
		String name = JOptionPane.showInputDialog("Please input a name!");
		String ip = JOptionPane.showInputDialog("Please input the ip to the server!");
		String port = JOptionPane.showInputDialog("Please input the port for the server!");
		
		try {
			Client instance = new Client(name);
			instance.connectToServer(ip, Integer.parseInt(port));
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Client(String userName) {
		m_name = userName;
		// Start up GUI (runs in its own thread)
		m_GUI = new ChatGUI(this, m_name);
		System.out.println(userName);
	}

	private void connectToServer(String hostName, int port) {
		// Create a new server connection
		m_connection = new ServerConnection(hostName, port, this);
		
		// Handshake is sent on serverconnection.
		
	}
	
	public void disconnectMessage(){
		m_GUI.displayMessage("You have now been disconnected from the server.");
	}

	
	
	public void displayMessage(String message){
		m_GUI.displayMessage(message);
	}

	// Sole ActionListener method; acts as a callback from GUI when user hits
	// enter in input field
	@Override
	public void actionPerformed(ActionEvent e) {
		// Since the only possible event is a carriage return in the text input
		// field,
		// the text in the chat input field can now be sent to the server.
		if(!m_connection.m_socket.isClosed()){
			m_connection.sendChatMessage(m_GUI.getInput());
			m_GUI.clearInput();
		}else{
			displayMessage("You are  not connected. Please restart.");
			m_GUI.clearInput();
		}
		
	}
}
