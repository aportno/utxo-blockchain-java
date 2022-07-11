package chapter9;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.PublicKey;

public class PeerOutgoingConnection implements Runnable {
    private final Wallet wallet;
    private final Socket socket;
    private final String connectionIPAddress;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final PublicKey connectionPeerPublicKey;
    private final String connectionPeerName;
    private PeerConnectionManager connectionManager;
    private final WalletMessageTaskManager messageTaskManager;
    private boolean isServerRunning = true;

    protected PeerOutgoingConnection(String serverIPAddress, Wallet wallet, WalletMessageTaskManager messageTaskManager, PeerConnectionManager connectionManager) throws Exception {
        this.wallet = wallet;
        this.messageTaskManager = messageTaskManager;
        LogManager.log(Configuration.getLogBarMax(), wallet.getName() + " is creating a peer outgoing connection to " + serverIPAddress);
        this.connectionIPAddress = serverIPAddress;
        socket = new Socket(serverIPAddress, Configuration.getPORT());
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        MessageID fromPeerServer = (MessageID) in.readObject();

        if (fromPeerServer.isValid()) {
            this.connectionPeerPublicKey = fromPeerServer.getSenderKey();
            this.connectionPeerName = fromPeerServer.getName();
        } else {
            throw new Exception("MessageID from peer server is invalid");
        }
        LogManager.log(Configuration.getLogBarMax(), "Obtained peer server address and stored it -> sending wallet public key to peer server ");
        LogManager.log(Configuration.getLogBarMax(), "Name=" + wallet.getName());
        MessageID mid = new MessageID(wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName());
        out.writeObject(mid);
        LogManager.log(Configuration.getLogBarMax(), "A peer client outgoing connection to " + connectionIPAddress + " is established successfully");

        if (wallet.getLocalLedger() == null) {
            MessageAskForBlockchainPrivate mabp = new MessageAskForBlockchainPrivate("Update blockchain", wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), connectionPeerPublicKey, true);
            out.writeObject(mabp);
            MessageBlockchainPrivate mbp = (MessageBlockchainPrivate) in.readObject();
            boolean isSetLocalLedger = wallet.setLocalLedger(mbp.getMessageBody());
            if (isSetLocalLedger) {
                LogManager.log(Configuration.getLogBarMax(), "Blockchain updated successfully");
            } else {
                LogManager.log(Configuration.getLogBarMax(), "In PeerOutgoingConnection -> blockchain update failed for " + wallet.getName() + "/ IP=" + getConnectionIPAddress());
                throw new RuntimeException("In PeerOutgoingConnection.constructor() :: blockchain update failed for -> " + wallet.getName() + " from " + getConnectionIPAddress());
            }
        } else {
            MessageAskForBlockchainPrivate mabp = new MessageAskForBlockchainPrivate("update blockchain", wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), connectionPeerPublicKey, false);
            out.writeObject(mabp);
        }
        connectionManager.addAddress(fromPeerServer.getKeyNamePair());
    }
    public synchronized boolean sendMessage(Message msg) {
        if (msg == null) {
            LogManager.log(Configuration.getLogBarMin(), "Message is null -> cannot send");
            return false;
        }

        try {
            out.writeObject(msg);
            return true;
        } catch (Exception e) {
            LogManager.log(Configuration.getLogBarMax(), "Exception in PeerOutgoingConnection.sendMessage() | " + getConnectionIPAddress() + "| failed to send message -> " + e.getMessage());
            if (!this.connectionManager.recreatePeerOutgoingConnection(this)) {
                close();
            }
            return false;
        }
    }

    public PublicKey getConnectionPeerPublicKey() {
        return connectionPeerPublicKey;
    }

    public String getConnectionPeerName() {
        return  connectionPeerName;
    }

    public KeyNamePair getConnectionPeerNamePair() {
        return new KeyNamePair(connectionPeerPublicKey, connectionPeerName);
    }

    public String getConnectionIPAddress() {
        return connectionIPAddress;
    }

    protected void activeClose() {
        MessageTextCloseConnectionPrivate mtccp = new MessageTextCloseConnectionPrivate(wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), getConnectionPeerPublicKey());
        this.sendMessage(mtccp);
        try {
            Thread.sleep(Configuration.getThreadSleepTimeShort());
        } catch (Exception e) {
            LogManager.log(Configuration.getLogBarMax(), "Exception in PeerOutgoingConnection.activeClose() | " + getConnectionIPAddress() + "|" + e.getMessage());
        }
        close();
    }

    private void close() {
        isServerRunning = false;
        try {
            in.close();
            out.close();
        } catch (Exception e) {
            // do nothing
        }
        connectionManager.removePeerConnection(this);
        LogManager.log(Configuration.getLogBarMax(), "Peer outgoing connection to " + connectionIPAddress + "/" + getConnectionPeerName() + " is terminated");
    }


    @Override
    public void run() {
        while (isServerRunning) {
            try {
                Thread.sleep(Configuration.getThreadSleepTimeMedium());
                Message msg = (Message) in.readObject();
                LogManager.log(Configuration.getLogBarMin(), "Got a message in outgoing connection -> " + msg.getMessageType() + "|from  " + connectionIPAddress + "/" + connectionPeerName + "|" + msg.getMessageBody());
                if (msg.getMessageType() == Message.TEXT_PRIVATE_CLOSE_CONNECTION) {
                    MessageTextCloseConnectionPrivate mtccp = (MessageTextCloseConnectionPrivate) msg;
                    if (mtccp.getSenderKey().equals(getConnectionPeerPublicKey()) && mtccp.getReceiver().equals(wallet.getPublicKey())) {
                        close();
                        LogManager.log(Configuration.getLogBarMax(), getConnectionIPAddress() + "/" + getConnectionPeerName() + " initiates connection close");
                    }
                } else {
                    messageTaskManager.addMessageIntoQueue(msg);
                }
            } catch (Exception e) {
                LogManager.log(Configuration.getLogBarMax(), "Exception in PeerOutgoingConnection.run()|" + connectionIPAddress + "/" + connectionPeerName + "|" + e.getMessage());
                close();
            }
        }
    }
}
