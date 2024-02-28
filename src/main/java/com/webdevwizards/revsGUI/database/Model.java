package com.webdevwizards.revsGUI.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import javax.swing.JOptionPane;


import javax.swing.JFrame;




public class Model {
    public static Connection conn = null;
    private static boolean initialized = false;
    public String phoneNumber;
    private static final String INSERT_ORDER_QUERY = "INSERT INTO c_orders (c_order_date, c_order_time, c_order_subtotal, c_order_tax, c_order_total, c_order_payment_type) VALUES ( ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ORDER_ITEM_QUERY = "INSERT INTO c_oti (c_order_id, item_id, item_quantity) VALUES (?, ?, ?)";
    private static final String SELECT_INGREDIENT= "SELECT ingredient_id, ingredient_quantity FROM item_to_ingredient_list WHERE item_id = ?;";
    private static final String SELECT_INGREDIENT_NAME = "SELECT ingredient_current_stock FROM ingredients WHERE ingredient_id = ?;";
    private static final String UPDATE_INGREDIENT_COUNT= "UPDATE ingredients SET ingredient_current_stock = ? WHERE ingredient_id = ?;";

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

    public Connection getConnection() {
        return conn;
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

    public boolean login(String phoneNumber) {
        try {
            String sql = "SELECT * FROM users WHERE phonenumber = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isManager() {
        try {
            String sql = "SELECT * FROM users WHERE phone_number = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("ismanager").equals("t")) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return;
    }

    public void setOrderDetails(PreparedStatement preparedStatement, String subtotal, String paymenttype) throws SQLException {
        preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
        preparedStatement.setTime(2, new Time(System.currentTimeMillis()));
        preparedStatement.setDouble(3, Double.parseDouble(subtotal));
        preparedStatement.setDouble(4, 0.0825* Double.parseDouble(subtotal));
        preparedStatement.setDouble(5, 1.0825* Double.parseDouble(subtotal));
        preparedStatement.setString(6, paymenttype);
    }

    public void insert_order_item(int order_id, int[][] orderItems, Connection connection) throws SQLException {
        try{

            // Insert order items
            for (int i = 0; i < orderItems.length; i++) {
                try (PreparedStatement preparedStatementInsertItem = connection.prepareStatement(INSERT_ORDER_ITEM_QUERY)) {
                    preparedStatementInsertItem.setInt(1, order_id);
                    preparedStatementInsertItem.setInt(2, orderItems[i][0]);
                    preparedStatementInsertItem.setInt(3, orderItems[i][1]);
                    preparedStatementInsertItem.executeUpdate();
                }
            }
            
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    } 

    public void updateIngredientCount(int ingredient_id, int ingredient_quantity, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_INGREDIENT_NAME)) {
            preparedStatement.setInt(1, ingredient_id);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            int current_stock = rs.getInt(1);
            try (PreparedStatement preparedStatementUpdate = connection.prepareStatement(UPDATE_INGREDIENT_COUNT)) {
                preparedStatementUpdate.setInt(1, current_stock - ingredient_quantity);
                preparedStatementUpdate.setInt(2, ingredient_id);
                preparedStatementUpdate.executeUpdate();
            }
        }
    }

    public boolean insert_order(String subtotal, int[][] orderItems, String paymenttype) {
        try (Connection connection = this.getConnection()) {
            PreparedStatement preparedStatementInsert = connection.prepareStatement(INSERT_ORDER_QUERY, Statement.RETURN_GENERATED_KEYS);
            // Set order details
            setOrderDetails(preparedStatementInsert, subtotal, paymenttype);
            preparedStatementInsert.executeUpdate();
            ResultSet rs = preparedStatementInsert.getGeneratedKeys();
            if(rs.next()){
            int order_id = rs.getInt(1);
                // Insert order items
                for (int i = 0; i < orderItems.length; i++) {
                    try (PreparedStatement preparedStatementInsertItem = connection.prepareStatement(INSERT_ORDER_ITEM_QUERY)) {
                        preparedStatementInsertItem.setInt(1, order_id);
                        preparedStatementInsertItem.setInt(2, orderItems[i][0]);
                        preparedStatementInsertItem.setInt(3, orderItems[i][1]);
                        preparedStatementInsertItem.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
