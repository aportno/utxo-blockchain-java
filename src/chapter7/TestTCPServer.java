package chapter7;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.Scanner;

public class TestTCPServer {
    public static final int port = 8888;

    public static void main(String[] args) throws IOException {
        System.out.println("Enter username:");

        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Server is listening at port=" + port);
        Socket socket = serverSocket.accept();
        InetAddress clientAddress = socket.getInetAddress();

        System.out.println(userInput + " is from " + clientAddress.getHostName());
        System.out.println("Connected, start chat!");

        MessageManagerTCP messageManagerTCP = new MessageManagerTCP(socket, userInput);
        messageManagerTCP.start();
        System.out.println("Message manager started");

        CommunicationChannel communicationChannel = new CommunicationChannel(messageManagerTCP);
        communicationChannel.start();

        System.out.println("Communication channel is ready");
        serverSocket.close();
    }
}
