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

/**
 * Model class for the database.
 *
 * @author Caden, Carson, Jesung, Kevin, Mihir
 */
public class Model {
  /** The connection to be locally referenced. */
  public static Connection conn = null;

  private static boolean initialized = false;

  /** The phone number of the user. */
  public String phoneNumber;

  /** Used to display current order number. */
  public int orderNumber = 0;

  /** Constructor for the Model class : just calls initialize. */
  public Model() {
    this.initialize();
  }

  /** Sets up config file and initializes the database connection. */
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
    String database_url =
        String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);

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
   * Executes query and returns the result set of the query.
   *
   * @param sql the query to be executed
   * @return the ResultSet of the query unless the query fails, in which case it returns null
   */
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

  /** Closes connection. */
  public static void close() {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Failed to close database connection.");
    }
  }

  /**
   * Checks whether the inputted phone number is in the users database.
   *
   * @param phoneNumber the phone number to be checked
   * @return boolean true if the phone number is in the table, false if it is not
   */
  public boolean login(String phoneNumber) {
    try {
      String sql = "SELECT * FROM users WHERE phonenumber = ?";
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, phoneNumber);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks whether the inputted phone number is a manager's number.
   *
   * @param phoneNumber the phone number to be checked
   * @return boolean true if the phone number is a manager's number, false if it is not
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
        } else {
          return false;
        }
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  private static final String INSERT_ORDER_QUERY =
      "INSERT INTO customer_order (c_order_date, c_order_time, c_order_subtotal, c_order_tax,"
          + " c_order_total, c_order_payment_type) VALUES ( ?, ?, ?, ?, ?, ?)";
  private static final String INSERT_ORDER_ITEM_QUERY =
      "INSERT INTO c_order_to_item_list (c_order_id, item_id, item_quantity) VALUES (?, ?, ?)";
  private static final String SELECT_INGREDIENT =
      "SELECT ingredient_id, ingredient_quantity FROM item_to_ingredient_list WHERE item_id = ?;";
  private static final String SELECT_INGREDIENT_NAME =
      "SELECT ingredient_current_stock FROM ingredients WHERE ingredient_id = ?;";
  private static final String UPDATE_INGREDIENT_COUNT =
      "UPDATE ingredients SET ingredient_current_stock = ? WHERE ingredient_id = ?;";

  /**
   * Sets the details for the specific order.
   *
   * @param preparedStatement the prepared statement to be used
   * @param subtotal the subtotal of the order as a string
   * @param paymenttype the payment type of the order
   * @throws SQLException
   */
  private void setOrderDetails(
      PreparedStatement preparedStatement, String subtotal, String paymenttype)
      throws SQLException {
    preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
    preparedStatement.setTime(2, new Time(System.currentTimeMillis()));
    preparedStatement.setDouble(3, Double.parseDouble(subtotal));
    preparedStatement.setDouble(4, 0.0825 * Double.parseDouble(subtotal));
    preparedStatement.setDouble(5, 1.0825 * Double.parseDouble(subtotal));
    preparedStatement.setString(6, paymenttype);
  }

  /**
   * Inserts the customer order items into the database based on order_id.
   *
   * @param order_id the id of the order
   * @param orderItems the items in the order
   * @param connection the connection to the database
   * @throws SQLException
   */
  private void insert_order_item(int order_id, int[][] orderItems, Connection connection)
      throws SQLException {
    try {

      // Insert order items
      for (int i = 0; i < orderItems.length; i++) {
        if (orderItems[i][0] == 0) {
          continue;
        }
        try (PreparedStatement preparedStatementInsertItem =
            connection.prepareStatement(INSERT_ORDER_ITEM_QUERY)) {
          preparedStatementInsertItem.setInt(1, order_id);
          preparedStatementInsertItem.setInt(2, orderItems[i][0]);
          preparedStatementInsertItem.setInt(3, orderItems[i][1]);
          preparedStatementInsertItem.executeUpdate();
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } // Done
  }

  /**
   * Checks if the selected ingredient is in stock.
   *
   * @param ingredient_id the id of the ingredient
   * @param ingredient_quantity the quantity of the ingredient
   * @param connection the connection to the database
   * @return boolean true if the ingredient is in stock, false if it is not
   * @throws SQLException
   */
  private boolean checkSelectIngredient(
      int ingredient_id, int ingredient_quantity, Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement(SELECT_INGREDIENT_NAME)) {
      preparedStatement.setInt(1, ingredient_id);
      ResultSet rs = preparedStatement.executeQuery();
      rs.next();
      int current_stock = rs.getInt(1);
      if (current_stock < ingredient_quantity) {
        return false;
      }
      return true;
    } // Done
  }

  /**
   * Attaches the associated ingredients to the new item ; used for new seasonal item.
   *
   * @param itemID the id of the item
   * @param associatedIngredients the ingredients associated with the item
   * @param quantities the quantities of the ingredients
   * @return boolean true if the ingredients are successfully attached, false if they are not
   * @throws SQLException if the SQL query fails
   */
  public boolean attachAssociatedInventoryToNewItem(
      int itemID, String[] associatedIngredients, String[] quantities) throws SQLException {
    PreparedStatement statement =
        conn.prepareStatement(
            "insert into item_to_ingredient_list (item_id, ingredient_id, ingredient_quantity)"
                + " values (?, ?, ?)");
    int ingredient_id;
    try {
      for (int i = 0; i < associatedIngredients.length; i++) {
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
   * merges the lists of ingredients and quantities into a map.
   *
   * @param list1 list of id's
   * @param list2 list of quantities
   * @param resultMap the map to be populated
   */
  public static void mergeLists(
      List<Integer> list1, List<Integer> list2, Map<Integer, Integer> resultMap) {
    for (int i = 0; i < list1.size(); i++) {
      int id = list1.get(i);
      int quantity = list2.get(i);
      resultMap.put(id, resultMap.getOrDefault(id, 0) + quantity);
    }
  }

  /**
   * Selects the ingredients for the order so it can be subtracted from the inventory.
   *
   * @param orderItems the items and their quantities in the order
   * @param connection the connection to the database
   * @return boolean true if the ingredients are successfully selected, false if they are not
   * @throws SQLException
   */
  private boolean selectIngredient(int[][] orderItems, Connection connection) throws SQLException {
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
          if (checkSelectIngredient(mergedListid.get(j), mergedListquant.get(j), connection)
              == false) {
            connection.prepareStatement(
                "SELECT ingredient_name FROM ingredients WHERE ingredient_id = ?");
            throw new SQLException("Not enough stock for ingredients" + mergedListid.get(j));
          }
        }
        for (int j = 0; j < mergedListid.size(); j++) {
          subtractIngredientCount(mergedListid.get(j), mergedListquant.get(j));
        }
      }
      return true;
    }
  } // Done?

  /**
   * Decreases the current number of ingredients left in stocafter a customer order is completed.
   *
   * @param ingredient_id the id of the ingredient
   * @param ingredient_quantity the quantity to decrease by
   * @throws SQLException if the SQL query fails
   */
  public void subtractIngredientCount(int ingredient_id, int ingredient_quantity)
      throws SQLException {
    try (PreparedStatement preparedStatement = conn.prepareStatement(SELECT_INGREDIENT_NAME)) {
      preparedStatement.setInt(1, ingredient_id);
      ResultSet rs = preparedStatement.executeQuery();
      rs.next();
      int current_stock = rs.getInt(1);
      try (PreparedStatement preparedStatementUpdate =
          conn.prepareStatement(UPDATE_INGREDIENT_COUNT)) {
        preparedStatementUpdate.setInt(1, current_stock - ingredient_quantity);
        preparedStatementUpdate.setInt(2, ingredient_id);
        preparedStatementUpdate.executeUpdate();
      }
    }
  }

  /**
   * Increase the current number of ingredients left in stocafter a customer order is completed.
   *
   * @param ingredient_id the id of the ingredient
   * @param ingredient_quantity the quantity to increase by
   * @throws SQLException if the SQL query fails
   */
  public void addIngredientCount(int ingredient_id, int ingredient_quantity) throws SQLException {
    try (PreparedStatement preparedStatement = conn.prepareStatement(SELECT_INGREDIENT_NAME)) {
      preparedStatement.setInt(1, ingredient_id);
      ResultSet rs = preparedStatement.executeQuery();
      rs.next();
      int current_stock = rs.getInt(1);
      try (PreparedStatement preparedStatementUpdate =
          conn.prepareStatement(UPDATE_INGREDIENT_COUNT)) {
        preparedStatementUpdate.setInt(1, current_stock + ingredient_quantity);
        preparedStatementUpdate.setInt(2, ingredient_id);
        preparedStatementUpdate.executeUpdate();
      }
    }
  }

  /**
   * Inserts order details to customer_order table.
   *
   * @param subtotal the subtotal of the order
   * @param orderItems the items and their quantities in the order
   * @param paymenttype the payment type of the order
   * @return boolean
   */
  public boolean insert_order(String subtotal, int[][] orderItems, String paymenttype) {
    try {
      PreparedStatement preparedStatementInsert =
          conn.prepareStatement(INSERT_ORDER_QUERY, Statement.RETURN_GENERATED_KEYS);
      // Set order details
      if (selectIngredient(orderItems, conn) == false) {
        return false;
      }
      setOrderDetails(preparedStatementInsert, subtotal, paymenttype);
      preparedStatementInsert.executeUpdate();
      ResultSet rs = preparedStatementInsert.getGeneratedKeys();
      if (rs.next()) {
        orderNumber = rs.getInt(1);
        insert_order_item(orderNumber, orderItems, conn);
      }
      return true;

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Tells user their order number when they finish payment, mod 100.
   *
   * @return int the order number mod 100
   */
  public int getOrderNumber() {
    return orderNumber % 100;
  }

  /**
   * Adds new item to the order, updates names, price, category, and dates if applicable.
   *
   * @param itemName the name of the item
   * @param itemPrice the price of the item
   * @param category the category of the item
   * @param startDate the start date of the item
   * @param endDate the end date of the item
   * @param flagDate boolean flag for date
   * @return boolean true if the item is successfully added, false if it is not
   */
  public static boolean addNewItem(
      String itemName,
      Double itemPrice,
      String category,
      String startDate,
      String endDate,
      boolean flagDate) {
    try {
      PreparedStatement statement;
      if (flagDate) { // If a date was provided
        statement =
            conn.prepareStatement(
                "insert into menu_items (item_name, item_price, category, date_range) values (?, ?,"
                    + " ?, ?::daterange)");
        String date_range = "[" + startDate + ", " + endDate + "]";
        statement.setString(4, date_range);
      } else {
        statement =
            conn.prepareStatement(
                "insert into menu_items (item_name, item_price, category) values (?, ?, ?)");
      }
      statement.setString(1, itemName);
      statement.setDouble(2, itemPrice);
      statement.setString(3, category);
      statement.execute();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return false;
    }
  }

  /**
   * Gets ResultSet of all ingredients.
   *
   * @return ResultSet of all ingredients in the database ingredient_id | ingredient_name |
   *     ingredient_current_stock | ingredient_unit_price
   */
  public static ResultSet getAllIngredients() {
    try {
      String sql = "select * from ingredients order by ingredient_id";
      PreparedStatement statement =
          conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }

  /**
   * returns all attribute from menu items that is ordered by item_id.
   *
   * @return ResultSet of all items in the database
   */
  public static ResultSet getAllMenuItems() {
    try {
      PreparedStatement statement =
          conn.prepareStatement("select * from menu_items order by item_id");
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }

  /**
   * Retrieves all items and their corresponding ingredients.
   *
   * @return ResultSet of all items with corresponding ingredients in the database item_name |
   *     item_price | category | ingredients
   */
  public static ResultSet getAllItemsAndIngredients() {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "SELECT menu_items.item_name, menu_items.item_price, menu_items.category,"
                  + " STRING_AGG(ingredients.ingredient_name || ' : ' ||"
                  + " item_to_ingredient_list.ingredient_quantity, '; ') AS ingredients FROM"
                  + " menu_items JOIN item_to_ingredient_list ON menu_items.item_id ="
                  + " item_to_ingredient_list.item_id JOIN ingredients ON"
                  + " item_to_ingredient_list.ingredient_id = ingredients.ingredient_id GROUP BY"
                  + " menu_items.item_id, menu_items.item_name, menu_items.item_price,"
                  + " menu_items.category ORDER BY menu_items.item_id");
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }

  /**
   * returns ResultSet of all customer orders.
   *
   * @return ResultSet of all customer orders in the database c_order_id | c_order_date |
   *     c_order_time | c_order_subtotal | c_order_tax | c_order_total | c_order_payment_type
   */
  public static ResultSet getAllCustomerOrders() {
    try {
      PreparedStatement statement =
          conn.prepareStatement("SELECT * FROM customer_order ORDER BY c_order_id");
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }

  /**
   * Retrieves all manager orders.
   *
   * @return ResultSet of all manager orders in the database m_order_id | m_order_date |
   *     m_order_time | m_order_total | phonenumber
   */
  public static ResultSet getAllManagerOrders() {
    try {
      PreparedStatement statement =
          conn.prepareStatement("SELECT * FROM manager_order ORDER BY m_order_id");
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }

  /**
   * Retrives all users.
   *
   * @return ResultSet of all users in the database user_id | phonenumber | name | ismanager
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
   * Add a new ingredient/inventory item.
   *
   * @param ingredientName ingredient name
   * @param ingredientStock ingredient stock
   * @param ingredientPrice ingredient price
   * @return boolean true if the ingredient is successfully added, false if it is not
   */
  public static boolean addNewInventory(
      String ingredientName, int ingredientStock, Double ingredientPrice) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "insert into ingredients (ingredient_name, ingredient_current_stock,"
                  + " ingredient_unit_price) values (?, ?, ?)");
      statement.setString(1, ingredientName);
      statement.setInt(2, ingredientStock);
      statement.setDouble(3, ingredientPrice);
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
   * Updates inventory and includes flags so some fields can be left empty.
   *
   * @param ingredient_name ingredient name
   * @param stock current ingredient stock
   * @param price ingredient price
   * @param phoneNumber the phone number of the user making the order
   * @param flagName boolean flag for ingredient name
   * @param flagStock boolean flag for ingredient stock
   * @param flagPrice boolean flag for ingredient price
   * @return boolean true if the inventory is successfully updated, false if it is not
   */
  public static boolean updateInventory(
      String ingredient_name,
      int stock,
      double price,
      String phoneNumber,
      boolean flagName,
      boolean flagStock,
      boolean flagPrice) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("SELECT * FROM ingredients WHERE ingredient_name = ?");
      statement.setString(1, ingredient_name);
      ResultSet rs = statement.executeQuery();
      Double ingredientPrice = 0.00;

      if (rs.next()) {
        int current_stock = rs.getInt(3);
        ingredientPrice = rs.getDouble(4);
        int new_stock = current_stock + stock;
        PreparedStatement statement2 =
            conn.prepareStatement(
                "UPDATE ingredients SET ingredient_current_stock = CASE WHEN ? = true THEN ? ELSE"
                    + " ingredient_current_stock END,ingredient_unit_price = CASE WHEN ? = true"
                    + " THEN ? ELSE ingredient_unit_price END WHERE ingredient_name = ?",
                Statement.RETURN_GENERATED_KEYS);

        statement2.setBoolean(1, flagStock);
        statement2.setInt(2, new_stock);
        statement2.setBoolean(3, flagPrice);
        statement2.setDouble(4, price);
        statement2.setString(5, ingredient_name);
        statement2.execute();
        ResultSet Updateset = statement2.getGeneratedKeys();
        int ingredient_id = 0;
        if (Updateset.next()) {
          ingredient_id = Updateset.getInt(1);
        }

        if (flagStock) { // Only if we updated the stock do we need to insert into manager_order and
          // junction table
          Double order_total = stock * ingredientPrice;
          PreparedStatement statement3 =
              conn.prepareStatement(
                  "INSERT INTO manager_order (m_order_date, m_order_time, m_order_total,"
                      + " phonenumber) VALUES (?, ?, ?, ?)",
                  Statement.RETURN_GENERATED_KEYS);
          statement3.setDate(1, new Date(System.currentTimeMillis()));
          statement3.setTime(2, new Time(System.currentTimeMillis()));
          statement3.setDouble(3, order_total);
          statement3.setString(4, phoneNumber);

          statement3.execute();
          ResultSet rs3 = statement3.getGeneratedKeys();
          if (rs3.next()) {
            PreparedStatement statement4 =
                conn.prepareStatement(
                    "INSERT INTO m_order_to_ingredient_list (m_order_id, ingredient_id,"
                        + " ingredient_quantity) VALUES (?, ?, ?)");
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
   * Updates all fields of a menu item for CRUD.
   *
   * @param itemName the name of the item
   * @param price the price of the item
   * @param category the category of the item
   * @param flagName boolean flag for item name
   * @param flagPrice boolean flag for item price
   * @param flagCategory boolean flag for item category
   * @return boolean
   */
  public static boolean updateMenuItem(
      String itemName,
      double price,
      String category,
      boolean flagName,
      boolean flagPrice,
      boolean flagCategory) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("SELECT * FROM menu_items WHERE item_Name = ?");
      statement.setString(1, itemName);
      ResultSet rs = statement.executeQuery();

      if (rs.next()) {
        PreparedStatement statement2 =
            conn.prepareStatement(
                "UPDATE menu_items SET "
                    + "item_price = CASE WHEN ? = true THEN ? ELSE item_price END,"
                    + "category = CASE WHEN ? = true THEN ? ELSE category END "
                    + "WHERE item_name = ?");
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
   * Fetches the item id when passing item name.
   *
   * @param item_name the name of the item
   * @return int the id of the item
   */
  public static int getItemID(String item_name) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("select item_id from menu_items where item_name = ?");
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
   * fetches ingredient id when passing ingredient name.
   *
   * @param ingredient_name the name of the ingredient
   * @return int the id of the ingredient
   */
  public static int getIngredientID(String ingredient_name) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("select ingredient_id from ingredients where ingredient_name = ?");
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
   * fetches item name when passing item id.
   *
   * @param item_id the id of the item
   * @return String the name of the item
   */
  public static String getItemName(int item_id) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("select item_name from menu_items where item_id = ?");
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
   * fetches item price when passing item id.
   *
   * @param item_id the id of the item
   * @return String the price of the item
   */
  public static String getItemPrice(int item_id) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("select item_price from menu_items where item_id = ?");
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
   * sums item prices given a list of item orders.
   *
   * @param orderItems the items and their quantities in the order
   * @return float the sum of the item prices (subtotal)
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
   * fetches user name when passing phone number.
   *
   * @param phoneNumber the phone number of the user
   * @return String the name of the user
   */
  public static String getUserName(String phoneNumber) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("select name from users where phonenumber = ?");
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
   * inserts a new user to the database table, allows specification of their number, name, and if they are a manager.
   *
   * @param phonenumber new user's phone number
   * @param name new user's name
   * @param ismanager boolean true if the user is a manager, false if they are not
   * @return boolean true if the user is successfully added, false if they are not
   */
  public static boolean insertUser(String phonenumber, String name, boolean ismanager) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "insert into users (phonenumber, name, ismanager) values (?, ?, ?)");
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
   * Deletes a specified item from any table.
   *
   * @param table the table to delete from
   * @param id the id of the item to delete
   * @param value the value of the item to delete
   * @return boolean
   */
  public static boolean delete(String table, int id, int value) {
    try {
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
   * retrieves the orders made between date 1 to date 2.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return ResultSet of all orders made between the two dates c_order_id | c_order_date |
   *     c_order_time | c_order_subtotal | c_order_tax | c_order_total | c_order_payment_type
   */
  public ResultSet getOrderDaytoDay(String startDate, String endDate) {
    try {
      String sql =
          "select * from customer_order where c_order_date between date(?) and date(?) ORDER BY"
              + " c_order_id";
      PreparedStatement statement =
          conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
   * retrieves the id based on the table, idColumnName, clickedRow, and maxRow.
   *
   * @param tableName name of the table to retrieve from
   * @param idColumnName name of the id column of the item (based on table)
   * @param clickedRow the row clicked
   * @param maxRow the maximum row in the JTable
   * @return int the id of row clicked
   */
  public int getIDFromRow(String tableName, String idColumnName, int clickedRow, int maxRow) {
    try {
      String query =
          "SELECT "
              + idColumnName
              + " FROM "
              + tableName
              + " ORDER BY "
              + idColumnName
              + " DESC LIMIT 1 OFFSET ?";
      int offset = maxRow - clickedRow;
      PreparedStatement statement = conn.prepareStatement(query);
      statement.setInt(1, offset);
      ResultSet rs = statement.executeQuery();

      if (rs.next()) {
        return rs.getInt(idColumnName);
      } else {
        return -1;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * retrieves the information of an item based on table, idColumnName, id, and type.
   *
   * @param table the table to retrieve from
   * @param idColumnName the name of the id column of the item
   * @param id the row of the item
   * @param column the type of information to retrieve
   * @return String the information of the specfied item at the specified column in the specified
   *     table
   */
  public String getObject(String table, String idColumnName, int id, String column) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "select " + column + " from " + table + " where " + idColumnName + " = ?");
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
   * updates the manager order given all fields.
   *
   * @param id the id of the order
   * @param date the date of the order
   * @param time the time of the order
   * @param total the total of the order
   * @param phoneNumber the phone number of the user making the order
   */
  public void updateManagerOrder(
      int id, String date, String time, Double total, String phoneNumber) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "update manager_order "
                  + "set m_order_date = ?, "
                  + "m_order_time = ?, "
                  + "m_order_total = ?, "
                  + "phonenumber = ? "
                  + "where m_order_id = ?");
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
   * updates the customer order given all fields.
   *
   * @param id the id of the order
   * @param date the date of the order
   * @param time the time of the order
   * @param subtotal the subtotal of the order
   * @param tax the tax of the order
   * @param total the total of the order
   * @param paymentType the payment type of the order
   */
  public void updateCustomerOrder(
      int id,
      String date,
      String time,
      String subtotal,
      String tax,
      String total,
      String paymentType) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "update customer_order "
                  + "set c_order_date = ?, "
                  + "c_order_time = ?, "
                  + "c_order_subtotal = ?, "
                  + "c_order_tax = ?, "
                  + "c_order_total = ?, "
                  + "c_order_payment_type = ? "
                  + "where c_order_id = ?");
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
   * updates a user given all fields.
   *
   * @param id the id of the user
   * @param phoneNumber the phone number of the user
   * @param name the name of the user
   * @param isManager boolean true if the user is a manager, false if they are not
   */
  public void updateUser(int id, String phoneNumber, String name, boolean isManager) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "update users "
                  + "set phonenumber = ?, "
                  + "name = ?, "
                  + "ismanager = ? "
                  + "where user_id = ?");
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
   * updates item given all fields.
   *
   * @param id the id of the item
   * @param name the name of the item
   * @param price the price of the item
   * @param category the category of the item
   * @param startDate the start date of the item
   * @param endDate the end date of the item
   * @param flagDate boolean true if the date of the item has changed, false if it has not
   */
  public void updateItem(
      int id,
      String name,
      double price,
      String category,
      String startDate,
      String endDate,
      boolean flagDate) {
    try {
      PreparedStatement statement;
      if (flagDate) {
        String date_range = "[" + startDate + ", " + endDate + "]";
        statement =
            conn.prepareStatement(
                "update menu_items "
                    + "set item_name = ?, "
                    + "item_price = ?, "
                    + "category = ?, "
                    + "date_range = (?::daterange) "
                    + "where item_id = ?");
        statement.setString(4, date_range);
        statement.setInt(5, id);
      } else {
        statement =
            conn.prepareStatement(
                "update menu_items "
                    + "set item_name = ?, "
                    + "item_price = ?, "
                    + "category = ? "
                    + "where item_id = ?");
        statement.setInt(4, id);
      }
      statement.setString(1, name);
      statement.setDouble(2, price);
      statement.setString(3, category);
      statement.execute();

    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return;
    }
  }

  /**
   * updates ingredient given all fields.
   *
   * @param id the id of the ingredient
   * @param name the name of the ingredient
   * @param stock the stock of the ingredient
   * @param price the price of the ingredient
   * @param stockChanged boolean true if the stock of the ingredient has changed, false if it has
   *     not
   * @param phoneNumber the phone number of the user making the order
   */
  public void updateIngredient(
      int id, String name, int stock, double price, boolean stockChanged, String phoneNumber) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "update ingredients "
                  + "set ingredient_name = ?, "
                  + "ingredient_current_stock = ?, "
                  + "ingredient_unit_price = ? "
                  + "where ingredient_id = ?");
      statement.setString(1, name);
      statement.setInt(2, stock);
      statement.setDouble(3, price);
      statement.setInt(4, id);
      statement.execute();
      if (stockChanged) { // Only if we updated the stock do we need to insert into manager_order
        // and junction table
        Double order_total = stock * price;
        PreparedStatement statement3 =
            conn.prepareStatement(
                "INSERT INTO manager_order (m_order_date, m_order_time, m_order_total, phonenumber)"
                    + " VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        statement3.setDate(1, new Date(System.currentTimeMillis()));
        statement3.setTime(2, new Time(System.currentTimeMillis()));
        statement3.setDouble(3, order_total);
        statement3.setString(4, phoneNumber);

        statement3.execute();
        ResultSet rs3 = statement3.getGeneratedKeys();
        if (rs3.next()) {
          PreparedStatement statement4 =
              conn.prepareStatement(
                  "INSERT INTO m_order_to_ingredient_list (m_order_id, ingredient_id,"
                      + " ingredient_quantity) VALUES (?, ?, ?)");
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
   * deletes user based on id.
   *
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
   * Deletes customer order based on id.
   *
   * @param id the id of the order
   */
  public void deleteCustomerOrder(int id) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("delete from customer_order where c_order_id = ?");
      statement.setInt(1, id);
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
    }
  }

  /**
   * Deletes manager order based on id.
   *
   * @param id the id of the order
   */
  public void deleteManagerOrder(int id) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("delete from manager_order where m_order_id = ?");
      statement.setInt(1, id);
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
    }
  }

  /**
   * Deletes item based on id.
   *
   * @param id the id of the item
   */
  public void deleteItem(int id) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("delete from item_to_ingredient_list where item_id = ?");
      statement.setInt(1, id);
      statement.execute();
      PreparedStatement statement1 =
          conn.prepareStatement("delete from menu_items where item_id = ?");
      statement1.setInt(1, id);
      statement1.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
    }
  }

  /**
   * Deletes ingredient based on id.
   *
   * @param id the id of the ingredient
   */
  public void deleteIngredient(int id) {
    try {
      PreparedStatement statement =
          conn.prepareStatement("delete from item_to_ingredient_list where ingredient_id = ?");
      statement.setInt(1, id);
      statement.execute();
      PreparedStatement statement1 =
          conn.prepareStatement("delete from ingredients where ingredient_id = ?");
      statement1.setInt(1, id);
      statement1.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
    }
  }

  /**
   * creates customer order, takes in all attributes.
   *
   * @param date the date of the order
   * @param time the time of the order
   * @param subtotal the subtotal of the order
   * @param tax the tax of the order
   * @param total the total of the order
   * @param paymentType the payment type of the order
   */
  public void createCustomerOrder(
      String date, String time, String subtotal, String tax, String total, String paymentType) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "insert into customer_order(c_order_date, c_order_time, c_order_subtotal,"
                  + " c_order_tax, c_order_total, c_order_payment_type)values (?, ?, ?, ?, ?, ?)");
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
   * Creates manager order, takes in all attributes.
   *
   * @param date the date of the order
   * @param time the time of the order
   * @param total the total of the order
   * @param phoneNumber the phone number of the user making the order
   */
  public void createManagerOrder(String date, String time, Double total, String phoneNumber) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "insert into manager_order"
                  + "(m_order_date, m_order_time, m_order_total, phonenumber)"
                  + "values (?, ?, ?, ?)");
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
   * Creates new user, takes in all attributes.
   *
   * @param phoneNumber the phone number of the user
   * @param name the name of the user
   * @param isManager boolean true if the user is a manager, false if they are not
   */
  public void createUser(String phoneNumber, String name, boolean isManager) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "insert into users" + "(phonenumber, name, ismanager)" + "values (?, ?, ?)");
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
   * Create new ingredients, takes in all attributes.
   *
   * @param name the name of the ingredient
   * @param stock the stock of the ingredient
   * @param price the price of the ingredient
   */
  public void createIngredient(String name, int stock, double price) {
    try {
      PreparedStatement statement =
          conn.prepareStatement(
              "insert into ingredients"
                  + "(ingredient_name, ingredient_current_stock, ingredient_unit_price)"
                  + "values (?, ?, ?)");
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
   * finds items that sell together using test-query/find_Pairs.txt.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return ResultSet of items that sell together
   */
  public ResultSet findPair(String startDate, String endDate) {
    try {
      String text = "";
      try {
        text = new String(Files.readAllBytes(Paths.get("test-query/find_Pairs.txt")));
      } catch (IOException e) {
        e.printStackTrace();
      }

      PreparedStatement statement = conn.prepareStatement(text);
      statement.setString(1, startDate);
      statement.setString(2, endDate);
      statement.setString(3, startDate);
      statement.setString(4, endDate);
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }

  /**
   * Retrieves the total sales per item for a given date range.
   *
   * @param startDate the start date (YYYY-MM-DD format)
   * @return ResultSet of the total sales per item for the given date range
   */
  public ResultSet findExcess(String startDate) {
    try {
      String text = "";
      try {
        text = new String(Files.readAllBytes(Paths.get("test-query/excessReport.txt")));
      } catch (IOException e) {
        e.printStackTrace();
      }
      String endDate = (new Date(System.currentTimeMillis())).toString();
      PreparedStatement statement = conn.prepareStatement(text);
      statement.setString(1, startDate);
      statement.setString(2, endDate);
      statement.setString(3, startDate);
      statement.setString(4, endDate);
      statement.setString(5, startDate);
      statement.setString(6, endDate);
      statement.setString(7, startDate);
      statement.setString(8, endDate);
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }

  /**
   * Retrieves the total sales per ingredient for a given date range.
   *
   * @param startDate the start date (YYYY-MM-DD format)
   * @param endDate the end date (YYYY-MM-DD format)
   * @return ResultSet of the total sales per ingredient for the given date range
   */
  public ResultSet getProductUsage(String startDate, String endDate) {
    try {
      String sql =
          // add columns for ingredient name and used quantity
          "SELECT i.ingredient_name AS ingredient, "
              +
              // use coalesce to set used_quantity to sum(ingredientCount * itemCount) or
              // 0 if it is null
              "COALESCE(SUM(oi.item_quantity * iti.ingredient_quantity), 0) AS used_quantity "
              +

              // join the ingredients table with the customer orders table
              "FROM ingredients i "
              + "LEFT JOIN item_to_ingredient_list iti ON i.ingredient_id = iti.ingredient_id "
              + "LEFT JOIN c_order_to_item_list oi ON iti.item_id = oi.item_id "
              + "LEFT JOIN customer_order co ON oi.c_order_id = co.c_order_id "
              +

              // so we can filter product usage by date
              "WHERE co.c_order_date BETWEEN ? AND ? "
              + "GROUP BY ingredient "
              + "ORDER BY used_quantity desc;";

      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setDate(1, java.sql.Date.valueOf(startDate));
      pstmt.setDate(2, java.sql.Date.valueOf(endDate));

      ResultSet rs = pstmt.executeQuery();
      return rs;

    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Retrieves the total sales per item for a given date range.
   *
   * @param startTime the start date
   * @param endTime the end date
   * @return ResultSet of the total sales per item for the given date range
   */
  public ResultSet getSalesReport(String startTime, String endTime) {

    String sql =
        "SELECT menu_Items.item_name as item, DATE_PART('month', c_order_date) as month, COUNT(*)"
            + " as quantity \r\n"
            + //
            "FROM customer_order\r\n"
            + //
            "JOIN c_order_to_item_list ON customer_order.c_order_id ="
            + " c_order_to_item_list.c_order_id \r\n"
            + //
            "JOIN menu_Items ON c_order_to_item_list.item_id = menu_Items.item_id \r\n"
            + //
            "WHERE c_order_date >= date(?) AND c_order_date <= date(?) \r\n"
            + //
            "GROUP BY item, c_order_to_item_list.item_id, month\r\n"
            + //
            "ORDER BY quantity DESC;\r\n"
            + //
            "";
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(new String(Files.readAllBytes(Paths.get("test-query/sales.txt"))));
      pstmt.setDate(1, java.sql.Date.valueOf(startTime));
      pstmt.setDate(2, java.sql.Date.valueOf(endTime));
      ResultSet r = pstmt.executeQuery();
      return r;
    } catch (Exception er) {
      er.printStackTrace();
      return null;
    }
  }

  /**
   * Checks all the restock needed ingredients.
   *
   * @return ResultSet containing restock need items
   */
  public ResultSet findRestock() {
    try {
      String text = "";
      try {
        text = new String(Files.readAllBytes(Paths.get("test-query/restock.txt")));
      } catch (IOException e) {
        e.printStackTrace();
      }
      PreparedStatement statement = conn.prepareStatement(text);
      ResultSet rs = statement.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error executing SQL query: " + e.getMessage());
      return null;
    }
  }
}
