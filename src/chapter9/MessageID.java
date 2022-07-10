package chapter9;

import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class MessageID extends MessageSigned {
    /*
    Used to send the name and public key of a wallet to the message service provider
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private final byte[] signature;
    private final PublicKey sender;
    private final String walletName;
    private final String info;
    private final String uniqueHashID;
    private long timeStamp = 0;

    public MessageID(PrivateKey privateKey, PublicKey sender, String walletName) {
        this.info = Message.JCOIN_MESSAGE;
        this.sender = sender;
        this.walletName = walletName;
        signature = UtilityMethods.generateSignature(privateKey, this.info);
        this.timeStamp = UtilityMethods.getTimeStamp();
        String msg = UtilityMethods.getKeyString(sender) + walletName + UtilityMethods.getUniqueNumber() + timeStamp;
        this.uniqueHashID = UtilityMethods.messageDigestSHA256_toString(msg);
    }

    public String getMessageBody() {
        return this.info;
    }

    public int getMessageType() {
        return Message.ID;
    }

    public PublicKey getPublicKey() {
        return this.sender;
    }

    public String getName() {
        return this.walletName;
    }

    public KeyNamePair getKeyNamePair() {
        return new KeyNamePair(this.getPublicKey(), this.getName());
    }

    public String getMessageHashID() {
        return uniqueHashID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public PublicKey getSenderKey() {
        return sender;
    }

    public boolean isValid() {
        return UtilityMethods.verifySignature(this.getPublicKey(), signature, this.info);
    }

    public boolean isForBroadcast() {
        return false;
    }
}
