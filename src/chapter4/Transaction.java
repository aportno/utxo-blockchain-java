package chapter4;

import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String hashID;
    private PublicKey sender;
    private PublicKey[] receivers;
    private double[] amountToTransfer;
    private long timestamp;
    private long mySequentialNumber;
    private ArrayList<UTXO> inputs = new ArrayList<UTXO>();
    private ArrayList<UTXO> outputs = new ArrayList<UTXO>(4);
    private byte[] signature = null;
    private boolean signed = false;
    public static final double TRANSACTION_FEE = 1.0;

    public Transaction(PublicKey sender, PublicKey receiver, double amountToTransfer, ArrayList<UTXO> inputs) {
        PublicKey[] publicKeys = new PublicKey[1];
        publicKeys[0] = receiver;
        double[] funds = new double[1];
        funds[0] = amountToTransfer;
        this.setUp(sender, publicKeys, funds, inputs);
    }

    public Transaction(PublicKey sender, PublicKey[] receivers, double[] amountToTransfer, ArrayList<UTXO> inputs) {
        this.setUp(sender, receivers, amountToTransfer, inputs);
    }

    private void setUp(PublicKey sender, PublicKey[] receivers, double[] amountToTransfer, ArrayList<UTXO> inputs) {
        this.mySequentialNumber = UtilityMethods.getUniqueNumber();
        this.sender = sender;
        this.receivers = new PublicKey[1];
        this.receivers = receivers;
        this.amountToTransfer = amountToTransfer;
        this.inputs = inputs;
        this.timestamp = java.util.Calendar.getInstance().getTimeInMillis();

        computeHashID();
    }

    public void signTheTransaction(PrivateKey privateKey) {
        if(this.signature == null && !signed) {
            this.signature = UtilityMethods.generateSignature(privateKey, getMessageData());
            signed = true;
        }
    }

    public boolean verifySignature() {
        String message = getMessageData();
        return UtilityMethods.verifySignature(this.sender, this.signature, message);
    }

    protected void computeHashID() {
        String message = getMessageData();
        this.hashID = UtilityMethods.messageDigestSHA256_toString(message);
    }

    protected void addOutputUTXO(UTXO utxo) {
        if(!signed) {
            outputs.add(utxo);
        }
    }

    public boolean equals(Transaction T) {
        return this.getHashID().equals(T.getHashID());
    }

    private String getMessageData() {
        StringBuilder sb = new StringBuilder();
        sb.append(UtilityMethods.getKeyString(sender))
                .append(Long.toHexString(this.timestamp))
                .append(Long.toString(this.mySequentialNumber));
        for (int i = 0; i < this.receivers.length; i++) {
            sb.append(UtilityMethods.getKeyString(this.receivers[i])).append(Double.toHexString(this.amountToTransfer[i]));
        }
        for (int i = 0; i < this.getNumberOfInputUTXOs(); i++) {
            UTXO ut = this.getInputUTXO(i);
            sb.append(ut.getHashID());
        }
        return sb.toString();
    }

    public String getHashID() {
        return this.hashID;
    }

    public PublicKey getSender() {
        return this.sender;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getMySequentialNumber() {
        return this.mySequentialNumber;
    }

    public double getTotalAmountToTransfer() {
        double f = 0;
        for (int i = 0; i < this.amountToTransfer.length; i++) {
            f += this.amountToTransfer[i];
        }
        return f;
    }

    public int getNumberOfOutputUTXOs() {
        return this.outputs.size();
    }

    public UTXO getOutputUTXO(int i) {
        return this.outputs.get(i);
    }

    public int getNumberOfInputUTXOs() {
        if (this.inputs == null) {
            return 0;
        } else {
            return this.inputs.size();
        }
    }

    public UTXO getInputUTXO(int i) {
        return this.inputs.get(i);
    }
}
