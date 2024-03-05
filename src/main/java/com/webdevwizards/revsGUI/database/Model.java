

package com.webdevwizards.revsGUI.database;

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
    public static Connection conn = null;
    private static boolean initialized = false;
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

    
    /** 
     * Executes query and returns the result set of the query
     * @param sql
     * @return ResultSet
     */
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

    /**
     * Closes connection 
     */
    public static void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to close database connection.");
        }
    }

    
    /** 
     * Checks whether the inputted phone number is a manager's or employee's number
     * @param phoneNumber
     * @return boolean
     */
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

    
    /** 
     * Checks whether the inputted phone number is a manager's number
     * @param phoneNumber
     * @return boolean
     */
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
    
    /** 
     * Sets the details for the specific order
     * @param preparedStatement
     * @param subtotal
     * @param paymenttype
     * @throws SQLException
     */
    private void setOrderDetails(PreparedStatement preparedStatement, String subtotal, String paymenttype) throws SQLException {
        preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
        preparedStatement.setTime(2, new Time(System.currentTimeMillis()));
        preparedStatement.setDouble(3, Double.parseDouble(subtotal));
        preparedStatement.setDouble(4, 0.0825* Double.parseDouble(subtotal));
        preparedStatement.setDouble(5, 1.0825* Double.parseDouble(subtotal));
        preparedStatement.setString(6, paymenttype);
    }
    
    /** 
     * Inserts the customer order items into the database based on order_id
     * @param order_id
     * @param orderItems
     * @param connection
     * @throws SQLException
     */
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
    
    /** 
     * Checks the selected ingredients
     * @param ingredient_id
     * @param ingredient_quantity
     * @param connection
     * @return boolean
     * @throws SQLException
     */
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

    
    /** 
     * Attaches the associated ingredients to the new item ; used for new seasonal item
     * @param itemID
     * @param associatedIngredients
     * @param quantities
     * @return boolean
     * @throws SQLException
     */
    public boolean attachAssociatedInventoryToNewItem(int itemID, String[] associatedIngredients, String[] quantities) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("insert into item_to_ingredient_list (item_id, ingredient_id, ingredient_quantity) values (?, ?, ?)");
        int ingredient_id;
        try{
            for(int i = 0; i < associatedIngredients.length; i++){
                statement.setInt(1, itemID);
                ingredient_id = getIngredientID(associatedIngredients[i]);
                statement.setInt(2, ingredient_id);
                statement.setInt(3, Integer.parseInt(quantities[i]));
                statement.execute();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }
    
    /** 
     * merges the lists of ingredients and quantities into a map
     * @param list1
     * @param list2
     * @param resultMap
     */
    public static void mergeLists(List<Integer> list1, List<Integer> list2, Map<Integer, Integer> resultMap) {
        for (int i = 0; i < list1.size(); i++) {
            int id = list1.get(i);
            int quantity = list2.get(i);
            resultMap.put(id, resultMap.getOrDefault(id, 0) + quantity);
        }
    }
    
    
    /** 
     * Selects the ingredients for the order so it can be subtracted from the inventory
     * @param orderItems
     * @param connection
     * @return boolean
     * @throws SQLException
     */
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

    
    /** 
     * Updates the current number of ingredients left in stocafter a customer order is completed
     * @param ingredient_id
     * @param ingredient_quantity
     * @param connection
     * @throws SQLException
     */
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

    
    /** 
     * Inserts order details to customer_order table
     * @param subtotal
     * @param orderItems
     * @param paymenttype
     * @return boolean
     */
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
    
    /** 
     * Adds new item to the order, updates names, price and category
     * @param itemName
     * @param itemPrice
     * @param category
     * @return boolean
     */
    public static boolean addNewItem(String itemName, Double itemPrice, String category){
        try{
            PreparedStatement statement = conn.prepareStatement("insert into menu_items (item_name, item_price, category) values (?, ?, ?)");
            statement.setString(1,itemName);
            statement.setDouble(2,itemPrice);
            statement.setString(3,category);
            statement.execute();
            statement.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return false;
        }
    }
    
    
    /** 
     * Gets ResultSet of all ingredients
     * @return ResultSet
     */
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

    
    /** 
     * returns all attribute from menu items that is ordered by item_id 
     * @return ResultSet
     */
    public static ResultSet getAllMenuItems(){
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

    
    /** 
     * Retrieves all items and ingredients
     * @return ResultSet
     */
    public static ResultSet getAllItemsAndIngredients() {
        try {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT menu_items.item_name, menu_items.item_price, menu_items.category, " +
                    "STRING_AGG(ingredients.ingredient_name || ' : ' || item_to_ingredient_list.ingredient_quantity, '; ') AS ingredients " +
                    "FROM menu_items " +
                    "JOIN item_to_ingredient_list ON menu_items.item_id = item_to_ingredient_list.item_id " +
                    "JOIN ingredients ON item_to_ingredient_list.ingredient_id = ingredients.ingredient_id " +
                    "GROUP BY menu_items.item_id, menu_items.item_name, menu_items.item_price, menu_items.category " +
                    "ORDER BY menu_items.item_id"
                );
                ResultSet rs = statement.executeQuery();
                return rs;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
                return null;
            }
        }

    
    /** 
     * returns ResultSet of all cusommt
     * @return ResultSet
     */
    public static ResultSet getAllCustomerOrders() {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM customer_order ORDER BY c_order_id");
            ResultSet rs = statement.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return null;
        }
    }

    
    /** 
     * Retrieves all manager orders
     * @return ResultSet
     */
    public static ResultSet getAllManagerOrders() {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM manager_order ORDER BY m_order_id");
            ResultSet rs = statement.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return null;
        }
    }

    
    /** 
     * Retrives all users using the input query
     * @return ResultSet
     */
    public static ResultSet getAllUsers() {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM users ORDER BY user_id");
            ResultSet rs = statement.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return null;
        }
    }
    
    
    /** 
     * Add  a new ingredient/inventory item
     * @param ingredientName
     * @param ingredientStock
     * @param ingredientPrice
     * @return boolean
     */
    public static boolean addNewInventory(String ingredientName, int ingredientStock, Double ingredientPrice){
        try{
            PreparedStatement statement = conn.prepareStatement("insert into ingredients (ingredient_name, ingredient_current_stock, ingredient_unit_price) values (?, ?, ?)");
            statement.setString(1,ingredientName);
            statement.setInt(2,ingredientStock);
            statement.setDouble(3,ingredientPrice);
            statement.execute();
            statement.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return false;
        }
    }

    
    /** 
     * Updates inventory and includes flags so some fields can be left empty
     * @param ingredient_name
     * @param stock
     * @param price
     * @param phoneNumber
     * @param flagName
     * @param flagStock
     * @param flagPrice
     * @return boolean
     */
    public static boolean updateInventory( String ingredient_name, int stock, double price, String phoneNumber,
                                           boolean flagName, boolean flagStock, boolean flagPrice){
        try{
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM ingredients WHERE ingredient_name = ?");
            statement.setString(1, ingredient_name);
            ResultSet rs = statement.executeQuery();
            Double ingredientPrice = 0.00;

            if(rs.next()){
                int current_stock = rs.getInt(3);
                ingredientPrice = rs.getDouble(4);
                int new_stock = current_stock + stock;
                PreparedStatement statement2 = conn.prepareStatement("UPDATE ingredients SET ingredient_current_stock = CASE WHEN ? = true THEN ? ELSE ingredient_current_stock END," +
                        "ingredient_unit_price = CASE WHEN ? = true THEN ? ELSE ingredient_unit_price END " +
                        "WHERE ingredient_name = ?", Statement.RETURN_GENERATED_KEYS);

                statement2.setBoolean(1, flagStock);
                statement2.setInt(2, new_stock);
                statement2.setBoolean(3, flagPrice);
                statement2.setDouble(4, price);
                statement2.setString(5, ingredient_name);
                statement2.execute();
                ResultSet Updateset = statement2.getGeneratedKeys();
                int ingredient_id = 0;
                if(Updateset.next()){
                    ingredient_id = Updateset.getInt(1);
                }


                if(flagStock) { //Only if we updated the stock do we need to insert into manager_order and junction table
                    Double order_total = stock * ingredientPrice;
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
                        statement4.setInt(2, ingredient_id);
                        statement4.setInt(3, stock);
                        statement4.execute();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return false;
        }
    }

    
    /** 
     * Updates all fields of a menu item for CRUD
     * @param itemName
     * @param price
     * @param category
     * @param flagName
     * @param flagPrice
     * @param flagCategory
     * @return boolean
     */
    public static boolean updateMenuItem( String itemName, double price, String category,
                                          boolean flagName, boolean flagPrice, boolean flagCategory){
        try{
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM menu_items WHERE item_Name = ?");
            statement.setString(1, itemName);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                PreparedStatement statement2 = conn.prepareStatement("UPDATE menu_items SET " +
                        "item_price = CASE WHEN ? = true THEN ? ELSE item_price END," +
                        "category = CASE WHEN ? = true THEN ? ELSE category END " +
                        "WHERE item_name = ?");
                statement2.setBoolean(1, flagPrice);
                statement2.setDouble(2, price);
                statement2.setBoolean(3, flagCategory);
                statement2.setString(4, category);
                statement2.setString(5, itemName);
                statement2.execute();

            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return false;
        }
    }

    
    /** 
     * Fetches the item id when passing item name
     * @param item_name
     * @return int
     */
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
    
    
    /** 
     * fetches ingredient id when passing ingredient name
     * @param ingredient_name
     * @return int
     */
    public static int getIngredientID(String ingredient_name){
        try{
            PreparedStatement statement = conn.prepareStatement("select ingredient_id from ingredients where ingredient_name = ?");
            statement.setString(1, ingredient_name);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return -1;
        }
    }

    
    /**
     * fetches item name when passing item id 
     * @param item_id
     * @return String
     */
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

    
    /** 
     * fetches item price when passing item id
     * @param item_id
     * @return String
     */
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

    
    /** 
     * sums item prices given a list of item orders
     * @param orderItems
     * @return float
     */
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

    
    /** 
     * fetches user name when passing phone number
     * @param phoneNumber
     * @return String
     */
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

    
    /** 
     * 
     * inserts a new user to the database table, allows specification of their number, name, and if they are a manager
     * @param phonenumber
     * @param name
     * @param ismanager
     * @return boolean
     */
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

    
    /** 
     * Deletes a specified item from any table
     * @param table
     * @param id
     * @param value
     * @return boolean
     */
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
    
    /** 
     * retrieves the orders made between date 1 to date 2
     * @param startDate
     * @param endDate
     * @return ResultSet
     */
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

    
    /** 
     * retrieves the information of an item based on item name, id, and its table
     * @param table
     * @param id
     * @param row
     * @param type
     * @return String
     */
    public String getObject(String table, String id, int row, String type) {
        try {
            PreparedStatement statement = conn.prepareStatement("select " + type + " from " + table + " where " + id + " = ?");
            statement.setInt(1, row);
            
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
     * @param id
     * @param date
     * @param time
     * @param total
     * @param phoneNumber
     */
    public void updateManagerOrder(int id, String date, String time, Double total, String phoneNumber) {
        try {
            PreparedStatement statement = conn.prepareStatement("update manager_order" +
                "set m_order_date = ?" +
                "set m_order_time = ?" +
                "set m_order_total = ?" +
                "set phonenumber = ?" +
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
     * @param id
     * @param date
     * @param time
     * @param subtotal
     * @param tax
     * @param total
     * @param paymentType
     */
    public void updateCustomerOrder(int id, String date, String time, String subtotal, String tax, String total, String paymentType) {
        try {
            PreparedStatement statement = conn.prepareStatement("update customer_order" +
                "set c_order_date = ?" +
                "set c_order_time = ?" +
                "set c_order_subtotal = ?" +
                "set c_order_tax = ?" +
                "set c_order_total = ?" +
                "set c_order_payment_type = ?" +
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
     * @param id
     * @param phoneNumber
     * @param name
     * @param isManager
     */
    public void updateUser(int id, String phoneNumber, String name, boolean isManager) {
        try {
            PreparedStatement statement = conn.prepareStatement("update users" +
                "set phonenumber = ?" +
                "set name = ?" +
                "set ismanager = ?" +
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
     * @param id
     * @param name
     * @param price
     * @param category
     */
    public void updateItem(int id, String name, double price, String category) {
        try {
            PreparedStatement statement = conn.prepareStatement("update menu_items" +
                "set item_name = ?" +
                "set item_price = ?" +
                "set category = ?" +
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
     * @param id
     * @param name
     * @param stock
     * @param price
     */
    public void updateIngredient(int id, String name, int stock, double price) {
        try {
            PreparedStatement statement = conn.prepareStatement("update ingredients" +
                "set ingredient_name = ?" +
                "set ingredient_current_stock = ?" +
                "set ingredient_unit_price = ?" +
                "where ingredient_id = ?");
            statement.setString(1, name);
            statement.setInt(2, stock);
            statement.setDouble(3, price);
            statement.setInt(4, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
            return;
        }
    }

    
    /** 
     * 
     * @param id
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
     * @param id
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
     * Deletes from Manager table
     * @param id
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
     * delete from item table
     * @param id
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
     * delete from ingredient table
     * @param id
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
     * creates customer order, takes in attributes 
     * @param date
     * @param time
     * @param subtotal
     * @param tax
     * @param total
     * @param paymentType
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
     * Creates manager order, takes in attributes
     * @param date
     * @param time
     * @param total
     * @param phoneNumber
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
     * Insets into users table
     * @param phoneNumber
     * @param name
     * @param isManager
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
     * Create new ingredients takes in name stock price
     * @param name
     * @param stock
     * @param price
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

}

