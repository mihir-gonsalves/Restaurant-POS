

<<<<<<< Updated upstream
    package com.webdevwizards.revsGUI.database;
=======
package com.webdevwizards.revsGUI.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.naming.spi.DirStateFactory.Result;
import javax.swing.JFrame;



/**
 * Model class for the database
 * @author Caden, Carson, Jesung, Kevin
 */
public class Model {
    public static Connection conn = null;
    private static boolean initialized = false;

    /**
     * The phone number of the user
     */
    public String phoneNumber;
    /**
     * Constructor for the Model class : just calls initialize
     */
    public Model() {
        this.initialize();
    }

    /**
     * Sets up config file and initializes the database connection
     */
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
>>>>>>> Stashed changes

    import java.io.FileInputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Properties;
    import javax.swing.JOptionPane;
    import javax.naming.spi.DirStateFactory.Result;
    import javax.swing.JFrame;
        
    
    
    
    public class Model {
        private static Connection conn = null;
        private static boolean initialized = false;
        public String phoneNumber;
    
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
    
        public boolean isManager(String phoneNumber) {
            try {
                String sql = "SELECT * FROM users WHERE phonenumber = ?";
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
                    if (orderItems[i][0] == 0) {
                        continue;
                    }
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
            }// Done
        } 
        private boolean checkSelectIngredient(int ingredient_id, int ingredient_quantity , Connection connection) throws SQLException {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_INGREDIENT_NAME)) {
                preparedStatement.setInt(1, ingredient_id);
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                int current_stock = rs.getInt(1);
                if (current_stock < ingredient_quantity) {
                    return false;
                }
                return true;
            }//Done
        }
        public static void mergeLists(List<Integer> list1, List<Integer> list2, Map<Integer, Integer> resultMap) {
            for (int i = 0; i < list1.size(); i++) {
                int id = list1.get(i);
                int quantity = list2.get(i);
                resultMap.put(id, resultMap.getOrDefault(id, 0) + quantity);
            }
        }
        private boolean selectIngredient(int[][] orderItems , Connection connection) throws SQLException {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_INGREDIENT)) {
                for (int i = 0; i < orderItems.length; i++) {
                    if (orderItems[i][0] == 0) {
                        continue;
                    }
                    int item_id = orderItems[i][0];
                    int count = orderItems[i][1];
                    preparedStatement.setInt(1, item_id);
                    ResultSet rs = preparedStatement.executeQuery();
                    ArrayList<Integer> ingredient_ids = new ArrayList<Integer>();
                    ArrayList<Integer> ingredient_quantities = new ArrayList<Integer>();
                    
                    while (rs.next()) {
                        ingredient_ids.add(rs.getInt(1));
                        ingredient_quantities.add(rs.getInt(2));
                    }
                    Map<Integer, Integer> mergedMap = new HashMap<>();
                    mergeLists(ingredient_ids, ingredient_quantities, mergedMap);
                    List<Integer> mergedListid = new ArrayList<>(mergedMap.keySet());
                    List<Integer> mergedListquant = new ArrayList<>(mergedMap.values());
                    for (int j = 0; j < mergedListid.size(); j++) {
                        if(checkSelectIngredient(mergedListid.get(j), mergedListquant.get(j), connection) == false){
                            throw new SQLException("Not enough stock for ingredients" + mergedListid.get(j));
                        }
                    }
                    for (int j = 0; j < mergedListid.size(); j++) {
                        updateIngredientCount(mergedListid.get(j), mergedListquant.get(j), connection);
                    }
                }
                return true;
            }
    
        }//Done? 
    
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
            try{
                PreparedStatement preparedStatementInsert = conn.prepareStatement(INSERT_ORDER_QUERY, Statement.RETURN_GENERATED_KEYS);
                // Set order details
                if(selectIngredient(orderItems, conn) == false){
                    return false;
                }
                setOrderDetails(preparedStatementInsert, subtotal, paymenttype);
                preparedStatementInsert.executeUpdate();
                ResultSet rs = preparedStatementInsert.getGeneratedKeys();
                if(rs.next()){
                    int order_id = rs.getInt(1);
                    insert_order_item(order_id, orderItems, conn);
                }
                return true;
                
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    
        public static boolean updateItem(int item_id, String category, String check){ //When manager needs to update the price of a menu_item
            try{
                PreparedStatement statement = conn.prepareStatement("update menu_items set ? = ? where item_id = ?");
                statement.setString(1, category);
                System.out.println(check);
                if (category.equals("item_price")) {
                    statement.setDouble(2, Double.parseDouble(check));
                } else {
                    statement.setString(2, check);
                }
                statement.setInt(3,item_id);
                statement.execute();
                statement.close();
                return true;
    
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return false;
            }
        }

        public static boolean createItem(int item_id, String item_name, String item_price, String item_category){ //When manager needs to update the price of a menu_item
            try{
                PreparedStatement statement = conn.prepareStatement("INSERT INTO menu_items (item_id, item_name, item_price, category) VALUES (?, ?, ?, ?)");
                statement.setInt(1, item_id);
                statement.setString(2, item_name);
                statement.setDouble(3, Double.parseDouble(item_price));
                statement.setString(4, item_category);

                statement.execute();
                statement.close();
                return true;
    
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return false;
            }
        }
        public static boolean updateIngredient(int ingredient_id, String category, String value){ //When manager needs to update the price of a menu_item
            try{
                PreparedStatement statement = conn.prepareStatement("update ingredients set ? = ? where ingredient_id = ?");
                statement.setString(1, category);
                statement.setString(2, value);
                statement.setInt(3, ingredient_id);
                statement.execute();
                statement.close();
                return true;
    
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return false;
            }
        }
    
    
        public static ResultSet viewIngredient(int ingredient_id){
            try{
                PreparedStatement statement = conn.prepareStatement("select * from ingredients where ingredient_id = ?");
                statement.setInt(1, ingredient_id);
                ResultSet rs = statement.executeQuery();
                return rs;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return null;
            }
        }



        public static ResultSet getAllIngredients(){
            try{
                PreparedStatement statement = conn.prepareStatement("select * from ingredients order by ingredient_id");
                ResultSet rs = statement.executeQuery();
                return rs;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return null;
            }
        }
        public static ResultSet getAllMenus(){
            try{
                PreparedStatement statement = conn.prepareStatement("select * from menu_items order by item_id");
                ResultSet rs = statement.executeQuery();
                return rs;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return null;
            }
        }

        public static boolean addIngredient(String ingredient_id, String ingredient_quantity, String phoneNumber){
            try{
                PreparedStatement statement = conn.prepareStatement("SELECT * FROM ingredients WHERE ingredient_id = ?");
                int ingredient_num = Integer.parseInt(ingredient_quantity);
                statement.setInt(1, Integer.parseInt(ingredient_id));
                ResultSet rs = statement.executeQuery();
                Double ingredientPrice = 0.00;
                if(rs.next()){
                    int current_stock = rs.getInt(3);
                    ingredientPrice = rs.getDouble(4);
                    int new_stock = current_stock + Integer.parseInt(ingredient_quantity);
                    PreparedStatement statement2 = conn.prepareStatement("UPDATE ingredients SET ingredient_current_stock = ? WHERE ingredient_id = ?");
                    statement2.setInt(1, new_stock);
                    statement2.setInt(2, Integer.parseInt(ingredient_id));
                    statement2.execute();

                    
                    Double order_total = ingredient_num * ingredientPrice;
                    PreparedStatement statement3 = conn.prepareStatement("INSERT INTO manager_order (m_order_date, m_order_time, m_order_total, phonenumber) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    statement3.setDate(1, new Date(System.currentTimeMillis()));
                    statement3.setTime(2, new Time(System.currentTimeMillis()));
                    statement3.setDouble(3, order_total);
                    statement3.setString(4, phoneNumber);

                    statement3.execute();
                    ResultSet rs3 = statement3.getGeneratedKeys();
                    if(rs3.next()){
                        PreparedStatement statement4 = conn.prepareStatement("INSERT INTO m_order_to_ingredient_list (m_order_id, ingredient_id, ingredient_quantity) VALUES (?, ?, ?)");
                        statement4.setInt(1, rs3.getInt(1));
                        statement4.setInt(2, Integer.parseInt(ingredient_id));
                        statement4.setInt(3, ingredient_num);
                        statement4.execute();
                    }
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return false;
            }
        }
    
        public static ResultSet viewItem(int item_id){
            try{
                PreparedStatement statement = conn.prepareStatement("select * from menu_items where item_id = ?");
                statement.setInt(1, item_id);
                ResultSet rs = statement.executeQuery();
                return rs;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return null;
            }
        }
    
        public static int getItemID(String item_name){
            try{
                PreparedStatement statement = conn.prepareStatement("select item_id from menu_items where item_name = ?");
                statement.setString(1, item_name);
                ResultSet rs = statement.executeQuery();
                rs.next();
                return rs.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return -1;
            }
        }
        
        
        public static String getItemName(int item_id){
            try{
                PreparedStatement statement = conn.prepareStatement("select item_name from menu_items where item_id = ?");
                statement.setInt(1, item_id);
                ResultSet rs = statement.executeQuery();
                rs.next();
                return rs.getString(1);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return "-1";
            }
        }
    
        public static String getItemPrice(int item_id){
            try{
                PreparedStatement statement = conn.prepareStatement("select item_price from menu_items where item_id = ?");
                statement.setInt(1, item_id);
                System.out.println("item_id: " + item_id);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getString("item_price");
                } else {
                    throw new SQLException("No item found with id " + item_id);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return "-1";
            }
        }
    
        public static float sumItemPrices(int[][] orderItems) {
            float sum = 0;
            for (int i = 0; i < orderItems.length; i++) {
                int item_id = orderItems[i][0];
                int quantity = orderItems[i][1];
                if (item_id != 0) {
                    sum += Float.parseFloat(getItemPrice(item_id)) * quantity;
                }
            }
            return sum;
        }

    public static String getUserName(String phoneNumber){
        try{
            PreparedStatement statement = conn.prepareStatement("select name from users where phonenumber = ?");
            statement.setString(1, phoneNumber);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return "-1";
        }
    }

    public static boolean insertUser(String phonenumber, String name, boolean ismanager){
        try{
            PreparedStatement statement = conn.prepareStatement("insert into users (phonenumber, name, ismanager) values (?, ?, ?)");
            statement.setString(1, phonenumber);
            statement.setString(2, name);
            statement.setBoolean(3, ismanager);
            statement.execute();
            statement.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(String table, int id, int value){
        try{
            PreparedStatement statement = conn.prepareStatement("delete from ? where ? = ?");
            statement.setString(1, table);
            statement.setInt(2, id);
            statement.setInt(3, value);
            statement.execute();
            statement.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return false;
        }
    }
    public ResultSet getOrderDaytoDay(String startDate, String endDate){
        try{
            PreparedStatement statement = conn.prepareStatement("select * from customer_order where c_order_date between date(?) and date(?) ORDER BY c_order_id");
            statement.setString(1, startDate);
            statement.setString(2, endDate);
            ResultSet rs = statement.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return null;
        }
    }

<<<<<<< Updated upstream
=======
    
    /** 
     * retrieves the information of an item based on table, idColumnName, id, and type
     * @param table the table to retrieve from
     * @param idColumnName the name of the id column of the item
     * @param id the row of the item
     * @param column the type of information to retrieve
     * @return String the information of the specfied item at the specified column in the specified table
     */
    public String getObject(String table, String idColumnName, int id, String column) {
        try {
            PreparedStatement statement = conn.prepareStatement("select " + column + " from " + table + " where " + idColumnName + " = ?");
            statement.setInt(1, id);
            
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return null;
        }
    }

   /**
     * updates the manager order given all fields
     * @param id the id of the order
     * @param date the date of the order
     * @param time the time of the order
     * @param total the total of the order
     * @param phoneNumber the phone number of the user making the order
     */
    public void updateManagerOrder(int id, String date, String time, Double total, String phoneNumber) {
        try {
            PreparedStatement statement = conn.prepareStatement("update manager_order " +
                    "set m_order_date = ?, " +
                    "m_order_time = ?, " +
                    "m_order_total = ?, " +
                    "phonenumber = ? " +
                    "where m_order_id = ?");
            statement.setString(1, date);
            statement.setString(2, time);
            statement.setDouble(3, total);
            statement.setString(4, phoneNumber);
            statement.setInt(5, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return;
        }
    }

    /**
     * updates the customer order given all fields
     * @param id the id of the order
     * @param date the date of the order
     * @param time the time of the order
     * @param subtotal the subtotal of the order
     * @param tax the tax of the order
     * @param total the total of the order
     * @param paymentType the payment type of the order
     */
    public void updateCustomerOrder(int id, String date, String time, String subtotal, String tax, String total, String paymentType) {
        try {
            PreparedStatement statement = conn.prepareStatement("update customer_order " +
                    "set c_order_date = ?, " +
                    "c_order_time = ?, " +
                    "c_order_subtotal = ?, " +
                    "c_order_tax = ?, " +
                    "c_order_total = ?, " +
                    "c_order_payment_type = ? " +
                    "where c_order_id = ?");
            statement.setString(1, date);
            statement.setString(2, time);
            statement.setString(3, subtotal);
            statement.setString(4, tax);
            statement.setString(5, total);
            statement.setString(6, paymentType);
            statement.setInt(7, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return;
        }
    }

    /**
     * updates a user given all fields
     * @param id the id of the user
     * @param phoneNumber the phone number of the user
     * @param name the name of the user
     * @param isManager boolean true if the user is a manager, false if they are not
     */
    public void updateUser(int id, String phoneNumber, String name, boolean isManager) {
        try {
            PreparedStatement statement = conn.prepareStatement("update users " +
                    "set phonenumber = ?, " +
                    "name = ?, " +
                    "ismanager = ? " +
                    "where user_id = ?");
            statement.setString(1, phoneNumber);
            statement.setString(2, name);
            statement.setBoolean(3, isManager);
            statement.setInt(4, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return;
        }
    }

    /**
     * updates item given all fields
     * @param id the id of the item
     * @param name the name of the item
     * @param price the price of the item
     * @param category the category of the item
     */
    public void updateItem(int id, String name, double price, String category) {
        try {
            PreparedStatement statement = conn.prepareStatement("update menu_items " +
                    "set item_name = ?, " +
                    "item_price = ?, " +
                    "category = ? " +
                    "where item_id = ?");
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setString(3, category);
            statement.setInt(4, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return;
        }
    }

    /**
     * updates ingredient given all fields
     * @param id the id of the ingredient
     * @param name the name of the ingredient
     * @param stock the stock of the ingredient
     * @param price the price of the ingredient
     * @param stockChanged boolean true if the stock of the ingredient has changed, false if it has not
     * @param phoneNumber the phone number of the user making the order
     */
    public void updateIngredient(int id, String name, int stock, double price, boolean stockChanged, String phoneNumber) {
        try {
            PreparedStatement statement = conn.prepareStatement("update ingredients " +
                    "set ingredient_name = ?, " +
                    "ingredient_current_stock = ?, " +
                    "ingredient_unit_price = ? " +
                    "where ingredient_id = ?");
            statement.setString(1, name);
            statement.setInt(2, stock);
            statement.setDouble(3, price);
            statement.setInt(4, id);
            statement.execute();
            if(stockChanged) { //Only if we updated the stock do we need to insert into manager_order and junction table
                Double order_total = stock * price;
                PreparedStatement statement3 = conn.prepareStatement("INSERT INTO manager_order (m_order_date, m_order_time, m_order_total, phonenumber) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement3.setDate(1, new Date(System.currentTimeMillis()));
                statement3.setTime(2, new Time(System.currentTimeMillis()));
                statement3.setDouble(3, order_total);
                statement3.setString(4, phoneNumber);

                statement3.execute();
                ResultSet rs3 = statement3.getGeneratedKeys();
                if (rs3.next()) {
                    PreparedStatement statement4 = conn.prepareStatement("INSERT INTO m_order_to_ingredient_list (m_order_id, ingredient_id, ingredient_quantity) VALUES (?, ?, ?)");
                    statement4.setInt(1, rs3.getInt(1));
                    statement4.setInt(2, id);
                    statement4.setInt(3, stock);
                    statement4.execute();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return;
        }
    }
    
    /** 
     * deletes user based on id
     * @param id the id of the user
     */
    public void deleteUser(int id) {
        try {
            PreparedStatement statement = conn.prepareStatement("delete from users where user_id = ?");
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return;
        }
    }

    
    /** 
     * Deletes customer order based on id
     * @param id the id of the order
     */
    public void deleteCustomerOrder(int id) {
        try {
            PreparedStatement statement = conn.prepareStatement("delete from customer_order where c_order_id = ?");
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    
    /** 
     * Deletes manager order based on id
     * @param id the id of the order
     */
    public void deleteManagerOrder(int id) {
        try {
            PreparedStatement statement = conn.prepareStatement("delete from manager_order where m_order_id = ?");
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    
    /** 
     * Deletes item based on id
     * @param id the id of the item
     */
    public void deleteItem(int id) {
        try {
            PreparedStatement statement = conn.prepareStatement("delete from menu_items where item_id = ?");
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    
    /** 
     * Deletes ingredient based on id
     * @param id the id of the ingredient
     */
    public void deleteIngredient(int id) {
        try {
            PreparedStatement statement = conn.prepareStatement("delete from ingredients where ingredient_id = ?");
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    
    /**
     * creates customer order, takes in all attributes 
     * @param date the date of the order
     * @param time the time of the order
     * @param subtotal the subtotal of the order
     * @param tax the tax of the order
     * @param total the total of the order
     * @param paymentType the payment type of the order
     */
    public void createCustomerOrder(String date, String time, String subtotal, String tax, String total, String paymentType) {
        try {
            PreparedStatement statement = conn.prepareStatement("insert into customer_order" +
                "(c_order_date, c_order_time, c_order_subtotal, c_order_tax, c_order_total, c_order_payment_type)" +
                "values (?, ?, ?, ?, ?, ?)"
                );
            statement.setString(1, date);
            statement.setString(2, time);
            statement.setString(3, subtotal);
            statement.setString(4, tax);
            statement.setString(5, total);
            statement.setString(6, paymentType);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    
    /** 
     * Creates manager order, takes in all attributes
     * @param date the date of the order
     * @param time the time of the order
     * @param total the total of the order
     * @param phoneNumber the phone number of the user making the order
     */
    public void createManagerOrder(String date, String time, Double total, String phoneNumber) {
        try {
            PreparedStatement statement = conn.prepareStatement("insert into manager_order" +
                "(m_order_date, m_order_time, m_order_total, phonenumber)" +
                "values (?, ?, ?, ?)"
                );
            statement.setString(1, date);
            statement.setString(2, time);
            statement.setDouble(3, total);
            statement.setString(4, phoneNumber);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    
    /** 
     * Creates new user, takes in all attributes
     * @param phoneNumber the phone number of the user
     * @param name the name of the user
     * @param isManager boolean true if the user is a manager, false if they are not
     */
    public void createUser(String phoneNumber, String name, boolean isManager) {
        try {
            PreparedStatement statement = conn.prepareStatement("insert into users" +
                "(phonenumber, name, ismanager)" +
                "values (?, ?, ?)"
                );
            statement.setString(1, phoneNumber);
            statement.setString(2, name);
            statement.setBoolean(3, isManager);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    
    /** 
     * Create new ingredients, takes in all attributes
     * @param name the name of the ingredient
     * @param stock the stock of the ingredient
     * @param price the price of the ingredient
     */
    public void createIngredient(String name, int stock, double price) {
        try {
            PreparedStatement statement = conn.prepareStatement("insert into ingredients" +
                "(ingredient_name, ingredient_current_stock, ingredient_unit_price)" +
                "values (?, ?, ?)"
                );
            statement.setString(1, name);
            statement.setInt(2, stock);
            statement.setDouble(3, price);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
        }
    }

    /**
     * Finds list of pair that sells the best
     */
    public ResultSet findPair(String startDate,String endDate){
        try{
            String text = "";
            try{
                text = new String(Files.readAllBytes(Paths.get("test-query/find_Pairs.txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        
            PreparedStatement statement = conn.prepareStatement(text);
            statement.setString(1, startDate);statement.setString(2, endDate);
            statement.setString(3, startDate);statement.setString(4, endDate);
            ResultSet rs = statement.executeQuery();
            return rs;
        }


        catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return null;
        }
    }

>>>>>>> Stashed changes
}

