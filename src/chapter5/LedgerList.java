package chapter5;

import java.io.Serial;
import java.util.ArrayList;

public class LedgerList<Data> implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<Data> list;

    public LedgerList() {
        list = new ArrayList<>();
    }

    public int size() {
        return this.list.size();
    }

    public Data getLast() {
        return this.list.get(size() - 1);
    }

    public Data getFirst() {
        return this.list.get(0);
    }

    public boolean add(Data data) {
        return this.list.add(data);
    }

    public Data findByIndex(int index) {
        return this.list.get(index);
    }
}
