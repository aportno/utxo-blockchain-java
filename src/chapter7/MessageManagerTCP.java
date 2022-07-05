package chapter7;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageManagerTCP extends Thread {
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private boolean isServerRunning = true;
    private final String userName;

    public MessageManagerTCP(Socket socket, String userName) throws IOException {
        /*
        A TCP socket must create the output stream first then the input stream
         */
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.userName = userName;
    }

    public void sendMessage(String msg) {
        try {
            this.objectOutputStream.writeObject(msg);
        } catch (IOException ioe) {
            System.out.println("Error: writing message runs into exception");
            ioe.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Message manager is up...");
        while (isServerRunning) {
            try {
                String inputMsg = (String)(this.objectInputStream.readObject());
                System.out.println(this.userName + ": " + inputMsg);
                if (inputMsg.trim().startsWith("END")) {
                    isServerRunning = false;
                }
            } catch (Exception e) {
                System.out.println("Error: this is only for text messaging");
                System.exit(1);
            }
        }
        System.out.println("Message manager retired");
        System.exit(1);
    }
}
