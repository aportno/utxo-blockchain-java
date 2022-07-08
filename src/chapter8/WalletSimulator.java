package chapter8;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.*;

public class WalletSimulator extends JFrame {
    protected static MessageFrame messageFrame = new MessageFrame();
    protected static FrameHelp help = new FrameHelp();
    private boolean balanceShowPublicKey;
    private JTextArea textInput;
    private JTextArea displayArea;
    private JButton sentButton;
    private GridBagLayout gbl;
    private GridBagConstraints gbc;
    private Wallet wallet;
    private WalletConnectionAgent connectionAgent;
    private WalletMessageTaskManager taskManager;
    private Calendar calendar = Calendar.getInstance();

    public WalletSimulator(Wallet wallet, WalletConnectionAgent agent, WalletMessageTaskManager manager) {
        super(wallet.getName());
        this.wallet = wallet;
        this.connectionAgent = agent;
        this.taskManager = manager;
        setUpGUI();
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    connectionAgent.isSendMessage(new MessageTextPrivate(Message.TEXT_CLOSE, wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), connectionAgent.getServerAddress()));
                } catch (Exception e) {
                    try {
                        connectionAgent.activeClose();
                        taskManager.close();
                    } catch (Exception e1) {
                        // do nothing
                    }
                    dispose();
                    System.exit(2);
                }
            }
        });
    }

    public boolean showPublicKeyInBalance() {
        return this.balanceShowPublicKey;
    }

    private void setBalanceShowPublicKey(boolean bool) {
        this.balanceShowPublicKey = bool;
    }

    private void setUpGUI() {
        this.setSize(500, 400);
        setBar();

        Container container = getContentPane();
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        container.setLayout(gbl);
        JLabel labelInput = new JLabel("Message Board");
        labelInput.setForeground(Color.GREEN);

        this.displayArea = new JTextArea(50, 100);
        this.textInput = new JTextArea(5, 100);
        this.sentButton = new JButton("Send message");
        this.sentButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MessageTextBroadcast mtb = new MessageTextBroadcast(textInput.getText(), wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName());
                    connectionAgent.isSendMessage(mtb);
                } catch (Exception e1) {
                    System.out.println("Error: " + e1.getMessage());
                    throw new RuntimeException(e1);
                }
                textInput.setText("");
            }
        });

        this.gbc.fill = GridBagConstraints.BOTH;
        this.gbc.weightx = 1;
        this.gbc.weighty = 0.0;
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.gridwidth = 1;
        this.gbc.gridheight = 1;
        this.gbl.setConstraints(labelInput, this.gbc);
        container.add(labelInput);

        this.gbc.weighty = 0.9;
        this.gbc.gridx = 0;
        this.gbc.gridy = 1;
        this.gbc.gridheight = 9;

        JScrollPane scroll = new JScrollPane(this.displayArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.gbl.setConstraints(scroll, this.gbc);
        container.add(scroll);


    }

    private void setBar() {
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        JMenu askMenu = new JMenu("Ask");

        JMenuItem helpItem = new JMenuItem("Help");
        helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelpMessage("1. Update blockchain -> a broadcast message is sent retrieve the latest blockchain from the network to update your local copy with the most recent state.\n"
                    + "2. Update users -> the service provider will update your user list.\n"
                    + "3. Show balance -> displays your balance in the display board\n"
                    + "4. Display blockchain -> displays your local blockchain in the display board.");
            }
        });

        JMenuItem askBlockchainItem = new JMenuItem("Update blockchain");
        askBlockchainItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageAskForBlockchainBroadcast mabb = new MessageAskForBlockchainBroadcast("Ask request",
                        wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName());
                connectionAgent.isSendMessage(mabb);
            }
        });

        JMenuItem askAddressesItem = new JMenuItem("Update users");
        askAddressesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageTextPrivate mtp = new MessageTextPrivate(Message.TEXT_ASK_ADDRESSES, wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), connectionAgent.getServerAddress());
                connectionAgent.isSendMessage(mtp);
            }
        });

        JMenuItem askBalanceItem = new JMenuItem("Display balance");
        askBalanceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBalance(wallet);
            }
        });
    }

    protected void displayBalance(Wallet wallet) {
        StringBuilder sb = new StringBuilder();
        Blockchain ledger = wallet.getLocalLedger();

        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();
        ArrayList<UTXO> unspent = new ArrayList<>();
        ArrayList<UTXO> rewards = new ArrayList<>();
        ArrayList<Transaction> sentTx = new ArrayList<>();

        double balance = ledger.findRelatedUTXOs(wallet.getPublicKey(), all, spent, unspent, sentTx, rewards);
        ArrayList<UTXO> receivedOutputUTXO = new ArrayList<>();
        for (int i = 0; i < sentTx.size(); i++) {
            Transaction tx = sentTx.get(i);
            for (int j = 0; j < tx.getNumberOfOutputUTXOs(); j++) {
                UTXO outputUTXO = tx.getOutputUTXO(j);
                if (!outputUTXO.getReceiver().equals(wallet.getPublicKey())) {
                    receivedOutputUTXO.add(outputUTXO);
                }
            }
        }

        int level = 0;
        displayTab(sb, level, wallet.getName() + ":");
    }

    private void displayUTXOs(StringBuilder sb, ArrayList<UTXO> utxos, int level) {
        for (UTXO each : utxos) {
            if (showPublicKeyInBalance()) {
                displayTab(sb, level, "amount: " + each.getAmountTransferred() + ", receiver: " + UtilityMethods.getKeyString(each.getReceiver()) + ", sender: " + UtilityMethods.getKeyString(each.getSender()));
            } else {
                displayTab(sb, level, "amount: " + each.getAmountTransferred() + ", receiver: " + connectionAgent.getNameFromAddress(each.getReceiver()) + ", sender: " + connectionAgent.getNameFromAddress(each.getSender()));
            }
        }
    }

    private void displayTab(StringBuilder sb, int level, String msg) {
        sb.append("\t".repeat(Math.max(0, level)));
        sb.append(msg);
        sb.append(System.getProperty("line.separator"));
    }
}

class MessageFrame extends javax.swing.JFrame {
    Container container = this.getContentPane();
    javax.swing.JTextArea message = new JTextArea();
    JScrollPane pane = new JScrollPane();

    public MessageFrame() {
        super("Information Board");
        this.setBounds(0, 0, 600, 450);
        JScrollPane pane = new JScrollPane(this.message);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        container.add(pane);
        message.setLineWrap(false);
        message.setRows(100);
        message.setColumns(80);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
            // do nothing;
            }
        });
    }

    public void setMessage(String msg) {
        message.setText(msg);
        this.validate();
        this.setVisible(true);
    }

    public void appendMessage(String msg) {
        message.append(msg);
        this.validate();
        this.setVisible(true);
    }
}

class FrameHelp extends javax.swing.JFrame {
    javax.swing.JTextPane message = new JTextPane();
    public FrameHelp() {
        super("Help");
        Container container = this.getContentPane();
        this.setBounds(500, 500, 300, 220);
        message.setBounds(0, 0, this.getWidth(), this.getHeight());
        container.add(message);
    }

    public void setMessage(String msg) {
        message.setText(msg);
        this.validate();
        this.setVisible(true);
    }
}
