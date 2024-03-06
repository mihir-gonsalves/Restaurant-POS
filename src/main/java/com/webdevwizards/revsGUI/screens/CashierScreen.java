package com.webdevwizards.revsGUI.screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.webdevwizards.revsGUI.database.Model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CashierScreen extends JFrame{
    JFrame frame;
    JPanel itemsPanel;
    JPanel navPanel;
    JPanel orderPanel;
    JPanel orderFieldsPanel;
    JPanel bottomPanel;
    JButton orderCompleteButton;
    
    
    public CashierScreen() {
        frame = new JFrame("Rev's GUI: Cashier Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Left panel for navigational categories
        navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(8, 1)); // Changed to GridLayout for icons
        navPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // Middle panel for items
        JPanel middlePanel = new JPanel(new BorderLayout()); // Changed to BorderLayout
        // middlePanel.setPreferredSize(new Dimension(600, 600));
        
        // Changed layout to GridLayout for items 
        itemsPanel = new JPanel(new GridLayout(3, 2)); // Increase grid size as needed

        // Wrap itemsPanel in a JScrollPane
        JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);

        middlePanel.add(itemsScrollPane, BorderLayout.CENTER); // Changed this line to add the scroll pane instead of itemsPanel directly
        
        
        middlePanel.add(itemsPanel, BorderLayout.CENTER);

        middlePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));


        // Right panel for order items
        orderPanel = new JPanel();
        orderPanel.setLayout(new BorderLayout()); // Changed to BorderLayout for better organization
        orderPanel.setPreferredSize(new Dimension(200, 600));
        orderPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));


        // Panel for adding text fields
        orderFieldsPanel = new JPanel();
        orderFieldsPanel.setLayout(new BoxLayout(orderFieldsPanel, BoxLayout.Y_AXIS));

        //create bottom panel
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        frame.add(navPanel, BorderLayout.WEST);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.add(orderPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        
        frame.pack();
    }

    
    /** 
     * @return JFrame
     */
    /*
     * GETTERS AND SETTERS
     */
    public JFrame getFrame() {
        return frame;
    }

    public JPanel getItemsPanel() {
        return itemsPanel;
    }

    public JPanel getNavPanel() {
        return navPanel;
    }

    public JPanel getOrderPanel() {
        return orderPanel;
    }

    public JPanel getBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
            bottomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }
        return bottomPanel;
    }
    
    public JPanel getOrderFieldsPanel() {
        if (orderFieldsPanel == null) {
            orderFieldsPanel = new JPanel();
            orderFieldsPanel.setLayout(new BoxLayout(orderFieldsPanel, BoxLayout.Y_AXIS));
        }
        return orderFieldsPanel;
    }

    public JButton getOrderCompleteButton() {
        if (orderCompleteButton == null) {
            orderCompleteButton = new JButton("Order Complete");
        }
        return orderCompleteButton;
    }

    public void setOrderCompleteButton(JButton orderCompleteButton) {
        this.orderCompleteButton = orderCompleteButton;
    }
}
