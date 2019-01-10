import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class to represent the game-board
 */
public class ConnectFour implements Board, Cloneable {

    private Checker[][] currBoard = new Checker[ROWS][COLS];
    private GroupManager groups;
    private Player[] players = new Player[2];
    private Player currentPlayer;
    private Player lastPlayer;
    private int boardValue;
    private int level = 4;
    private ConnectFour[] gameTree = new ConnectFour[7];
    private boolean gameOver = false;

    /**
     * Default constructor for game.
     * Automatically sets players.
     *
     * @param switchPlayers If true, the bot will start the game.
     */
    public ConnectFour(boolean switchPlayers) {
        Player human = new Player(Color.YELLOW, false);
        Player machine = new Player(Color.RED, true);

        if (switchPlayers) {
            players[0] = machine;
            players[1] = human;
        } else {
            players[0] = human;
            players[1] = machine;
        }
        currentPlayer = players[0];
        groups = new GroupManager(players[0], players[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getFirstPlayer() {
        return players[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board move(int col) {
        if (gameOver || (lastPlayer != null
                && lastPlayer.compareTo(currentPlayer) == 0)) {

            throw new IllegalMoveException();
        }

        if (col > COLS && col < 1) {
            throw new IllegalArgumentException();
        }

        int column = col - 1;
        ConnectFour newBoard = (ConnectFour) this.clone();

        for (int i = 0; i < ROWS; i++) {
            if (currBoard[i][column] == null) {
                Checker newChecker = new Checker(new Coordinates2D(i, column),
                        currentPlayer);

                newBoard.currBoard[i][column] = newChecker;
                newBoard.groupSearch(newChecker); //check groups for new checker
                return newBoard;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board machineMove() {
        // switch current player to machine
        switchPlayer(true);

        gameTree = generateGameTree(this, level);
        calculateValues(gameTree, level);

        // check if bot win is possible in next draw
        addValueForPossibleBotWin();

        // get largest board value
        int indexOfMaximum = getIndexOfMaximum(gameTree);

        ConnectFour machineMove = (ConnectFour) move(indexOfMaximum + 1);
        gameTree = null;
        //switch current player to human
        machineMove.switchPlayer(false);

        return machineMove;
    }

    /**
     * Adds the r-value from specification to board value if bot win is possible
     * in next draw
     */
    private void addValueForPossibleBotWin() {
        for (int i = 0; i < gameTree.length; i++) {

            if (gameTree[i] != null) {
                if (gameTree[i].groups.isBotWinPossible()) {
                    gameTree[i].boardValue += 500000;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameOver() {
        if (isTie()) {
            gameOver = true;
            return true;
        }

        Group winningGroup = groups.getWinningGroup();

        if (winningGroup != null) {

            if (winningGroup.getOwner().compareTo(players[0]) == 0) {
                players[0].setWinner(true);
            } else {
                players[1].setWinner(true);
            }
            gameOver = true;

            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (players[0].isWinner()) {
            return players[0];

        } else if (players[1].isWinner()) {
            return players[1];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Coordinates2D> getWitness() {
        if (!gameOver) {
            throw new IllegalStateException("There is no winner available!");
        }

        List<Coordinates2D> witness = new ArrayList<>(4);
        Collection<Checker> winningGroup
                = groups.getWinningGroup().getSortedMembers();

        for (Checker checker : winningGroup) {
            witness.add(checker.getPosition());
        }

        return witness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getSlot(int row, int col) {
        int arrayRow = row - 1;
        int arrayCol = col - 1;
        if (currBoard[arrayRow][arrayCol] != null) {
            return currBoard[arrayRow][arrayCol].getOwner();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board clone() {
        ConnectFour copy;
        try {
            copy = (ConnectFour) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new Error(ex);
        }

        // Deep copy game board.
        copy.currBoard = currBoard.clone();
        for (int i = 0; i < currBoard.length; i++) {
            copy.currBoard[i] = currBoard[i].clone();

            for (int j = 0; j < currBoard[i].length; j++) {

                if (currBoard[i][j] != null) {
                    copy.currBoard[i][j] = currBoard[i][j].clone();
                }
            }
        }

        // Deep copy groups and current player.
        copy.groups = groups.clone();
        copy.currentPlayer = currentPlayer.clone();

        Player[] newPlayers = new Player[2];
        newPlayers[0] = players[0].clone();
        newPlayers[1] = players[1].clone();

        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        String newLine = "";

        for (int row = ROWS - 1; row >= 0; row--) {

            b.append(newLine);
            for (int col = 0; col < COLS; col++) {

                Checker currSlot = currBoard[row][col];

                if (currSlot == null) {
                    b.append(".");
                } else {
                    b.append(currSlot.getOwner().getSymbol());
                }

                //If its the last column, no space must be added.
                if (col < COLS - 1) {
                    b.append(" ");
                }
            }
            newLine = "\n";
        }
        return b.toString();
    }

    /**
     * Checks if game has no winner
     *
     * @return true if its a tie
     */
    private boolean isTie() {
        for (int col = 0; col < COLS; col++) {
            if (currBoard[ROWS - 1][col] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generates game tree of current board.
     *
     * @param current Current board.
     * @param depth Depth to generate tree for.
     * @return The generated game tree
     */
    private ConnectFour[] generateGameTree(ConnectFour current, int depth) {
        ConnectFour[] gameTree = new ConnectFour[7];
        boolean machineDraw;

        for (int i = 0; i < depth; i++) {

            //detect if generated move is made by machine or human
            machineDraw = isMachineDraw(depth);
            current.switchPlayer(machineDraw);

            for (int col = 0; col < COLS; col++) {
                ConnectFour newBoard = (ConnectFour) current.move(col + 1);
                gameTree[col] = newBoard;

                if (newBoard != null) {
                    gameTree[col].gameTree = generateGameTree(gameTree[col],
                            depth - 1);
                }
            }
        }
        return gameTree;
    }

    /**
     * Checks if current draw is made by machine or not when generating the game
     * tree and calculating values.
     *
     * @param depth Depth of the tree.
     * @return true if current draw is made by machine.
     */
    private boolean isMachineDraw(int depth) {
        if (level % 2 == 0) {
            return depth % 2 == 0;
        } else {
            return depth % 2 == 1;
        }
    }

    /**
     * Calculates the board value of the given game tree including its depth.
     *
     * @param currentGameTree Tree to calculate value for.
     * @param depth Current depth of tree.
     */
    private void calculateValues(ConnectFour[] currentGameTree, int depth) {
        if (depth > 0) {
            boolean machineDraw = isMachineDraw(depth);

            for (int i = 0; i < COLS; i++) {

                if (currentGameTree[i] != null) {
                    calculateValues(currentGameTree[i].gameTree,
                            depth - 1);
                    currentGameTree[i].calculateBoardValue(!machineDraw);
                }
            }
        }
    }

    /**
     * Check if there are new groups for all group-types
     *
     * @param checker Checker to search groups for
     */
    private void groupSearch(Checker checker) {
        findDiagonalFallingMembers(checker);
        findDiagonalRisingMembers(checker);
        findHorizontalNeighbours(checker);
        findVerticalNeighbours(checker);
    }

    /**
     * Searches for the vertical neighbours of the given checker
     *
     * @param checker Checker for search.
     */
    private void findVerticalNeighbours(Checker checker) {

        // List of surrounding checkers.
        List<Checker> surrounding = new ArrayList<>(2);
        int actRow = checker.getPosition().getRow();
        int actCol = checker.getPosition().getColumn();

        // Calculate neighbour positions.
        Checker underneath = getCheckerByPosition(new Coordinates2D(
                actRow - 1, actCol));

        Checker above = getCheckerByPosition(new Coordinates2D(
                actRow + 1, actCol));

        // Add checker underneath if possible.
        if (isValidNeighbour(underneath, checker)) {
            surrounding.add(underneath);
        }

        // Add checker above if possible.
        if (isValidNeighbour(above, checker)) {
            surrounding.add(above);
        }

        groups.check(checker, surrounding, GroupType.VERTICAL);
    }

    /**
     * Searches for the right and left neighbours of the given checker.
     *
     * @param checker Checker for search.
     */
    private void findHorizontalNeighbours(Checker checker) {

        // List of surrounding checkers.
        List<Checker> surrounding = new ArrayList<>(2);
        int actRow = checker.getPosition().getRow();
        int actCol = checker.getPosition().getColumn();

        // Calculate neighbour positions.
        Checker left = getCheckerByPosition(new Coordinates2D(
                actRow, actCol - 1));

        Checker right = getCheckerByPosition(new Coordinates2D(
                actRow, actCol + 1));

        // Add left checker if possible.
        if (isValidNeighbour(left, checker)) {
            surrounding.add(left);
        }

        // Add right checker if possible.
        if (isValidNeighbour(right, checker)) {
            surrounding.add(right);
        }

        groups.check(checker, surrounding, GroupType.HORIZONTAL);
    }

    /**
     * Searches for the neighbours on a rising diagonal through the checker
     *
     * @param checker Checker for search.
     */
    private void findDiagonalRisingMembers(Checker checker) {

        // List of surrounding checkers.
        List<Checker> surrounding = new ArrayList<>(2);
        int actRow = checker.getPosition().getRow();
        int actCol = checker.getPosition().getColumn();

        // Calculate neighbour positions.
        Checker topRight = getCheckerByPosition(new Coordinates2D(
                actRow + 1, actCol + 1));

        Checker bottomLeft = getCheckerByPosition(new Coordinates2D(
                actRow - 1, actCol - 1));

        // Add checker from the top right if possible.
        if (isValidNeighbour(topRight, checker)) {
            surrounding.add(topRight);
        }

        // Add left checker if possible.
        if (isValidNeighbour(bottomLeft, checker)) {
            surrounding.add(bottomLeft);
        }

        groups.check(checker, surrounding, GroupType.DIAGONALRISING);
    }

    /**
     * Searches for the neighbours on a falling diagonal through the checker
     *
     * @param checker Checker for search.
     */
    private void findDiagonalFallingMembers(Checker checker) {

        // List of surrounding checkers.
        List<Checker> surrounding = new ArrayList<>(2);
        int actRow = checker.getPosition().getRow();
        int actCol = checker.getPosition().getColumn();

        // Calculate neighbour positions.
        Checker topLeft = getCheckerByPosition(new Coordinates2D(
                actRow + 1, actCol - 1));

        Checker bottomRight = getCheckerByPosition(new Coordinates2D(
                actRow - 1, actCol + 1));

        // Add top left checker if possible.
        if (isValidNeighbour(topLeft, checker)) {
            surrounding.add(topLeft);
        }

        // Add bottom right checker if possible.
        if (isValidNeighbour(bottomRight, checker)) {
            surrounding.add(bottomRight);
        }

        groups.check(checker, surrounding, GroupType.DIAGONALFALLING);
    }


    private boolean isValidNeighbour(Checker neighbour, Checker checker) {
        return neighbour != null
                && neighbour.getOwner().equals(checker.getOwner());
    }

    /**
     * Checks if given position is on the game board.
     *
     * @param position Position to check.
     * @return True if the given position is on the game board.
     */
    private boolean isValidPosition(Coordinates2D position) {
        int row = position.getRow();
        int col = position.getColumn();

        return row < currBoard.length
                && col < currBoard[0].length
                && row >= 0 && col >= 0;
    }

    private Checker getCheckerByPosition(Coordinates2D position) {
        if (isValidPosition(position)) {
            return currBoard[position.getRow()][position.getColumn()];
        }
        return null;
    }


    /**
     * Calculates Q value by using the formula given in the task-specification.
     *
     * @return Q value for number of Checkers in board.
     */
    private int getCheckerValue() {
        int valueP1 = 0;
        int valueP2 = 0;

        // Counting checkers of each player for each column.
        for (int i = 1; i < COLS - 1; i++) {

            int checkersP1 = 0; //Number of checkers in column for player 1.
            int checkersP2 = 0;

            for (int row = 0; row < ROWS; row++) {
                Checker currChecker = currBoard[row][i];
                if (currChecker != null) {
                    Player owner = currChecker.getOwner();

                    //To whom does checker belong to?
                    if (owner.equals(players[0])) {
                        checkersP1 += 1;
                    } else if (owner.equals(players[1])) {
                        checkersP2 += 1;
                    }
                }
            }

            // Determine the value that the number of checkers in a certain row
            // have to be multiplied.
            int multiplicator = i;

            if (i == 4) {
                multiplicator = 2;
            }

            if (i == 5) {
                multiplicator = 1;
            }

            valueP1 += multiplicator * checkersP1;
            valueP2 += multiplicator * checkersP2;
        }

        //Detect which player is the bot.
        if (players[0].isMachine()) {
            return valueP1 - valueP2;
        } else {
            return valueP2 - valueP1;
        }
    }

    /**
     * Calculates the board value of current board by summing up checker- and
     * group-value and the maximum or minimum of board values in game tree
     * of current board
     *
     * @param addMaximumToValue Parameter to determine if max or min should
     *                          be added to value of this board
     */
    private void calculateBoardValue(boolean addMaximumToValue) {
        boardValue = getCheckerValue() + groups.calculateGroupValue();

        if (!isGameTreeNull()) {
            int maxMin;

            if (addMaximumToValue) {
                // If machine move, get node with highest board value of tree.
                maxMin = getIndexOfMaximum(gameTree);
            } else {
                // Else with lowest (human move).
                maxMin = getIndexOfMinimum(gameTree);
            }

            // Add value to current board.
            boardValue += gameTree[maxMin].boardValue;
        }
    }

    /**
     * Gets the index of the board in tree with the maximum board value.
     *
     * @param tree Game tree to check.
     * @return Index of the max board value.
     */
    private int getIndexOfMaximum(ConnectFour[] tree) {
        int largest = COLS - 1;

        for (int i = COLS - 2; i >= 0; i--) {
            if (tree[i] != null) {

                if (tree[largest] == null
                        || tree[largest].boardValue <= tree[i].boardValue) {

                    largest = i;
                }
            }
        }
        return largest;
    }


    /**
     * Gets the index of the board in tree with the minimum board value.
     *
     * @param tree Game tree to check.
     * @return Index of the min board value.
     */
    private int getIndexOfMinimum(ConnectFour[] tree) {
        int smallest = COLS - 1;

        for (int i = COLS - 2; i >= 0; i--) {
            if (tree[i] != null) {

                if (tree[smallest] == null
                        || tree[smallest].boardValue >= tree[i].boardValue) {
                    smallest = i;
                }
            }
        }
        return smallest;
    }

    private boolean isGameTreeNull() {
        for (int i = 0; i < gameTree.length; i++) {
            if (gameTree[i] != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Switches between players.
     * Requirement for this method is that one machine,
     * and one human player exist.
     *
     * @param toMachine Switch to machine or not.
     */
    private void switchPlayer(boolean toMachine) {

        if (players.length > 0) {
            Player machine;
            Player human;

            if (players[0].isMachine()) {
                machine = players[0];
                human = players[1];
            } else {
                machine = players[1];
                human = players[0];
            }

            if (toMachine) {
                currentPlayer = machine;
                lastPlayer = human;
            } else {
                currentPlayer = human;
                lastPlayer = machine;
            }
        }
    }
}