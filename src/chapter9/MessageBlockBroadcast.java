package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public class MessageBlockBroadcast extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Block block;
    private PublicKey sender;
    private long timeStamp;

    public MessageBlockBroadcast(Block block) {
        this.block = block;
    }

    public int getMessageType() {
        return Message.BLOCK_BROADCAST;
    }

    public Block getMessageBody() {
        return block;
    }

    public boolean isForBroadcast() {
        return true;
    }

    @Override
    public String getMessageHashID() {
        return block.getHashID();
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public PublicKey getSenderKey() {
        return sender;
    }

    protected boolean isSelfMessageAllowed() {
        return true;
    }
}
