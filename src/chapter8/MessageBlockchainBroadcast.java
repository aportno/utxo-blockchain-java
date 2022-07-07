package chapter8;

import java.io.Serial;
import java.security.PublicKey;

public class MessageBlockchainBroadcast extends Message {
    @Serial
    private final static long serialVersionUID = 1L;
    private final Blockchain ledger;
    private final PublicKey sender;
    private final int initialSize;

    public MessageBlockchainBroadcast(Blockchain ledger, PublicKey sender) {
        this.ledger = ledger;
        this.sender = sender;
        this.initialSize = ledger.getBlockchainSize();
    }

    public Blockchain getMessageBody() {
        return ledger;
    }

    public PublicKey getSender() {
        return sender;
    }

    public int getInfoSize() {
        return initialSize;
    }

    public int getMessageType() {
        return Message.BLOCKCHAIN_BROADCAST;
    }

    public boolean isForBroadcast() {
        return true;
    }
}
