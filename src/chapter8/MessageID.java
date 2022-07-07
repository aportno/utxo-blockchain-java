package chapter8;

import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MessageID extends MessageSigned {
    /*
    Used to send the name and public key of a wallet to the message service provider
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private final String info;
    private final byte[] signature;
    private final PublicKey sender;
    private final String walletName;

    public MessageID(PrivateKey privateKey, PublicKey sender, String walletName) {
        this.info = Message.JCOIN_MESSAGE;
        this.sender = sender;
        this.walletName = walletName;
        signature = UtilityMethods.generateSignature(privateKey, this.info);
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

    public boolean isValid() {
        return UtilityMethods.verifySignature(this.getPublicKey(), signature, this.info);
    }

    public boolean isForBroadcast() {
        return false;
    }
}
