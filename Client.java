//Authors: Sowmya and Nishna
//Importing the required libraries


import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class Client{


    static Socket socket = null;
    static InputStream ip_stream = null;//accepts input bytes and receives them
    static PrintStream print_stream = null;//adds functionality to output stream
    static OutputStream output_stream = null;//accepts output bytes and sends them
    static DataInputStream input_from_server = null;
    static BufferedReader input_from_client = null;
    static String clientName = "client" + (int)(Math.random() * 50);//Random function to generate client names

    public static void main(String[] args) throws Exception{
        int portNumber = 5000;

        socket = new Socket("localhost", portNumber);

        output_stream = socket.getOutputStream();
        print_stream = new PrintStream(output_stream);
        print_stream.println("create " + clientName);
        print_stream.flush();
        print_stream.println();

        ip_stream = socket.getInputStream();
        input_from_server = new DataInputStream(ip_stream);
        input_from_client = new BufferedReader(new InputStreamReader(System.in));

        ClientRead input = new ClientRead();
        input.start();

        HearMessage msg_print = new HearMessage();
        msg_print.start();


    }
    //Reads the inputs from client and sends them to server
    static class ClientRead extends Thread{
        @Override
        public void run(){
            String message_to_server;
            while(true){
                try{
                    message_to_server = input_from_client.readLine();
                    print_stream.println(message_to_server);
                    if(message_to_server.equals("quit")) System.exit(0); //exit(0) is used to indicate successful termination
                    print_stream.flush();
                } catch(Exception e){
                    break;
                }
            }
        }
    }

    //Recieves the keyboard input from client
    static class HearMessage extends Thread{
        @Override
        public void run() {
            String message_from_server;
            while(true) {
                try{
                    message_from_server = input_from_server.readLine();
                    if(message_from_server.startsWith("quit")) System.exit(0);
                    System.out.println(message_from_server);
                } catch(Exception e) {
                    System.err.println("Server unexpectedly stopped working, Exiting to handle server crash gracefully");
                    System.exit(0);
                }
            }
        }
    }
}