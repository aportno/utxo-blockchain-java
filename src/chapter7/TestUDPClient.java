package chapter7;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.util.Scanner;
import java.io.IOException;

public class TestUDPClient {
    private final int serverPort;
    private final InetAddress serverAddress;
    private boolean isServerRunning = true;
    private final Scanner userInput;
    private final DatagramSocket clientSocket;

    public TestUDPClient(int serverPort, String serverAddress) {
        this.serverPort = serverPort;

        userInput = new Scanner(System.in);
        try {
            this.serverAddress = InetAddress.getByName(serverAddress);
            clientSocket = new DatagramSocket();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() throws IOException {
        while (isServerRunning) {
            System.out.println("Please enter your response: ");
            String msg = userInput.nextLine();

            // generate a sending datagram
            DatagramPacket datagramPacket = new DatagramPacket(msg.getBytes(), msg.length(), this.serverAddress, serverPort);

            // send the datagram
            this.clientSocket.send(datagramPacket);
            if (msg.startsWith("END")) {
                isServerRunning = false;
                continue;
            }

            // wait for a message
            byte[] data = new byte[2048];
            DatagramPacket datagramPacket1 = new DatagramPacket(data, data.length);
            this.clientSocket.receive(datagramPacket1);

            System.out.println("Client received a message from the server:");
            System.out.println("\tServer address: " + datagramPacket1.getAddress().getHostName());
            System.out.println("\tPort=" + datagramPacket1.getPort());

            String trimDp = (new String(datagramPacket1.getData())).trim();
            if (trimDp.startsWith("END")) {
                System.out.println("==========Ending now===========");
                isServerRunning = false;
                userInput.close();
            } else {
                System.out.println("Server: " + trimDp);
            }
            this.clientSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Please enter server IP address:");
        Scanner userInput = new Scanner(System.in);
        String ipAddress = userInput.nextLine();

        if (ipAddress.trim().length() < 5) {
            ipAddress = "localhost";
        }

        System.out.println("IP: " + ipAddress);
        TestUDPClient client = new TestUDPClient(8888, ipAddress);
        System.out.println("UDP client starting now, server is listening at port 8888");
        client.start();
        userInput.close();
        System.out.println("==========Client terminated==========");
    }
}
