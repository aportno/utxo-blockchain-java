package chapter7;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class PeerTCP {
    public static final int port = 8888;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("What is the users name?");
        String peersName = scanner.nextLine();

        System.out.println("What is your username?");
        String usersName = scanner.nextLine().trim();

        PeerTCPOutgoingMessageManager peerTCPOutgoingMessageManager = new PeerTCPOutgoingMessageManager(peersName, usersName);
        peerTCPOutgoingMessageManager.start();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening...");

        Socket socket = serverSocket.accept();
        InetAddress clientIPAddress = socket.getInetAddress();

        System.out.println(peersName + " is from " + clientIPAddress.getHostAddress());
        System.out.println("Incoming connect established, receiving messages now");

        PeerTCPIncomingMessageManager peerTCPIncomingMessageManager = new PeerTCPIncomingMessageManager(socket, peersName);
        peerTCPIncomingMessageManager.start();
        serverSocket.close();
    }
}

class PeerTCPIncomingMessageManager extends Thread {
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private boolean isServerRunning = true;
    private final String peersName;

    public PeerTCPIncomingMessageManager(Socket socket, String peersName) throws IOException {
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.peersName = peersName;
    }

    public void run() {
        System.out.println("PeerIncomingMessageManager is up...");
        while (isServerRunning) {
            try {
                String msg = (String)(this.objectInputStream.readObject());
                System.out.println(this.peersName + ": " + msg);
                if (msg.trim().startsWith("END")) {
                    isServerRunning = false;
                }
            } catch (Exception e) {
                System.out.println("Error: this is only for text message");
                isServerRunning = false;
            }
        }
        System.out.println("PeerIncomingMessageManager retired");
        try {
            this.objectOutputStream.close();
            this.objectInputStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.exit(1);
    }
}

class PeerTCPOutgoingMessageManager extends Thread {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private boolean isServingRunning = true;
    private final String peersName;
    private final String userName;
    private Scanner scanner;

    public PeerTCPOutgoingMessageManager(String peersName, String userName) {
        this.peersName = peersName;
        this.userName = userName;
    }

    @SuppressWarnings({"resource", "BusyWait"})
    public void run() {
        System.out.println("Outgoing connect is not established. Please enter the peers IP address");
        String peerIPAddress = scanner.nextLine();
        try {
            Socket socket = new Socket(peerIPAddress, PeerTCP.port);
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        System.out.println("Outgoing connect established. Trying sending " + peersName + " a message");
        while (isServingRunning) {
            try {
                Thread.sleep(500);
                try {
                    String msg = scanner.nextLine();
                    System.out.println(userName + ":" + msg);
                    this.objectOutputStream.writeObject(msg);
                    if (msg.trim().startsWith("END")) {
                        isServingRunning = false;
                    }
                } catch (Exception e) {
                    isServingRunning = false;
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        try {
            this.objectOutputStream.close();
            this.objectInputStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println("PeerIncomingMessageManager retired");
        System.exit(1);
    }
}