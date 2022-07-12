package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public class UTXO implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String hashID;
    private final String parentTransactionID;
    private final PublicKey receiver;
    private final PublicKey sender;
    private final long timestamp;
    private final double amountTransferred;
    private final long sequentialNumber;

    public UTXO(String parentTransactionID, PublicKey sender, PublicKey receiver, double amountToTransfer) {
        this.sequentialNumber = UtilityMethods.getUniqueNumber();
        this.parentTransactionID = parentTransactionID;
        this.receiver = receiver;
        this.sender = sender;
        this.amountTransferred = amountToTransfer;
        this.timestamp = UtilityMethods.getTimeStamp();
        this.hashID = computeHashID();
    }

    protected String computeHashID() {
        String message = this.parentTransactionID
                + UtilityMethods.getKeyString(this.sender)
                + UtilityMethods.getKeyString(receiver)
                + Double.toHexString(this.amountTransferred)
                + Long.toHexString(this.timestamp)
                + Long.toHexString(this.sequentialNumber);
        return UtilityMethods.messageDigestSHA256_toString(message);
    }

    public boolean equals(UTXO utxo) {
        return this.getHashID().equals(utxo.getHashID());
    }

    public String getHashID() {
        return this.hashID;
    }

    public PublicKey getReceiver() { return this.receiver; }

    public PublicKey getSender() {
        return this.sender;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public double getAmountTransferred() {
        return this.amountTransferred;
    }
}
