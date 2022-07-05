package chapter7;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import java.io.IOException;

public class TestUDPServer {
    private final int serverPort;
    private final DatagramSocket serverUDPSocket;
    private boolean isRunServer = true;
    private final Scanner userInput;

    public TestUDPServer(int serverPort) {
        this.serverPort = serverPort;
        this.userInput = new Scanner(System.in);
        try {
            this.serverUDPSocket = new DatagramSocket(this.serverPort);
        } catch (SocketException e){
            throw new RuntimeException(e);
        }
    }

    public void start() {
        System.out.println("UDP server starting at port " + this.serverPort);
        while (isRunServer) {
            byte[] buf = new byte[2048];
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);

            try {
                System.out.println("Server listening for incoming messages...");
                serverUDPSocket.receive(datagramPacket);
                System.out.println("Client:\nport=" + datagramPacket.getPort() + "\nIP=" + datagramPacket.getAddress().getHostAddress());

                String trimDp = (new String(datagramPacket.getData())).trim();
                if (trimDp.startsWith("END")) {
                    System.out.println("Ending now...");
                    isRunServer = false;
                    userInput.close();
                    continue;
                } else {
                    System.out.println("Client:\n " + trimDp);
                }

                System.out.println("Please enter your response: ");
                String userResponse = userInput.nextLine();
                DatagramPacket datagramPacket1 = new DatagramPacket(userResponse.getBytes(), userResponse.getBytes().length,
                        datagramPacket.getAddress(), datagramPacket.getPort());

                serverUDPSocket.send(datagramPacket1);

                if (userResponse.startsWith("END")) {
                    isRunServer = false;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.serverUDPSocket.close();
    }

    public static void main(String[] args) {
        TestUDPServer server = new TestUDPServer(8888);
        server.start();
        System.out.println("========== Server Closed ==========");
    }
}
