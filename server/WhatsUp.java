package server;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class WhatsUp {
	public static void main(String[] args) {
		//VARIABLES*************************************************************************************************************
		
				ServerSocket me = null;
				Socket client = null;
				boolean listening = true;
				final HashMap<String, List<String>> tbl_groups= new HashMap<String, List<String>>();
				final HashMap<Integer, List<String>> tbl_msgs= new HashMap<Integer, List<String>>();
				final HashMap<String, UserInfo> tbl_users= new HashMap<String, UserInfo>();
				final List<Atiende> agents = new ArrayList<Atiende>();
				int n = 0;

		//SEND UPDATES***********************************************************************************************************
				
				Thread update = new Thread(new Runnable()
		        {
		            public void run()
		            {
		            	while(true){
			            	synchronized (agents) {
			                    for (Atiende agent : agents) {
			                       //send Updates
			                    	if(agent.username != null && agent.continuar){
			                    		if(tbl_users.get(agent.username) != null ){
			                    			if(tbl_users.get(agent.username).update == 1){
					                    		try {
												  agent.showMsgs(1);
												} catch (ClassNotFoundException	| IOException e) {
													e.printStackTrace();
												}
			                    			}
				                    	}
			                    	}
			                    }//for
			                 }//synchronized
		            	}//while
		            }//run
		        });
		        update.start();
		        
		//LISTEN FOR CONNECTIONS************************************************************************************************	     
				try{
				    me = new ServerSocket(5000);
				    System.out.println("WhatsUp SERVER*****************************************************************");
					System.out.println("Socket listening on port:5000");
				    while(listening){
						client = me.accept(); //listen until someone tries to connect
						Atiende agent  = new Atiende(client, tbl_groups, tbl_msgs, tbl_users);
						System.out.println("Client "+ n++ +" connected from: " + 
											client.getInetAddress().getHostName() + 
											" @"+ client.getInetAddress().toString().split("/")[1] +
											":"+ client.getPort());
						agent.start(); //create new Thread that attends this client
						synchronized (agents) {
					        agents.add(agent);
					     }
						
					}//while
				    me.close();
				} catch (IOException e){
					System.err.println(e.getMessage());
					System.exit(1);
				}//catch
		//**********************************************************************************************************************
	}
}
