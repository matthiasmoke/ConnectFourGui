import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Slot extends JPanel {

    private static final Color BACKGROUND_COLOR = Color.BLUE;
    private Color circleColor;
    private View parent;

    public Slot() {
        circleColor = Color.WHITE;
    }

    public Slot(Dimension size, View parent) {
        circleColor = Color.WHITE;
        this.parent = parent;

        setPreferredSize(size);
        setBackground(BACKGROUND_COLOR);
        addMouseListener(new SlotClickListener());
    }

    class SlotClickListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            Slot clickedSlot = (Slot) e.getSource();
            Rectangle r = clickedSlot.getBounds();
            Point p = clickedSlot.getLocation();
            int col = p.x / r.width;
            parent.columnClickedEvent(col + 1);
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int radius = (int) calcRadius();

        g.setColor(circleColor);
        g.fillOval(0, 0, radius, radius);
    }

    private double calcRadius() {
        int width = getPreferredSize().width;
        return width * 0.9;
    }

    public void setCircleColor(Color color) {
        circleColor = color;
        repaint();
    }
}
