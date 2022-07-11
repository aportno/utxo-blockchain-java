package chapter9;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

public class PeerServer implements Runnable {
    private Wallet wallet;
    private ServerSocket server;
    private static String IPAddress;
    private PeerConnectionManager connectionManager;
    private WalletMessageTaskManager messageTaskManager;
    private boolean isServerRunning = true;

    public PeerServer(Wallet wallet, WalletMessageTaskManager messageTaskManager, PeerConnectionManager connectionManager) throws IOException {
        this.wallet = wallet;
        this.connectionManager = connectionManager;
        this.messageTaskManager = messageTaskManager;
        server = new ServerSocket(Configuration.getPORT());
        PeerServer.IPAddress = server.getInetAddress().getHostAddress();
    }

    public static String getIPAddress() {
        return PeerServer.IPAddress;
    }

    @Override
    public void run() {
        LogManager.log(Configuration.getLogBarMax(), "Peer server of " + wallet.getName() + " is listening now");
        while (isServerRunning) {
            try {
                if (connectionManager.numberOfExistingIncomingConnection() >= Configuration.getIncomingConnectionsLimit()) {
                    Thread.sleep(Configuration.getThreadSleepTimeLong());
                } else {
                    Socket socket = server.accept();
                    InetAddress clientAddress = socket.getInetAddress();
                    LogManager.log(Configuration.getLogBarMax(), "Received an incoming connection request from " + clientAddress.getHostAddress());
                    PeerIncomingConnection peer = new PeerIncomingConnection(wallet, socket, messageTaskManager, connectionManager);
                    Thread thread = new Thread(peer);
                    LogManager.log(Configuration.getLogBarMax(), "PeerIncomingConnection with " + peer.getConnectionIP() + " established");
                    connectionManager.addIncomingConnection(peer);
                }
            } catch (Exception e) {
                LogManager.log(Configuration.getLogBarMax(), "Exception in PeerServer.run() -> " + e.getMessage());
                isServerRunning = false;
            }
        }
        System.exit(0);
    }
}
