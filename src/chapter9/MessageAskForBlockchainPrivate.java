package chapter9;

import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MessageAskForBlockchainPrivate extends MessageTextPrivate {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String uniqueHashID;
    private final long timeStamp;
    private final boolean required;

    public MessageAskForBlockchainPrivate(String info, PrivateKey privateKey, PublicKey senderKey, String senderName, PublicKey receiverKey, boolean required) {
        super(info, privateKey, senderKey, senderName, receiverKey);
        timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(senderKey) + senderName + timeStamp + UtilityMethods.getUniqueNumber();
        uniqueHashID = UtilityMethods.messageDigestSHA256_toString(msg);
        this.required = required;
    }

    @Override
    public String getMessageHashID() {
        return uniqueHashID;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public int getMessageType() {
        return Message.BLOCKCHAIN_ASK_PRIVATE;
    }

    public boolean isRequired() {
        return required;
    }
}
