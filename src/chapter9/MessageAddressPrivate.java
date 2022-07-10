package chapter9;

import java.io.Serial;
import java.util.ArrayList;

public class MessageAddressPrivate extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<KeyNamePair> addresses;

    public MessageAddressPrivate(ArrayList<KeyNamePair> addresses) {
        this.addresses = addresses;
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
}
