package chapter2;

public class TestBlockMining {
    public static void main(String[] args) {
        Block block = new Block("0", 25);

        // Creating 10 test transactions to include in the block
        for (int t=0; t<10; t++) {
            block.addTransaction("Transaction" + t);
        }

        System.out.println("Mining the block...");
        block.mineTheBlock();
        System.out.println("Block is successfully mined! Hash ID is: ");
        System.out.println(block.getHashID());
    }
}
