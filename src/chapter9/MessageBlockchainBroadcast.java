package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public class MessageBlockchainBroadcast extends Message {
    @Serial
    private final static long serialVersionUID = 1L;
    private final Blockchain ledger;
    private final PublicKey sender;
    private final String uniqueHashID;
    private final long timeStamp;

    public MessageBlockchainBroadcast(Blockchain ledger, PublicKey sender) {
        this.ledger = ledger;
        this.sender = sender;
        int initialSize = ledger.getBlockchainSize();
        timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(sender) + timeStamp + UtilityMethods.getUniqueNumber();
        uniqueHashID = UtilityMethods.messageDigestSHA256_toString(msg);
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

    public Blockchain getMessageBody() {
        return ledger;
    }

    public PublicKey getSender() {
        return sender;
    }

    public int getMessageType() {
        return Message.BLOCKCHAIN_BROADCAST;
    }

    public boolean isForBroadcast() {
        return true;
    }
}
