import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Slot extends JPanel {

    private static final Color BACKGROUND_COLOR = Color.BLUE;
    private Color circleColor;
    private View parent;
    private boolean witness = false;

    public Slot(Dimension size, View parent) {
        circleColor = Color.WHITE;
        this.parent = parent;

        setPreferredSize(size);
        setBackground(BACKGROUND_COLOR);
        addMouseListener(new SlotClickListener());
    }

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

        int width = getSize().width;
        int radius = (int) (Math.round(width * 0.9));
        int posFactorX = (int) (Math.round(width * 0.05));
        int posFactorY = (int) (Math.round(getSize().height * 0.02));

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(circleColor);
        graphics2D.fillOval(posFactorX, posFactorY, radius, radius);

        if (witness) {
            int positionFactor = (int) (width * 0.22);

            graphics2D.setColor(Color.BLACK);
            graphics2D.fillOval(positionFactor, positionFactor,
                    radius/2, radius/2);
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

    public void setWitness(boolean isWitness) {
        this.witness = isWitness;
    }

    public boolean isWitness() {
        return witness;
    }
}
