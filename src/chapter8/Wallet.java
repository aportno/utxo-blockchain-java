package chapter8;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Wallet {
    private KeyPair keyPair;
    private String walletName;
    private static final String keyLocation = "keys";
    private Blockchain localLedger;

    public Wallet(String walletName, String password) {
        this.keyPair = UtilityMethods.generateKeyPair();
        this.walletName = walletName;

        try {
            populateExistingWallet(walletName, password);
        } catch (Exception e) {
            try {
                this.prepareWallet(password);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public Wallet(String walletName) {
        this.keyPair = UtilityMethods.generateKeyPair();
        this.walletName = walletName;
    }

    private void prepareWallet(String password) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(this.keyPair);

        byte[] keyBytes = UtilityMethods.encryptionByXOR(byteArrayOutputStream.toByteArray(), password);
        File file = new File(Wallet.keyLocation);

        if (!file.exists()) {
            file.mkdir();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(Wallet.keyLocation
                + "/"
                + this.getName().replace(' ', '_')
                + "_keys");

        fileOutputStream.write(keyBytes);
        fileOutputStream.close();
        byteArrayOutputStream.close();
    }

    private void populateExistingWallet(String walletName, String password) {
        try {
            FileInputStream fileInputStream = new FileInputStream(Wallet.keyLocation
                    + "/"
                    + walletName.replace(' ', '_') + "_keys");

            byte[] bb = new byte[4096];
            int size = fileInputStream.read(bb);

            fileInputStream.close();

            byte[] data = new byte[size];

            System.arraycopy(bb, 0, data, 0, data.length);

            byte[] keyBytes = UtilityMethods.decryptionByXOR(data, password);
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(keyBytes));

            this.keyPair = (KeyPair) (objectInputStream.readObject());
            this.walletName = walletName;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Transaction transferFund(PublicKey[] receivers, double[] amountToTransfer) {
        ArrayList<UTXO> unspent = new ArrayList<>();
        double available = this.getLocalLedger().findUnspentUTXOs(this.getPublicKey(), unspent);
        double totalNeeded = Transaction.TRANSACTION_FEE;
        for (double fund : amountToTransfer) {
            totalNeeded += fund;
        }

        if (available < totalNeeded) {
            System.out.println(this.walletName + " balance=" + available);
            System.out.println("Not enough funds available to complete the transfer of " + totalNeeded);
            return null;
        }

        // Create input for the transaction
        ArrayList<UTXO> inputUTXOs = new ArrayList<>();
        available = 0;
        for (int i = 0; i < unspent.size() && available < totalNeeded; i++) {
            UTXO unspentUTXO = unspent.get(i);
            available += unspentUTXO.getAmountTransferred();
            inputUTXOs.add(unspentUTXO);
        }

        // Create the transaction
        Transaction transaction = new Transaction(this.getPublicKey(), receivers, amountToTransfer, inputUTXOs);

        // Prepare output UTXO
        boolean isPreparedOutputUTXO = transaction.isPreparedOutputUTXOs();
        if (isPreparedOutputUTXO) {
            transaction.signTheTransaction(this.getPrivateKey());
            return transaction;
        } else {
            return null;
        }
    }

    public Transaction transferFund(PublicKey receiver, double amountToTransfer) {
        PublicKey[] receivers = new PublicKey[1];
        double[] amount = new double[1];
        receivers[0] = receiver;
        amount[0] = amountToTransfer;
        return transferFund(receivers, amount);
    }

    public String getName() {
        return this.walletName;
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    public synchronized Blockchain getLocalLedger() {
        return this.localLedger;
    }

    public double getCurrentBalance(Blockchain ledger) {
        return ledger.checkBalance(this.getPublicKey());
    }

    public synchronized boolean setLocalLedger(Blockchain ledger) {
        boolean isValidatedBlockchain = Blockchain.isValidatedBlockchain(ledger);
        if (!isValidatedBlockchain) {
            System.out.println(this.getName() + "] Warning: the incoming blockchain failed validation");
            return false;
        }

        if (this.localLedger == null) {
            this.localLedger = ledger;
            return true;
        } else {
            if (ledger.getBlockchainSize() > this.localLedger.getBlockchainSize() && ledger.getGenesisMiner().equals(this.localLedger.getGenesisMiner())) {
                this.localLedger = ledger;
                return true;
            } else if (ledger.getBlockchainSize() <= this.localLedger.getBlockchainSize()) {
                System.out.println(this.getName() + "] Warning: the incoming blockchain is no longer than current local one");
                System.out.println("Local size: " + this.localLedger.getBlockchainSize());
                System.out.println("Incoming size: " + ledger.getBlockchainSize());
                return false;
            } else {
                System.out.println(this.getName() + "] Warning: the incoming blockchain has a different genesis miner than current local one");
                return false;
            }
        }
    }

    public synchronized boolean isUpdatedLocalLedger(ArrayList<Blockchain> blockchains) {
        if (blockchains.size() == 0) {
            return false;
        }

        if (this.localLedger != null) {
            Blockchain max = this.localLedger;
            for (Blockchain blockchain : blockchains) {
                boolean isGenesisMiner = blockchain.getGenesisMiner().equals(this.localLedger.getGenesisMiner());
                if (isGenesisMiner && blockchain.getBlockchainSize() > max.getBlockchainSize() && Blockchain.isValidatedBlockchain(blockchain)) {
                    max = blockchain;
                }
            }
            this.localLedger = max;
            return true;
        } else {
            Blockchain max = null;
            int currentLength = 0;
            for (Blockchain blockchain : blockchains) {
                boolean isValidatedBlockchain = Blockchain.isValidatedBlockchain(blockchain);
                if (isValidatedBlockchain && blockchain.getBlockchainSize() > currentLength) {
                    max = blockchain;
                    currentLength = max.getBlockchainSize();
                }
            }

            if (max != null) {
                this.localLedger = max;
                return true;
            } else {
                return false;
            }
        }
    }

    public synchronized boolean isUpdatedLocalLedger(Block block) {
        if (isVerifiedGuestBlock(block)) {
            return this.localLedger.isAddedBlock(block);
        }
        return false;
    }

    public boolean isVerifiedGuestBlock(Block block, Blockchain ledger) {
        if (!block.isVerifiedSignature(block.getCreator())) {
            System.out.println("\tWarning: block(" + block.getHashID() + ") invalid signature");
            return false;
        }

        if (!UtilityMethods.hashMeetsDifficultyLevel(block.getHashID(), block.getDifficultyLevel()) || !block.computeHashID().equals(block.getHashID())) {
            System.out.println("\tWarning: block(" + block.getHashID() + ") is not linking to last block");
            return false;
        }

        if (!ledger.getLastBlock().getHashID().equals(block.getPreviousBlockHashID())) {
            System.out.println("\tWarning: block(" + block.getHashID() + ") is not linked to previous block");
            return false;
        }

        int totalNumTx = block.getTotalNumberOfTransactions();
        for (int i = 0; i < totalNumTx; i++) {
            Transaction tx = block.getTransaction(i);
            if (!isValidatedTransaction(tx)) {
                System.out.println("\tWarning: block(" + block.getHashID() +") transaction " + i + " is invalid");
                return false;
            }
        }

        Transaction rewardTx = block.getRewardTransaction();
        if (rewardTx.getTotalAmountToTransfer() > Blockchain.MINING_REWARD + block.getTransactionFeeAmount()) {
            System.out.println("\tWarning: block(" + block.getHashID() + ") over rewarded");
            return false;
        }
        return true;
    }

    public boolean isVerifiedGuestBlock(Block block) {
        return this.isVerifiedGuestBlock(block, this.getLocalLedger());
    }

    public boolean isValidatedTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        if (!transaction.verifySignature()) {
            System.out.println("WARNING: transaction ID=" + transaction.getHashID() + " from " + " is invalid. It has been tampered");
            return false;
        }

        boolean isExisting;
        if (this.getLocalLedger() == null) {
            isExisting = false;
        } else {
            isExisting = this.getLocalLedger().isTransactionExist(transaction);
        }
        return !isExisting;
    }
}
