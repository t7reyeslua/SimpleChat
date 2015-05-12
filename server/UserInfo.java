package server;
import java.io.*;
import java.util.*;

class UserInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String password;
	String ip="";
	int port;
	Date lastLogin = null;
	HashMap<Integer, Integer> msgs= new HashMap<Integer, Integer>(); //[msgId, status:0read/1unread]	
	int update = 0; //0 = no update; 1 = update
	
	public UserInfo(String password,String ip, int port){
		this.password = password;
		this.ip = ip;
		this.port = port;
		this.update = 0;
	}
}
