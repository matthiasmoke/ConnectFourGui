/**
 * Class to represent the coordinates of a checker in the game-board
 */
public class Coordinates2D implements Cloneable {

    private int posRow;
    private int posCol;

    /**
     * Creates new coordinates
     *
     * @param row Certain row (x-coordinate)
     * @param col Certain column (y-coordinate)
     */
    public Coordinates2D(int row, int col) {
        posRow = row;
        posCol = col;
    }

    /**
     * Returns x-coordinate
     *
     * @return row of coordinate
     */
    public int getRow() {
        return posRow;
    }

    /**
     * Returns y-coordinate
     *
     * @return column of coordinate
     */
    public int getColumn() {
        return posCol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinates2D clone() {
        Coordinates2D copy;

        try {
            copy = (Coordinates2D) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new Error(ex);
        }

        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("(%d, %d)",  posRow + 1, posCol + 1);
    }
}
