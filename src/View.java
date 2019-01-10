import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class View extends JFrame implements ActionListener {

    private static JFrame mainFrame;
    private static JPanel gamePanel;
    private static JButton newGameButton;
    private static JButton switchButton;
    private static JButton quitButton;
    private static JComboBox<Integer> levelSelection;
    private static Board gameModel;
    private static Thread machineThread;
    private static boolean machinePlaying = false;
    private static final int[] LEVELS = {1, 2, 3, 4, 5};
    private static final int DEFAULT_HEIGHT = 650;
    private static final int DEFAULT_WIDTH = 700;
    private static final String MSG_ILLEGAL_MOVE = "Illegal Move!";
    private static final String MSG_NOT_INITIATED = "Game has not started yet!";
    private static final String MSG_VICTORY = "Congratulations! You won.";
    private static final String MSG_DEFEAT = "Sorry! Machine wins.";
    private static final String MSG_GAMEOVER = "Game is already over!";

    public View() {

    }

    public void showGame() {
        initMainView();
    }

    public  void initMainView() {
        mainFrame = new JFrame("Connect Four");
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
        initGamePanel();

        // Adding components to the main container.
        Container mainContainer = mainFrame.getContentPane();
        mainContainer.add(BorderLayout.CENTER, gamePanel);
        mainContainer.add(BorderLayout.SOUTH, menuPanel);

        mainFrame.pack();
        mainFrame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        mainFrame.setVisible(true);

    }

    private void initLevelComboBox() {
        for (int level : LEVELS) {
            levelSelection.addItem(level);
            levelSelection.setSelectedItem(LEVELS[3]);
        }
    }

    private void initGamePanel() {
        int numberOfSlots = Board.COLS * Board.ROWS;
        Dimension slotDim = getSlotSize();

        while (numberOfSlots > 0) {
            gamePanel.add(new Slot(slotDim, this));
            numberOfSlots--;
        }
    }

    private Dimension getSlotSize() {
        int relHeight = DEFAULT_HEIGHT / Board.ROWS;
        int relWidth = DEFAULT_WIDTH / Board.COLS;

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

    private void performMachineMove() {
        machineThread = new Thread() {
            @Override
            public void run() {
                super.run();
                machinePlaying = true;
                Board machineMove = gameModel.machineMove();

                if (machineMove != null) {
                    //TODO get column of machine move
                }
            }
        };
        machineThread.start();
    }

    private static void performMove(int column, Board newBoard) {
        for (int i = Board.ROWS - 1; i > 0; i--) {

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
        int allSlots = Board.COLS * Board.ROWS;
        int slotsBeforeCurrent = ((row - 1) * Board.COLS) - (Board.COLS - col);
        int index = allSlots - slotsBeforeCurrent;

        return index;
    }

    public void columnClickedEvent(int column) {
        if (initiated()) {
            if (!machinePlaying) {
                if (gameModel.isGameOver()) {
                    showMessage(MSG_GAMEOVER);
                } else {
                    Board playerMove = gameModel.move(column);

                    if (playerMove != null) {
                        gameModel = playerMove;
                        performMove(column, gameModel);

                        if (!checkWinner()) {
                            gameModel = gameModel.machineMove();
                            checkWinner();
                        }
                    } else {
                        showMessage(MSG_ILLEGAL_MOVE);
                    }
                }
            } else {
                showMessage(MSG_ILLEGAL_MOVE);
            }
        } else {
            showMessage(MSG_NOT_INITIATED);
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this,
                 message, "Attention",
                JOptionPane.WARNING_MESSAGE);
    }

    private boolean checkWinner() {
        if (gameModel.isGameOver()) {
            Player winner = gameModel.getWinner();

            if (winner != null) {
                if (winner.isMachine()) {
                    showMessage(MSG_DEFEAT);
                } else {
                    showMessage(MSG_VICTORY);
                }
                return true;
            }
        }
        return false;
    }

    private void addActionListeners() {
        levelSelection.addActionListener(new SelectionListener());
        newGameButton.addActionListener(new NewGameListener());
        switchButton.addActionListener(new SwitchListener());
        quitButton.addActionListener(new QuitListener());
        gamePanel.addComponentListener(new ResizeListener());
    }

    class SelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (initiated()) {
                gameModel.setLevel((int)levelSelection.getSelectedItem());
            }
        }
    }

    class NewGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gameModel = new ConnectFour(false);
        }
    }

    class SwitchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            createNewGame(true);
        }

        /**
         * Creates a new game. Takes over level from old game.
         *
         * @param switchPlayer Determines if machine should start.
         */
        private void createNewGame(boolean switchPlayer) {
            gameModel = new ConnectFour(switchPlayer);
            gameModel.setLevel((int) levelSelection.getSelectedItem());

            if (gameModel.getFirstPlayer().isMachine()) {
                performMachineMove();
            }
        }
    }

    class QuitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            Dimension newSize = getSlotSize();

            //TODO
            for (Component slot : gamePanel.getComponents()) {
                slot.setPreferredSize(newSize);
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }

    public static void main(String[] args) {
        View mainView = new View();
        mainView.showGame();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }
}
