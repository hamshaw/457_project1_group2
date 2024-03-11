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
            port = Integer.parseInt(firstLine);
            clientCommand = tokens.nextToken();
            try {
                String givenFilename = tokens.nextToken();
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
                          dataOutToClient.writeUTF("directory not found")// Either dir does not exist or is not a directory
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
                File dir = new File(curDir);

                String[] children = dir.list();
                if (children == null) {
                    dataOutToClient.writeUTF("file name not found");
                } else {
                    int found = 0;
                    for (int i = 0; i < children.length; i++) {
                        String filename = children[i];
                        if (filename.equals(givenFilename)) {
                            found = 1;
                            outToClient.writeBytes(children[i].read());
                        }
                        if (found == 0) {
                            outToClient.writeUTF("file name not found");
                        }
                    }
                }
                dataSocket.close();
            }

            if (clientCommand.equals("stor:")) {
                ServerSocket incomingData = new ServerSocket(port);
                Socket dataSocket = incomingData.accept();
                DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                File file = new File(givenFilename);
                FileOutputStream fileOut = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inData.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                }
                fileOut.close();
                incomingData.close();
                dataSocket.close();
            }

            if (clientCommand.equals("close")) {
                connectionSocket.close();
            }
        }
    }
}

