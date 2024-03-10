import java.io.*; 
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

    public class ftpserver extends Thread{ 
      private Socket connectionSocket;
      int port;
      int count=1;
    public ftpserver(Socket connectionSocket)  {
	this.connectionSocket = connectionSocket;
    }


      public void run() 
        {
                if(count==1)
                    System.out.println("User connected" + connectionSocket.getInetAddress());
                count++;

	try {
		processRequest();
		
	} catch (Exception e) {
		System.out.println(e);
	}
	 
	}
	
	
	private void processRequest() throws Exception
	{
            String fromClient;
            String clientCommand;
            byte[] data;
            String frstln;
                    
            while(true)
            {
                if(count==1)
                    System.out.println("User connected" + connectionSocket.getInetAddress());
                count++;
         
                DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                fromClient = inFromClient.readLine();
            
      		//System.out.println(fromClient);
                  StringTokenizer tokens = new StringTokenizer(fromClient);
            
                  frstln = tokens.nextToken();
                  port = Integer.parseInt(frstln);
                  clientCommand = tokens.nextToken();
                  try{
                      String givenfilename = tokens.nextToken();
                  }
                  catch{
                  }
                  //System.out.println(clientCommand);


                  if(clientCommand.equals("list:"))
                  { 
                      String curDir = System.getProperty("user.dir");
       
                      Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                      DataOutputStream  dataOutToClient = 
                      new DataOutputStream(dataSocket.getOutputStream());
                      File dir = new File(curDir);
    
                      String[] children = dir.list();
                      if (children == null) 
                      {
                          dataOutToClient.writeUTF("directory not available");
                      } 
                      else 
                      {
                          for (int i=0; i<children.length; i++)
                          {
                              // Get filename of file or directory
                              String filename = children[i];

                              if(filename.endsWith(".txt"))
                                dataOutToClient.writeUTF(children[i]+ '\n');
                             //System.out.println(filename);
                             if(i-1==children.length-2)
                             {
                                 dataOutToClient.writeUTF("eof");
                                 // System.out.println("eof");
                             }//if(i-1)

     
                          }//for

                           dataSocket.close();
		          //System.out.println("Data Socket closed");
                     }//else
        

                }//if list:


                if(clientCommand.equals("get:"))
                {
					Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
					DataOutputStream  dataOutToClient =
					new DataOutputStream(dataSocket.getOutputStream());
					File dir = new File(curDir);

					String[] children = dir.list()
					if(children == null){
						data.OutToClient.writeUTF("file name not found");
					}
					else{
                        int found = 0;
						for(int i=0; i<children.length; i++){
							String filename = children[i];
							if(filename == givenfilename){
								found = 1;
                                OutToClient.writeBytes(children[i].read());
							}
							if(found==0){
						        OutToClient.writeUTF("file name not found");
							}
						}
                    }
                    dataSocket.close();
				}


                if(clientCommand.equals("stor:")){
                    ClientSocket incomingData = new ClientSocket(port);
                    Socket dataSocket = incomingData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputeStream(dataSocket.getInputStream()));
                    File file = new File(givenfilename);
                    FileOutputStream fileOut = new FIleOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while((bytesRead = inData.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                    }
                    fileOut.close();
                    incomingData.close();
                    dataSocket.close();
                }


                if(clientCommand.equals("close")){
                    connectionSocket.close()
                }


            }//main
        }
}
	

