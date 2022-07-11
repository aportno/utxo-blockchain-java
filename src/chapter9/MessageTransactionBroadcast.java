package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public class MessageTransactionBroadcast extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Transaction transaction;
    private final long timeStamp;

    public MessageTransactionBroadcast(Transaction transaction) {
        this.transaction = transaction;
        timeStamp = UtilityMethods.getTimeStamp();
    }

    public int getMessageType() {
        return Message.TRANSACTION_BROADCAST;
    }

    public Transaction getMessageBody() {
        return this.transaction;
    }

    public String getMessageHashID() {
        return transaction.getHashID();
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public PublicKey getSenderKey() {
        return transaction.getSender();
    }

    public boolean isForBroadcast() {
        return true;
    }

    protected boolean isSelfMessageAllowed() {
        return true;
    }

}
