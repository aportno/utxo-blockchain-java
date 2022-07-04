package chapter6;

import java.security.PublicKey;
import java.util.ArrayList;

public class BlockchainPlatform {
    public static Blockchain ledger;
    public static void main(String[] args) {
        ArrayList<Wallet> users = new ArrayList<>();
        int difficultyLevel = 22;

        System.out.println("Starting blockchain platform...");
        System.out.println("Creating genesis miner, genesis transaction and genesis block");

        Miner genesisMiner = new Miner("genesis", "genesis");
        users.add(genesisMiner);

        Block genesisBlock = new Block("0", difficultyLevel, genesisMiner.getPublicKey());

        UTXO inputUTXO1 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10001.0);
        UTXO inputUTXO2 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0);

        ArrayList<UTXO> inputUTXOs = new ArrayList<>();
        inputUTXOs.add(inputUTXO1);
        inputUTXOs.add(inputUTXO2);

        Transaction genesisTransaction = new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0, inputUTXOs);
        boolean isPreparedOutputUTXOs = genesisTransaction.isPreparedOutputUTXOs();
        if (!isPreparedOutputUTXOs) {
            System.out.println("Genesis transaction failed");
            System.exit(1);
        }

        genesisTransaction.signTheTransaction(genesisMiner.getPrivateKey());

        boolean isAddedTransaction = genesisBlock.isAddedTransaction(genesisTransaction, genesisMiner.getPublicKey());
        if (!isAddedTransaction) {
            System.out.println("Failed to add genesis transaction to the genesis block");
            System.exit(1);
        }

        System.out.println("Genesis miner is mining the genesis block...");
        boolean isMinedBlock = genesisMiner.isMinedBlock(genesisBlock);
        if (isMinedBlock) {
            System.out.println("Genesis block is successfully mined...");
            System.out.println("Hash ID: " + genesisBlock.getHashID());
        } else {
            System.out.println("Failed to mine genesis block");
            System.exit(1);
        }

        ledger = new Blockchain(genesisBlock);
        System.out.println("Blockchain genesis successful");

        genesisMiner.setLocalLedger(ledger);
        System.out.println("Genesis miner balance: " + genesisMiner.getCurrentBalance(genesisMiner.getLocalLedger()));

        System.out.println("Creating 2 miners and 1 wallet");
        Miner minerA = new Miner("A", "A");
        Miner minerB = new Miner("B", "B");
        Wallet walletA = new Wallet("C", "C");

        users.add(minerA); users.add(minerB); users.add(walletA);
        minerA.setLocalLedger(ledger.copy_NotDeepCopy());
        minerB.setLocalLedger(ledger.copy_NotDeepCopy());
        walletA.setLocalLedger(ledger.copy_NotDeepCopy());

        Block block2 = minerA.createNewBlock(minerA.getLocalLedger(), difficultyLevel);

        System.out.println("Block 2 created by miner A");
        System.out.println("Genesis miner attempting to funds to:");
        System.out.println("Wallet A: 500+200=700");
        System.out.println("Miner B: 300+100=400");

        PublicKey[] receiver = {walletA.getPublicKey(), walletA.getPublicKey(), minerB.getPublicKey(), minerB.getPublicKey()};
        double[] amounts = {500, 200, 300, 100};
        Transaction tx1 = genesisMiner.transferFund(receiver, amounts);

        System.out.println("Miner A is collecting transactions...");
        if (minerA.isAddedTransaction(tx1, block2)) {
            System.out.println("Transaction 1 added block 2");
        } else {
            System.out.println("Warning: transaction 1 cannot be added into block 2");
        }

        System.out.println("Miner A is generating reward");
        if (minerA.isGeneratedRewardTransaction(block2)) {
            System.out.println("Reward transaction successfully added to block 2");
        } else {
            System.out.println("Reward transaction cannot be added to block 2");
        }

        System.out.println("Miner A is mining block 2");
        if (minerA.isMinedBlock(block2)) {
            System.out.println("Block 2 is mined and signed by A");
        }

        boolean isVerifiedBlock = isVerifiedBlock(minerB, block2, "b2");
        if (isVerifiedBlock) {
            System.out.println("All users synchronizing local blockchains with block 2");
            synchronizeLocalLedgers(users, block2);

            System.out.println("Current balances:");
            displayAllBalances(users);
        }

        System.out.println("Total should equal:");
        System.out.println(20000 + Blockchain.MINING_REWARD);
        System.out.println("Total of all wallets:");
        System.out.println(genesisMiner.getCurrentBalance(ledger)
                + minerA.getCurrentBalance(ledger)
                + walletA.getCurrentBalance(ledger)
                + minerB.getCurrentBalance(ledger)
        );

        Block block3 = minerA.createNewBlock(ledger, difficultyLevel);
        System.out.println("Genesis miner attempting to funds to:");
        System.out.println("Wallet A: 500+200=700");
        System.out.println("Miner B: 300+100=400");
        Transaction tx2 = genesisMiner.transferFund(receiver, amounts);

        if (minerA.isAddedTransaction(tx1, block3)) {
            System.out.println("Transaction 1 added into block 3");
        } else {
            System.out.println("Warning: transaction 1 cannot be added into block 3. Transaction 1 already exists");
        }

        if (minerA.isAddedTransaction(tx2, block3)) {
            System.out.println("Transaction 2 added into block 3");
        } else {
            System.out.println("Warning: transaction 2 cannot be added into block 3");
        }

        System.out.println("Miner A is collecting transactions");
        System.out.println("Miner A is generating reward");
        if (minerA.isGeneratedRewardTransaction(block3)) {
            System.out.println("Rewarding transaction successfully added to block 3");
        } else {
            System.out.println("Reward transaction cannot be added to block 3");
        }

        if (minerB.isMinedBlock(block3)) {
            System.out.println("Block 3 is mined and signed by miner B");
        } else {
            System.out.println("Miner B cannot mine block 3");
        }

        System.out.println("Test: miner C attempting to change the block");
        if (minerB.isDeletedTransaction(block3.getTransaction(0), block3)) {
            System.out.println("Miner B deleted the first transaction from block 3");
        } else {
            System.out.println("Error: miner B cannot delete the first transaction from block 3 ");
        }

        if (minerA.isMinedBlock(block3)) {
            System.out.println("Block 3 is mined and signed by miner A");
        } else {
            System.out.println("Error: block 3 is created by miner A");
        }

        System.out.println("Test: miner A attempting to change the block");
        if (minerA.isDeletedTransaction(block3.getTransaction(0), block3)) {
            System.out.println("Miner A deleted the first transaction from block 3");
        } else {
            System.out.println("Miner A cannot delete the first transaction from block 3. Block is already signed");
        }

        isVerifiedBlock = isVerifiedBlock(minerB, block3, "block3");
        if (isVerifiedBlock) {
            System.out.println("All blockchain users begin to update their local blockchain now with block 3");
            synchronizeLocalLedgers(users, block3);
            System.out.println("Current balances:");
            displayAllBalances(users);
        }

        System.out.println("Total should equal:");
        System.out.println(20000 + (Blockchain.MINING_REWARD * 2));
        System.out.println("Total of all wallets:");
        System.out.println(genesisMiner.getCurrentBalance(ledger)
                + minerA.getCurrentBalance(ledger)
                + walletA.getCurrentBalance(ledger)
                + minerB.getCurrentBalance(ledger)
        );

        Transaction tx5 = minerB.transferFund(minerB.getPublicKey(), 20);
        if (minerA.isAddedTransaction(tx5, block3)) {
            System.out.println("Miner A added transaction 5 into block 3");
        } else {
            System.out.println("Miner A cannot add transaction 5 into block 3. Block is already signed.");
        }

        System.out.println();
        Block block4 = minerB.createNewBlock(ledger, difficultyLevel);
        System.out.println("Miner B created block 4");

        if (minerB.isAddedTransaction(tx5, block4)) {
            System.out.println("Miner B added transaction 5 into block 4");
        } else {
            System.out.println("Miner B failed to add transaction 5 into block 4");
        }

        Transaction tx6 = minerB.transferFund(minerA.getPublicKey(), 100);
        Transaction tx7 = walletA.transferFund(minerA.getPublicKey(), 100);
        Transaction tx8 = minerB.transferFund(walletA.getPublicKey(), 100);

        if (minerB.isAddedTransaction(tx6, block4)) {
            System.out.println("Miner B added transaction 6 into block 4");
        } else {
            System.out.println("Miner B failed to add transaction 6 into block 4");
        }

        if (minerB.isAddedTransaction(tx7, block4)) {
            System.out.println("Miner B added transaction 7 into block 4");
        } else {
            System.out.println("Miner B failed to add transaction 7 into block 4");
        }

        if (minerB.isAddedTransaction(tx8, block4)) {
            System.out.println("Miner B added transaction 8 into block 4");
        } else {
            System.out.println("Miner B failed to add transaction 8 into block 4");
        }

        if (minerB.isGeneratedRewardTransaction(block4)) {
            System.out.println("Miner B generated reward for block 4");
        } else {
            System.out.println("Miner B cannot generate reward for block 4");
        }

        if (minerB.isMinedBlock(block4)) {
            System.out.println("Miner B mined block 4");
            System.out.println("Hash ID: " + block4.getHashID());

            isVerifiedBlock = isVerifiedBlock(minerB, block4, "block 4");
            if (isVerifiedBlock) {
                System.out.println("All blockchain users begin to update their local blockchain now with block 4");
                synchronizeLocalLedgers(users, block4);
                System.out.println("Current balances:");
                displayAllBalances(users);
            }
        }

        System.out.println("Test: try to add block 4 into the ledger again");
        boolean isAddedBlock = ledger.isAddedBlock(block4);
        if (isAddedBlock) {
            System.out.println("Error: block 4 is already in the ledger");
        } else {
            System.out.println("Block 4 cannot be re-added into the ledger");
        }

        System.out.println("Current balances after block 4:");
        displayAllBalances(users);

        System.out.println("Total should equal:");
        System.out.println(20000 + (Blockchain.MINING_REWARD * 3));
        System.out.println("Total of all wallets:");
        System.out.println(genesisMiner.getCurrentBalance(ledger)
                + minerA.getCurrentBalance(ledger)
                + walletA.getCurrentBalance(ledger)
                + minerB.getCurrentBalance(ledger)
        );

        System.out.println();
        System.out.println("=====================================================");
        System.out.println("Displaying blockchain");
        System.out.println();
        UtilityMethods.displayBlockchain(ledger, System.out, 0);
        System.out.println("==========Blockchain platform shutting down==========");
    }

    public static boolean isVerifiedBlock(Wallet wallet, Block block, String name) {
        if (wallet.isVerifiedGuestBlock(block)) {
            System.out.println(wallet.getName() + " accepted block " + name);
            return true;
        } else {
            System.out.println(wallet.getName() + " rejected block " + name);
            return false;
        }
    }

    public static void synchronizeLocalLedgers(ArrayList<Wallet> users, Block block) {
        for (Wallet wallet : users) {
            wallet.isUpdatedLocalLedger(block);
            System.out.println(wallet.getName() + " updated its local blockchain...");
        }
    }

    public static void displayUTXOs(ArrayList<UTXO> utxos, int level) {
        for (UTXO utxo : utxos) {
            UtilityMethods.displayUTXO(utxo, System.out, level);
        }
    }

    public static void displayBalance(Wallet wallet) {
        Blockchain ledger = wallet.getLocalLedger();
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();
        ArrayList<UTXO> unspent = new ArrayList<>();
        ArrayList<UTXO> rewards = new ArrayList<>();
        ArrayList<Transaction> sentTx = new ArrayList<>();

        double balance = ledger.findRelatedUTXOs(wallet.getPublicKey(), all, spent, unspent, sentTx, rewards);
        int level = 0;
        UtilityMethods.displayTab(System.out, level, wallet.getName());

        UtilityMethods.displayTab(System.out, level + 1, "All UTXOs:");
        displayUTXOs(all, level + 2);

        UtilityMethods.displayTab(System.out, level + 1, "Spent UTXOs:");
        displayUTXOs(spent, level + 2);

        UtilityMethods.displayTab(System.out, level + 1, "Unspent UTXOs:");
        displayUTXOs(unspent, level + 2);

        if (wallet instanceof Miner) {
            UtilityMethods.displayTab(System.out, level + 1, "Mining rewards");
            displayUTXOs(rewards, level + 2);
        }
        UtilityMethods.displayTab(System.out, level + 1, "Balance: " + balance);
        UtilityMethods.displayTab(System.out, level, "}");
    }

    public static void displayAllBalances(ArrayList<Wallet> user) {
        for (Wallet wallet : user) {
            displayBalance(wallet);
        }
    }
}
