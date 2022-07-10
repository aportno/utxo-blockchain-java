package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public class MessageBlockchainPrivate extends Message implements java.io.Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    private final Blockchain ledger;
    private final PublicKey sender;
    private final PublicKey receiver;
    private final int initialSize;
    private final String uniqueHashID;
    private final long timeStamp;

    public MessageBlockchainPrivate(Blockchain ledger, PublicKey sender, PublicKey receiver) {
        this.ledger = ledger;
        this.sender = sender;
        this.receiver = receiver;
        this.initialSize = this.ledger.getBlockchainSize();
        timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(sender) + timeStamp + UtilityMethods.getUniqueNumber();
        uniqueHashID = UtilityMethods.messageDigestSHA256_toString(msg);
    }

    public int getInfoSize() {
        return initialSize;
    }

    public int getMessageType() {
        return Message.BLOCKCHAIN_PRIVATE;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public Blockchain getMessageBody() {
        return ledger;
    }

    public PublicKey getSender() {
        return sender;
    }

    public boolean isForBroadcast() {
        return false;
    }

    @Override
    public String getMessageHashID() {
        return uniqueHashID;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public PublicKey getSenderKey() {
        return sender;
    }
}
