package chapter8;

import java.io.Serial;
import java.security.PublicKey;
import java.security.PrivateKey;

public class MessageTextPrivate extends MessageSigned {
    @Serial
    private static final long serialVersionUID = 1L;
    private final byte[] signature;
    private final PublicKey senderKey;
    private final PublicKey receiver;
    private final String senderName;
    private final String info;

    public MessageTextPrivate(String info, PrivateKey privateKey, PublicKey senderKey, String senderName, PublicKey receiverKey) {
        this.info = info;
        signature = UtilityMethods.generateSignature(privateKey, this.info);
        this.senderKey = senderKey;
        this.receiver = receiverKey;
        this.senderName = senderName;
    }

    public String getMessageBody() {
        return info;
    }

    public int getMessageType() {
        return Message.TEXT_PRIVATE;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public PublicKey getSenderKey() {
        return senderKey;
    }

    public String getSenderName() {
        return senderName;
    }

    public KeyNamePair getSenderKeyNamePair() {
        return new KeyNamePair(this.getSenderKey(), this.senderName);
    }

    public boolean isValid() {
        return UtilityMethods.verifySignature(senderKey, signature, info);
    }

    public boolean isForBroadcast() {
        return false;
    }
}
