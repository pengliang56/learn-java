package com.github.devex.demo;

import java.awt.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createGUI);
    }

    private static void createGUI() {
        JFrame frame = new JFrame("测试JFrame");
        frame.setBounds(300, 300, 600, 500);

        JList<String> showPerson = new JList<>();
        // 调用数据项的 toString() 方法来显示
        DefaultListModel<String> personModel = new DefaultListModel<>();
        personModel.addElement("张三");
        personModel.addElement("李四");
        personModel.addElement("王五");
        personModel.addElement("Alice");
        personModel.addElement("Mr.Wang");
        personModel.addElement("Tom");
        personModel.addElement("Jack");
        personModel.addElement("Jackson");
        personModel.addElement("Spring");

        showPerson.setModel(personModel);
        // 设置单选模式
        showPerson.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        showPerson.setCellRenderer(new ListCellRenderer<String>() {
            JPanel panel = new JPanel();
            JLabel j2 = new JLabel();
            JLabel j3 = new JLabel();
            JButton button = new JButton("确定");
            @Override
            /**
             * @param list 列表控件
             * @param value 列表项的值
             * @param index 索引
             * @param isSelected 该项是否被选中
             * @param cellHasFocus 该项是否是焦点
             */
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                /*// 设置水平对齐
                label.setHorizontalAlignment(SwingConstants.CENTER);
                // 设置文本
                label.setText(value);
                //设置边框
                label.setBorder(BorderFactory.createEmptyBorder(30, 4, 10, 4));
                // 设置背景是否透明
                label.setOpaque(true);*/

                // label.setFont();

                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                    panel.setForeground(list.getSelectionForeground());
                } else {
                    panel.setBackground(list.getBackground());
                    panel.setForeground(list.getForeground());
                }
                j2.setText("asdsd");
                j3.setText(value);
                panel.add(j2);
                panel.add(j3);
                panel.add(button);
                panel.setLayout(new FlowLayout());
                return panel;
            }
        });

        // 数据表的滚动模式
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(showPerson);
        frame.add(scrollPane);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
