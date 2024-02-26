package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TestScreen extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public TestScreen() {
        setTitle("Test Screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        
        // Test Button
        JButton testButton = new JButton("Test1");
        testButton.addActionListener(e -> System.out.println("clicked button 1"));
        buttonsPanel.add(testButton);
        testButton = new JButton("Test1");
        testButton.addActionListener(e -> System.out.println("clicked button 1"));
        buttonsPanel.add(testButton);
        testButton = new JButton("Test1");
        testButton.addActionListener(e -> System.out.println("clicked button 1"));
        buttonsPanel.add(testButton);
        testButton = new JButton("Test1");
        testButton.addActionListener(e -> System.out.println("clicked button 1"));
        buttonsPanel.add(testButton);
        // Add more buttons as needed

        

        add(buttonsPanel, BorderLayout.EAST);

        setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainScreen());
    }
}
