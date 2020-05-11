package com.github.devex.demo;

import javax.swing.*;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createGUI);
    }

    private static void createGUI() {
        CustomJFrame customJFrame = new CustomJFrame("custom");
        customJFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        customJFrame.setSize(800, 600);
        customJFrame.setVisible(true);
    }
}
