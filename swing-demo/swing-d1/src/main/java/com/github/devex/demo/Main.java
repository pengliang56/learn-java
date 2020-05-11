package com.github.devex.demo;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createGUI);
    }

    private static void createGUI() {
        CustomJFrame frame = new CustomJFrame("demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 650);
        frame.setVisible(true);
    }
}
