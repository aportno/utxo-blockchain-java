package chapter9;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.PublicKey;
import java.util.*;

public class WalletSimulator extends JFrame {
    protected static MessageFrame messageFrame = new MessageFrame();
    protected static FrameHelp help = new FrameHelp();
    private boolean balanceShowPublicKey;
    private JTextArea textInput;
    private JTextArea displayArea;
    private JButton sentButton;
    private GridBagLayout gbl;
    private GridBagConstraints gbc;
    private final Wallet wallet;
    private final PeerConnectionManager connectionManager;
    private final WalletMessageTaskManager messageManager;
    private final Calendar calendar = Calendar.getInstance();

    public WalletSimulator(Wallet wallet, PeerConnectionManager agent, WalletMessageTaskManager manager) {
        super(wallet.getName());
        this.wallet = wallet;
        this.connectionManager = agent;
        this.messageManager = manager;

        setUpGUI();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    con
                } catch (Exception e1) {
                    try {
                        connectionManager.shutdownAll();
                        messageManager.close();
                    } catch (Exception e2) {
                        // do nothing
                    }
                    LogManager.log(Configuration.getLogBarMax(), wallet.getName() + " is shutting down");
                    dispose();
                    System.exit(2);
                }
            }
        });
    }

    public boolean showPublicKeyInBalance() {
        return this.balanceShowPublicKey;
    }

    protected void appendMessageLineOnBoard(String message) {
        String timeStamp = calendar.getTime().toString();
        this.displayArea.append("(" + timeStamp + ") " + message + System.getProperty("line.separator"));
        this.displayArea.setCaretPosition(this.displayArea.getText().length());
    }

    protected void setBalanceShowPublicKey(boolean bool) {
        this.balanceShowPublicKey = bool;
    }

    private void setUpGUI() {
        this.setSize(500, 600);
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
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    MessageTextBroadcast mtb = new MessageTextBroadcast(
                            textInput.getText(), wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName());
                    connectionManager.sendMessageByAll(mtb);
                    appendMessageLineOnBoard(wallet.getName() + ":" + textInput.getText());
                } catch (Exception e2) {
                    LogManager.log(Configuration.getLogBarMax(), "Exception in WalletSimulator.sentButton.addActionListener" + e2.getMessage());
                    throw new RuntimeException(e2);
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

        JScrollPane displayAreaScroll = new JScrollPane(this.displayArea);
        displayAreaScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        displayAreaScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.gbl.setConstraints(displayAreaScroll, this.gbc);
        container.add(displayAreaScroll);

        this.displayArea.setEditable(false);
        this.displayArea.setBackground(Color.LIGHT_GRAY);
        this.displayArea.setLineWrap(true);
        this.displayArea.setWrapStyleWord(true);

        this.gbc.weighty = 0.0;
        this.gbc.gridx = 0;
        this.gbc.gridy = 11;
        this.gbc.gridheight = 1;
        this.gbl.setConstraints(this.sentButton, this.gbc);
        container.add(this.sentButton);

        this.gbc.weighty = 0.1;
        this.gbc.gridx = 0;
        this.gbc.gridy = 12;
        this.gbc.gridheight = 2;
        JScrollPane textInputScroll = new JScrollPane(this.textInput);
        textInputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        textInputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.gbl.setConstraints(textInputScroll, this.gbc);
        container.add(textInputScroll);

        this.textInput.setLineWrap(true);
        this.textInput.setWrapStyleWord(true);
        this.textInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // do nothing
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown() || e.isControlDown()) {
                        textInput.append(System.getProperty("line.separator"));
                    } else {
                        try {
                            MessageTextBroadcast mtb = new MessageTextBroadcast(textInput.getText(), wallet.getPrivateKey(),
                                    wallet.getPublicKey(), wallet.getName());
                            connectionManager.sendMessageByAll(mtb);
                        } catch (Exception e1) {
                            LogManager.log(Configuration.getLogBarMax(), "Exception in WalletSimulator.textInput.addKeyListener" + e1.getMessage());
                        }
                        e.consume();
                        textInput.setText("");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });
        this.setVisible(true);
    }

    private void setBar() {
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        JMenu askMenu = new JMenu("Options");

        JMenuItem askMenuHelpItem = new JMenuItem("Help");
        askMenuHelpItem.addActionListener(e -> showHelpMessage(
                """
                        1. Update blockchain -> a broadcast message is sent retrieve the latest blockchain from the network to update your local copy with the most recent state.
                        2. Update users -> the service provider will update your user list.
                        3. Show balance -> displays your balance in the display board
                        4. Display blockchain -> displays your local blockchain in the display board."""));

        JMenuItem askBlockchainItem = new JMenuItem("Update blockchain");
        askBlockchainItem.addActionListener(e -> {
            connectionManager.broadcastRequestForBlockchainUpdate();
        });

        JMenuItem askAddressesItem = new JMenuItem("Update users");
        askAddressesItem.addActionListener(e -> {
            MessageAddressBroadcastAsk maba = new MessageAddressBroadcastAsk(wallet.getPublicKey(), wallet.getName());
            LogManager.log(Configuration.getLogBarMin(), "Updating user list");
            connectionManager.sendMessageByAll(maba);
        });

        JMenuItem askBalanceItem = new JMenuItem("Display balance");
        askBalanceItem.addActionListener(e -> displayBalance(wallet));

        JMenuItem displayBlockchain = new JMenuItem("Display blockchain");
        displayBlockchain.addActionListener(e -> displayBlockchain(wallet));

        JMenuItem makingFriendsItem = new JMenuItem("Making friends");
        makingFriendsItem.addActionListener(e -> {
            connectionManager.makingFriends();
        });

        askMenu.add(askMenuHelpItem);
        askMenu.add(askBlockchainItem);
        askMenu.add(askAddressesItem);
        askMenu.add(askBalanceItem);
        askMenu.add(displayBlockchain);
        askMenu.add(makingFriendsItem);
        bar.add(askMenu);

        JMenu sendMenu = new JMenu("Send");
        JMenuItem sendMenuHelpItem = new JMenuItem("Help");
        sendMenuHelpItem.addActionListener(e -> showHelpMessage("1. Start a transaction -> choose recipient(s) and amount to transfer.\n"
                + "2. Private message -> broadcast to message board but only visible by intended recipient."));

        JMenuItem sendTransactionItem = new JMenuItem("Start a transaction");
        sendTransactionItem.addActionListener(e -> {
            FrameTransaction ft = new FrameTransaction(connectionManager.getAllStoredAddresses(), connectionManager);
        });

        JMenuItem sendPrivateMessageItem = new JMenuItem("Send a private message");
        sendPrivateMessageItem.addActionListener(e -> {
            FramePrivateMessage fpm = new FramePrivateMessage(connectionManager.getAllStoredAddresses(),
                    connectionManager, WalletSimulator.this);
        });

        sendMenu.add(sendMenuHelpItem);
        sendMenu.add(sendTransactionItem);
        sendMenu.add(sendPrivateMessageItem);
        bar.add(sendMenu);
    }

    protected static void showHelpMessage(String message) {
        help.setMessage(message);
    }

    protected void displayBlockchain(Wallet wallet) {
        StringBuilder sb = new StringBuilder();
        UtilityMethods.displayBlockchain(wallet.getLocalLedger(), sb, 0);
        messageFrame.setMessage(sb.toString());
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
        for (Transaction each : sentTx) {
            for (int j = 0; j < each.getNumberOfOutputUTXOs(); j++) {
                UTXO outputUTXO = each.getOutputUTXO(j);
                if (!outputUTXO.getReceiver().equals(wallet.getPublicKey())) {
                    receivedOutputUTXO.add(outputUTXO);
                }
            }
        }

        int level = 0;
        displayTab(sb, level, wallet.getName() + ":");
        displayTab(sb, level + 1, "All UTXOs");
        displayUTXOs(sb, all, level + 2);
        displayTab(sb, level + 1, "Spent UTXOs");
        displayUTXOs(sb, spent, level + 2);
        displayTab(sb, level + 1, "Unspent UTXOs");
        displayUTXOs(sb, unspent, level + 2);

        if (wallet instanceof Miner) {
            displayTab(sb, level + 1, "Mining Rewards:");
            displayUTXOs(sb, rewards, level + 2);
        }

        displayTab(sb, level + 1, "Paid UTXOs");
        displayUTXOs(sb, receivedOutputUTXO, level + 2);
        displayTab(sb, level + 1, "Paid Transaction Fee:");
        displayTab(sb, level + 2, "" + (sentTx.size() * Transaction.TRANSACTION_FEE));
        displayTab(sb, level + 1, "Balance: " + balance);
        String msgStr = sb.toString();
        messageFrame.setMessage(msgStr);
    }

    private void displayUTXOs(StringBuilder sb, ArrayList<UTXO> utxos, int level) {
        for (UTXO each : utxos) {
            if (showPublicKeyInBalance()) {
                displayTab(sb, level, "amount: " + each.getAmountTransferred() + ", receiver: " + UtilityMethods.getKeyString(each.getReceiver()) + ", sender: " + UtilityMethods.getKeyString(each.getSender()));
            } else {
                displayTab(sb, level, "amount: " + each.getAmountTransferred() + ", receiver: " + connectionManager.getNameFromAddress(each.getReceiver()) + ", sender: " + connectionManager.getNameFromAddress(each.getSender()));
            }
        }
    }

    private void displayTab(StringBuilder sb, int level, String msg) {
        sb.append("\t".repeat(Math.max(0, level)));
        sb.append(msg);
        sb.append(System.getProperty("line.separator"));
    }

    private static boolean isInvalidIPLength(String ipAddress) {
        return ipAddress.length() < 5;
    }

    public static void main(String[] args) throws Exception {
        Random randNumGenerator = new Random();
        int randOutcome = randNumGenerator.nextInt(4);
        Scanner scanner = new Scanner(System.in);
        LogManager.log(Configuration.getLogBarMax(), "Provide a name:")
        String providedName = scanner.nextLine();
        LogManager.log(Configuration.getLogBarMax(), "Provide a password");
        String providedPassword = scanner.nextLine();
        LogManager.log(Configuration.getLogBarMax(), "Display public key as address? Y/N")
        String providedPkChoice = scanner.nextLine();
        boolean isShowPkAsAddress = providedPkChoice.toUpperCase(Locale.ROOT).startsWith("Y");

        LogManager.log(Configuration.getLogBarMax(), "Enter IP address of a peer");
        String ipAddress = scanner.nextLine();
        if (isInvalidIPLength(ipAddress)) {
            ipAddress = "localhost";
        }

        try {
            if (randOutcome == 0) {
                LogManager.log(Configuration.getLogBarMax(),"==========Wallet created==========");
                Wallet wallet = new Wallet(providedName);
                System.out.println();
                LogManager.log(Configuration.getLogBarMax(), providedName + " created");
                System.out.println();

                WalletMessageTaskManager manager;
                PeerConnectionManager agent = new PeerConnectionManager(wallet, null);
                manager = new WalletMessageTaskManager(wallet, agent);
                agent.setMessageTaskManager(manager);

                if (!agent.createOutgoingConnection(ipAddress)) {
                    LogManager.log(Configuration.getLogBarMax(), "Connection to " + ipAddress + " failed");
                    System.exit(2);
                }

                PeerServer peerServer = new PeerServer(wallet, manager, agent);

                Thread managerThread = new Thread(manager);
                Thread agentThread = new Thread(agent);
                Thread serverThread = new Thread(peerServer);

                WalletSimulator simulator = new WalletSimulator(wallet, agent, manager);
                manager.setSimulator(simulator);

                serverThread.start();
                LogManager.log(Configuration.getLogBarMax(), "Peer server started");

                agentThread.start();
                LogManager.log(Configuration.getLogBarMax(), "Peer clients manager started");

                managerThread.start();
                LogManager.log(Configuration.getLogBarMax(), "Wallet task manager started");

                simulator.setBalanceShowPublicKey(isShowPkAsAddress);
                agent.broadcastRequestForBlockchainUpdate();
            } else {
                LogManager.log(Configuration.getLogBarMax(), "==========Miner created==========");

                Miner miner = new Miner(providedName, providedPassword);
                MinerMessageTaskManager manager;
                PeerConnectionManager agent = new PeerConnectionManager(miner, null);
                manager = new MinerMessageTaskManager(miner, agent);
                agent.setMessageTaskManager(manager);

                if (!agent.createOutgoingConnection(ipAddress)) {
                    LogManager.log(Configuration.getLogBarMax(), "Connection to " + ipAddress + "failed -> exiting");
                    System.exit(2);
                }

                PeerServer peerServer = new PeerServer(miner, manager, agent);

                Thread managerThread = new Thread(manager);
                Thread agentThread = new Thread(agent);
                Thread serverThread = new Thread(peerServer);

                WalletSimulator simulator = new WalletSimulator(miner, agent, manager);
                manager.setSimulator(simulator);

                serverThread.start();
                LogManager.log(Configuration.getLogBarMax(), "Peer server started");

                agentThread.start();
                LogManager.log(Configuration.getLogBarMax(), "Peer clients manager started");

                managerThread.start();
                LogManager.log(Configuration.getLogBarMax(), "Wallet task manager started");

                simulator.setBalanceShowPublicKey(isShowPkAsAddress);
                agent.broadcastRequestForBlockchainUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

class MessageFrame extends JFrame {
    Container container = this.getContentPane();
    JTextArea message = new JTextArea();
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

class FrameHelp extends JFrame {
    JTextPane message = new JTextPane();
    public FrameHelp() {
        super("Help");
        Container container = this.getContentPane();
        this.setBounds(500, 500, 360, 300);
        message.setBounds(0, 0, this.getWidth(), this.getHeight());
        container.add(message);
    }

    public void setMessage(String msg) {
        message.setText(msg);
        this.validate();
        this.setVisible(true);
    }
}

class FrameTransaction extends JFrame implements ActionListener {
    private final ArrayList<KeyNamePair> users;
    private final PeerConnectionManager agent;

    public FrameTransaction(ArrayList<KeyNamePair> users, PeerConnectionManager agent) throws HeadlessException {
        this.users = users;
        this.agent = agent;
        setUp();
    }

    private void setUp() {
        Container container = this.getContentPane();
        this.setSize(300, 120);
        GridLayout gl = new GridLayout(3, 2, 5, 5);
        JLabel selectUser = new JLabel("Select a user");
        JLabel selectTxAmount = new JLabel("Select transaction amount");
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        container.setLayout(gl);
        container.add(selectUser);
        container.add(selectTxAmount);

        JComboBox<String> candidates = new JComboBox<>();
        for (KeyNamePair each : users) {
            candidates.addItem(each.getWalletName());
        }
        container.add(candidates);

        JTextField input = new JTextField();
        container.add(input);
        container.add(submitButton);
        container.add(cancelButton);

        submitButton.addActionListener(e -> {
            int selectedIndex = candidates.getSelectedIndex();
            double amount;
            String inputText = input.getText();
            if (inputText != null && inputText.length() > 10) {
                try {
                    amount = Double.parseDouble(inputText);
                } catch (Exception e1) {
                    amount = -1;
                }
                if (amount <= 0.0) {
                    input.setText("Must be a positive number");
                    return;
                }
                boolean isSendTransaction = agent.isSendTransaction(users.get(selectedIndex).getPublicKey(), amount);
                if (!isSendTransaction) {
                    input.setText("Failed to send");
                } else {
                    input.setText("Transaction sent");
                }
            }
        });
        cancelButton.addActionListener(this);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}

class FramePrivateMessage extends JFrame implements ActionListener {
    private final ArrayList<KeyNamePair> users;
    private final PeerConnectionManager agent;
    private JTextArea messageBoard;
    private final WalletSimulator walletSimulator;

    public FramePrivateMessage(ArrayList<KeyNamePair> users, PeerConnectionManager agent, WalletSimulator walletSimulator) throws HeadlessException {
        super("Send a private message");
        this.users = users;
        this.agent = agent;
        this.walletSimulator = walletSimulator;
        setUp();
    }

    private void setUp() {
        Container container = getContentPane();
        this.setSize(300, 200);
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        container.setLayout(gbl);

        JLabel selectLabel = new JLabel("Please select:");
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbl.setConstraints(selectLabel, gbc);
        container.add(selectLabel);

        JComboBox<String> candidates = new JComboBox<>();
        for (KeyNamePair each: users) {
            int n = agent.hasDirectConnection(each.getPublicKey());
            candidates.addItem(each.getWalletName() + "-" + n);
        }

        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbl.setConstraints(candidates, gbc);
        container.add(candidates);

        gbc.weighty = 0.9;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.gridwidth = 2;
        JTextArea input = new JTextArea(2, 30);
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        gbl.setConstraints(input, gbc);
        container.add(input);

        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        JButton sendButton = new JButton("Send");
        gbl.setConstraints(sendButton, gbc);
        container.add(sendButton);

        sendButton.addActionListener(e -> {
            int selectedIndex = candidates.getSelectedIndex();
            String inputText = input.getText();
            if (inputText != null && inputText.length() > 0) {
                PublicKey publicKey = users.get(selectedIndex).getPublicKey();
                boolean isSendPrivateMessage = agent.isSendPrivateMessage(publicKey, inputText);
                if (isSendPrivateMessage) {
                    input.setText("Message sent");
                    walletSimulator.appendMessageLineOnBoard("Private -> " + agent.getNameFromAddress(publicKey)
                            + ":" + inputText);
                } else {
                    input.setText("Error: message failed");
                }
            }
        });

        input.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // do nothing
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown() || e.isControlDown()) {
                        input.append(System.getProperty("line.separator"));
                    } else {
                        int selectedIndex = candidates.getSelectedIndex();
                        String text = input.getText();
                        if (text != null && text.length() > 0) {
                            PublicKey publicKey = users.get(selectedIndex).getPublicKey();
                            agent.isSendPrivateMessage(publicKey, text);
                            walletSimulator.appendMessageLineOnBoard("Private -> " + agent.getNameFromAddress(publicKey) + " : " + text);
                        }
                        e.consume();
                        input.setText("");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        JButton cancelButton = new JButton("Cancel");
        gbl.setConstraints(cancelButton, gbc);
        container.add(cancelButton);
        cancelButton.addActionListener(this);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
