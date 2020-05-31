//Authors: Sowmya and Nishna
//This file contains Server code, which can handle requests from client and displays all the activities the client does.

//Importing the required libraries

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.io.PrintStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataInputStream;

public class Server {

    public static HashMap<String, Set<String>> clientsInRoom = new HashMap<>();
    public static HashMap<String, Set<String>> clientRooms = new HashMap<>();
    public static HashMap<String, clientThread> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {

        int port_num = 5000;
        ServerSocket server_sock = new ServerSocket(port_num);
        System.out.println("Server  runs on port " + port_num);
        System.out.println("waiting for connection");
        serverShutdown reader = new serverShutdown();
        reader.start();
        String message_from_client, message_to_client = "";
        while(true) {
            try{
                Socket socket = server_sock.accept(); //Establishing connection between Client and server.
                DataInputStream input_stream = new DataInputStream(socket.getInputStream());
                PrintStream output_stream = new PrintStream(socket.getOutputStream());
                message_from_client = input_stream.readLine();
                if(message_from_client!= null){
                    if(message_from_client.contains( "create")) {
                        String name = message_from_client.split(" ")[1];
                        clients.put(name, new clientThread(name, socket));
                        clients.get(name).start();
                        message_to_client = "Welcome " + name;
                        output_stream.println(message_to_client);
                    }
                }
                output_stream.flush();
            } catch(Exception e){
                break;
            }

        }
        server_sock.close();
    }

    //This function runs if the keyboard input is quit and closes down the connection
    public static class serverShutdown extends Thread{
        @Override
        public void run(){
            String in_operator;
            BufferedReader read_key = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                try{
                    in_operator = read_key.readLine();
                    if(in_operator.startsWith("quit")) {
                        for(String key : clients.keySet()){
                            clients.get(key).print_stream.println("Server is down, try later.");
                            clients.get(key).print_stream.println("quit");
                            clients.get(key).print_stream.close();
                            try {
                                clients.get(key).ip_stream.close();
                                clients.get(key).client_socket.close();
                            }
                            catch (Exception ex) {

                            }
                        }
                        System.exit(0);
                    }
                } catch(Exception e){

                }
            }
        }


    }

    //This function is for client after connection
    public static class clientThread extends Thread{
        PrintStream print_stream = null;
        String client_name;
        DataInputStream ip_stream = null;
        Socket client_socket = null;


        public clientThread(String name, Socket clientSocket) {
            this.client_socket = clientSocket;
            this.client_name = name;
        }

        @Override
        public void run() {
            try {
                ip_stream = new DataInputStream(client_socket.getInputStream());
                print_stream = new PrintStream(client_socket.getOutputStream());
                System.out.println(client_name + " connected");
                String name = ip_stream.readLine().trim();
                synchronized(this){
                    while (true) {
                        String inputCommand = ip_stream.readLine();
                        String[] lineArray = inputCommand.split(" ");
                        if(lineArray.length > 0){
                            String action = lineArray[0].toLowerCase();
                            switch (action) {
                                case "createroom":
                                    createRoom(inputCommand);
                                    break;
                                case "joinroom":
                                    join_room(inputCommand);
                                    break;
                                case "listrooms":
                                    list_rooms();
                                    break;
                                case "listmembers":
                                    list_members(inputCommand);
                                    break;
                                case "messageroom":
                                    group_message(inputCommand);
                                    break;
                                case "private":
                                    private_message(inputCommand);
                                    break;
                                case "leaveroom":
                                    leave_room(inputCommand);
                                    break;
                                default:
                                    if (inputCommand.startsWith("quit")) {
                                        client_thread_close_down();
                                        Thread.currentThread().stop();
                                    }
                                    invalid_statement();
                                    break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                client_thread_close_down();
            }
        }



        //This function contains logic for cliendThread close Down
        public void client_thread_close_down() {
            try {
                print_stream.println("See you later! " + client_name);
                System.out.println(client_name + " disconnected!");
                clientRooms.remove(client_name);
                for(String room: clientsInRoom.keySet()) {
                    if(clientsInRoom.get(room).contains(client_name)) {
                        clientsInRoom.get(room).remove(client_name);
                        for(String key: clientsInRoom.get(room))
                            clients.get(key).print_stream.println(client_name + " leaves " + room);
                    }
                }
                clients.remove(client_name);
                print_stream.close();
                ip_stream.close();
                client_socket.close();
            } catch (Exception ex) {

            }
        }

        public void invalid_statement() {
            print_stream.println("Invalid command..Please try with correct command");
        }
        // Command: createroom <ROOMNAME/>

        public void createRoom(String reader) {
            if (reader.contains(" ")) {
                String room = reader.split(" ")[1];

                if (!clientsInRoom.containsKey(room)) {
                    clientsInRoom.put(room, new HashSet<String>());
                    print_stream.println("Room " + room + " created");
                    System.out.println("Room " + room + " created by " + client_name);
                } else {
                    print_stream.println("Room " + room + " already exits");
                }

            }
            else{
                invalid_statement();
            }
        }


        // Command: joinroom <ROOMNAME/>
        public void join_room(String reader){
            if(reader.contains(" ")) {
                String room = reader.split(" ")[1];

                if (!clientsInRoom.containsKey(room)) {
                    print_stream.println("This Room " + room + " doesn't exist...");
                    return;
                } else if (!clientRooms.containsKey(client_name)) {
                    clientRooms.put(client_name, new HashSet<String>());
                } else if (clientsInRoom.get(room).contains(client_name) || clientRooms.get(client_name).contains(room)) {
                    print_stream.println("You  already exist in this room: " + room);
                    return;
                }
                clientRooms.get(client_name).add(room);
                clientsInRoom.get(room).add(client_name);
                print_stream.println("Joined to Room: " + room);
                for (String key : clientsInRoom.get(room)) {
                    if (!key.equals(this.client_name)) {
                        clients.get(key).print_stream.println(client_name + " joined the room " + room);
                    }
                }
                System.out.println(client_name + " joined room " + room);
            }
            else{
                invalid_statement();
            }
        }

        //command: listrooms
        public void list_rooms() {
            for(String reader: clientsInRoom.keySet()) print_stream.println(reader);
        }

        // Command: listmembers <ROOMNAME/>
        public void list_members(String reader) {
            if(reader.contains(" ")) {

                String room = reader.split(" ")[1];
                if (!clientsInRoom.containsKey(room)) {
                    print_stream.println("Room " + room + " doesn't exist!");
                    return;
                }


                for (String client : clientsInRoom.get(room)) print_stream.println(client);
            }
            else{
                invalid_statement();
            }
        }

        // Command: messageroom <ROOMNAME/> <MESSAGE/>
        public void group_message(String reader){

            if(reader.contains(" ")) {
                String room = reader.split(" ")[1];
                String message = this.client_name + ":" + reader.substring(reader.indexOf(room));
                if (!clientsInRoom.get(room).contains(this.client_name)) {
                    print_stream.println("You are not member of this room " + room);
                    return;
                }
                clientsInRoom.get(room).forEach((key) -> {
                    if (key.equals(this.client_name)) {
                        clients.get(key).print_stream.println("Message Sent");
                    }
                    clients.get(key).print_stream.println(message);
                });
                System.out.println(this.client_name + " sent a message");
            }
            else{
                invalid_statement();
            }
        }

        // Command: private <client_name/> <MESSAGE/>
        public void private_message(String reader){
            if(reader.contains(" ")) {
                String user = reader.split(" ")[1];
                String message = this.client_name + ":" + reader.substring(reader.indexOf(user) + user.length());

                if (clients.containsKey(user)) clients.get(user).print_stream.println(message);
                else {
                    print_stream.println("Client " + user + " doesn't exist ");
                    return;
                }
                clients.get(client_name).print_stream.println("Message Sent");
                System.out.println(client_name + " sents private message to " + user);
            }
            else{
                invalid_statement();
            }
        }


        // Command: leaveroom <ROOMNAME/>
        public void leave_room(String reader) {
            if(reader.contains(" ")) {
                String room = reader.split(" ")[1];
                if (!clientsInRoom.containsKey(room)) {
                    print_stream.println("Room " + room + " doesn't exist..");
                    return;
                }

                if (clientRooms.containsKey(client_name) && clientRooms.get(client_name).contains(room)) {
                    clientRooms.get(client_name).remove(room);
                    clientsInRoom.get(room).remove(client_name);
                    print_stream.println("You left room " + room);
                    for (String key : clientsInRoom.get(room)) {
                        clients.get(key).print_stream.println(client_name + " left the room " + room);
                    }
                    System.out.println(client_name + " left the room " + room);
                    return;
                }
                print_stream.println("You are not member of room " + room);
            }
            else{
                invalid_statement();
            }
        }


    }
}