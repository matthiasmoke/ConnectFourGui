package ViewXController;
import Model.Board;
import Model.ConnectFour;
import Model.Coordinates2D;
import Model.Player;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.Collection;

/**
 * Represents main view of the game.
 */
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
    private static final String MSG_MACHINE_IS_PLAYING
            = "Your enemy has not finished thinking yet...";
    private static final String MSG_NO_WINNER = "No one won...";
    private static MachineThread machineThread;

    private View() {

    }

    /**
     * Method that initializes the view.
     */
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

        mainFrame.setMinimumSize(new Dimension(300, 300));
        mainFrame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        initGamePanel();
        mainFrame.setVisible(true);

    }

    /**
     * Adds all necessary listeners to controls.
     */
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
     * @return ViewXController.Slot size that fits the panel size.
     */
    private Dimension getSlotSize() {
        int relHeight = gamePanel.getSize().height / Board.ROWS;
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
     * Event that is triggered from a slot the human clicked.
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
                showMessage(MSG_MACHINE_IS_PLAYING);
            }
        } else {
            showMessage(MSG_NOT_INITIATED);
        }
    }

    /**
     * Performs the human move.
     *
     * @param column Column that human moved into.
     */
    private void performHumanMove(int column) {
        Board playerMove = gameModel.move(column);

        if (playerMove != null) {
            performMove(column, playerMove);
            gameModel = playerMove;

            if (!checkWinner()) {
                performMachineMove();
            }
        } else {
            showMessage(MSG_ILLEGAL_MOVE);
        }
    }

    /**
     * Thread class for machine move to use swing worker for time consuming move
     */
    class MachineThread extends Thread {

        @Override
        public void run() {
            machinePlaying = true;
            Board machineMove = gameModel.machineMove();

            if (machineMove != null) {
                int column = getMachineMoveColumn(gameModel, machineMove);
                SwingUtilities.invokeLater(()
                        -> performMove(column, machineMove));
            }
            gameModel = machineMove;
            checkWinner();
            machinePlaying = false;
        }
    }

    /**
     * Stops the machine thread if its running
     */
    @Deprecated
    private void killThread() {
        if (machinePlaying) {
            machineThread.stop();
            machinePlaying = false;
        }
    }

    /**
     * Performs machine move.
     */
    private void performMachineMove() {
        machineThread = new MachineThread();
        machineThread.start();
    }

    /**
     * Gets the column in that the machine moved its checker.
     *
     * @param oldBoard Model.Board before machine move.
     * @param machineMove Model.Board after machine move.
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

    /**
     * Shows a message box with the given message.
     *
     * @param message Message to display.
     */
    private void showMessage(String message) {
        int messageType;
        String header = "Attention";

        // Define message type and header for option pane.
        if (message.equals(MSG_DEFEAT) ||message.equals(MSG_VICTORY)) {
            messageType = JOptionPane.INFORMATION_MESSAGE;
            header = "Info";
        } else {
            messageType = JOptionPane.WARNING_MESSAGE;
        }

        JOptionPane.showMessageDialog(this, message, header, messageType);
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
            } else {
                showMessage(MSG_NO_WINNER);
                return true;
            }
        }
        return false;
    }

    /**
     * Marks the witness slots by given coordinates.
     *
     * @param witness Collection of witness coordinates.
     */
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
        killThread();
        clearGame();
        gameModel = new ConnectFour(switchPlayer);
        gameModel.setLevel((int) levelSelection.getSelectedItem());

        if (gameModel.getFirstPlayer().isMachine()) {
            performMachineMove();
        }
    }

    /**
     * Sets the color of all slots to white;
     */
    private void clearGame() {
        gameModel = null;
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
            killThread();
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
