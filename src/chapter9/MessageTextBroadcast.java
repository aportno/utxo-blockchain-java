package chapter9;

import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MessageTextBroadcast extends MessageSigned {
    @Serial
    private static final long serialVersionUID = 1L;
    private final byte[] signature;
    private final PublicKey senderKey;
    private final String senderName;
    private final String info;

    public MessageTextBroadcast(String info, PrivateKey privateKey, PublicKey senderKey, String senderName) {
        this.info = info;
        signature = UtilityMethods.generateSignature(privateKey, this.info);
        this.senderKey = senderKey;
        this.senderName = senderName;
    }

    public String getMessageBody() {
        return info;
    }

    public int getMessageType() {
        return Message.TEXT_BROADCAST;
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
        return true;
    }
}
