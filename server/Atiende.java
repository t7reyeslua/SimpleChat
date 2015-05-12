package server;
import java.util.List;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

//THREAD ATENDER********************************************************************************************************

class Atiende extends Thread{

//VARIABLES THREAD*****************************************************************************************************
  public PrintWriter writer;
  public BufferedReader reader;  
  public Socket cliente = null;
  boolean continuar = false;
  boolean login = false;
  String username = null;
  String password = null;
  HashMap<String, List<String>> tgroups= null;
  HashMap<Integer, List<String>> tmsgs= null;
  HashMap<String, UserInfo> tusers= null;
  UserInfo userInfo = null;
  SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

//CONSTRUCTOR**********************************************************************************************************
  public Atiende(Socket cliente, HashMap<String, List<String>> tgroups, HashMap<Integer, List<String>> tmsgs, HashMap<String, UserInfo> tusers){
    this.cliente = cliente;
	this.tgroups=tgroups;
	this.tmsgs=tmsgs;
	this.tusers=tusers;
  }
  
//RUN THREAD***********************************************************************************************************
	public void run(){
	    try{	
	    	writer = new PrintWriter(cliente.getOutputStream(), true);
	        reader = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
	        
  	       //Greet client
	        writer.println("********************************WhatsUP********************************");
			
			//Ask & verify credentials
			userInfo = askCredentials();
			login = verifyCredentials(userInfo);
						
			if (login){
				String srv_msg = checkLastLogin();
				writer.println(srv_msg); 
				showMsgs(0);
				continuar = true;
			}
			
			//Attend Client while it exists		
			while (login && continuar){ 
									
				String request = reader.readLine(); //Ask for Client request
				String srv_msg = "";
				if (request != null){
					srv_msg = attendRequests(request);//Analyze Client request
				}else{
					srv_msg = "exit";
				}
								
				if (srv_msg.equals("exit")){
					continuar = false;					
					srv_msg = "Goodbye! Press any key to exit";
					writer.println(srv_msg);
					reader.readLine();
				}else{
					writer.println(srv_msg); 
				}
			}	//while			
			writer.close();
			reader.close();
      }catch(IOException e){
          System.out.println(e.getMessage());
          System.exit(1);
      }catch (ClassNotFoundException ex){ }
	  finally {
	    //Client disconnected 	 
		  
	  	  if (login && tusers.containsKey(username)){ 
	  		  System.out.println(username + " disconnected");
	  		  Date dNow = new Date( );
	  		  tusers.get(username).lastLogin = dNow;
	  		  login = false;
	  	  }
	   }
	}//run
//*******************************************************************************************************************************

	
	public UserInfo askCredentials()throws IOException,ClassNotFoundException{
		UserInfo userInfo = null;
		writer.println("Username:");
		username = reader.readLine(); //response from Client
		writer.println("Password:"); 
		password = reader.readLine(); //response from Client
				
		String ip   = cliente.getInetAddress().toString().split("/")[1];
		int port = cliente.getPort();
		userInfo = new UserInfo(password, ip, port);		
		return userInfo;		
	} //askCredentials

	public boolean verifyCredentials(UserInfo userInfo)throws IOException,ClassNotFoundException{
		boolean login = false;
		
		//Verify credentials		
		if (tusers.containsKey(username)){ //client exists
			if (tusers.get(username).password.equals(userInfo.password)){//check if password given matches with stored one
				//Update userInfo
				tusers.get(username).ip = userInfo.ip;
				tusers.get(username).port = userInfo.port;
				userInfo.msgs = tusers.get(username).msgs;
				login = true;
			}else{ 
				writer.println("Sorry, login failed! Wrong username/password. Goodbye!");
				writer.println("Press any key to exit");
				reader.readLine();
			}
		}else{ 
			 //Add new user to library
			tusers.put(username, userInfo);
			login = true;
		}
		
		return login;		
	} //verifyCredentials

	public String checkLastLogin(){
		String srvmsg = null;
		Date dLastLogin =	tusers.get(username).lastLogin;	
		if (dLastLogin != null){//check if it has connected before
				srvmsg = "Welcome back, " + username + "! Last login @"+ ft.format(dLastLogin);	;
		}else{ 
				srvmsg = "Welcome, " + username + "! This is the first time you connect!";				
		}
		return srvmsg;
	}
	
	public void showMsgs(int mode)throws IOException,ClassNotFoundException{
		Iterator<Entry<Integer, Integer>> it = tusers.get(username).msgs.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, Integer> pairs = (Map.Entry<Integer, Integer>)it.next();
	        Integer id= pairs.getKey();
	        Integer status = pairs.getValue();
	        if  ((mode==0) || (mode == 1 && status == 1)){ //print msg only if it is new or all if it is login
		        String tmp = null;
		        if (status == 0){
		        	tmp = "[Old msg]";
		        }else{
		        	tmp = "[New msg]";
		        	//Mask as read
		        	tusers.get(username).msgs.put(id, 0);
		        }
		        String date = "[Date: " + (String) tmsgs.get(id).get(0) +"]";
		        String sender= "[From: " +(String) tmsgs.get(id).get(1) +"] ";
		        String msg= (String) tmsgs.get(id).get(2);		         
		        writer.println(tmp + date + sender + msg); 
	        }
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
	}

	public String attendRequests(String request){
		String srv_msg = null;
		String msg = null;
		String group = null;
		String buddy = null;
		String reg_groupJoin = "^#\\w[\\w!*^'&%$_-]:(join)$";
		String reg_groupLeave ="^#\\w[\\w!*^'&%$_-]:(leave)$";
		String reg_groupMsg =  "^#\\w[\\w!*^'&%$_-] [\\w!* ^&%$@#'\"_?():;-]+$";
		String reg_msg =       "^@\\w[\\w!*^'&%$_-] [\\w!* ^&%$@#'\"_?():;-]+$";
		String reg_exit =      "^&exit$";
		
		if (request.matches(reg_msg)){//regular message @
			request = request.replaceFirst("@", "");
			buddy   = request.split(" ", 2)[0];
			msg     = request.split(" ", 2)[1];
			if (tusers.containsKey(buddy)){ //Buddy exists
				Date dNow = new Date();
				List<String> msgInfo = Arrays.asList(ft.format(dNow), username, msg);
				int n = tmsgs.size()+1;
				tmsgs.put(n,msgInfo);				//add msg to GlobalMsgList
				tusers.get(buddy).msgs.put(n, 1);   //update buddyMsgQueue
				tusers.get(buddy).update = 1; 		//indicate buddy Update needed	
				srv_msg = "Message sent to "+ buddy;
			}else{
				srv_msg = "Sorry, that contact does not exists!";
			}					
		}else if (request.matches(reg_groupMsg)){//group message #
			request = request.replaceFirst("#", "");
			group   = request.split(" ", 2)[0];
			msg     = request.split(" ", 2)[1];
			if (tgroups.containsKey(group)){ //Group exists
				if (tgroups.get(group).contains(username)){//client is memeber
					Date dNow = new Date();
					List<String> msgInfo = Arrays.asList(ft.format(dNow), username + "@" + group, msg);
					int n = tmsgs.size()+1;
					tmsgs.put(n,msgInfo);				//add msg to GlobalMsgList
					
					for (String member: tgroups.get(group)){
						//Update msg Queue of all members but himself
						if(!member.equals(username)){
							tusers.get(member).msgs.put(n, 1);   //update buddyMsgQueue
							tusers.get(member).update = 1; 		//indicate buddy Update needed
						}
					}
					srv_msg = "Messege sent to "+ group;
				}else{
					srv_msg = "Sorry, only members of "+ group +" can post messages!";						
				}						
			}else{//Group doesn't exists
				srv_msg = "Sorry, group "+ group +" doesn't exists!";
			}
		}else if (request.matches(reg_groupJoin)){//group join :join
			request = request.replaceFirst("#", "");
			group   = request.split(":", 2)[0];
			if (tgroups.containsKey(group)){ //Group exists
				if (tgroups.get(group).contains(username)){//client is memeber
					srv_msg = "You already are a member of group "+ group + "!";
				}else{
					tgroups.get(group).add(username);
					srv_msg ="Joined group: "+ group;							
				}						
			}else{//Group doesn't exists, then create it with the only user
				srv_msg = "[New group] You just joined group "+ group +"!";
				List<String> groupMembers = new LinkedList<String>(Arrays.asList(username));
				tgroups.put(group, groupMembers);
			}						
		}else if (request.matches(reg_groupLeave)){//group leave :leave
			request = request.replaceFirst("#", "");
			group   = request.split(":", 2)[0];
			if (tgroups.containsKey(group)){ //Group exists
				if (tgroups.get(group).contains(username)){//client is memeber
					int index = tgroups.get(group).indexOf(username);
					tgroups.get(group).remove(index);
					srv_msg = "Left group: "+ group + "!";
				}else{
					srv_msg ="You are not a member of group "+ group + "!";							
				}						
			}else{//Group doesn't exists
				srv_msg = "Group "+ group +"doesn't exists!";
			}
		}else if (request.matches(reg_exit)){
			srv_msg = "exit";
		}else{//bad requests
			srv_msg = "Sorry, wrong message format!";
		}
		return srv_msg;	
	}
}//class ATIENDE
