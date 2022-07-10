package chapter9;

import java.io.Serial;
import java.util.ArrayList;

public class LedgerList<E> implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<E> list;

    public LedgerList() { list = new ArrayList<>(); }

    public int size() {
        return this.list.size();
    }

    public E getLast() {
        return this.list.get(size() - 1);
    }

    public E getFirst() {
        return this.list.get(0);
    }

    public boolean add(E e) { return this.list.add(e); }

    public E findByIndex(int index) {
        return this.list.get(index);
    }
}
