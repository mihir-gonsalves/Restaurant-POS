package com.webdevwizards.revsGUI;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;



import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;

import com.formdev.flatlaf.FlatLightLaf;
import com.webdevwizards.revsGUI.screens.*;
import com.webdevwizards.revsGUI.database.Model;

public class Controller implements ActionListener{
    private Model model;
    private LoginScreen loginScreen;
    private CashierScreen cashierScreen;
    private ManagerScreen managerScreen;
    private PaymentScreen paymentScreen;
    private boolean isManager;
    private Popup po;
    private PopupFactory pf;
    private int[][] orderItems;
    private String phoneNumber;


    public static void main(String[] args) {
        // setup and display the login screen
        FlatLightLaf.setup();
        Controller controller = new Controller();
        controller.initialize();
        controller.switchToLoginScreen();

        // switch to appropriate screen based on login's phone number
        controller.loginScreen.getLoginButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.model.login(controller.loginScreen.getPhoneNumber())) {
                    controller.loginScreen.getFrame().dispose();
                    controller.phoneNumber = controller.loginScreen.getPhoneNumber();
                    if (controller.model.isManager(controller.phoneNumber)) {
                        controller.switchToManagerScreen();
                        controller.populateManagerNavBar();
                        controller.populateManagerMainPanel("chart");
                    } else {
                        controller.switchToCashierScreen();
                        controller.populateCashierNavBar();
                        controller.populateItemPanel("Burgers");
                        controller.completeCashierOrder();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid phone number");
                }
            }
        });
    }

    public Controller() {
        // TODO Auto-generated constructor stub
    }

    public void initialize() {
        this.model = new Model();
        this.loginScreen = new LoginScreen();
        this.loginScreen.getFrame().setVisible(false);
        this.cashierScreen = new CashierScreen();
        this.cashierScreen.getFrame().setVisible(false);
        this.managerScreen = new ManagerScreen();
        this.managerScreen.getFrame().setVisible(false);
        this.paymentScreen = new PaymentScreen();
        this.paymentScreen.getFrame().setVisible(false);
        this.isManager = false;
        this.pf = new PopupFactory();
        this.orderItems = new int[10][2];
    }

    public void switchToLoginScreen() {
        this.loginScreen.getFrame().setVisible(true);
    }

    public void switchToCashierScreen() {
        this.isManager = false;
        this.cashierScreen.getFrame().setVisible(true);
    }

    public void switchToManagerScreen() {
        this.isManager = true;
        this.managerScreen.getFrame().setVisible(true);
    }

    public void switchToPaymentScreen() {
        this.paymentScreen.getFrame().setVisible(true);
    }

    public static ImageIcon resizeIcon(String iconPath, int width, int height) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public void populateItemPanel(String category) {
        // Get items and sort by category
        ResultSet rs = this.model.executeQuery("SELECT * FROM menu_items ORDER BY category;"); // EDIT THIS LATER
        JPanel itemsPanel = cashierScreen.getItemsPanel();
        JFrame frame = cashierScreen.getFrame();
        itemsPanel.removeAll();
        
        try {
            while (rs.next()) {
                String db_category = rs.getString("category");
                if (db_category.equals(category)) {
                    String item_name = rs.getString("item_name");
                    String item_price = rs.getString("item_price");
                    String jlabel_text =  item_name + "  " +"$"+ item_price;
                    StringBuilder item_image = new StringBuilder();
                    for (int i = 0; i < item_name.length(); i++) {
                        char c = item_name.charAt(i);
                        if (Character.isLetter(c)) {
                            item_image.append(Character.toLowerCase(c));
                        }
                        else if (c == ' ') {
                            item_image.append('_');
                        }
                    }
                    
                    String item_image_path = "./images/" + item_image + ".png";
                    // System.out.println("Item image: " + item_image_path);
                    JPanel itemPanel = new JPanel(new BorderLayout());

                    // Create a new button with an image
                    // System.out.println("Adding item: " + item_name);
                    JButton itemButton = new JButton(new ImageIcon(item_image_path));
                    itemPanel.add(itemButton, BorderLayout.CENTER);
    
                    // Add action listener to the image button
                    itemButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JPanel popUpPanel = new JPanel();

                            // Get the screen size
                            Dimension frameSize = frame.getSize();
                            int size = (int) (frameSize.getWidth() - 600 * 1.1f);
                            popUpPanel.setPreferredSize(new Dimension(size, size));
                            popUpPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 10)); // Add a border
                            popUpPanel.setLayout(new BoxLayout(popUpPanel, BoxLayout.PAGE_AXIS));
                            JLabel popUpLabel = new JLabel("Item: " + item_name);
                            popUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            JButton popUpButton = new JButton("Add to Order");
                            popUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                            popUpButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // System.out.println("Item added to order: " + item_name);
                                    model.getItemID(item_name);
                                    for (int i = 0; i < orderItems.length; i++) {
                                        if (orderItems[i][0] == model.getItemID(item_name) || orderItems[i][0] == 0) {
                                            orderItems[i][0] = model.getItemID(item_name);
                                            orderItems[i][1] = orderItems[i][1] + 1;
                                            populateCashierOrderPanel();
                                            break;
                                        }
                                    }
                                    po.hide();
                                }
                            });
                            popUpPanel.add(popUpLabel);
                            popUpPanel.add(popUpButton);
                    
                            
                            Point frameLocation = frame.getLocation();

                            // Calculate the center coordinates
                            int x = (int) (frameLocation.getX() + (frameSize.getWidth() - popUpPanel.getPreferredSize().getWidth()) / 2);
                            int y = (int) (frameLocation.getY() + (frameSize.getHeight() - popUpPanel.getPreferredSize().getHeight()) / 2);
                    
                            // Create and show the popup
                            po = pf.getPopup(frame, popUpPanel, x, y);
                            po.show();
                        }
                    });

                    // Create a new label with the item name
                    JLabel itemName = new JLabel(jlabel_text, SwingConstants.CENTER);
                    itemPanel.add(itemName, BorderLayout.SOUTH);

                    // Add the panel to the itemsPanel
                    itemsPanel.add(itemPanel);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    public void populateCashierNavBar() {
        ResultSet rs = this.model.executeQuery("SELECT * FROM menu_items ORDER BY category;"); // EDIT THIS LATER
        JPanel navPanel = cashierScreen.getNavPanel();
        String current_category = "";
        try {
            while (rs.next()) {
                String db_category = rs.getString("category");
                // System.out.println("Category: " + db_category);
                if (!db_category.equals(current_category)) {
                    // System.out.println("|" + db_category + "|" + " is not equal to " + "|" + current_category + "|");
                    current_category = db_category;
                    
                    // Get category icon image
                    StringBuilder category_file_name = new StringBuilder();
                    for (int i = 0; i < current_category.length(); i++) {
                        char c = current_category.charAt(i);
                        if (Character.isLetter(c)) {
                            category_file_name.append(Character.toLowerCase(c));
                        }
                        else if (c == ' ') {
                            category_file_name.append('_');
                        }
                        else if (c == '&') {
                            category_file_name.append("and");
                        }
                    }
                    String category_image_path = "./images/" + category_file_name + ".png";
                    JButton categoryButton = new JButton(resizeIcon(category_image_path, 60, 60));
                    // System.out.println("Category image: " + category_image_path);
                    final String current_category_final = current_category;
                    categoryButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            populateItemPanel(current_category_final);
                        }
                    });
                    navPanel.add(categoryButton);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void populateCashierOrderPanel() {
        if (cashierScreen.getOrderFieldsPanel() != null) {
            cashierScreen.getOrderFieldsPanel().removeAll();
            cashierScreen.getOrderPanel().remove(cashierScreen.getOrderFieldsPanel());
        }
        for (int i = 0; i < orderItems.length; i++) {
            if (orderItems[i][0] != 0) {
                JTextField orderItemTextField = new JTextField(model.getItemName(orderItems[i][0]) + " x" + String.valueOf(orderItems[i][1]));
                orderItemTextField.setEditable(false);
                orderItemTextField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                orderItemTextField.setPreferredSize(new Dimension(200 , 60));
                cashierScreen.getOrderFieldsPanel().add(orderItemTextField);
            }
        }
        cashierScreen.getOrderPanel().add(cashierScreen.getOrderFieldsPanel(), BorderLayout.CENTER);
        cashierScreen.getOrderPanel().revalidate();
        cashierScreen.getOrderPanel().repaint();
        populateCashierBottomPanel();
    }

    public void populateCashierBottomPanel() {
        JPanel bottomPanel = cashierScreen.getBottomPanel();
        if (cashierScreen.getFrame().isAncestorOf(bottomPanel)) {
            cashierScreen.getFrame().remove(bottomPanel);
        }
        if (bottomPanel.getComponentCount() > 0) {
            bottomPanel.removeAll();
        }
        bottomPanel.add(new JLabel("Cashier Name: " + model.getUserName(phoneNumber)));
        bottomPanel.add(Box.createHorizontalGlue());
        JLabel totalLabel = new JLabel("Total: " + model.sumItemPrices(orderItems));
        JButton orderCompleteButton = cashierScreen.getOrderCompleteButton();
        orderCompleteButton.setText("Complete Order");
        bottomPanel.add(totalLabel);
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(orderCompleteButton);
        bottomPanel.revalidate();
        bottomPanel.repaint();
        cashierScreen.getFrame().add(bottomPanel, BorderLayout.SOUTH);
    }

    public void completeCashierOrder() {
        JButton orderCompleteButton = cashierScreen.getOrderCompleteButton();
        orderCompleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // System.out.println("Order complete");
                String subtotal = String.valueOf(model.sumItemPrices(orderItems));
                JOptionPane.showMessageDialog(null, "Subtotal: " + subtotal);
                if(model.insert_order(subtotal, orderItems,"credit") == true){
                    JOptionPane.showMessageDialog(null, "Order submitted");
                }
                else{
                    JOptionPane.showMessageDialog(null, "Order not submitted");
                }
                switchToPaymentScreen();
                cashierScreen.getFrame().dispose();
            }
        });
    }

    public void populateManagerNavBar() {
        ImageIcon chartImageIcon = new ImageIcon("./images/chart.png");
        chartImageIcon = new ImageIcon(chartImageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
        ImageIcon orderImageIcon = new ImageIcon("./images/order.png");
        orderImageIcon = new ImageIcon(orderImageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
        ImageIcon trackImageIcon = new ImageIcon("./images/track.png");
        trackImageIcon = new ImageIcon(trackImageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
        ImageIcon tableImageIcon = new ImageIcon("./images/table.png");
        tableImageIcon = new ImageIcon(tableImageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
        JButton chartButton = new JButton(chartImageIcon);
        chartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateManagerMainPanel("chart");
            }
        });
        JButton orderButton = new JButton(orderImageIcon);
        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateManagerMainPanel("order");
            }
        });
        JButton trackButton = new JButton(trackImageIcon);
        trackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateManagerMainPanel("track");
            }
        });
        JButton tableButton = new JButton(tableImageIcon);
        tableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateManagerMainPanel("table");
            }
        });
        managerScreen.getNavPanel().add(chartButton);
        managerScreen.getNavPanel().add(Box.createVerticalGlue());
        managerScreen.getNavPanel().add(orderButton);
        managerScreen.getNavPanel().add(Box.createVerticalGlue());
        managerScreen.getNavPanel().add(trackButton);
        managerScreen.getNavPanel().add(Box.createVerticalGlue());
        managerScreen.getNavPanel().add(tableButton);
        managerScreen.getNavPanel().revalidate();
        managerScreen.getNavPanel().repaint();
    }

    private String parseDate(String dateString) {
        // Implement your date parsing logic here
        dateString = dateString.trim();
        dateString = dateString.replaceAll("/", "-");
        return dateString;
    }
    private void updateTable(JTable table) {
        // Fetch data from SQL and update the table
        try {
            ResultSet resultSet = model.getAllIngredients();
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.setColumnIdentifiers(new String[]{"ingredient_id", "ingredient_name", "ingredient_current_stock", "ingredient_unit_price"}); // Set column names
    
            while (resultSet.next()) {
                Object[] rowData = new Object[4];
                rowData[0] = resultSet.getObject(1); 
                rowData[1] = resultSet.getObject(2);
                rowData[2] = resultSet.getObject(3);
                rowData[3] = resultSet.getObject(4);
                tableModel.addRow(rowData);//add it to table
            }
    
            table.setModel(tableModel); // Set the updated table model
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void updateTableMenu(JTable table) {
        // Fetch data from SQL and update the table
        try {
            ResultSet resultSet = model.getAllMenus();
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.setColumnIdentifiers(new String[]{"item_id", "item_name", "item_price", "item_category"}); // Set column names
    
            while (resultSet.next()) {
                Object[] rowData = new Object[4];
                rowData[0] = resultSet.getObject(1); 
                rowData[1] = resultSet.getObject(2);
                rowData[2] = resultSet.getObject(3);
                rowData[3] = resultSet.getObject(4);
                tableModel.addRow(rowData);//add it to table
            }
    
            table.setModel(tableModel); // Set the updated table model
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    
    
    public void populateManagerMainPanel(String content) {
        JPanel mainPanel = managerScreen.getMainPanel();
        if (mainPanel.getComponentCount() > 0) {
            mainPanel.removeAll();
        }
        if (content.equals("chart")) {
            JTextArea chartTextArea = new JTextArea("Chart");
            chartTextArea.setEditable(false);
            chartTextArea.setPreferredSize(new Dimension(450, 500));
            mainPanel.add(chartTextArea);
        }
        else if (content.equals("order")) {
            mainPanel.setLayout(new BorderLayout());
    
            // Panel for ingredient ID and count
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new FlowLayout());
    
            JLabel ingredientIdLabel = new JLabel("Ingredient ID:");
            JTextField ingredientIdField = new JTextField(10);
    
            JLabel countLabel = new JLabel("Count:");
            JTextField countField = new JTextField(10);
    
            JButton commitButton = new JButton("Commit");
    
            inputPanel.add(ingredientIdLabel);
            inputPanel.add(ingredientIdField);
            inputPanel.add(countLabel);
            inputPanel.add(countField);
            inputPanel.add(commitButton);
    
            mainPanel.add(inputPanel, BorderLayout.NORTH);
            
            // Table to display results
            JTable table = new JTable();
            JScrollPane scrollPane = new JScrollPane(table);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            updateTable(table);
            // Add action listener to commit button
            commitButton.addActionListener(e -> {
                // Retrieve ingredient ID and count from text fields
                String ingredientId = ingredientIdField.getText();
                String count = countField.getText();
                
                if(model.addIngredient(ingredientId, count, phoneNumber) == true){
                    JOptionPane.showMessageDialog(null, "Stock updated");
                }
                else{
                    JOptionPane.showMessageDialog(null, "Stock not updated");
                }
                // Perform commit action here
                // You may want to update the table based on the committed data
                updateTable(table);
            });
        }
        else if (content.equals("track")) {
                        // Add start date text area
            JPanel startDatePanel = new JPanel();
            startDatePanel.setLayout(new FlowLayout());
            JLabel startDateLabel = new JLabel("Start Date yyyy-mm-dd:");
            JTextField startDateField = new JTextField(10); 
            startDatePanel.add(startDateLabel);
            startDatePanel.add(startDateField);
            mainPanel.add(startDatePanel);

            // Add end date text area
            JPanel endDatePanel = new JPanel();
            endDatePanel.setLayout(new FlowLayout());
            JLabel endDateLabel = new JLabel("End Date yyyy-mm-dd:");
            JTextField endDateField = new JTextField(10); 
            endDatePanel.add(endDateLabel);
            endDatePanel.add(endDateField);
            mainPanel.add(endDatePanel);

            // Add a table
            JTable table = new JTable(); // Initialize your table
            table.setSize(1000, 400);    
            JScrollPane scrollPane = new JScrollPane(table);
            mainPanel.add(scrollPane);
            String[] columnNames = {"ID", "Date", "Time", "Subtotal", "Tax", "Total", "Payment Method"};
            DefaultTableModel tablemodel = new DefaultTableModel(columnNames, 0);

            // Add an action listener to a button to fetch data from SQL based on dates
            JButton fetchDataButton = new JButton("Fetch Data");
            fetchDataButton.addActionListener(e -> {
                // Fetch data from SQL table based on start and end dates
                String startDate = parseDate(startDateField.getText());
                String endDate = parseDate(endDateField.getText());
                // Query SQL table using startDate and endDate
                ResultSet rs = model.getOrderDaytoDay(startDate, endDate);
                try{
                while (rs.next()) {
                    Object[] row = new Object[7]; // Assuming 7 columns in the result set
                    for (int i = 0; i < row.length; i++) {
                        row[i] = rs.getObject(i + 1); // Columns are 1-indexed in ResultSet
                    }
                    tablemodel.addRow(row);
                }
                table.setModel(tablemodel);

            } catch (Exception ex) {
                ex.printStackTrace();}
            });
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(Box.createVerticalStrut(10)); // Add some spacing
            mainPanel.add(scrollPane);
            mainPanel.add(Box.createVerticalStrut(10)); // Add some spacing
            mainPanel.add(fetchDataButton);
        }
        else if (content.equals("table")) {
            mainPanel.setLayout(new BorderLayout());
    
            // Panel for ingredient ID and count
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new FlowLayout());

            JLabel CorULabelk = new JLabel("Create or Update:");
            JTextField CorULabelField = new JTextField(10);

            JLabel itemIdLabel = new JLabel("Item ID:(only for update)");
            JTextField itemIdField = new JTextField(10);
            
            JLabel itemNameLabel = new JLabel("Item Name:");
            JTextField itemNameField = new JTextField(10);

<<<<<<< Updated upstream
            JLabel priceLabel = new JLabel("Price:");
            JTextField priceField = new JTextField(10);
=======
            // make table uneditable
            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            if (rs != null) {
                ResultSetMetaData data = rs.getMetaData();
                int cols = data.getColumnCount();
                // clear if data exists
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);

                // create columns with count
                for (int i = 2; i <= cols; i++) {
                    tableModel.addColumn(data.getColumnName(i));
                }

                // add rows
                while (rs.next()) {
                    Vector<Object> v = new Vector<Object>();
                    for (int i = 2; i <= cols; i++) {
                        v.add(rs.getObject(i));
                    }
                    tableModel.addRow(v);
                }
            }
            table.setModel(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to fetch data: " + e.getMessage());
        }
    }

    // populates the manager screen with a table of CRUD operations for each table
    public void populateManagerTablePanel() {
        JPanel mainPanel = managerScreen.getMainPanel();
        mainPanel.setLayout(new BorderLayout());

        // Panel for ingredient ID and count
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        //COMBOBOX
        JLabel tableLabel = new JLabel("Query Select: ");
        String[] dropDownList = {"Product Usage","Sales Report", "Excess Report", "Restock Report", "What Sells Together"};
        JComboBox comboBox = new JComboBox(dropDownList);

        JLabel timeStart = new JLabel("Start Date yyyy-mm-dd:");
        JTextField timeStart2 = new JTextField(10);

        JLabel timeEnd = new JLabel("End Date yyyy-mm-dd:");
        JTextField timeEnd2 = new JTextField(10);

        JLabel timeStamp = new JLabel("Time Stamp yyyy-mm-dd:");
        JTextField timeStamp2 = new JTextField(10);

        //inputPanel.add(tableLabel);
        inputPanel.add(comboBox);

        inputPanel.add(timeStart);
        inputPanel.add(timeStart2);

        inputPanel.add(timeEnd);
        inputPanel.add(timeEnd2);

        inputPanel.add(timeStamp);
        inputPanel.add(timeStamp2);
        timeStart2.setText("2024-01-15");
        timeEnd2.setText("2024-06-15");


        

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Table to display results. Used in viewTable
        JTable table = new JTable();

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        //String sql = "SELECT * FROM users WHERE phonenumber = ?";
            //PreparedStatement pstmt = conn.prepareStatement(sql);
            //pstmt.setString(1, phoneNumber);

        comboBox.addActionListener(e -> { //Resets boxes to white and then grays out and sets to uneditable the unneeded ones based on the option you select
            if(comboBox.getSelectedItem().equals("Product Usage")){
                TableQuery(model.executeQuery("SELECT * FROM menu_items ORDER BY category;"), table);
            } else if(comboBox.getSelectedItem().equals("Sales Report")){
                String sql = "SELECT c_order_to_item_list.item_id as item_id, COUNT(*) as itemCount, menu_Items.item_name as itemName\r\n" + //
                "FROM customer_order\r\n" + //
                "JOIN c_order_to_item_list ON customer_order.c_order_id = c_order_to_item_list.c_order_id \r\n" + //
                "JOIN menu_Items ON c_order_to_item_list.item_id = menu_Items.item_id \r\n" + //
                "WHERE c_order_date >= date(?) AND c_order_date <= date(?) \r\n" + //
                "GROUP BY c_order_to_item_list.item_id, menu_Items.item_name,DATE_PART('month', c_order_date)\r\n" + //
                "ORDER BY itemCount DESC;\r\n" + //
                "";
                try {
                    PreparedStatement pstmt = model.conn.prepareStatement(sql);
                    pstmt.setString(1,parseDate(timeStart2.getText()));
                    pstmt.setString(2, parseDate(timeEnd2.getText()));
                    ResultSet r = pstmt.executeQuery();
                    TableQuery(r, table);
                }
                catch (Exception er) {
                    er.printStackTrace();
                }

            } else if (comboBox.getSelectedItem().equals("Excess Report")) {
                TableQuery(model.executeQuery("SELECT * FROM menu_items ORDER BY category;"), table);
            } else if (comboBox.getSelectedItem().equals("Restock Report")) {
                //Adds the items that are currently less than 15 in number to the restock report
                TableQuery(model.executeQuery("SELECT *\r\n" + //
                                        "FROM ingredients\r\n" + 
                                        "WHERE ingredient_current_stock < 15\r\n" + //
                                        //In ascending order so that item with least amount 
                                        //is displayed first
                                        "ORDER BY ingredient_current_stock ASC;"), table);
            } else if(comboBox.getSelectedItem().equals("What Sells Together")){
                ResultSet Rs = model.findPair(timeStart2.getText(), timeEnd2.getText());
                TableQuery(Rs, table);
            }
            // updateFontSizes(table, managerScreen.getFrame());
            table.setFont(new Font("Arial", Font.PLAIN, preferredHeight / 65));
        });     
    }


    /*
     * PAYMENT SCREEN METHODS
     */
    // sets the payment screen to visible
    public void switchToPaymentScreen() {
        paymentScreen.getFrame().setVisible(true);
    }
>>>>>>> Stashed changes

            JLabel categoryLabel = new JLabel("Category:");
            JTextField categoryField = new JTextField(10);
    
            JButton commitButton = new JButton("Commit");
    
            inputPanel.add(CorULabelk);
            inputPanel.add(CorULabelField);
            inputPanel.add(itemIdLabel);
            inputPanel.add(itemIdField);

            inputPanel.add(itemNameLabel);
            inputPanel.add(itemNameField);

            inputPanel.add(priceLabel);
            inputPanel.add(priceField);

            inputPanel.add(categoryLabel);
            inputPanel.add(categoryField);

            inputPanel.add(commitButton);
    
            mainPanel.add(inputPanel, BorderLayout.NORTH);
            
            // Table to display results
            JTable table = new JTable();
            JScrollPane scrollPane = new JScrollPane(table);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            updateTableMenu(table);
            // Add action listener to commit button
            commitButton.addActionListener(e -> {
                // Retrieve ingredient ID and count from text fields
                String CorULabel = CorULabelField.getText();
                int itemId = Integer.parseInt(itemIdField.getText());
                String itemName = itemNameField.getText();
                String price = priceField.getText();
                String category = categoryField.getText();
                if (CorULabel.equals("Create")){
                    if(model.createItem(itemId, itemName, price, category) == true){
                        JOptionPane.showMessageDialog(null, "Stock updated");
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "Stock not updated");
                    }
                    updateTableMenu(table);
                }
<<<<<<< Updated upstream
                else{
                    try{
                        if(itemName != null){
                            model.updateItem(itemId, "item_name", itemName);
=======
            }
        });
    }

    
    /** 
     * @param table
     */
    private void ingredientsPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int id = table.rowAtPoint(p) + 1; // row + 1 becaues row 0 in Java == row 1 in SQL
                int tableType = 4;

                // if right clicked and not column header
                if (me.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    // create a popup menu for CRUD operations
                    JPopupMenu popupMenu = new JPopupMenu();

                    // create items for menu
                    JMenuItem updateItem = new JMenuItem("Update");
                    JMenuItem deleteItem = new JMenuItem("Delete");
                    JMenuItem createItem = new JMenuItem("Create");

                    // add items to menu
                    popupMenu.add(updateItem);
                    popupMenu.add(deleteItem);
                    popupMenu.add(createItem);

                    // add action listeners to items
                    updateItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a JDialog 
                            JDialog updateIngredientPopupDialog = new JDialog();
                            updateIngredientPopupDialog.setTitle("Update Ingredient");
                            updateIngredientPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateIngredientPopupDialog.setLocationRelativeTo(null);

                            // create a Container of Dialog's content pane for layout purposes
                            Container updateIngredientPopup = updateIngredientPopupDialog.getContentPane();
                            updateIngredientPopup.setLayout(new BoxLayout(updateIngredientPopup, BoxLayout.Y_AXIS));

                            // create and collect name label and field 
                            JPanel namePanel = new JPanel();
                            JLabel nameLabel = new JLabel("Ingredient Name: ");
                            JTextField nameField = new JTextField(model.getObject("ingredients", "ingredient_id", id, "ingredient_name"));
                            namePanel.add(nameLabel);
                            namePanel.add(nameField);

                            // create and collect current stock label and field 
                            JPanel currentStockPanel = new JPanel();
                            JLabel currentStockLabel = new JLabel("Ingredient Current Stock: ");
                            String previousStock = model.getObject("ingredients", "ingredient_id", id, "ingredient_current_stock");
                            JTextField currentStockField = new JTextField(previousStock);
                            currentStockPanel.add(currentStockLabel);
                            currentStockPanel.add(currentStockField);

                            // create and collect unit price label and ComboBox
                            JPanel unitPricePanel = new JPanel();
                            JLabel unitPriceLabel = new JLabel("Ingredient Unit Price: ");
                            JTextField unitPriceField = new JTextField(model.getObject("ingredients", "ingredient_id", id, "ingredient_unit_price"));
                            unitPricePanel.add(unitPriceLabel);
                            unitPricePanel.add(unitPriceField);

                            // create a button to commit the new ingredient to the database, dispose of dialog, and update table
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the ingredient in the database and also the table 
                                    String name = nameField.getText();
                                    Integer currentStock = Integer.parseInt(currentStockField.getText());
                                    Double unitPrice = Double.parseDouble(unitPriceField.getText());
                                    //model.updateIngredient(id, name, currentStock, unitPrice, (previousStock != currentStock), phoneNumber);
                                    updateIngredientPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and make it visible 
                            updateIngredientPopup.add(namePanel);
                            updateIngredientPopup.add(currentStockPanel);
                            updateIngredientPopup.add(unitPricePanel);
                            updateIngredientPopup.add(commitButton);
                            updateIngredientPopup.setVisible(true);
                            updateIngredientPopupDialog.setVisible(true);
                        
>>>>>>> Stashed changes
                        }
                        if(price != null){
                            model.updateItem(itemId, "item_price", price);
                        }
                        if(category != null){
                            model.updateItem(itemId, "category", category);
                        }

                    
                        JOptionPane.showMessageDialog(null, "Stock updated");
                    
                        // Perform commit action here
                        // You may want to update the table based on the committed data
                        updateTableMenu(table);
                 }
                    catch (Exception ex){
                        JOptionPane.showMessageDialog(null, "Stock not updated");
                    }
                }
            });
            
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }
}
