package chapter7;

import java.io.Serial;

public class SimpleTextMessage implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String senderName;
    private final String msg;

    public SimpleTextMessage(String senderName, String msg) {
        this.senderName = senderName;
        this.msg = msg;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMsg() {
        return msg;
    }
}
