package chapter4;

import java.util.ArrayList;

public class Block implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<String> transactions = new ArrayList<String>();
    private String hashID;
    private String previousBlockHashID;
    private long timestamp;
    private int nonce = 0;
    private int difficultyLevel = 20;

    public Block(String previousBlockHashID, int difficultyLevel) {
        this.previousBlockHashID = previousBlockHashID;
        this.timestamp = UtilityMethods.getTimeStamp();
        this.difficultyLevel = difficultyLevel;
    }

    protected String computeHashID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.previousBlockHashID).append(Long.toHexString(this.timestamp));
        for (String transaction : transactions) {
            sb.append(transaction);
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

    public void addTransaction (String transaction) {
        this.transactions.add(transaction);
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
