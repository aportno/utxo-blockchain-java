package chapter3;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;

public class TestTransaction {
    public static void main(String[] args) {
        KeyPair sender = UtilityMethods.generateKeyPair();
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

        // Ensure sender has enough funds
        double available = 0.0;
        for (UTXO value : input) {
            available += value.getAmountTransferred();
        }

        // Compute the total cost and add the transaction fee
        double totalCost = transaction.getTotalAmountToTransfer() + Transaction.TRANSACTION_FEE;

        // Abort if there is insufficient funds
        if (available < totalCost) {
            System.out.println("Fund available=" + available + ", not enough for total cost of " + totalCost);
        }

        // Generate the output UTXOs
        for (int i = 0; i < receivers.length; i++) {
            UTXO outputUTXO = new UTXO(transaction.getHashID(), sender.getPublic(), receivers[i], amountToTransfer[i]);
            transaction.addOutputUTXO(outputUTXO);
        }

        // Generate the change as an UTXO to the sender
        double remainingFunds = available - totalCost;
        UTXO remainingUTXO = new UTXO(transaction.getHashID(), sender.getPublic(), sender.getPublic(), remainingFunds);
        transaction.addOutputUTXO(remainingUTXO);

        // Sign the transaction
        transaction.signTheTransaction(sender.getPrivate());

        // Display the transaction to take a look
        displayTransaction(transaction);
    }

    // A method written to display the transaction properly
    private static void displayTransaction(Transaction transaction) {
        System.out.println("Transaction{");
        System.out.println("\tID: " + transaction.getHashID());
        System.out.println("\tSender: " + UtilityMethods.getKeyString(transaction.getSender()));
        System.out.println("\tAmount to be transferred total: " + transaction.getTotalAmountToTransfer());
        System.out.println("\tReceivers:");

        for (int i = 0; i < transaction.getNumberOfOutputUTXOs() - 1; i++) {
            UTXO utxo4 = transaction.getOutputUTXO(i);

            System.out.println("\t\tfund="
                    + utxo4.getAmountTransferred()
                    + ", receiver="
                    + UtilityMethods.getKeyString(utxo4.getReceiver()));
        }

        UTXO utxo5 = transaction.getOutputUTXO(transaction.getNumberOfOutputUTXOs() - 1);

        System.out.println("\tTransaction fee: " + Transaction.TRANSACTION_FEE);
        System.out.println("\tChange: " + utxo5.getAmountTransferred());

        boolean b = transaction.verifySignature();

        System.out.println("\tSignature verification: " + b);
        System.out.println("}");
    }
}
