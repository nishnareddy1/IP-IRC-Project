import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;


public class Client {

    public static void main(String[] args) throws Exception {
        int portNum = 8000;
        sock = new Socket("localhost", portNumber);
        System.out.println("Client is connected to server");

    }
}

