import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;

public class Server {


    public static HashMap<String, clientThread> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        int portNum = 8000;
        Socket connection;
        String messageToServer;

        ServerSocket serversock = new ServerSocket(portNum); //creating server soclet
        System.out.println("Server runs on port " + portNum);
        System.out.println("Waiting for connection");
        connection = serversock.accept();
        shutdownServer s = new shutdownServer();
        s.start();

        while(true){
            DataInputStream input = new DataInputStream(connection.getInputStream());
            PrintStream printMessage = new PrintStream(connection.getOutputStream());
            messageToServer = input.readLine();
            if (messageToServer!= null && messageToServer.contains("add")){
                String clientName = messageToServer.split("")[1];
                clients.put(clientName, new clientThread(clientName, connection));
                clients.get(clientName).start();
                String printInClient = clientName + "Started";
                printMessage.println(printInClient);
            }
            printMessage.flush();

        }


    }
    public static class shutdownServer  extends Thread{
        public void run() {
            BufferedReader inputRead = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    if (inputRead.readLine().contains("quit")) {
                        for (String key : clients.keySet()) {
                            clients.get(key).opstream.println("Server has shutdown. Try later");
                            clients.get(key).opstream.close();
                            try {
                                clients.get(key).ipstream.close();
                                clients.get(key).clientSock.close();
                            } catch (Exception ex) {

                            }
                        }
                        System.exit(0);
                    }
                } catch (Exception e) {

                }
            }
        }
    }
    public static class clientThread extends Thread {
        String clientName;
        Socket clientSock = null;
        PrintStream opstream = null;
        DataInputStream ipstream = null;

        public clientThread(String name, Socket clientSocket) {
            this.clientSock = clientSocket;
            this.clientName = name;
        }
        public void run() {

                System.out.println(clientName + " connected");
        }
    }

}