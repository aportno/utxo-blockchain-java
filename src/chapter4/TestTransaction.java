package chapter4;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;

public class TestTransaction {
    public static void main(String[] args) {
        // Generate the sender
        KeyPair sender = UtilityMethods.generateKeyPair();

        // Create two receivers
        PublicKey[] receivers = new PublicKey[2];
        double[] amountToTransfer = new double[receivers.length];

        for (int i = 0; i < receivers.length; i++) {
            receivers[i] = UtilityMethods.generateKeyPair().getPublic();
            amountToTransfer[i] = (i + 1) * 100;
        }

        // Create the input UTXOs and output UTXOs
        UTXO inputUTXO = new UTXO("0", sender.getPublic(), sender.getPublic(), 1000);
        ArrayList<UTXO> input = new ArrayList<UTXO>();
        input.add(inputUTXO);
        Transaction transaction = new Transaction(sender.getPublic(), receivers, amountToTransfer, input);

        boolean isPreparedOutputUTXO = transaction.prepareOutputUTXOs();
        if (!isPreparedOutputUTXO) {
            System.out.println("Transaction failed");
        } else {
            transaction.signTheTransaction(sender.getPrivate());
            UtilityMethods.displayTransaction(transaction, System.out, 1);
        }
    }
}