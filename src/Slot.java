import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Slot extends JPanel {

    private static final Color BACKGROUND_COLOR = Color.BLUE;
    private Color circleColor;
    private View parent;

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
            Rectangle r = clickedSlot.getBounds();
            Point p = clickedSlot.getLocation();
            int col = p.x / r.width;
            parent.columnClickedEvent(col + 1);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        int radius = (int) calcRadius();

        graphics2D.setColor(circleColor);
        graphics2D.fillOval(0, 0, radius, radius);
    }

    /**
     * Calculates circle radius for checker circle.
     *
     * @return Radius for circle drawing.
     */
    private double calcRadius() {
        int width = getPreferredSize().width;
        return width * 0.9;
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
}
