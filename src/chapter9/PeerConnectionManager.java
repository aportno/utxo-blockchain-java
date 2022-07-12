package chapter9;

import java.security.PublicKey;
import java.util.*;

public class PeerConnectionManager implements Runnable {
    private final Wallet wallet;
    private WalletMessageTaskManager messageTaskManager;
    private final Hashtable<String, KeyNamePair> allAddresses = new Hashtable<>();
    private final Hashtable<String, PeerOutgoingConnection> outgoingConnections = new Hashtable<>();
    private final Hashtable<String, PeerIncomingConnection> incomingConnections = new Hashtable<>();
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
            LogManager.log(Configuration.getLogBarMax(), "Cannot create the outgoing connection to " + peerServerIP + " because the current size " + outgoingConnections.size() + " has already reached the limit of " + Configuration.getOutgoingConnectionsLimit());
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
                this.addAddress(client.getConnectionPeerNamePair());
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

    public ArrayList<KeyNamePair> getAllStoredAddresses() {
        Iterator<KeyNamePair> allAddr = this.allAddresses.values().iterator();
        ArrayList<KeyNamePair> holder = new ArrayList<>();
        while (allAddr.hasNext()) {
            holder.add(allAddr.next());
        }
        return holder;
    }

    public String getNameFromAddress(PublicKey publicKey) {
        if (publicKey.equals(wallet.getPublicKey())) {
            return wallet.getName();
        }

        String address = UtilityMethods.getKeyString(publicKey);
        KeyNamePair keyNamePair = allAddresses.get(address);

        if (keyNamePair != null) {
            return keyNamePair.walletName();
        } else {
            return address;
        }
    }

    public void addAddress(KeyNamePair address) {
        if (!isExistingUserByPublicKey(address.publicKey())) {
            allAddresses.put(UtilityMethods.getKeyString(address.publicKey()), address);
        }
    }

    protected synchronized void closeAllPeerConnectionsActively() {
        Enumeration<PeerOutgoingConnection> Eo = outgoingConnections.elements();
        while (Eo.hasMoreElements()) {
            PeerOutgoingConnection poc = Eo.nextElement();
            poc.activeClose();
            removePeerConnection(poc);
        }

        Enumeration<PeerIncomingConnection> Ei = incomingConnections.elements();
        while (Ei.hasMoreElements()) {
            PeerIncomingConnection pic = Ei.nextElement();
            pic.activeClose();
            removePeerConnection(pic);
        }
    }

    protected synchronized void shutdownAll() {
        closeAllPeerConnectionsActively();
        isServerRunning = false;
    }

    protected boolean isSendTransaction(PublicKey receiver, double amountToTransfer) {
        Transaction tx = wallet.transferFund(receiver, amountToTransfer);
        if (tx != null && tx.verifySignature()) {
            MessageTransactionBroadcast mtb = new MessageTransactionBroadcast(tx);
            sendMessageByAll(mtb);
            return true;
        }
        return false;
    }

    protected boolean isSendPrivateMessage(PublicKey receiver, String text) {
        MessageTextPrivate mtp = new MessageTextPrivate(text, wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), receiver);
        if (!sendMessageByKey(receiver, mtp)) {
            sendMessageByAll(mtp);
        }
        return true;
    }

    protected void broadcastRequestForBlockchainUpdate() {
        MessageAskForBlockchainBroadcast mabb = new MessageAskForBlockchainBroadcast("update blockchain", wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName());
        LogManager.log(Configuration.getLogBarMin(), "Sending message for updating local blockchain of " + wallet.getName());
        sendMessageByAll(mabb);
    }

    protected  int hasDirectConnection(PublicKey publicKey) {
        int n = 0;
        String msg = UtilityMethods.getKeyString(publicKey);
        if (incomingConnections.get(msg) != null && outgoingConnections.get(msg) != null) {
            n = 3;
        } else if (outgoingConnections.get(msg) != null) {
            n = 2;
        } else if (incomingConnections.get(msg) != null) {
            n = 1;
        }
        return n;
    }

    protected int numberOfExistingIncomingConnections() {
        return incomingConnections.size();
    }

    public boolean isExistingUserByPublicKey(PublicKey publicKey) {
        return allAddresses.containsKey(UtilityMethods.getKeyString(publicKey));
    }

    @Override
    public void run() {
        while (isServerRunning) {
            try {
                Thread.sleep(Configuration.getThreadSleepTimeLong());
            } catch (InterruptedException ie) {
                LogManager.log(Configuration.getLogBarMin(), "Exception in PeerConnectionManager.run() " + ie.getMessage());
            }

            if ((idleTime + 2) % 10 == 0 && autoMakingFriends < 3) {
                makingFriends();
                autoMakingFriends++;
            }

            try {
                Thread.sleep(Configuration.getThreadSleepTimeLong());
                idleTime++;
            } catch (InterruptedException ie) {
                LogManager.log(Configuration.getLogBarMin(), "Exception in PeerConnectionManager.run() " + ie.getMessage());
            }

            if (idleTime % 100 == 0) {
                idleTime = 0;
            }
        }
    }
}
