package com.github.devex.demo;

import java.awt.*;

import javax.swing.*;

public class CustomJFrame extends JFrame {
    public CustomJFrame(String titleCustomJFrame) {
        super(titleCustomJFrame);
        addControls(initContainerCustomJFrame());
    }

    private void addControls(Container container) {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(true);
        JLabel nameLabel = new JLabel("姓名");
        JTextField nameTextField = new JTextField(16);
        JButton button = new JButton("提交");
        JComboBox<JButton> comboBox = new JComboBox<>();


        button.addActionListener(e -> JOptionPane.showMessageDialog(this, nameTextField.getText()));
        checkBox.addActionListener(e -> {
            if (checkBox.isSelected()) {
                nameTextField.setEnabled(true);
            } else {
                nameTextField.setEnabled(false);
            }
        });

        container.add(comboBox);
        container.add(checkBox);
        container.add(nameLabel);
        container.add(nameTextField);
        container.add(button);
    }


    private Container initContainerCustomJFrame() {
        Container container = getContentPane();
        container.setLayout(new FlowLayout());
        return container;
    }
}
