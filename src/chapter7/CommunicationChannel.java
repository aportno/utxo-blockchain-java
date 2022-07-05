package chapter7;

import java.util.Scanner;

public class CommunicationChannel extends Thread {
    private final Scanner userInput;
    private final MessageManagerTCP messageManagerTCP;
    private boolean isServerRunning = true;

    public CommunicationChannel(MessageManagerTCP messageManagerTCP) {
        this.messageManagerTCP = messageManagerTCP;
        userInput = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Communication channel is up, please type below:");
        while (isServerRunning) {
            try {
                String userMsg = userInput.nextLine();
                messageManagerTCP.sendMessage(userMsg);
                if (userMsg.trim().startsWith("END")) {
                    isServerRunning = false;
                }
            } catch (Exception e) {
                isServerRunning = false;
            }
        }
        System.out.println("Channel closed");
        System.exit(1);
    }
}
