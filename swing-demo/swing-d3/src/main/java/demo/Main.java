package demo;

import java.awt.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createGUI);
    }

    private static void createGUI() {
        JFrame frame = new JFrame("Care Layout");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(300, 300, 600, 550);

        JComboBox<String> comboBox = new JComboBox<>();
        JPanel cards = new JPanel();

        comboBox.addItem("面板一");
        comboBox.addItem("面板二");

        JPanel p1 = new JPanel();
        p1.add(new JTextField(20));

        JPanel p2 = new JPanel();
        p2.add(new JButton("hah"));
        p2.add(new JButton("234"));
        p2.add(new JButton("dfg"));

        cards.setLayout(new CardLayout());
        cards.add(p1, "p1");
        cards.add(p2, "p2");

        /*comboBox.addActionListener(e -> {
            CardLayout cardLayout = (CardLayout) cards.getLayout();
            int in = comboBox.getSelectedIndex();
            if (in == 0) {
                cardLayout.show(cards, "p1");
            } else {
                cardLayout.show(cards, "p2");
            }
        });*/

        frame.add(cards, BorderLayout.CENTER);
        frame.add(comboBox, BorderLayout.NORTH);
        frame.setVisible(true);
    }
}
