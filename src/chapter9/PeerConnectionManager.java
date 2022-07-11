package chapter9;

import java.security.PublicKey;
import java.util.*;

public class PeerConnectionManager implements Runnable {
    private Wallet wallet;
    private WalletMessageTaskManager messageTaskManager;
    private Hashtable<String, KeyNamePair> allAddresses = new Hashtable<>();
    private Hashtable<String, PeerOutgoingConnection> outgoingConnections = new Hashtable<>();
    private Hashtable<String, PeerIncomingConnection> incomingConnections = new Hashtable<>();
    private int autoMakingFriends;
    private int idleTime;
    private boolean isServerRunning = true;

    public PeerConnectionManager(Wallet wallet, WalletMessageTaskManager messageTaskManager) {
        this.wallet = wallet;
        this.messageTaskManager = messageTaskManager;
    }

    protected void setMessageTaskManager(WalletMessageTaskManager messageTaskManager) {
        this.messageTaskManager = messageTaskManager;
    }

    protected void makingFriends() {
        if (PeerServer.getIPAddress() != null && this.numberOfExistingIncomingConnections() < Configuration.getIncomingConnectionsLimit()) {
            MessageBroadcastMakingFriend mbmf = new MessageBroadcastMakingFriend(wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), PeerServer.getIPAddress());
            sendMessageByAll(mbmf);
        }
    }

    protected synchronized boolean createOutgoingConnection(String peerServerIP) {
        if (outgoingConnections.size() >= Configuration.getOutgoingConnectionsLimit()) {
            LogManager.log(Configuration.getLogBarMax(), "Cannot create the outgoing connection to " + peerServerIP + " because the current size " + outgoingConnections.size() + " has already reached the limit of " + Configuration.getOutgoingConnectionsLimit();
            return false;
        }

        PeerIncomingConnection pic = this.incomingConnections.get(peerServerIP);
        if (pic != null) {
            LogManager.log(Configuration.getLogBarMin(), "Cannot create the outgoing connection to " + peerServerIP + "because there is already an incoming connection from " + pic.getConnectionIP() + "/" + pic.getConnectionPeerName());
            return false;
        }

        try {
            PeerOutgoingConnection client = outgoingConnections.get(peerServerIP);
            if (client == null) {
                client = new PeerOutgoingConnection(peerServerIP, wallet, messageTaskManager, this);
                Thread thread = new Thread(client);
                thread.start();

                outgoingConnections.put(UtilityMethods.getKeyString(client.getConnectionPeerPublicKey()), client);
                outgoingConnections.put(peerServerIP, client);
                this.addAddress(client.getConnectionPeerName());
                LogManager.log(Configuration.getLogBarMax(), "Created an outgoing connection to " + peerServerIP);
                return true;
            }
        } catch (Exception e) {
            LogManager.log(Configuration.getLogBarMax(), "Exception in PeerConnectionManager.createOutgoingConnection | " + peerServerIP + " -> " + e.getMessage());
            return false;
        }
        return false;
    }

    protected boolean recreatePeerOutgoingConnection(PeerOutgoingConnection connection) {
        this.removePeerConnection(connection);
        return createOutgoingConnection(connection.getConnectionIPAddress());
    }

    protected void addIncomingConnection(PeerIncomingConnection connection) {
        this.incomingConnections.put(UtilityMethods.getKeyString(connection.getConnectionPeerPublicKey()), connection);
        this.incomingConnections.put(connection.getConnectionIP(), connection);
    }

    protected synchronized void removePeerConnection(PeerOutgoingConnection connection) {
        outgoingConnections.remove(connection.getConnectionIPAddress());
        outgoingConnections.remove(UtilityMethods.getKeyString(connection.getConnectionPeerPublicKey()));
    }

    protected synchronized void removePeerConnection(PeerIncomingConnection connection) {
        incomingConnections.remove(connection.getConnectionIP());
        incomingConnections.remove(UtilityMethods.getKeyString(connection.getConnectionPeerPublicKey()));
    }

    protected synchronized void sendMessageByAll(Message msg) {
        Enumeration<PeerOutgoingConnection> eOutgoing = outgoingConnections.elements();
        while (eOutgoing.hasMoreElements()) {
            PeerOutgoingConnection conn = eOutgoing.nextElement();
            conn.sendMessage(msg);
        }

        Enumeration<PeerIncomingConnection> eIncoming = incomingConnections.elements();
        while (eIncoming.hasMoreElements()) {
            PeerIncomingConnection conn = eIncoming.nextElement();
            conn.sendMessage(msg);
        }
    }

    protected synchronized boolean sendMessageByKey(PublicKey publicKey, Message msg) {
        String ks = UtilityMethods.getKeyString(publicKey);
        PeerOutgoingConnection outgoingConn = outgoingConnections.get(ks);
        if (outgoingConn != null) {
            outgoingConn.sendMessage(msg);
            return true;
        }

        PeerIncomingConnection incomingConn = incomingConnections.get(ks);
        if (incomingConn != null) {
            incomingConn.sendMessage(msg);
            return true;
        }
        return false;
    }




    @Override
    public void run() {

    }
}
