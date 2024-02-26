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

public class MainScreen extends JFrame implements ActionListener{
    // Fields for main screen components...
    static JFrame f;
    static JTextArea textArea;
    public MainScreen() {
        // Initialize the main screen...
        //Building the connection
        Connection conn = null;

        // Load config
        Properties prop = new Properties();
        String rootPath = System.getProperty("user.dir");
        try (InputStream input = new FileInputStream(rootPath + "/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            // Handle exception (file not found or unable to read)
            ex.printStackTrace();
        }

        // Load local config, if available
        try (InputStream localInput = new FileInputStream(rootPath + "/config-local.properties")) {
            prop.load(localInput);
        } catch (IOException ex) {
            // Local config not found, using default values
        }

        // Get values
        String database_name = prop.getProperty("db.name");
        String database_user = prop.getProperty("db.user");
        String database_password = prop.getProperty("db.password");

        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(database_url, database_user, database_password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        JOptionPane.showMessageDialog(null,"Opened database successfully");

        String name = "";
        try{
        //create a statement object
        Statement stmt = conn.createStatement();
        //create a SQL statement
        //TODO Step 2 (see line 8)
        String sqlStatement = "SELECT * FROM customer_order LIMIT 1;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sqlStatement);
        while (result.next()) {
          // TODO you probably need to change the column name tat you are retrieving
          //      this command gets the data from the "name" attribute
          name += result.getString("c_order_id")+"\n";
        }
        } catch (Exception e){
            JOptionPane.showMessageDialog(null,"Error accessing Database.");
        }
    }

    public void actionPerformed(ActionEvent e) {
        // Handle user interactions...
    }
}
