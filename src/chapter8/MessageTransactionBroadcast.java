package chapter8;

import java.io.Serial;

public class MessageTransactionBroadcast extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Transaction transaction;

    public MessageTransactionBroadcast(Transaction transaction) {
        this.transaction = transaction;
    }

    public int getMessageType() {
        return Message.TRANSACTION_BROADCAST;
    }

    public Transaction getMessageBody() {
        return this.transaction;
    }

    public boolean isForBroadcast() {
        return true;
    }
}
