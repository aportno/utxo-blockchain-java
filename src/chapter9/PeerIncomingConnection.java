package chapter9;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.security.PublicKey;

public class PeerIncomingConnection implements Runnable {
    private Wallet wallet;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PublicKey connectionPeerPublicKey;
    private String connectionPeerName;
    private peerConnectionManager connectionManager;
    private WalletMessageTaskManager messageTaskManager;
    private boolean isServerRunning = true;

    public PeerIncomingConnection(Wallet wallet, Socket socket, peerConnectionManager connectionManager, WalletMessageTaskManager messageTaskManager)  throws Exception {
        this.wallet = wallet;
        this.socket = socket;
        this.connectionManager = connectionManager;
        this.messageTaskManager = messageTaskManager;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        MessageID mid = new MessageID(wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName());
        out.writeObject(mid);
        MessageID readMid = (MessageID) in.readObject();
        connectionPeerPublicKey = readMid.getSenderKey();
        connectionPeerName = readMid.getName();
        MessageAskForBlockchainPrivate mabp = (MessageAskForBlockchainPrivate) in.readObject();

        if (mabp.isRequired()) {
            PublicKey receiver = mabp.getSenderKey();
            Blockchain ledger = wallet.getLocalLedger().copy_NotDeepCopy();
            MessageBlockchainPrivate msg = new MessageBlockchainPrivate(ledger, wallet.getPublicKey(), receiver);
            sendMessage(msg);
        }
        connectionManager.addAddress(mabp.getSenderKeyNamePair());
    }

    @Override
    public void run() {
        while (isServerRunning) {
            try {
                Thread.sleep(Configuration.getThreadSleepTimeMedium();
            } catch (InterruptedException ie) {
                LogManager.log(Configuration.getLogBarMin(), "Exception in PeerIncomingConnection.run() -> " + ie.getMessage());
                activeClose();
            }

            try {
                Message msg = (Message) in.readObject();
                LogManager.log(Configuration.getLogBarMin(), "Got a message in IncomingConnection: " + msg.getMessageType() + "|" + msg.getMessageBody());
                if (msg.getMessageType() == Message.TEXT_PRIVATE_CLOSE_CONNECTION) {
                    MessageTextCloseConnectionPrivate mtccp = (MessageTextCloseConnectionPrivate) msg;
                    if (mtccp.getSenderKey().equals(connectionPeerPublicKey) && mtccp.getReceiver().equals(wallet.getPublicKey())) {
                        LogManager.log(Configuration.getLogBarMax(), "The incomingConnection from " + getConnectionIP() + "/" + connectionPeerName + " is requested to be terminated");
                        connectionManager.removePeerConnection(this);
                        close();
                    }
                } else {
                    messageTaskManager.addMessageIntoQueue(msg);
                }
            } catch (Exception e) {
                LogManager.log(Configuration.getLogBarMin(), "Exception in PeerIncomingConnection.run() :: " + e.getMessage());
            }
        }
    }

    protected synchronized boolean sendMessage(Message message) {
        if (message == null) {
            return false;
        }
        try {
            out.writeObject(message);
            return true;
        } catch (IOException ioe) {
            LogManager.log(Configuration.getLogBarMax(), "Exception in PeerIncomingConnection.sendMessage() ->" + " type=" + message.getMessageType() + "::" + ioe.getMessage());
            close();
        }
        return false;
    }

    protected synchronized void activeClose() {
        MessageTextCloseConnectionPrivate mtccp = new MessageTextCloseConnectionPrivate(wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), connectionPeerPublicKey);
        try {
            out.writeObject(mtccp);
            Thread.sleep(Configuration.getThreadSleepTimeShort());
        } catch (Exception e) {
            LogManager.log(Configuration.getLogBarMin(), "Exception in PeerIncomingConnection.activeClose() -> " +  e.getMessage());
        }
        close();
    }

    private synchronized void close() {
        isServerRunning = false;
        try {
            in.close();
            out.close();
        } catch (Exception e) {
            LogManager.log(Configuration.getLogBarMin(), "Exception in PeerIncomingConnection.close()" + e.getMessage());
        }
        connectionPeerName.removePeerConnection(this);
        LogManager.log(Configuration.getLogBarMin(), "IncomingConnection from " + getConnectionIP() + "/" + connectionPeerName + " is closed");
    }

    public String getConnectionIP() {
        return socket.getInetAddress().getHostAddress();
    }

    public PublicKey getConnectionPeerPublicKey() {
        return connectionPeerPublicKey;
    }

    public String getConnectionPeerName() {
        return connectionPeerName;
    }
}
