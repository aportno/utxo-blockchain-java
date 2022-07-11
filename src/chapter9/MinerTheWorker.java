package chapter9;

import java.util.ArrayList;

/*
This thread is dedicated to mine a block. It avoids continuing a time-consuming process such as mining a block that has
already been finished by another miner and broadcast to the network.
 */

public class MinerTheWorker implements Runnable {
    private Miner miner;
    private PeerConnectionManager agent;
    private MinerMessageTaskManager manager;
    private boolean isKeepMining = true;
    private ArrayList<Transaction> existingTransactions;

    public MinerTheWorker(Miner miner, MinerMessageTaskManager manager, PeerConnectionManager agent, ArrayList<Transaction> existingTransaction) {
        this.miner = miner;
        this.manager = manager;
        this.agent = agent;
        this.existingTransactions = existingTransaction;
    }

    public void run() {
        final long breakTime = 2;
        System.out.println("Miner " + miner.getName() + " started.");
        Block block = miner.createNewBlock(miner.getLocalLedger(), Configuration.getBlockMiningDifficultyLevel());

        for (Transaction each : existingTransactions) {
            miner.isAddedTransaction(each, block);
        }

        miner.isGeneratedRewardTransaction(block);
        try {
            Thread.sleep(breakTime);
        } catch (Exception e) {
            // do nothing
        }

        if (!isKeepMining) {
            manager.resetMiningAction();
            return;
        }

        boolean isMinedBlock = miner.isMinedBlock(block);
        if (isMinedBlock) {
            System.out.println(miner.getName() + " mined and signed the block -> hash ID: " + block.getHashID());
        } else {
            System.out.println(miner.getName() + " block completed by another miner -> miner abandoning current job");
            manager.resetMiningAction();
            return;
        }

        try {
            Thread.sleep(breakTime);
        } catch (Exception e2) {
            // do nothing
        }

        if (!isKeepMining) {
            manager.resetMiningAction();
            return;
        }

        MessageBlockBroadcast mbb = new MessageBlockBroadcast(block, miner.getPublicKey());
        this.agent.sendMessageByAll(mbb);
        manager.resetMiningAction();
    }

    protected void abort() {
        this.isKeepMining = false;
    }
}
