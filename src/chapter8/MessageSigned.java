package chapter8;

public abstract class MessageSigned extends Message {
    private static final long serialVersionUID = 1L;
    public abstract boolean isValid();
}
