package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.webdevwizards.revsGUI.database.DatabaseManager;

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
        JButton testButton = new JButton("Ingredients in cheeseburger");
        testButton.addActionListener(e -> TableQuery("SELECT ingredients.ingredient_name\r\n" + //
        "\r\n" + //
        "FROM menu_items\r\n" + //
        "\r\n" + //
        "JOIN item_to_ingredient_list ON menu_items.item_id = item_to_ingredient_list.item_id\r\n" + //
        "\r\n" + //
        "JOIN ingredients ON item_to_ingredient_list.ingredient_id = ingredients.ingredient_id\r\n" + //
        "\r\n" + //
        "WHERE menu_items.item_name = 'Cheeseburger';"));
        buttonsPanel.add(testButton);
        testButton = new JButton("Ingredients by manager");
        testButton.addActionListener(e -> TableQuery("SELECT ingredient_id, SUM(ingredient_quantity) AS total_quantity_ordered\r\n" + //
                        "FROM M_order_to_ingredient_list\r\n" + //
                        "GROUP BY ingredient_id\r\n" + //
                        "ORDER BY total_quantity_ordered DESC;\r\n" + //
                        ""));
        buttonsPanel.add(testButton);
        testButton = new JButton("Ingredients less than 25");
        testButton.addActionListener(e -> TableQuery("SELECT *\r\n" + //
                        "\r\n" + //
                        "FROM ingredients\r\n" + //
                        "\r\n" + //
                        "WHERE ingredient_current_stock < 25\r\n" + //
                        "\r\n" + //
                        "ORDER BY ingredient_current_stock ASC;"));
        buttonsPanel.add(testButton);
        testButton = new JButton("Ingredients most stock");
        testButton.addActionListener(e -> TableQuery("SELECT *\r\n" + //
                        "\r\n" + //
                        "FROM ingredients\r\n" + //
                        "\r\n" + //
                        "ORDER BY ingredient_current_stock DESC;"));
        buttonsPanel.add(testButton);
        // Add more buttons as needed

        add(buttonsPanel, BorderLayout.EAST);

        setVisible(true);
    }
    private void TableQuery(String query) {
        try {
            DatabaseManager db = new DatabaseManager();
            ResultSet rs = db.executeQuery(query);
            if (rs != null) {
                ResultSetMetaData data = rs.getMetaData();
                int cols = data.getColumnCount();
                // clear if data exists
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);

                // create columns with count
                for (int i = 1; i <= cols; i++) {
                    tableModel.addColumn(data.getColumnName(i));
                }

                // add rows
                while (rs.next()) {
                    Vector<Object> v = new Vector<Object>();
                    for (int i = 1; i <= cols; i++) {
                        v.add(rs.getObject(i));
                    }
                    tableModel.addRow(v);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to fetch data: " + e.getMessage());
        }
    }
}
