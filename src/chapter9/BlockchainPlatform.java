package chapter9;

import java.util.Scanner;

public class BlockchainPlatform {
    protected static Scanner keyboard;
    public static void main(String[] args) {
        keyboard = new Scanner(System.in);
        MinerGenesisSimulator genesisSimulator = new MinerGenesisSimulator();
        Thread simulatorThread = new Thread(genesisSimulator);
        simulatorThread.start();

        System.out.println("Genesis simulator running...");
        System.out.println("Starting the blockchain message service provider...");

        BlockchainMessageServiceProvider server = new BlockchainMessageServiceProvider();
        Blockchain ledger = genesisSimulator.getGenesisLedger();
        BlockchainMessageServiceProvider.updateGenesisBlock(ledger);
        Blockchain checkLedger = genesisSimulator.getGenesisMiner().getLocalLedger();

        if (ledger.getBlockchainSize() != checkLedger.getBlockchainSize()) {
            System.out.println("Error: genesis ledger initialization failed due to block size -> exiting");
            System.exit(1);
        }

        if (!ledger.getLastBlock().getHashID().equals(checkLedger.getLastBlock().getHashID())) {
            System.out.println("Error: genesis ledger initialization failed due to bad hash code -> exiting");
            System.out.println(ledger.getLastBlock().getPreviousBlockHashID() + "\n" + checkLedger.getLastBlock().getPreviousBlockHashID());
            System.exit(2);
        }

        System.out.println("**********************************************");
        System.out.println("Genesis blockchain ready for service provider");
        System.out.println("Server starting...");
        System.out.println("**********************************************");

        server.startWorking();
        System.out.println("========Blockchain Network Terminating========");
    }
}

