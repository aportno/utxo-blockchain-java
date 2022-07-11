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



    @Override
    public void run() {

    }
}
