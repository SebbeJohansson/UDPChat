package MsgData;

import java.io.Serializable;
import java.net.*;

public class MsgData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String m_message = null;
	public long m_timestamp;
	public String m_name;
	
	
	public MsgData(){
		
	}
	
	
	
	public MsgData(String message) {
		m_message = message;
		m_timestamp = System.currentTimeMillis();
	}
	
	public MsgData(String message, String name) {
		m_message = message;
		m_name = name;
		m_timestamp = System.currentTimeMillis();
	}


}