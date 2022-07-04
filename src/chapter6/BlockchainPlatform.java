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

    }

    public static boolean isVerifiedBlock(Wallet wallet, Block block, String blockname) {
        if (wallet.isVerifiedGuestBlock(block)) {
            System.out.println(wallet.getName() + " accepted block " + blockname);
            return true;
        } else {
            System.out.println(wallet.getName() + " rejected block " + blockname);
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
