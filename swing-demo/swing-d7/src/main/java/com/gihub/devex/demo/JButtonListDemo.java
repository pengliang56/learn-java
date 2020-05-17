package com.gihub.devex.demo;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * A JList of JButtons.
 */
public class JButtonListDemo implements Runnable
{
    private JList jlist;

    public static void main(String args[])
    {
        SwingUtilities.invokeLater(new JButtonListDemo());
    }

    public void run()
    {
        Object[] items = new ButtonItem[] {
                new ButtonItem("Apple"),
                new ButtonItem("Banana"),
                new ButtonItem("Carrot"),
                new ButtonItem("Date"),
                new ButtonItem("Eggplant"),
                new ButtonItem("Fig"),
                new ButtonItem("Guava"),
        };

        jlist = new JList(items);
        jlist.setCellRenderer(new ButtonListRenderer());
        jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jlist.setVisibleRowCount(5);
        jlist.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent event)
            {
                clickButtonAt(event.getPoint());
            }
        });

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JScrollPane(jlist));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void clickButtonAt(Point point)
    {
        int index = jlist.locationToIndex(point);
        ButtonItem item = (ButtonItem) jlist.getModel().getElementAt(index);
        item.getButton().doClick();
//    jlist.repaint(jlist.getCellBounds(index, index));
    }

    public class ButtonItem
    {
        private JButton button;

        public ButtonItem(String name)
        {
            this.button = new JButton(name);
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println(button.getText() + " was clicked.");
                }
            });
        }

        public JButton getButton()
        {
            return button;
        }

        @Override
        public String toString()
        {
            return button.getText();
        }
    }

    class ButtonListRenderer extends JButton implements ListCellRenderer
    {
        public Component getListCellRendererComponent(JList comp, Object value, int index,
                                                      boolean isSelected, boolean hasFocus)
        {
            setEnabled(comp.isEnabled());
            setFont(comp.getFont());
            setText(value.toString());

            if (isSelected)
            {
                setBackground(comp.getSelectionBackground());
                setForeground(comp.getSelectionForeground());
            }
            else
            {
                setBackground(comp.getBackground());
                setForeground(comp.getForeground());
            }

            return this;
        }
    }
}