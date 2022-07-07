package chapter8;

import java.io.Serial;
import java.security.PublicKey;
import java.security.PrivateKey;

public class MessageAskForBlockchainBroadcast extends MessageTextBroadcast {
    @Serial
    private final static long serialVersionUID = 1L;

    public MessageAskForBlockchainBroadcast(String info, PrivateKey privateKey, PublicKey senderKey, String senderName) {
        super(info, privateKey, senderKey, senderName);
    }

    public int getMessageType() {
        return Message.BLOCK_ASK_BROADCAST;
    }
}
