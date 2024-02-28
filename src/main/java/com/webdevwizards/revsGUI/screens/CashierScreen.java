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
    JButton orderCompleteButton;
    
    
    public CashierScreen() {
        frame = new JFrame("Point of Sale");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Left panel for categories
        navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(8, 1)); // Changed to GridLayout for icons
        // Add icons for each category
        navPanel.setPreferredSize(new Dimension(100, 600));
        // for (int i = 0; i <= 6; i++) {
        //     // leftPanel.add(new JButton(new ImageIcon("icon_" + i + ".png")));
        //     navPanel.add(new JButton(resizeIcon("./images/icon_" + i + ".png", 70, 70)));
        // }

        // Middle panel for items
        JPanel middlePanel = new JPanel(new BorderLayout()); // Changed to BorderLayout
        middlePanel.setPreferredSize(new Dimension(600, 600));
        
        // Added search bar at the top
        JTextField searchBar = new JTextField("Enter an item name to search");
        middlePanel.add(searchBar, BorderLayout.NORTH);
        
        // Changed layout to GridLayout for items 
        itemsPanel = new JPanel(new GridLayout(3, 2)); // Increase grid size as needed

        // Wrap itemsPanel in a JScrollPane
        JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);

        middlePanel.add(itemsScrollPane, BorderLayout.CENTER); // Changed this line to add the scroll pane instead of itemsPanel directly
        
        
        middlePanel.add(itemsPanel, BorderLayout.CENTER);

        // Right panel for order items
        orderPanel = new JPanel();
        orderPanel.setLayout(new BorderLayout()); // Changed to BorderLayout for better organization
        orderPanel.setPreferredSize(new Dimension(200, 600));

        JLabel orderItemsLabel = new JLabel("Order Items");
        orderPanel.add(orderItemsLabel, BorderLayout.NORTH);

        // Panel for adding text fields
        JPanel orderFieldsPanel = new JPanel();
        orderFieldsPanel.setLayout(new GridLayout(3, 1)); // Use GridLayout to organize text fields

        // Bottom panel for cashier and subtotal
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        
        bottomPanel.add(new JLabel("Cashier Name: Ritchey"));
        bottomPanel.add(new JLabel(" Order Subtotal: $0.00"));
        orderCompleteButton = new JButton(" Order Complete");
        bottomPanel.add(orderCompleteButton);

        frame.add(navPanel, BorderLayout.WEST);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.add(orderPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();
    }

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
    
    public JPanel getOrderFieldsPanel() {
        if (orderFieldsPanel == null) {
            orderFieldsPanel = new JPanel();
            orderFieldsPanel.setLayout(new GridLayout(3, 1));
        }
        return orderFieldsPanel;
    }

    public JButton getOrderCompleteButton() {
        return orderCompleteButton;
    }
    
}
