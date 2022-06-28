package chapter5;

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
            System.out.println(this.walletName + "balance=" + available + ", not enough to make the transfer of " + totalNeeded);
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
        boolean isPreparedOutputUTXO = transaction.prepareOutputUTXOs();
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
        this.localLedger = ledger;
        return true;
    }
}
