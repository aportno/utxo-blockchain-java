package chapter9;

import java.io.Serial;

public abstract class MessageSigned extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    public abstract boolean isValid();
}
