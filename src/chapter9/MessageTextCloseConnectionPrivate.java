package chapter9;

import java.io.Serial;
import java.security.PublicKey;
import java.security.PrivateKey;

public class MessageTextCloseConnectionPrivate extends MessageTextPrivate {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String info = Message.TEXT_CLOSE;

    public MessageTextCloseConnectionPrivate(PrivateKey privateKey, PublicKey senderKey, String senderName, PublicKey receiverKey) {
        super(info, privateKey, senderKey, senderName, receiverKey);
    }

    public int getMessageType() {
        return Message.TEXT_PRIVATE_CLOSE_CONNECTION;
    }
}
