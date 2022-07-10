package chapter9;

import java.io.Serial;
import java.security.PublicKey;
import java.util.ArrayList;

public class MessageAddressPrivate extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<KeyNamePair> addresses;
    private final PublicKey sender;
    private final PublicKey receiver;
    private final String uniqueHashID;
    private final long timeStamp;

    public MessageAddressPrivate(ArrayList<KeyNamePair> addresses, PublicKey sender, PublicKey receiver) {
        this.addresses = addresses;
        this.sender = sender;
        this.receiver = receiver;
        this.timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(sender) + UtilityMethods.getKeyString(receiver) + timeStamp + UtilityMethods.getUniqueNumber();
        uniqueHashID = UtilityMethods.messageDigestSHA256_toString(msg);
    }

    public int getMessageType() {
        return Message.ADDRESS_PRIVATE;
    }

    public ArrayList<KeyNamePair> getMessageBody() {
        return addresses;
    }

    public boolean isForBroadcast() {
        return false;
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

    public PublicKey getReceiverKey() {
        return receiver;
    }
}
