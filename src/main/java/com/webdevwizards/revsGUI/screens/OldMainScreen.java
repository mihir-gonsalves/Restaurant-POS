package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class OldMainScreen extends JFrame implements ActionListener{
    // Components
    static JFrame frame;
    static JPanel panel, gridPanel, navPanel;
    static JTextArea textArea;


    public OldMainScreen() {
        // Create connection to database
        Connection conn = connectToDatabase();
        
        frame = new JFrame("Rev's GUI: Main Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.gray);

        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(3, 3, 10, 10));
        gridPanel.setBackground(Color.green);

        String query = "SELECT * FROM menu_items;";
        ResultSet rs = runQuery(conn, query);
        // Create JTextArea objects using the queried data
        try {
            for (int i = 0; i < 6; i++) {
                if (rs.next()) {
                    textArea = new JTextArea(10, 20);
                    String itemName = rs.getString("item_name");
                    
                    textArea.setText(itemName);
                    textArea.setEditable(false);
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setBackground(Color.red);
                    JPanel textAreaPanel = new JPanel(new BorderLayout());
                    textAreaPanel.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            JPanel source = (JPanel) e.getSource();
                            JTextArea textArea = (JTextArea) source.getComponent(0); // Get the JTextArea from the JPanel
                            Font sourceFont = textArea.getFont();
                            float newSize = source.getHeight() / 20.0f;
                            int minSize = 18;
                            if (newSize < minSize) {
                                newSize = minSize;
                            }
                            textArea.setFont(sourceFont.deriveFont(newSize));
                            textArea.setPreferredSize(new Dimension(source.getWidth(), source.getHeight()));
                            textAreaPanel.revalidate();
                            textAreaPanel.repaint();
                        }
                    });
                    textAreaPanel.add(textArea, BorderLayout.CENTER);
                    gridPanel.add(textAreaPanel);
                }
                else {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: Failed to retrieve item_name from ResultSet");
        }

        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.blue);
        navPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                JPanel source = (JPanel) e.getSource();
                float newWidth = source.getWidth() / 15.0f;
                int minWidth = 20;
                if (newWidth < minWidth) {
                    newWidth = minWidth;
                }
                for (Component component : source.getComponents()) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        Font originalFont = button.getFont();
                        float newFontSize = source.getHeight() / 20.0f;
                        Font newFont = originalFont.deriveFont(newFontSize);
                        button.setFont(newFont);
                    }
                }
            }
        });
        JButton btn1 = new JButton("Burgers");
        JButton btn2 = new JButton("Chicken");
        JButton btn3 = new JButton("Sandwiches");
        JButton btn4 = new JButton("Value Deals");
        navPanel.add(btn1);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btn2);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btn3);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btn4);


        panel.add(gridPanel, BorderLayout.CENTER);
        panel.add(navPanel, BorderLayout.WEST);
        frame.add(panel);

        frame.setSize(500, 500);
        frame.setVisible(true);



    }


    public void actionPerformed(ActionEvent e) {
        // Handle user interactions...
    }

    // Connects java to the database
    // Returns the connection object
    public Connection connectToDatabase() {
        // Load default config
        Properties prop = new Properties();
        String rootPath = System.getProperty("user.dir");
        try (InputStream input = new FileInputStream(rootPath + "/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            // Handle exception (file not found or unable to read)
            System.out.println("Error: config.properties not found");
            ex.printStackTrace();
        }

        // Load local config, if available
        try (InputStream localInput = new FileInputStream(rootPath + "/config-local.properties")) {
            prop.load(localInput);
        } catch (IOException ex) {
            // Local config not found, using default values
            System.out.println("Error: config-local.properties not found");
        }

        // Get values
        String database_name = prop.getProperty("db.name");
        String database_user = prop.getProperty("db.user");
        String database_password = prop.getProperty("db.password");

        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(database_url, database_user, database_password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        return null;

    }
    
    // Runs a query on the database
    // Returns the result set of query
    public ResultSet runQuery(Connection conn, String query) {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (Exception e) {
            System.out.println("Error: Query failed");
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }
}
