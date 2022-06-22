package chapter3;

public class TestBlockMining {
    public static void main(String[] args) {
        Block block = new Block("0", 10);

        for (int num_transaction = 0; num_transaction < 10; num_transaction++) {
            block.addTransaction("Transaction" + num_transaction);
        }

        System.out.println("Mining the block...");
        block.mineTheBlock();
        System.out.println("Block is successfully mined! Hash ID is: ");
        System.out.println(block.getHashID());
    }
}
