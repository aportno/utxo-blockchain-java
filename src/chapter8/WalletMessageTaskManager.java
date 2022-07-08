package chapter8;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashMap;

public class WalletMessageTaskManager implements Runnable {
    private boolean isServerRunning = true;
    private WalletConnectionAgent agent;
    private Wallet wallet;
    private ConcurrentLinkedQueue<Message> messageConcurrentLinkedQueue;
    private HashMap<String, String> ackTransactions = new HashMap<>();
    private WalletSimulator simulator;

    public WalletMessageTaskManager(WalletConnectionAgent agent, Wallet wallet, ConcurrentLinkedQueue<Message> messageConcurrentLinkedQueue) {
        this.agent = agent;
        this.wallet = wallet;
        this.messageConcurrentLinkedQueue = messageConcurrentLinkedQueue;
    }

    protected Wallet myWallet() {
        return this.wallet;
    }

    public void setSimulator(WalletSimulator simulator) {
        this.simulator = simulator;
    }

    protected void askForLatestBlockchain() {
        MessageAskForBlockchainBroadcast askForLedger = new MessageAskForBlockchainBroadcast("Acknowledged",
                this.wallet.getPrivateKey(), this.wallet.getPublicKey(), this.wallet.getName());
        boolean isSendMsg = this.agent.isSendMessage(askForLedger);
        if (isSendMsg) {
            System.out.println("Sent a message to receive latest blockchain");
        } else {
            System.out.println("ERROR: failed to send a message to receive latest blockchain");
        }
    }

    public void run() {
        try {
            Thread.sleep(agent.sleepTime * 2);
        } catch (Exception e) {
            e.getMessage();
        }

        askForLatestBlockchain();

        while (isServerRunning) {
            if (this.messageConcurrentLinkedQueue.isEmpty()) {
                try {
                    Thread.sleep(this.agent.sleepTime);
                } catch (Exception e) {
                    System.out.println("Error while trying to sleep...");
                    e.printStackTrace();
                    this.close();
                    this.agent.activeClose();
                }
            } else {
                Message msg = this.messageConcurrentLinkedQueue.poll();
                if (msg == null) {
                    System.out.println("Error: message is null");
                } else {
                    try {
                        processMessage(msg);
                    } catch (Exception e) {
                        System.out.println("Error when processing message");
                        e.printStackTrace();
                        this.close();
                        this.agent.activeClose();
                    }
                }
            }
        }
    }

    protected void processMessage(Message message) {
        if (message == null) {
            return;
        } else if (!message.isForBroadcast()) {
            if (message.getMessageType() == Message.TEXT_PRIVATE) {
                MessageTextPrivate messageTextPrivate = (MessageTextPrivate) message;
                if (!messageTextPrivate.isValid()) {
                    System.out.println("Private message text has been tampered");
                    return;
                } else if (!messageTextPrivate.getReceiver().equals(this.wallet.getPublicKey())) {
                    System.out.println("Private text is not for intended user...ignoring it...");
                }

                String text = messageTextPrivate.getMessageBody();
                if (messageTextPrivate.getSenderKey().equals(agent.getServerAddress()) && text.equals(Message.TEXT_CLOSE)) {
                    System.out.println("Server is asking to close connection...closing now...");
                    this.close();
                    agent.close();
                } else {
                    receivePrivateChatMessage(messageTextPrivate);
                }
            } else if (message.getMessageType() == Message.ADDRESS_PRIVATE) {
                MessageAddressPrivate map = (MessageAddressPrivate) message;
                receiveMessageAddressPrivate(map);
            } else if (message.getMessageType() == Message.BLOCKCHAIN_PRIVATE) {
                MessageBlockchainPrivate mbp = (MessageBlockchainPrivate) message;
                receiveMessageBlockchainPrivate(mbp);
            } else {
                System.out.println("");
                System.out.println("...private message not supported, please check...");
                System.out.println("");
            }
        } else if (message.getMessageType() == Message.BLOCK_BROADCAST) {
            System.out.println("Block broadcast message -> check if necessary to update block");
            MessageBlockBroadcast mbb = (MessageBlockBroadcast) message;
            this.receiveMessageBlockBroadcast(mbb);
        } else if (message.getMessageType() == Message.BLOCKCHAIN_BROADCAST) {
            System.out.println("Blockchain broadcast message -> check if necessary to update blockchain");
            MessageBlockchainBroadcast mbb = (MessageBlockchainBroadcast) message;
            boolean isLocalLedgerSet = this.wallet.setLocalLedger(mbb.getMessageBody());
            if (isLocalLedgerSet) {
                System.out.println("Blockchain is updated...");
            } else {
                System.out.println("Rejected the new blockchain...");
            }
        } else if (message.getMessageType() == Message.TRANSACTION_BROADCAST) {
            System.out.println("Transaction broadcast message -> broadcasting accordingly");
            MessageTransactionBroadcast mtb = (MessageTransactionBroadcast) message;
            this.receiveMessageTransactionBroadcast(mtb);
        } else if (message.getMessageType() == Message.BLOCK_ASK_BROADCAST) {
            MessageAskForBlockchainBroadcast mabb = (MessageAskForBlockchainBroadcast) message;
            if (!(mabb.getSenderKey().equals(myWallet().getPublicKey())) && mabb.isValid()) {
                receiveQueryForBlockchainBroadcast(mabb);
            }
        } else if (message.getMessageType() == Message.TEXT_BROADCAST) {
            MessageTextBroadcast mtb = (MessageTextBroadcast) message;
            receiveMessageTextBroadcast(mtb);
        }
    }

    protected void receiveMessageTextBroadcast(MessageTextBroadcast mtb) {
        String msgBody = mtb.getMessageBody();
        String senderName = mtb.getSenderName();
        this.simulator.appendMessageLineOnBoard(senderName + ": " + msgBody);
        agent.addAddress(new KeyNamePair(mtb.getSenderKey(), mtb.getSenderName()));
    }

    protected void receiveMessageAddressPrivate(MessageAddressPrivate map) {
        ArrayList<KeyNamePair> allMsgBody = map.getMessageBody();
        System.out.println("Users available:");
        for (KeyNamePair knp : allMsgBody) {
            if (!knp.getPublicKey().equals(wallet.getPublicKey())) {
                agent.addAddress(knp);
                System.out.println(knp.getPublicKey() + "| key=" + UtilityMethods.getKeyString(knp.getPublicKey()));
            }
        }
    }

    protected void receivePrivateChatMessage(MessageTextPrivate mtp) {
        String msgBody = mtp.getMessageBody();
        String senderName = mtp.getSenderName();
        this.simulator.appendMessageLineOnBoard("Private <- " + senderName + ": " + msgBody);
        agent.addAddress(new KeyNamePair(mtp.getSenderKey(), mtp.getSenderName()));
    }

    protected void receiveQueryForBlockchainBroadcast(MessageAskForBlockchainBroadcast mabb) {
        System.out.println("Wallet instance -> ignoring query on blockchain");
    }

    protected void receiveMessageTransactionBroadcast(MessageTransactionBroadcast mtb) {
        Transaction tx = mtb.getMessageBody();
        if (!this.ackTransactions.containsKey(tx.getHashID())) {
            int totalOutputUTXOs = tx.getNumberOfOutputUTXOs();
            int receivedOutputUTXO = 0;
            for (int i = 0; i < totalOutputUTXOs; i++) {
                UTXO outputUTXO = tx.getOutputUTXO(i);
                if (outputUTXO.getReceiver().equals(this.wallet.getPublicKey())) {
                    receivedOutputUTXO += outputUTXO.getAmountTransferred();
                }
            }

            if (receivedOutputUTXO > 0 && !tx.getSender().equals(myWallet().getPublicKey())) {
                this.ackTransactions.put(tx.getHashID(), tx.getHashID());
                System.out.println("Received: " + receivedOutputUTXO);
                MessageTextPrivate mtp = new MessageTextPrivate("Amount received",
                        this.wallet.getPrivateKey(), this.wallet.getPublicKey(), this.wallet.getName(), tx.getSender());
                this.agent.isSendMessage(mtp);
            }
        }
    }

    protected void receiveMessageBlockBroadcast(MessageBlockBroadcast mbb) {
        Block block = mbb.getMessageBody();
        boolean isUpdatedLocalLedger = this.wallet.isUpdatedLocalLedger(block);
        if (isUpdatedLocalLedger) {
            System.out.println("New block is added to the local blockchain");
        } else {
            int totalBlockTx = block.getTotalNumberOfTransactions();
            int counter = 0;
            for (int i = 0; i < totalBlockTx; i++) {
                Transaction tx = block.getTransaction(i);
                if (!myWallet().getLocalLedger().isTransactionExist(tx)) {
                    MessageTransactionBroadcast mtb = new MessageTransactionBroadcast(tx);
                    this.agent.isSendMessage(mtb);
                    counter++;
                }
            }
            System.out.println("New block rejected -> released " + counter + " unpublished transactions into the pool");
        }
    }

    protected void receiveMessageBlockchainPrivate(MessageBlockchainPrivate mbp) {
        System.out.println("Private message to the chain...");
        if (mbp.getReceiver().equals(myWallet().getPublicKey())) {
            boolean isSetLocalLedger = this.myWallet().setLocalLedger(mbp.getMessageBody());
            if (isSetLocalLedger) {
                System.out.println("Blockchain is updated...");
            } else {
                System.out.println("Rejected the new blockchain...");
            }
        } else {
            System.out.println("ERROR: private message sent to the wrong wallet");
        }
    }

    public void close() {
        isServerRunning = false;
    }
}
