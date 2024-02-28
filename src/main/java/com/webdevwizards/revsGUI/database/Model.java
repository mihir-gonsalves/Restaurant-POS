package com.webdevwizards.revsGUI.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import javax.swing.JOptionPane;


import javax.swing.JFrame;




public class Model {
    private static Connection conn = null;
    private static boolean initialized = false;

    public Model() {
        this.initialize();
    }

    public void initialize() {

        if (initialized) return; // Prevent re-initialization

        System.out.println("initializing database connection");

        Properties prop = new Properties();
        String rootPath = System.getProperty("user.dir");
        try (InputStream input = new FileInputStream("./config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load main configuration.");
            return;
        }

        try (InputStream localInput = new FileInputStream("./config-local.properties")) {
            prop.load(localInput);
        } catch (IOException ex) {
            // Local config not found, using default values
        }

        String database_name = prop.getProperty("db.name");
        String database_user = prop.getProperty("db.user");
        String database_password = prop.getProperty("db.password");
        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);

        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(database_url, database_user, database_password);
            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to open database connection.");
            System.exit(1);
        }
    }

    // Execute and return query results
    public static ResultSet executeQuery(String sql) {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return null;
        }
    }

    public static void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to close database connection.");
        }
    }

}
