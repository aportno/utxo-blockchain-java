package chapter5;

import java.io.Serial;
import java.util.ArrayList;

public class LedgerList<Transactions> implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<Transactions> list;

    public LedgerList() {
        list = new ArrayList<>();
    }

    public int size() {
        return this.list.size();
    }

    public Transactions getLast() {
        return this.list.get(size() - 1);
    }

    public Transactions getFirst() {
        return this.list.get(0);
    }

    public boolean add(Transactions transaction) {
        return this.list.add(transaction);
    }

    public Transactions findByIndex(int index) {
        return this.list.get(index);
    }
}
