package chapter9;

import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MessageAskForBlockchainBroadcast extends MessageTextBroadcast {
    @Serial
    private final static long serialVersionUID = 1L;
    private final String uniqueHashID;
    private final long timeStamp;

    public MessageAskForBlockchainBroadcast(String info, PrivateKey privateKey, PublicKey sender, String walletName) {
        super(info, privateKey, sender, walletName);
        timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(sender) + walletName + timeStamp + UtilityMethods.getUniqueNumber();
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

    public int getMessageType() {
        return Message.BLOCK_ASK_BROADCAST;
    }
}
