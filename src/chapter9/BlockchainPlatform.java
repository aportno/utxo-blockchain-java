package chapter9;

import java.util.ArrayList;

public class BlockchainPlatform {
    public static void main(String[] args) {
        Miner genesisMiner = new Miner("genesis", "genesis");

        System.out.println("=========================");
        System.out.println("Creating genesis block...");
        System.out.println("=========================");

        Block genesisBlock = new Block("0", Configuration.getBlockMiningDifficultyLevel(), genesisMiner.getPublicKey());

        UTXO u1 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 1_000_001.0);
        UTXO u2 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 1_000_000.0);
        ArrayList<UTXO> inputUTXOs = new ArrayList<>();
        inputUTXOs.add(u1);
        inputUTXOs.add(u2);

        Transaction genesisTx = new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 1_000_000, inputUTXOs);

        boolean isPreparedOutputUTXOs = genesisTx.isPreparedOutputUTXOs();
        if (!isPreparedOutputUTXOs) {
            LogManager.log(Configuration.getLogBarMin(), "Genesis transaction failed -> exiting");
            System.exit(1);
        }
        genesisTx.signTheTransaction(genesisMiner.getPrivateKey());

        boolean isAddedTx = genesisBlock.isAddedTransaction(genesisTx, genesisMiner.getPublicKey());
        if (!isAddedTx) {
            LogManager.log(Configuration.getLogBarMin(), "Failed to add genesis transaction to genesis block -> exiting");
            System.exit(2);
        }

        System.out.println("=======================");
        System.out.println("Mining genesis block...");
        System.out.println("=======================");

        boolean isMinedBlock = genesisMiner.isMinedBlock(genesisBlock);
        if (isMinedBlock) {
            LogManager.log(Configuration.getLogBarMin(), "Genesis block is successfully mined -> hashID: " +  genesisBlock.getHashID());
        } else {
            LogManager.log(Configuration.getLogBarMin(), "Failed to mine genesis block -> exiting");
            System.exit(3);
        }

        System.out.println("=====================");
        System.out.println("Forming blockchain...");
        System.out.println("=====================");

        Blockchain ledger = new Blockchain(genesisBlock);
        genesisMiner.setLocalLedger(ledger);

        try {
            PeerConnectionManager agent = new PeerConnectionManager(genesisMiner, null);
            MinerMessageTaskManager manager = new MinerGenesisMessageTaskManager(genesisMiner, agent);
            agent.setMessageTaskManager(manager);
            PeerServer peerServer = new PeerServer(genesisMiner, manager, agent);

            Thread managerThread = new Thread(manager);
            Thread agentThread = new Thread(agent);
            Thread serverThread = new Thread(peerServer);

            WalletSimulator simulator = new WalletSimulator(genesisMiner, agent, manager);
            manager.setSimulator(simulator);

            serverThread.start();
            LogManager.log(Configuration.getLogBarMin(), "Peer server started...");

            agentThread.start();
            LogManager.log(Configuration.getLogBarMin(), "Peer clients manager started...");

            managerThread.start();
            LogManager.log(Configuration.getLogBarMin(), "Wallet task manager started...");

            simulator.setBalanceShowPublicKey(false);
            LogManager.log(Configuration.getLogBarMin(), "Genesis miner is running -> IP address: " + PeerServer.getIPAddress());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(4);
        }
    }
}

