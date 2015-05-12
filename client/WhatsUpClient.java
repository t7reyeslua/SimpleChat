package client;
import java.io.*;
import java.net.*;

/*
WhatsUp Client

argument 0 = IP address of Server
argument 1 = Port address of Server

*/

public class WhatsUpClient {
	public static String username="";
	public static String password="";
	
	public static void main(String[] args) {
		Socket yo = null;
		PrintWriter writer;
		final BufferedReader reader;  
		BufferedReader delTeclado;
		
		String srvip = args[0]; 
		String srvport = args[1];
		boolean continuar = false;
				
		
//Connect*************************************************************************************************************
			
		try{
			
			try{
				yo = new Socket(srvip,Integer.parseInt(srvport));
				continuar = true;
				
			}catch (UnknownHostException e){
				System.out.println(e.getMessage());
				System.exit(1);
			} 
			
//Authenticate w/credentials *******************************************************************************
			writer = new PrintWriter(yo.getOutputStream(), true);
	        reader = new BufferedReader(new InputStreamReader(yo.getInputStream()));
			delTeclado  = new BufferedReader(new InputStreamReader(System.in));			
			System.out.println("WhatsUP v1.0");
			System.out.println(reader.readLine()); //Greeting
			
			//Authentication
			System.out.println(reader.readLine());
			username = delTeclado.readLine(); 
			writer.println(username); //send to server
			System.out.println(reader.readLine());
			password = delTeclado.readLine();
			writer.println(password); //send to server
			
			System.out.println(reader.readLine()); //Last Login

//Rx *********************************************************************************************************		
			Thread update = new Thread(new Runnable()
	        {
	            public void run()
	            {
	            	while(true){
	            		try {
							System.out.println(reader.readLine());//msg from server	
						} catch (IOException e) {
							e.printStackTrace();
						} 	                 
	            	}//while
	            }//run
	        });
	        update.start();
//Tx **********************************************************************************************************
			do{	
				String request = delTeclado.readLine(); 
				writer.println(request); //send to server
				
				if (request.equals("&exit")){
					continuar = false;					
					String any = delTeclado.readLine(); 
					writer.println(any); //send to server
				}			
			}while (continuar);
//SALIDA*******************************************************************************************************
			delTeclado.close();
			writer.close();
			reader.close();
			yo.close();
			System.exit(1);
		} catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
