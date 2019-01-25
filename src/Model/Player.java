package Model;

import java.awt.*;

/**
 * Represents a player in the game
 */
public class Player implements Cloneable, Comparable {

    private Color checkerColor;
    private char symbol;
    private boolean isMachine;
    private boolean isWinner;


    public Player(Color checkerColor, boolean isMachine) {
        this.checkerColor = checkerColor;
        this.isMachine = isMachine;
    }

    /**
     * Creates a new human or machine Model.Player.
     * @param symbol Symbol of the player.
     * @param isMachine Indicates if player is type of machine or not.
     */
    public Player(char symbol, boolean isMachine) {
        this.symbol = symbol;
        this.isMachine = isMachine;
    }

    /**
     * Gets the checker-color of the player.
     *
     * @return The color the checkers of the player have.
     */
    public Color getCheckerColor() {
        return checkerColor;
    }

    /**
     * Gets the player-symbol.
     * @return Symbol of the player.
     */
    public char getSymbol() {
        return symbol;
    }


    /**
     * Gets information about the player type.
     * @return True if player is type of machine, false if not.
     */
    public boolean isMachine() {
        return isMachine;
    }

    /**
     * Sets player to winner or removes him from winners.
     *
     * @param isWinner Set player to winner if true.
     */
    public void setWinner(boolean isWinner) {
        this.isWinner = isWinner;
    }

    /**
     * Checks if player is winner.
     *
     * @return True if player is winner.
     */
    public boolean isWinner() {
        return isWinner;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public Player clone() {
        Player clone;

        try {
            clone = (Player) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new Error(ex);
        }

        return clone;
    }

    @Override
    public int compareTo(Object o) {
        Player player = (Player) o;

        if (symbol > 0) {
            if (this.symbol == player.symbol) {
                return 0;
            } else if (this.symbol < player.symbol) {
                return -1;
            } else {
                return 1;
            }
        } else  {
            if (this.checkerColor == player.checkerColor) {
                return 0;
            } else if (this.checkerColor != player.checkerColor) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
