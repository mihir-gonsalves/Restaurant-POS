package com.webdevwizards.revsGUI.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import javax.swing.JOptionPane;


import javax.swing.JFrame;

public class AddOrder {
    private static final String INSERT_ORDER_QUERY = "INSERT INTO c_orders (c_order_date, c_order_time, c_order_subtotal, c_order_tax, c_order_total, c_order_payment_type) VALUES ( ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ORDER_ITEM_QUERY = "INSERT INTO c_oti (c_order_id, item_id, item_quantity) VALUES (?, ?, ?)";
    private static final String SELECT_INGREDIENT= "SELECT ingredient_id, ingredient_quantity FROM item_to_ingredient_list WHERE item_id = ?;";
    private static final String SELECT_INGREDIENT_NAME = "SELECT ingredient_current_stock FROM ingredients WHERE ingredient_id = ?;";
    private static final String UPDATE_INGREDIENT_COUNT= "UPDATE ingredients SET ingredient_current_stock = ? WHERE ingredient_id = ?;";
    private void setOrderDetails(PreparedStatement preparedStatement, String subtotal, String paymenttype) throws SQLException {
        preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
        preparedStatement.setTime(2, new Time(System.currentTimeMillis()));
        preparedStatement.setDouble(3, Double.parseDouble(subtotal));
        preparedStatement.setDouble(4, 0.0825* Double.parseDouble(subtotal));
        preparedStatement.setDouble(5, 1.0825* Double.parseDouble(subtotal));
        preparedStatement.setString(6, paymenttype);
    }
    private void insert_order_item(int order_id, int[][] orderItems, Connection connection) throws SQLException {
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
    private void updateIngredientCount(int ingredient_id, int ingredient_quantity, Connection connection) throws SQLException {
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
        try (Connection connection = DatabaseManager.getConnection();
        PreparedStatement preparedStatementInsert = connection.prepareStatement(INSERT_ORDER_QUERY, Statement.RETURN_GENERATED_KEYS)) {
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