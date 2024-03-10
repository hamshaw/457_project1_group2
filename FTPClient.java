import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

class FTPClient {

    public static void main(String argv[]) throws Exception {
        String sentence;
        String modifiedSentence;
        boolean isOpen = true;
        int number = 1;
        boolean notEnd = true;
        int port1 = 1221;
        int port = 1200;
        String statusCode;
        boolean clientgo = true;

        System.out.println("Welcome to the simple FTP App   \n     Commands  \nconnect servername port# connects to a specified server \nlist: lists files on server \nget: fileName.txt downloads that text file to your current directory \nstor: fileName.txt Stores the file on the server \nclose terminates the connection to the server");
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);

        if (sentence.startsWith("connect")) {
            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            port1 = Integer.parseInt(tokens.nextToken());
            System.out.println("You are connected to " + serverName);
            Socket ControlSocket = new Socket(serverName, port1);
            while (isOpen && clientgo) {

                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
                sentence = inFromUser.readLine();

                if (sentence.equals("list:")) {

                    port = port + 2;
                    System.out.println(port);
                    ServerSocket welcomeData = new ServerSocket(port);

                    System.out.println("\n \n \nThe files on this server are:");
                    outToServer.writeBytes (port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while (notEnd) {
                        modifiedSentence = inData.readUTF();
                        if (modifiedSentence.equals("eof"))
                            break;
                       System.out.println("     " + modifiedSentence);
                    }

                 welcomeData.close();
                 dataSocket.close();
                 System.out.println("\nWhat would you like to do next: \nget: file.txt ||  stor: file.txt  || close");

                } else if (sentence.startsWith("list:")) {
                    port += 2;
                    ServerSocket incomingData = new ServerSocket(port);
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');
                    Socket dataSocket = incomingData.accept();
                    BufferedInputStream buffIn = new BufferedInputStream(dataSocket.getInputStream());
                    DataInputStream inData = new DataInputStream(buffIn);
                    System.out.println("The files on this server are: \n");
                    byte[] b = new byte[buffIn.available()];
                    int bytes = inData.read(b);
                    for (byte by : b) {
                        System.out.println((char) by);
                    }
                } else if (sentence.startsWith("get: ")) {
                    String filename = sentence.substring(6);
                    port += 2;
                    ServerSocket incomingData = new ServerSocket(port);
                    outToServer.writeBytes(port + " " + sentence + " "+ '\n');
                    Socket dataSocket = incomingData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    File file = new File(filename);
                    FileOutputStream fileOut = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inData.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                    }
                    fileOut.close();
                    incomingData.close();
                    dataSocket.close();
                    System.out.println("File " + filename + " downloaded.");
                } else if (sentence.startsWith("stor: ")) {
                    String filename = sentence.substring(7);
                    ServerSocket incomingData = new ServerSocket(port);
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');
                    Socket dataSocket = incomingData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    File dir = new File(System.getProperty("user.dir"));
                    String[] children = dir.list();
                    if(children == null){
                        System.out.print("cannot find "+filename+" file");
                    }
                    else{
                        int found = 0;
                        for(int i = 0; i<children.length; i++){
                            if (filename.equals(children[i])){
                                found = 1;
                                outToServer.writeBytes(children[i].read());
                            }
                        }
                        if(found==0){
                            System.out.print("cannot find "+filename+" file");
                        }
                    }
                    dataSocket.close();
                } else {
                    if (sentence.equals("close")) {
                            outToServer.writeBytes(sentence);
                    clientgo = false;
                    }
                    System.out.print("No server exists with that name or server not listening on that port try again");
                }
            }
        }
    }
}

