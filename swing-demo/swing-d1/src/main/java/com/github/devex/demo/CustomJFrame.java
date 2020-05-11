package com.github.devex.demo;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

public class CustomJFrame extends JFrame {
    final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public CustomJFrame(String titleCustomJFrame) {
        super(titleCustomJFrame);
        addControls(initContainerCustomJFrame());
    }

    private void addControls(Container container) {
        JLabel label = new JLabel(LocalDateTime.now().format(TIME_FORMATTER));
        label.setFont(new Font("微软雅黑", 0 , 20));
        label.setBackground(Color.YELLOW);
        label.setForeground(Color.BLUE);
        JButton bt1 = new JButton("SWITCH");

        bt1.addActionListener(event -> label.setText(LocalDateTime.now().format(TIME_FORMATTER)));

        container.add(label);
        container.add(bt1);
    }

    private Container initContainerCustomJFrame() {
        Container container = getContentPane();
        container.setLayout(new FlowLayout());
        return container;
    }
}
