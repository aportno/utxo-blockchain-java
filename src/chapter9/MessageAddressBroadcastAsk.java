package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public class MessageAddressBroadcastAsk extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final long timeStamp;
    private final PublicKey sender;
    private final String walletName;
    private final String uniqueHashID;

    public MessageAddressBroadcastAsk(PublicKey sender, String walletName) {
        this.sender = sender;
        this.walletName = walletName;
        timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(sender) + timeStamp + UtilityMethods.getUniqueNumber();
        uniqueHashID = UtilityMethods.messageDigestSHA256_toString(msg + walletName);
    }

    @Override
    public Object getMessageBody() {
        return "Request peer information";
    }

    @Override
    public int getMessageType() {
        return Message.ADDRESS_ASK_BROADCAST;
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

    @Override
    public boolean isForBroadcast() {
        return true;
    }

    public String getWalletName() {
        return walletName;
    }
}
