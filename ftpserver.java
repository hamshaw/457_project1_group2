import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

public class ftpserver extends Thread {
    private Socket connectionSocket;
    int port;
    int count = 1;

    public ftpserver(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public void run() {
        if (count == 1)
            System.out.println("User connected" + connectionSocket.getInetAddress());
        count++;

        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        String fromClient;
        String clientCommand;
        byte[] data;
        String firstLine;

        while (true) {
            if (count == 1)
                System.out.println("User connected" + connectionSocket.getInetAddress());
            count++;

            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            fromClient = inFromClient.readLine();

            StringTokenizer tokens = new StringTokenizer(fromClient);

            firstLine = tokens.nextToken();
            try {
                port = Integer.parseInt(firstLine);
            } catch (Exception e) {
                System.out.println(e);
            }
            clientCommand = (String)tokens.nextToken();
            String givenFilename = null;
            try {
            	givenFilename = tokens.nextToken();
            } catch (Exception e) {
		//System.out.println(e);
	    }

            if(clientCommand.equals("list:"))
                  {
                      String curDir = System.getProperty("user.dir");
       
                      Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                      DataOutputStream  dataOutToClient = 
                      new DataOutputStream(dataSocket.getOutputStream());
                      File dir = new File(curDir);
    
                      String[] children = dir.list();
                      if (children == null) {
                          throw new Exception("directory not found"); // Either dir does not exist or is not a directory
                      }
                      else {
                          for (int i=0; i<children.length; i++){
                              // Get filename of file or directory
                              String filename = children[i];

                              if(filename.endsWith(".txt"))
                                dataOutToClient.writeUTF(children[i]);
                             //System.out.println(filename);
                             if(i-1==children.length-2){
                                 dataOutToClient.writeUTF("eof");
                                 // System.out.println("eof");
                             }//if(i-1)
                          }//for

                           dataSocket.close();
		                    //System.out.println("Data Socket closed");
                     }//else
        

                }//if list:

            if (clientCommand.equals("get:")) {
                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                
		String curDir = System.getProperty("user.dir");
		File dir = new File(curDir);

                String[] children = dir.list();
				
                if (children == null) {
                    throw new Exception("file name not found");
                } else {
                    for (int i = 0; i < children.length; i++) {
                        String filename = children[i];
                        if (filename.equals(givenFilename)) {
                            File fileToSend = new File(curDir, filename);
		            FileInputStream fileInputStream = new FileInputStream(fileToSend);
		            byte[] buffer = new byte[1024];
		            int bytesRead;
	                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
		                dataOutToClient.write(buffer, 0, bytesRead);
		            }
		            fileInputStream.close();
		            dataOutToClient.flush();
		            dataOutToClient.close();
		            break;
                        }
                    }
                }
                dataSocket.close();
            }

            if (clientCommand.equals("stor:")) {
                //port += 2
                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                
                //Socket dataSocket = incomingData.accept();
                DataOutputStream outdata = new DataOutputStream(dataSocket.getOutputStream());
                DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                File file = new File(givenFilename);
                FileOutputStream fileOut = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inData.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                }
                fileOut.close();
                inData.close();
                outdata.close();
                dataSocket.close();
            }

            if (clientCommand.equals("close")) {
                connectionSocket.close();
            }
        }
    }
	
    public static void main(String[] args) {
        int portNumber = 1200;
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("FTP Server started on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ftpserver(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}

