package chapter7;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestTCPServerCenter {
    public static final int port = 8888;
    private static boolean isServerRunning = true;
    private static final ConcurrentLinkedQueue<SimpleTextMessage> messageQueue = new ConcurrentLinkedQueue<>();
    private static final Hashtable<String, UserChannelInfo> users = new Hashtable<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(port);
        ServerMessageManager serverMessageManager = new ServerMessageManager(messageQueue, users);
        Thread thread = new Thread(serverMessageManager);
        thread.start();
        serverSocket.setSoTimeout(3600000);
        while (isServerRunning) {
            try {
                System.out.println("Server is listening now");
                Socket socket = serverSocket.accept();

                System.out.println("Received connection...identifying name");
                UserChannelInfo userChannelInfo = new UserChannelInfo(socket, messageQueue);

                System.out.println("User is: " + userChannelInfo.getUserName());
                users.put(userChannelInfo.getUserName(), userChannelInfo);

                Thread thread1 = new Thread(userChannelInfo);
                thread1.start();
            } catch (java.net.SocketTimeoutException ste) {
                System.out.println("Server time out. Exiting...");
                isServerRunning = false;
            }
        }
        serverSocket.close();
        System.out.println("Server closed");
        System.exit(0);
    }

    public static synchronized UserChannelInfo removeUserChannel(String userName) {
        return users.remove(userName);
    }
}

class UserChannelInfo implements Runnable {
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private final Socket socket;
    private final String userName;
    private final ConcurrentLinkedQueue<SimpleTextMessage> messageQueue;
    private boolean isServerRunning = true;
    private int errorCount = 0;

    public UserChannelInfo(Socket socket, ConcurrentLinkedQueue<SimpleTextMessage> messageQueue) throws IOException, ClassNotFoundException {
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.socket = socket;
        this.userName = (String)(this.objectInputStream.readObject());
        this.messageQueue = messageQueue;
        this.messageQueue.add(new SimpleTextMessage("System", this.userName + " joined"));
    }

    public void run() {
        System.out.println("The communication channel for " + this.userName + " is up");
        while (isServerRunning) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                errorCount++;
            }

            try {
                String msg = (String)objectInputStream.readObject();
                if (msg.startsWith("END")) {
                    isServerRunning = false;
                } else {
                    SimpleTextMessage simpleTextMessage = new SimpleTextMessage(this.getUserName(), msg);
                    this.messageQueue.add(simpleTextMessage);
                }
            } catch (Exception e) {
                errorCount++;
                if (errorCount >= 5) {
                    System.out.println("Server is closing the channel for " + this.userName + " because of error: " + e.getMessage());
                }
            }
        }
        UserChannelInfo userChannelInfo = TestTCPServerCenter.removeUserChannel(this.getUserName());
        if (userChannelInfo != null) {
            SimpleTextMessage simpleTextMessage = new SimpleTextMessage("System", userChannelInfo.getUserName() + " left.");
        }
    }

    public void sendMessage(SimpleTextMessage msg) {
        try {
            this.objectOutputStream.writeObject(msg);
        } catch (Exception e) {
            errorCount++;
            if (errorCount >= 5) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getUserName() {
        return this.userName;
    }
}

class ServerMessageManager implements Runnable {
    private ConcurrentLinkedQueue<SimpleTextMessage> messageQueue = null;
    private Hashtable<String, UserChannelInfo> users = null;

    private boolean isServerRunning = true;
    public ServerMessageManager(ConcurrentLinkedQueue<SimpleTextMessage> messageQueue, Hashtable<String, UserChannelInfo> users) {
        this.messageQueue = messageQueue;
        this.users = users;
    }

    public void run() {
        System.out.println("ServerMessageManager is up waiting for incoming messages.");

        while (isServerRunning) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (!messageQueue.isEmpty()) {
                SimpleTextMessage simpleTextMessage = messageQueue.poll();
                Enumeration<UserChannelInfo> allUsers = users.elements();
                while (allUsers.hasMoreElements()) {
                    UserChannelInfo userChannelInfo = allUsers.nextElement();
                    userChannelInfo.sendMessage(simpleTextMessage);
                }
            }
        }
    }
}