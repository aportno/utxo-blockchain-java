package chapter9;

import java.io.Serial;
import java.security.PublicKey;
import java.security.PrivateKey;

public class MessageBroadcastMakingFriend extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String info;
    private final PublicKey sender;
    private final String senderName;
    private final String uniqueHashID;
    private final long timeStamp;
    private final String ipAddress;

    public MessageBroadcastMakingFriend(PrivateKey privateKey, PublicKey senderKey, String senderName, String ipAddress) {
        this.sender = senderKey;
        this.senderName = senderName;
        this.ipAddress = ipAddress;
        info = Message.JCOIN_MESSAGE;
        byte[] signature = UtilityMethods.generateSignature(privateKey, info);
        timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(senderKey) + senderName + UtilityMethods.getUniqueNumber() + timeStamp;
        uniqueHashID = UtilityMethods.messageDigestSHA256_toString(msg);
    }

    @Override
    public Object getMessageBody() {
        return info;
    }

    @Override
    public int getMessageType() {
        return Message.ADDRESS_BROADCAST_MAKING_FRIEND;
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
    public boolean isForBroadcast() { return true; }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public KeyNamePair getKeyNamePair() {
        return new KeyNamePair(getSenderKey(), getSenderName());
    }
}
