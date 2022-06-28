package chapter5;

import java.util.ArrayList;

public class BlockchainPlatform {
    private static Blockchain blockchain;
    private static final double transactionFee = 0.0;

    public static void main(String[] args) throws Exception {
        int difficultyLevel = 25;
        System.out.println("Starting blockchain platform...");

        Miner genesisMiner = new Miner("genesis", "genesis");
        System.out.println("Created genesis miner");

        Block genesisBlock = new Block("0", difficultyLevel);
        System.out.println("Created genesis block");

        UTXO firstUTXO = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10001.0);
        UTXO secondUTXO = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0);

        ArrayList<UTXO> inputUTXO = new ArrayList<>();
        inputUTXO.add(firstUTXO);
        inputUTXO.add(secondUTXO);

        Transaction genesisTransaction = new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0, inputUTXO);

        boolean isPreparedOutputUTXO = genesisTransaction.prepareOutputUTXOs();
        if (!isPreparedOutputUTXO) {
            System.out.println("Genesis transaction failed");
            System.exit(1);
        }

        genesisTransaction.signTheTransaction(genesisMiner.getPrivateKey());
        genesisBlock.addTransaction(genesisTransaction);

        System.out.println("Attempting to mine genesis block...");
        boolean isMinedBlock = genesisMiner.mineBlock(genesisBlock);
        if (isMinedBlock) {
            System.out.println("Genesis block is successfully mined");
            System.out.println("Hash ID: " + genesisBlock.getHashID());
        } else {
            System.out.println("Failed to mine genesis block. System exit");
            System.exit(1);
        }



    }

}
