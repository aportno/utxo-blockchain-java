package chapter8;

import java.security.Key;
import java.security.PublicKey;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.net.Socket;

public class WalletConnectionAgent implements Runnable {
    private final Wallet wallet;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PublicKey serverAddress;
    private final ConcurrentLinkedQueue<Message> messageConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    private final Hashtable<String, KeyNamePair> allAddresses = new Hashtable<>();
    private boolean isServerRunning = true;
    public final long sleepTime = 100;

    public WalletConnectionAgent(String host, int port, Wallet wallet) {
        this.wallet = wallet;
        System.out.println("Starting agent for network communication...");
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            MessageID msgFromServer = (MessageID) in.readObject();
            if (msgFromServer.isValid()) {
                this.serverAddress = msgFromServer.getPublicKey();
            } else {
                throw new Exception("MessageID from service provider is invalid.");
            }

            System.out.println("Stored server address...sending wallet public key to server...");
            System.out.println("Wallet name: " + this.wallet.getName());

            MessageID messageID = new MessageID(this.wallet.getPrivateKey(), this.wallet.getPublicKey(), this.wallet.getName());
            out.writeObject(messageID);
            MessageBlockchainPrivate messageBlockchainPrivate = (MessageBlockchainPrivate) in.readObject();
            this.wallet.setLocalLedger(messageBlockchainPrivate.getMessageBody());

            System.out.println("Genesis blockchain set...network ready...");
        } catch (Exception e) {
            System.out.println("WalletConnectionAgent: creation failed because -> " + e.getMessage());
            System.out.println("Please restart");
            System.exit(1);
        }
    }

    public void run() {
        try {
            Thread.sleep(this.sleepTime);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        while (isServerRunning) {
            try {
                Message msg = (Message) in.readObject();
                this.messageConcurrentLinkedQueue.add(msg);
            } catch (Exception e) {
                isServerRunning = false;
            }
        }
    }

    public void close() {
        this.isServerRunning = false;
        try {
            this.in.close();
            this.out.close();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void activeClose() {
        MessageTextPrivate messageTextPrivate = new MessageTextPrivate(Message.TEXT_CLOSE, this.wallet.getPrivateKey(),
                this.wallet.getPublicKey(), this.wallet.getName(), this.getServerAddress());
        this.sendMessage(messageTextPrivate);
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
            e.getMessage();
        }
        this.close();
    }

    public void addAddress(KeyNamePair address) {
        this.allAddresses.put(UtilityMethods.getKeyString(address.getPublicKey()), address);
    }

    public PublicKey getServerAddress() {
        return serverAddress;
    }

    public ConcurrentLinkedQueue<Message> getMessageConcurrentLinkedQueue() {
        return messageConcurrentLinkedQueue;
    }

    public String getNameFromAddress(PublicKey publicKey) {
        if (publicKey.equals(this.wallet.getPublicKey())) {
            return this.wallet.getName();
        }

        String address = UtilityMethods.getKeyString(publicKey);
        KeyNamePair keyNamePair = this.allAddresses.get(address);

        if (keyNamePair != null) {
            return keyNamePair.getWalletName();
        } else {
            return address;
        }
    }

    public ArrayList<KeyNamePair> getAllStoredAddresses() {
        Iterator<KeyNamePair> E = this.allAddresses.values().iterator();
        ArrayList<KeyNamePair> A = new ArrayList<>();
        while (E.hasNext()) {
            A.add(E.next());
        }
        return A;
    }

    public synchronized boolean sendMessage(Message msg) {
        if (msg == null) {
            System.out.println("Message is empty -> cannot send");
            return false;
        } else {
            try {
                this.out.writeObject(msg);
                return true;
            } catch (Exception e) {
                System.out.println("Failed to send message -> " + e.getMessage());
                return false;
            }
        }
    }

    protected boolean sendTransaction(PublicKey receiver, double amountToTransfer) {
        Transaction tx = this.wallet.transferFund(receiver, amountToTransfer);
        if (tx != null && tx.verifySignature()) {
            MessageTransactionBroadcast msg = new MessageTransactionBroadcast(tx);
            this.sendMessage(msg);
            return true;
        } else {
            return false;
        }
    }
}
