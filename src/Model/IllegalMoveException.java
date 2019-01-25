package Model;

/**
 * Exception that is thrown when player performs an invalid move
 */
public class IllegalMoveException extends RuntimeException {

    /**
     * Creates Model.IllegalMoveException.
     */
    public IllegalMoveException() {
        super();
    }

    /**
     * Creates Model.IllegalMoveException with certain message.
     *
     * @param message Exception message.
     */
    public IllegalMoveException(String message) {
        super(message);
    }
}
