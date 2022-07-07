package chapter8;

import java.io.Serial;

public class MessageBlockBroadcast extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Block block;

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
}
