package chapter8;

import java.security.PublicKey;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.net.Socket;
import java.net.ServerSocket;
import java.security.KeyPair;

public class BlockchainMessageServiceProvider {
    /*
    Provides the network server
     */

    private ServerSocket serverSocket;
    private boolean isServerRunning = true;
    private Hashtable<String, ConnectionChannelTaskManager> connections;
    private ConcurrentLinkedQueue<Message> messageQueue;
    private Hashtable<String, KeyNamePair> allAddresses;
    private static Blockchain genesisBlockchain;

    public BlockchainMessageServiceProvider() {
        System.out.println("BlockchainMessageServiceProvider is starting");
        connections = new Hashtable<String, ConnectionChannelTaskManager>();
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.allAddresses = new Hashtable<String, KeyNamePair>();
        try {
            serverSocket = new ServerSocket(Configuration.getPORT());
        } catch (Exception e) {
            System.out.println("BlockchainMessageServiceProvider failed to create a server socket -> exiting");
            System.exit(1);
        }
    }

    protected void startWorking() {
        System.out.println("BlockchainMessageServiceProvider is ready");
        KeyPair keyPair = UtilityMethods.generateKeyPair();
        MessageCheckingTaskManager checkingAgent = new MessageCheckingTaskManager(this, messageQueue, keyPair);
        Thread agent = new Thread(checkingAgent);
        agent.start();
        System.out.println("BlockchainMessageServiceProvider generated MessageCheckingTaskManager -> thread working");

        while (isServerRunning) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("BlockchainMessageServiceProvider accepts one connection");
                ConnectionChannelTaskManager cctm = new ConnectionChannelTaskManager(this, socket, keyPair);
                Thread thread = new Thread(cctm);
                thread.start();
            } catch (Exception e) {
                System.out.println("BlockchainMessageServiceProvider problem -> " + e.getMessage());
                System.out.println("Exiting");
                System.exit(1);
            }
        }
    }

    protected PublicKey findAddress(String id) {
        KeyNamePair keyNamePair = this.allAddresses.get(id);
        if (keyNamePair != null) {
            return keyNamePair.getPublicKey();
        } else {
            return null;
        }
    }

    public static void updateGenesisBlock(Blockchain genesisBlock) {
        if (BlockchainMessageServiceProvider.genesisBlockchain == null) {
            BlockchainMessageServiceProvider.genesisBlockchain = genesisBlock;
        }
    }

    protected synchronized void addConnectionChannel(ConnectionChannelTaskManager channel) {
        this.connections.put(channel.getConnectionChannelId(), channel);
    }

    protected synchronized void addPublicKeyAddress(KeyNamePair keyNamePair) {
        this.allAddresses.put(UtilityMethods.getKeyString(keyNamePair.getPublicKey()), keyNamePair);
    }

    public void addMessageIntoQueue(Message msg) {
        this.messageQueue.add(msg);
    }

    protected synchronized KeyNamePair removeConnectionChannel(String channelId) {
        this.connections.remove(channelId);
        KeyNamePair keyNamePair = this.removeAddress(channelId);
        return keyNamePair;
    }

    protected synchronized KeyNamePair removeAddress(String id) {
        return this.allAddresses.remove(id);
    }

    public static Blockchain getGenesisBlockchain() {
        return BlockchainMessageServiceProvider.genesisBlockchain;
    }

    protected synchronized ArrayList<ConnectionChannelTaskManager> getAllConnectionChannelTaskManager() {
        ArrayList<ConnectionChannelTaskManager> connectChannels = new ArrayList<>();
        connectChannels.addAll(this.connections.values());
        return connectChannels;
    }

    protected synchronized ArrayList<KeyNamePair> getAllAddresses() {
        ArrayList<KeyNamePair> address = new ArrayList<>();
        address.addAll(this.allAddresses.values());
    }

    protected synchronized ConnectionChannelTaskManager findConnectionChannelTaskManager(String connectionId) {
        return this.connections.get(connectionId);
    }

}

class ConnectionChannelTaskManager implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    boolean isServerRunning = true;
    private String connectionID;
    private BlockchainMessageServiceProvider server;
    private KeyPair keyPair;
    private PublicKey delegatePublicKey;
    private String name;

    public ConnectionChannelTaskManager(BlockchainMessageServiceProvider server, Socket socket, KeyPair keyPair) {
        this.socket = socket;
        this.server = server;
        this.keyPair = keyPair;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            MessageID toClient = new MessageID(this.keyPair.getPrivate(), this.keyPair.getPublic(), "ServiceProvider");
            out.writeObject(toClient);
            out.flush();

            MessageID inputMid = (MessageID) in.readObject();
            if (!inputMid.isValid()) {
                throw new Exception("Invalid messageID");
            }

            this.delegatePublicKey = inputMid.getPublicKey();
            this.connectionID = UtilityMethods.getKeyString(inputMid.getPublicKey());
            this.name = inputMid.getName();
            System.out.println("Connection successfully established for " + this.getDelegateName() + "|" + this.connectionID);

            server.addConnectionChannel(this);
            this.server.addPublicKeyAddress(inputMid.getKeyNamePair());
            System.out.println("Adding address for " + inputMid.getKeyNamePair().getWalletName());
            MessageBlockchainPrivate mbp = new MessageBlockchainPrivate(BlockchainMessageServiceProvider.getGenesisBlockchain(),
                    BlockchainMessageServiceProvider.getGenesisBlockchain().getGenesisMiner(), this.delegatePublicKey);
            out.writeObject(mbp);
        } catch (Exception e) {
            System.out.println("ConnectionChannelTaskManager exception: " + e.getMessage());
            System.out.println("Connection failed -> aborting");
            this.activeClose();
        }
    }

    public String getDelegateName() {
        return this.name;
    }

    public PublicKey getDelegateAddress() {
        return this.delegatePublicKey;
    }

    protected String getConnectionChannelId() {
        return this.connectionID;
    }

    private void activeClose() {
        this.isServerRunning = false;
        try {
            this.server.removeConnectionChannel(this.getConnectionChannelId());
            System.out.println("ConnectionChannelTaskManager: preparing to close connection -> " + this.getDelegateName() + "|" + this.getConnectionChannelId());
            MessageTextPrivate mtp = new MessageTextPrivate(Message.TEXT_CLOSE, this.keyPair.getPrivate(), this.keyPair.getPublic(), this.getDelegateName(), this.delegatePublicKey);
            this.sendMessage(mtp);

            Thread.sleep(1000);

            System.out.println("ConnectionChannelTaskManager" + this.getDelegateName() + " closed activity -> " + this.getConnectionChannelId());
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            // do nothing
        }
    }

    protected synchronized boolean sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void run() {
        int count = 0;
        while (isServerRunning) {
            try {
                Message inputMsg = (Message) in.readObject();
            } catch (Exception e) {
                count++;
                if (count >= 3) {
                    this.activeClose();
                }
            }
        }
    }

    protected void passiveClose() {
        this.isServerRunning = false;
        try {
            this.server.removeConnectionChannel(this.getConnectionChannelId());
            in.close();
            out.close();
            socket.close();
            System.out.println("ConnectionChannelTaskManager closed passively -> " + this.getConnectionChannelId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MessageCheckingTaskManager implements Runnable {
    private boolean isServerRunning = true;
    private long sleepTime = 100;
    private BlockchainMessageServiceProvider server;
    private ConcurrentLinkedQueue<Message> messageQueue;
    private KeyPair keyPair;

    public MessageCheckingTaskManager(BlockchainMessageServiceProvider server, ConcurrentLinkedQueue<Message> messageQueue, KeyPair keyPair) {
        this.server = server;
        this.messageQueue = messageQueue;
        this.keyPair = keyPair;
    }

    @Override
    public void run() {
        while (isServerRunning) {
            try {
                if (this.messageQueue.isEmpty()) {
                    Thread.sleep(this.sleepTime);
                } else {
                    while (!this.messageQueue.isEmpty()) {
                        Message msg = this.messageQueue.poll();
                        processMessage(msg);
                    }
                }
            }
        }
    }

    private void processMessage(Message msg) throws Exception {
        if (msg == null) {
            return;
        }
        if (msg.isForBroadcast()) {
            ArrayList<ConnectionChannelTaskManager> all = this.server.getAllConnectionChannelTaskManager();
            for (ConnectionChannelTaskManager each : all) {
                each.sendMessage(msg);
            }
        } else if (msg.getMessageType() == Message.TEXT_PRIVATE) {
            MessageTextPrivate mtp = (MessageTextPrivate) msg;
            if (!mtp.isValid()) {
                return;
            }

            String text;
            if (mtp.getReceiver().equals(this.keyPair.getPublic())) {
                text = mtp.getMessageBody();
                if (text.equals(Message.TEXT_CLOSE)) {
                    System.out.println(mtp.getSenderName() + " left the network");
                    ConnectionChannelTaskManager thread = this.server.findConnectionChannelTaskManager(UtilityMethods.getKeyString(mtp.getSenderKey()));
                    if (thread != null) {
                        thread.passiveClose();
                    }
                } else if (text.equals(Message.TEXT_ASK_ADDRESSES)) {
                    if (!mtp.getSenderKey().equals(BlockchainMessageServiceProvider.getGenesisBlockchain().getGenesisMiner())) {
                        System.out.println(mtp.getSenderName() + " is requesting for the list of users");
                    }
                    ArrayList<KeyNamePair> addresses = this.server.getAllAddresses();
                    if (addresses.size() == 0) {
                        return;
                    } else if (addresses.size() == 1) {
                        KeyNamePair keyNamePair = addresses.get(0);
                        if (keyNamePair.getPublicKey().equals(mtp.getSenderKey())) {
                            return;
                        }
                    }
                    ConnectionChannelTaskManager thread = this.server.findConnectionChannelTaskManager(UtilityMethods.getKeyString(mtp.getSenderKey()));
                    if (thread != null) {
                        MessageAddressPrivate map = new MessageAddressPrivate(addresses);
                        thread.sendMessage(map);
                    }
                } else {
                    System.out.println("Garbage message for service provider found: " + text);
                }
            } else {
                ConnectionChannelTaskManager thread = this.server.findConnectionChannelTaskManager(UtilityMethods.getKeyString(mtp.getReceiver()));
                try {
                    thread.sendMessage(mtp);
                } catch (Exception e) {
                    // do nothing
                }
            }
        } else if (msg.getMessageType() == Message.BLOCKCHAIN_PRIVATE) {
            System.out.println("Forwarding private message on blockchain");
            MessageBlockchainPrivate mbp = (MessageBlockchainPrivate) msg;
            ConnectionChannelTaskManager thread = this.server.findConnectionChannelTaskManager(UtilityMethods.getKeyString(mbp.getReceiver()));
            if (thread != null) {
                thread.sendMessage(mbp);
            }
        } else {
            System.out.println("Message type not supported -> type=" + msg.getMessageType() + ", object=" + msg.getMessageBody());
        }
    }

    public void close() {
        isServerRunning = false;
    }
}
