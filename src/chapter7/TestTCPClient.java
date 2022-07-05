package chapter7;

import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;

public class TestTCPClient {
    private final static int port = 8888;
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter the serve's IP address");
        String ipAddress = scanner.nextLine();
        Socket socket = new Socket(ipAddress, port);

        System.out.println("What is the users name?");
        String userName = scanner.nextLine();

        System.out.println("Connected, ready to go!");
        MessageManagerTCP messageManagerTCP = new MessageManagerTCP(socket, userName);
        messageManagerTCP.start();
        System.out.println("Manager started");

        CommunicationChannel communicationChannel = new CommunicationChannel(messageManagerTCP);
        communicationChannel.start();
        System.out.println("Channel is ready");
    }
}
