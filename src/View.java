import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;

public class View extends JFrame {

    private static JPanel gamePanel;
    private static JButton newGameButton;
    private static JButton switchButton;
    private static JButton quitButton;
    private static JComboBox<Integer> levelSelection;
    private static Board gameModel;
    private static boolean machinePlaying = false;
    private static final int[] LEVELS = {1, 2, 3, 4, 5};
    private static final int DEFAULT_HEIGHT = 650;
    private static final int DEFAULT_WIDTH = 700;
    private static final String MSG_ILLEGAL_MOVE = "Illegal Move!";
    private static final String MSG_NOT_INITIATED = "Game has not started yet!";
    private static final String MSG_VICTORY = "Congratulations! You won.";
    private static final String MSG_DEFEAT = "Sorry! Machine wins.";
    private static final String MSG_GAME_OVER = "Game is already over!";

    private View() {

    }

    private void showGame() {
        JFrame mainFrame = new JFrame("Connect Four");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initializing controls here.
        JPanel menuPanel = new JPanel();
        gamePanel = new JPanel();

        newGameButton = new JButton("New");
        switchButton = new JButton("Switch");
        quitButton = new JButton("Quit");
        levelSelection = new JComboBox<>();
        initLevelComboBox();
        addActionListeners();

        menuPanel.add(levelSelection);
        menuPanel.add(newGameButton);
        menuPanel.add(switchButton);
        menuPanel.add(quitButton);

        gamePanel.setLayout(new GridLayout(Board.ROWS, Board.COLS));

        // Adding components to the main container.
        Container mainContainer = mainFrame.getContentPane();
        mainContainer.add(BorderLayout.CENTER, gamePanel);
        mainContainer.add(BorderLayout.SOUTH, menuPanel);

        mainFrame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        initGamePanel();
        mainFrame.setVisible(true);

    }

    private void addActionListeners() {
        levelSelection.addActionListener(new SelectionListener());
        newGameButton.addActionListener(new NewGameListener());
        switchButton.addActionListener(new SwitchListener());
        quitButton.addActionListener(new QuitListener());
        gamePanel.addComponentListener(new ResizeListener());
    }

    /**
     * Adds possible levels to combo box.
     */
    private void initLevelComboBox() {
        for (int level : LEVELS) {
            levelSelection.addItem(level);
            levelSelection.setSelectedItem(LEVELS[3]);
        }
    }

    /**
     * Adds slots to game panel.
     */
    private void initGamePanel() {
        int numberOfSlots = Board.COLS * Board.ROWS;
        Dimension slotDim = getSlotSize();

        while (numberOfSlots > 0) {
            gamePanel.add(new Slot(slotDim, this));
            numberOfSlots--;
        }
    }

    /**
     * Calculates the relative slot size
     *
     * @return Slot size that fits the panel size.
     */
    private Dimension getSlotSize() {
        int relHeight = gamePanel.getSize().height/ Board.ROWS;
        int relWidth = gamePanel.getSize().width / Board.COLS;

        return new Dimension(relWidth, relHeight);
    }

    /**
     * Checks if game is initiated.
     *
     * @return true if game is running.
     */
    private static boolean initiated() {
        return gameModel != null;
    }

    /**
     * Event that is triggered from a slot the human clicked
     *
     * @param column Column in that human placed his checker.
     */
    public void columnClickedEvent(int column) {
        if (initiated()) {
            if (!machinePlaying) {
                if (gameModel.isGameOver()) {
                    showMessage(MSG_GAME_OVER);
                } else {
                    performHumanMove(column);
                }
            } else {
                showMessage(MSG_ILLEGAL_MOVE);
            }
        } else {
            showMessage(MSG_NOT_INITIATED);
        }
    }

    private void performHumanMove(int column) {
        Board playerMove = gameModel.move(column);

        if (playerMove != null) {
            performMove(column, playerMove);

            if (!checkWinner()) {
                performMachineMove();
            }
        } else {
            showMessage(MSG_ILLEGAL_MOVE);
        }
    }

    /**
     * Performs machine move.
     */
    private void performMachineMove() {
        Thread machineThread = new Thread() {
            @Override
            public void run() {
                super.run();
                machinePlaying = true;
                Board machineMove = gameModel.machineMove();

                if (machineMove != null) {
                    int column = getMachineMoveColumn(gameModel, machineMove);
                    performMove(column, machineMove);
                }

                checkWinner();
                machinePlaying = false;
            }
        };
        machineThread.start();
    }

    /**
     * Gets the column in that the machine moved its checker.
     *
     * @param oldBoard Board before machine move.
     * @param machineMove Board after machine move.
     * @return Column index of machine move.
     */
    private int getMachineMoveColumn(Board oldBoard, Board machineMove) {
        for (int col = 1; col <= Board.COLS; col++) {

            for (int row = 1; row <= Board.ROWS; row++) {

                if (oldBoard.getSlot(row, col) == null
                        && machineMove.getSlot(row, col) != null) {
                    return col;
                }
            }
        }
        return 0;
    }

    /**
     * Sets current game model to new board and draws move in game panel.
     *
     * @param column Column with new checker.
     * @param newBoard Game board with new move made.
     */
    private void performMove(int column, Board newBoard) {
        for (int i = Board.ROWS; i > 0; i--) {

            // Get position and player of the last checker that was put in game
            Player player = newBoard.getSlot(i, column);

            if(player != null) {
                int index = getComponentIndex(column, i);
                Slot currSlot = (Slot) gamePanel.getComponent(index - 1);
                currSlot.setCircleColor(player.getCheckerColor());
            }
        }
        gameModel = newBoard;
    }

    /**
     * Calculates the index of the slot in game panel by using its row and col.
     *
     * @param col Column of the slot.
     * @param row Row of the slot.
     * @return Component index in the game panel.
     */
    private static int getComponentIndex(int col, int row) {

        // Calc slots in all rows before row.
        int slotsInRowsBefore = (Board.ROWS - row) * Board.COLS;

        // Add slots in current row to get index.
        int index = slotsInRowsBefore + col;

        return index;
    }

    private void showMessage(String message) {
        int messageType;

        // Define message type for option pane.
        if (message.equals(MSG_DEFEAT) ||message.equals(MSG_VICTORY)) {
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else {
            messageType = JOptionPane.WARNING_MESSAGE;
        }

        JOptionPane.showMessageDialog(this,
                 message, "Attention",
                messageType);
    }

    /**
     * Checks if the game has a winner.
     *
     * @return True if someone won.
     */
    private boolean checkWinner() {
        if (gameModel.isGameOver()) {
            Player winner = gameModel.getWinner();

            if (winner != null) {
                if (winner.isMachine()) {
                    showMessage(MSG_DEFEAT);
                } else {
                    showMessage(MSG_VICTORY);
                }
                 markWitness(gameModel.getWitness());
                return true;
            }
        }
        return false;
    }

    private void markWitness(Collection<Coordinates2D> witness) {
        for (Coordinates2D slot : witness) {
            int slotIndex = getComponentIndex(slot.getColumn(),
                    slot.getRow() + 1);

            Slot currSlot = (Slot) gamePanel.getComponent(slotIndex);
            currSlot.setWitness(true);
        }
        gamePanel.repaint();
    }

    /**
     * Listener class for level selection.
     */
    class SelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (initiated()) {
                gameModel.setLevel((int)levelSelection.getSelectedItem());
            }
        }
    }

    /**
     * Listener class for new game button.
     */
    class NewGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            createNewGame(false);
        }
    }

    /**
     * Listener class for switch button.
     */
    class SwitchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            createNewGame(true);
        }
    }

    /**
     * Creates a new game. Takes over level from old game.
     *
     * @param switchPlayer Determines if machine should start.
     */
    private void createNewGame(boolean switchPlayer) {
        gameModel = new ConnectFour(switchPlayer);
        gameModel.setLevel((int) levelSelection.getSelectedItem());
        clearGamePanel();

        if (gameModel.getFirstPlayer().isMachine()) {
            performMachineMove();
        }
    }

    /**
     * Sets the color of all slots to white;
     */
    private void clearGamePanel() {
        Component[] slots = gamePanel.getComponents();

        for (Component slot : slots) {
            Slot currSlot = (Slot) slot;
            currSlot.setCircleColor(Color.WHITE);

            if (currSlot.isWitness()) {
                currSlot.setWitness(false);
            }
        }
    }

    /**
     * Listener class for quit button.
     */
    class QuitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    /**
     * Listener class for frame resizing.
     */
    class ResizeListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            Dimension newSize = getSlotSize();

            //TODO clean resize
            for (Component slot : gamePanel.getComponents()) {
                slot.setPreferredSize(newSize);
            }
        }
    }

    /**
     * Main method for view.
     *
     * @param args /
     */
    public static void main(String[] args) {
        View mainView = new View();
        mainView.showGame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
