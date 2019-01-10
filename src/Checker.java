/**
 * Represents a checker in the game
 */
public class Checker implements Cloneable {

    private Player owner;
    private Coordinates2D position;

    /**
     * Creates a new Checker
     *
     * @param position Position in game board
     * @param owner Owner of Checker
     */
    public Checker(Coordinates2D position, Player owner) {
        this.position = position;
        this.owner = owner;
    }

    /**
     * Returns the owner
     *
     * @return Owner of the checker
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Returns the position
     *
     * @return Position of the checker
     */
    public Coordinates2D getPosition() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Checker clone() {
        Checker copy;

        try {
            copy = (Checker) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new Error(ex);
        }
        return copy;
    }
}
