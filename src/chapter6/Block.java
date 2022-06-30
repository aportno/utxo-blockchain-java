package chapter6;

import java.io.Serial;
import java.util.ArrayList;

public class Block implements java.io.Serializable {
    public final static int TRANSACTION_UPPER_LIMIT = 2;
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private String hashID;
    private final String previousBlockHashID;
    private final long timestamp;
    private int nonce = 0;
    private final int difficultyLevel;

    public Block(String previousBlockHashID, int difficultyLevel) {
        this.previousBlockHashID = previousBlockHashID;
        this.timestamp = UtilityMethods.getTimeStamp();
        this.difficultyLevel = difficultyLevel;
    }

    public boolean addTransaction (Transaction transaction) {
        if (this.getTotalNumberOfTransactions() >= Block.TRANSACTION_UPPER_LIMIT) {
            return false;
        } else {
            this.transactions.add(transaction);
            return true;
        }
    }

    protected String computeHashID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.previousBlockHashID).append(Long.toHexString(this.timestamp));
        for (Transaction transaction : transactions) {
            sb.append(transaction.getHashID());
        }
        sb.append(Integer.toHexString(this.difficultyLevel)).append(nonce);
        byte[] b = UtilityMethods.messageDigestSHA256_toBytes(sb.toString());
        return UtilityMethods.toBinaryString(b);
    }

    protected boolean mineTheBlock() {
        this.hashID = this.computeHashID();
        while (!UtilityMethods.hashMeetsDifficultyLevel(this.hashID, this.difficultyLevel)) {
            this.nonce++;
            this.hashID = this.computeHashID();
        }
        return true;
    }

    public Transaction getTransaction(int index) {
        return this.transactions.get(index);
    }

    public int getTotalNumberOfTransactions() {
        return this.transactions.size();
    }

    public int getDifficultyLevel() {
        return this.difficultyLevel;
    }

    public int getNonce() {
        return this.nonce;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getPreviousBlockHashID() {
        return this.previousBlockHashID;
    }

    public String getHashID() {
        return this.hashID;
    }
}
