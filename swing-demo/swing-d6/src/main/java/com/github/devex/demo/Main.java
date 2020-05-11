package com.github.devex.demo;

import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createGUI);
    }

    private static void createGUI() {
        JFrame frame = new JFrame("test table");


        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("id");
        model.addColumn("name");
        model.addColumn("menu");
        JTable table = new JTable(model);
        table.setShowGrid(false);
        // table.setShowHorizontalLines(false);
        // table.setShowVerticalLines(false);

        //table.setEnabled(false);

        TableColumn tableColumn = table.getColumnModel().getColumn(0);
        tableColumn.setCellRenderer(new MyRenderer());

        //table.setBorder(BorderFactory.createBevelBorder(EtchedBorder.RAISED));

        TableColumn column = table.getColumnModel().getColumn(1);//0是代表的第一列
        column.setPreferredWidth(4);

        Vector<Object> v1 = new Vector<>();
        Vector<Object> v2 = new Vector<>();
        Vector<Object> v3 = new Vector<>();
        v1.add("1");
        v1.add("a");
        v2.add("2");
        v2.add("aasd");
        v3.add("3");
        v3.add("awqe");

        model.addRow(v1);
        model.addRow(v2);
        model.addRow(v3);

        frame.add(table);
        frame.setBounds(300, 300, 600, 500);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
