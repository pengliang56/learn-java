package demo;

import java.awt.*;

import javax.swing.*;

public class CustomJFrame extends JFrame {
    public CustomJFrame(String titleCustomJFrame) {
        super(titleCustomJFrame);
        setLayout(null);
        addControls(initContainerCustomJFrame());
    }

    private void addControls(Container container) {
        JLabel yellow = new JLabel("1");
        JLabel green = new JLabel("2");
        JLabel gray = new JLabel("3");
        JLabel blue = new JLabel("4");
        JLabel black = new JLabel("5");

        yellow.setBounds(10, 10, 20, 20);
        green.setBounds(20, 10, 20, 20);
        gray.setBounds(30, 10, 20, 20);
        blue.setBounds(40, 10, 20, 20);
        black.setBounds(50, 10, 20, 20);

        multicolorLabel(yellow, Color.YELLOW);
        multicolorLabel(green, Color.green);
        multicolorLabel(gray, Color.gray);
        //multicolorLabel(blue, Color.blue);
        multicolorLabel(black, Color.black);

        add(black);
        add(yellow);
        add(green);
        add(gray);
        //add(blue);
    }

    private void multicolorLabel(JLabel yellow, Color color) {
        yellow.setOpaque(true);
        yellow.setPreferredSize(new Dimension(60, 30));
        yellow.setHorizontalAlignment(SwingConstants.CENTER);
        yellow.setBackground(color);
    }


    private Container initContainerCustomJFrame() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        return container;
    }
}
