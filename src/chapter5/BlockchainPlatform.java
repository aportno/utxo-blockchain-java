package chapter5;

import java.util.ArrayList;

public class BlockchainPlatform {
    private static Blockchain blockchain;
    private static double transactionFee = 0.0;

    public static void main(String[] args) throws Exception {
        int difficultyLevel = 10;
        System.out.println("Starting blockchain platform...");

        Miner genesisMiner = new Miner("genesis", "genesis");
        System.out.println("Created genesis miner");

        Block genesisBlock = new Block("0", difficultyLevel);
        System.out.println("Created genesis block\n");

        UTXO firstInputUTXO = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10001.0);
        UTXO SecondInputUTXO = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0);

        ArrayList<UTXO> inputUTXO = new ArrayList<>();
        inputUTXO.add(firstInputUTXO);
        inputUTXO.add(SecondInputUTXO);

        Transaction genesisTransaction = new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0, inputUTXO);

        boolean isPreparedOutputUTXO = genesisTransaction.prepareOutputUTXOs();
        if (!isPreparedOutputUTXO) {
            System.out.println("Genesis transaction failed");
            System.exit(1);
        }

        genesisTransaction.signTheTransaction(genesisMiner.getPrivateKey());
        genesisBlock.addTransaction(genesisTransaction);
        System.out.println("Genesis transaction added to the genesis block successfully");

        System.out.println("Attempting to mine genesis block...");
        boolean isMinedBlock = genesisMiner.mineBlock(genesisBlock);
        if (isMinedBlock) {
            System.out.println("Genesis block successfully mined\n");
            System.out.println("Block hash ID: " + genesisBlock.getHashID() + "\n");
        } else {
            System.out.println("Failed to mine genesis block. System exit");
            System.exit(1);
        }

        System.out.println("Initializing blockchain...");
        blockchain = new Blockchain(genesisBlock);
        System.out.println("Blockchain initialized with genesis block\n");

        genesisMiner.setLocalLedger(blockchain);
        System.out.println("Genesis miner balance: " + genesisMiner.getCurrentBalance(genesisMiner.getLocalLedger()) + "\n");

        Miner userA = new Miner("Miner A", "Miner A");
        Wallet userB = new Wallet("Wallet A", "Wallet A");
        Miner userC = new Miner("Miner B", "Miner B");

        userA.setLocalLedger(blockchain);
        userB.setLocalLedger(blockchain);
        userC.setLocalLedger(blockchain);

        System.out.println("Creating block 2...");
        Block block2 = new Block(blockchain.getLastBlock().getHashID(), difficultyLevel);
        System.out.println("Block 2 created successfully\n");

        Transaction transaction2 = genesisMiner.transferFund(userA.getPublicKey(), 100);
        if (transaction2 != null) {
            if (transaction2.verifySignature() && block2.addTransaction(transaction2)) {
                System.out.println("Balances on the BLOCKCHAIN prior to block 2 addition");
                double total = genesisMiner.getCurrentBalance(blockchain)
                        + userA.getCurrentBalance(blockchain)
                        + userB.getCurrentBalance(blockchain)
                        + userC.getCurrentBalance(blockchain);
                System.out.println("Genesis miner: " + genesisMiner.getCurrentBalance(blockchain));
                System.out.println("User A: " + userA.getCurrentBalance(blockchain));
                System.out.println("User B: " + userB.getCurrentBalance(blockchain));
                System.out.println("User C: " + userC.getCurrentBalance(blockchain));
                System.out.println("Transaction 2 added to block 2...\n");
            } else {
                System.out.println("Failed to create transaction 2 \n");
            }
        }

        Transaction transaction3 = genesisMiner.transferFund(userB.getPublicKey(), 200);
        if (transaction3 != null) {
            if (transaction3.verifySignature() && block2.addTransaction(transaction3)) {
                System.out.println("Balances on the BLOCKCHAIN prior to block 2 addition");
                double total = genesisMiner.getCurrentBalance(blockchain)
                        + userA.getCurrentBalance(blockchain)
                        + userB.getCurrentBalance(blockchain)
                        + userC.getCurrentBalance(blockchain);
                System.out.println("Genesis miner: " + genesisMiner.getCurrentBalance(blockchain));
                System.out.println("User A: " + userA.getCurrentBalance(blockchain));
                System.out.println("User B: " + userB.getCurrentBalance(blockchain));
                System.out.println("User C: " + userC.getCurrentBalance(blockchain));
                System.out.println("Transaction 3 added to block 2...\n");
            } else {
                System.out.println("Failed to add transaction 3 to block 2\n");
            }
        } else {
            System.out.println("Failed to create transaction 3\n");
        }

        System.out.println("Attempting to mine block2...");
        if (userA.mineBlock(block2)) {
            blockchain.addBlock(block2);
            System.out.println("User A mined block2");
            System.out.println("Block hash ID: " + block2.getHashID() + "\n");
            System.out.println("Current balances on the blockchain");
            displayBalanceAfterBlock(block2, genesisMiner, userA, userB, userC);
        }

        System.out.println("\nCreating block...");
        Block block3 = new Block(block2.getHashID(), difficultyLevel);
        System.out.println("Block created successfully\n");

        Transaction transaction4 = userA.transferFund(userB.getPublicKey(), 200.0);
        if (transaction4 != null) {
            if (transaction4.verifySignature() && block3.addTransaction(transaction4)) {
                System.out.println("Transaction 4 added to block 3...\n");
            } else {
                System.out.println("Failed to add transaction 4 to block 3\n");
            }
        } else {
            System.out.println("Failed to create transaction 4\n");
        }

        Transaction transaction5 = userA.transferFund(userC.getPublicKey(), 300.0);
        if (transaction5 != null) {
            if (transaction5.verifySignature() && block3.addTransaction(transaction5)) {
                System.out.println("Transaction 5 added to block 3...\n");
            } else {
                System.out.println("Failed to add transaction 5 to block\n");
            }
        } else {
            System.out.println("Failed to create transaction 5\n");
        }

        Transaction transaction6 = userA.transferFund(userC.getPublicKey(), 20.0);
        if (transaction6 != null) {
            if (transaction6.verifySignature() && block3.addTransaction(transaction6)) {
                System.out.println("Transaction 6 added to block 3...\n");
            } else {
                System.out.println("Failed to add transaction 6 to block\n");
            }
        } else {
            System.out.println("Failed to create transaction 6\n");
        }

        Transaction transaction7 = userB.transferFund(userC.getPublicKey(), 80.0);
        if (transaction7 != null) {
            if (transaction7.verifySignature() && block3.addTransaction(transaction7)) {
                System.out.println("Transaction 7 added to block 3...\n");
            } else {
                System.out.println("Failed to add transaction 7 to block\n");
            }
        } else {
            System.out.println("Failed to create transaction 7\n");
        }

        System.out.println("Attempting to mine block3...");
        if (userC.mineBlock(block3)) {
            blockchain.addBlock(block3);
            System.out.println("User C mined block 3");
            System.out.println("Block hash ID: " + block3.getHashID() + "\n");
            System.out.println("Current balances on the blockchain");
            displayBalanceAfterBlock(block3, genesisMiner, userA, userB, userC);
        }

        System.out.println("\n==========Blockchain Platform Shutting Down==========");
    }

    private static void displayBalanceAfterBlock(Block block, Wallet genesisMiner, Wallet walletA, Wallet walletB, Wallet walletC) {
        double total = genesisMiner.getCurrentBalance(blockchain)
                + walletA.getCurrentBalance(blockchain)
                + walletB.getCurrentBalance(blockchain)
                + walletC.getCurrentBalance(blockchain);
        transactionFee += block.getTotalNumberOfTransactions() * Transaction.TRANSACTION_FEE;
        System.out.println("Genesis miner: " + genesisMiner.getCurrentBalance(blockchain));
        System.out.println("User A: " + walletA.getCurrentBalance(blockchain));
        System.out.println("User B: " + walletB.getCurrentBalance(blockchain));
        System.out.println("User C: " + walletC.getCurrentBalance(blockchain) + "\n");
        System.out.println("Blockchain data:");
        System.out.println("Total Cash: " + total);
        System.out.println("Transaction fee: " + transactionFee);
        System.out.println("Number of blocks: " + blockchain.getBlockchainSize());
    }
}
