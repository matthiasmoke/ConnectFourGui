import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents a slot where a checker can be placed in.
 */
public class Slot extends JPanel {

    private static final Color BACKGROUND_COLOR = Color.BLUE;
    private Color circleColor;
    private View parent;
    private boolean witness = false;

    /**
     * Initializes a Slot with given size.
     *
     * @param size Size of the slot.
     * @param parent Parent view where slot is located in.
     */
    public Slot(Dimension size, View parent) {
        circleColor = Color.WHITE;
        this.parent = parent;

        setPreferredSize(size);
        setBackground(BACKGROUND_COLOR);
        addMouseListener(new SlotClickListener());
    }

    /**
     * Called when slot is clicked, calculates column position and
     * triggers event in main view.
     */
    class SlotClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            Slot clickedSlot = (Slot) e.getSource();
            Point slotLocation = clickedSlot.getLocation();
            Rectangle boundRect = clickedSlot.getBounds();
            int col = slotLocation.x / boundRect.width;
            parent.columnClickedEvent(col + 1);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int height = getSize().height;
        int width = getSize().width;
        int radFactor = width;

        // Choose factor for radius so circles do not cut each other.
        if (width > height) {
            radFactor = height;
        }

        int radius = (int) (Math.round(radFactor * 0.9));
        int posFactorX = (int) (Math.round(width * 0.05));
        int posFactorY = (int) (Math.round(height * 0.05));

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(circleColor);
        graphics2D.fillOval(posFactorX, posFactorY, radius, radius);

        if (witness) {
            //TODO witness positioning
            int posX = (int) (Math.round(radius * 0.3));
            int posY = (int) (Math.round(radius * 0.3));

            graphics2D.setColor(Color.BLACK);
            graphics2D.fillOval((int) (radFactor * 0.22), (int)(radFactor * 0.22), radius/2, radius/2);
        }
    }

    /**
     * Sets the color that for the circle that represents the slot.
     *
     * @param color
     */
    public void setCircleColor(Color color) {
        circleColor = color;
        repaint();
    }

    /**
     * Mark or un-mark slot as witness.
     *
     * @param isWitness True if slot should be displayed as witness.
     */
    public void setWitness(boolean isWitness) {
        this.witness = isWitness;
    }

    /**
     * Checks if slot is witness.
     *
     * @return True if slot is a witness.
     */
    public boolean isWitness() {
        return witness;
    }
}
