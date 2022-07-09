package chapter8;

import java.util.Scanner;
import java.util.ArrayList;

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

final class MinerGenesisSimulator implements Runnable {
    private Blockchain genesisLedger;
    private Miner genesisMiner;
    synchronized Blockchain getGenesisLedger() {
        if (genesisLedger == null) {
            System.out.println("Blockchain platform starting...");
            System.out.println("Creating genesis miner, genesis transaction, and genesis block...");

            genesisMiner = getGenesisMiner();
            Block genesisBlock = new Block("0", Configuration.getBlockMiningDifficultyLevel(), genesisMiner.getPublicKey());
            UTXO u1 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000001.0);
            UTXO u2 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000000.0);

            ArrayList<UTXO> inputUTXOs = new ArrayList<>();
            inputUTXOs.add(u1);
            inputUTXOs.add(u2);
            Transaction genesisTx = new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000000.0, inputUTXOs);
            boolean isPreparedOutputUTXOs = genesisTx.isPreparedOutputUTXOs();
            if (!isPreparedOutputUTXOs) {
                System.out.println("Genesis transaction failed");
                System.exit(1);
            }

            genesisTx.signTheTransaction(genesisMiner.getPrivateKey());
            boolean isAddedTransaction = genesisBlock.isAddedTransaction(genesisTx, genesisMiner.getPublicKey());
            if (!isAddedTransaction) {
                System.out.println("Failed to add the genesis transaction to the genesis block -> exiting");
                System.exit(2);
            }

            System.out.println("Genesis miner is mining the genesis block...");
            boolean isMinedBlock = genesisMiner.isMinedBlock(genesisBlock);
            if (isMinedBlock) {
                System.out.println("Genesis block is successfully mined with hash ID:");
                System.out.println(genesisBlock.getHashID());
            } else {
                System.out.println("Failed to mine genesis block -> exiting");
                System.exit(3);
            }

            Blockchain ledger = new Blockchain(genesisBlock);
            System.out.println("Blockchain genesis successfully completed");
            genesisMiner.setLocalLedger(ledger);
            this.genesisLedger = ledger.copy_NotDeepCopy();
            System.out.println("Genesis miner balance: " + genesisMiner.getCurrentBalance(genesisMiner.getLocalLedger()));
        }
        return this.genesisLedger;
    }

    synchronized Miner getGenesisMiner() {
        if (genesisMiner == null) {
            genesisMiner = new Miner("genesis", "genesis");
        }
        return genesisMiner;
    }

    public static boolean isValidIpAddress(String ipAddress) {
        return ipAddress.length() < 5;
    }

    public void run() {
        System.out.println("Genesis miner starting before other miners and wallets join");
        Miner miner = getGenesisMiner();
        getGenesisLedger();
        System.out.println("Miner name -> " + miner.getName());
        System.out.println("Has the ServiceRelayProvider started yet? 1=yes, 0=no");
        int userInput = UtilityMethods.guaranteeIntegerInputByScanner(BlockchainPlatform.keyboard, 0, 1);
        while (userInput == 0) {
            System.out.println("Start the ServiceRelayProvider before continuing!");
            System.out.println("Has the ServiceRelayProvider started yet? 1=yes, 0=no");
            userInput = UtilityMethods.guaranteeIntegerInputByScanner(BlockchainPlatform.keyboard, 0, 1);
        }
        double balance = miner.getCurrentBalance(miner.getLocalLedger());

        System.out.println("Checking genesis miner balance: " + balance);
        System.out.println("Please enter the service provider IP address to join network");
        System.out.println("Enter 127.0.0.1 if serving locally");
        String ipAddress = BlockchainPlatform.keyboard.nextLine();
        if (ipAddress == null || isValidIpAddress(ipAddress)) {
            ipAddress = "localHost";
        }

        WalletConnectionAgent agent = new WalletConnectionAgent(ipAddress, Configuration.getPORT(), miner);
        Thread agentThread = new Thread(agent);
        agentThread.start();
        MinerGenesisMessageTaskManager genesisTaskManager = new MinerGenesisMessageTaskManager(agent,
                miner,
                agent.getMessageConcurrentLinkedQueue());
        Thread taskManagerThread = new Thread(genesisTaskManager);
        taskManagerThread.start();
    }
}
