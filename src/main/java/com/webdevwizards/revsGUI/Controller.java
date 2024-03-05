package com.webdevwizards.revsGUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import com.formdev.flatlaf.FlatLightLaf;

import com.webdevwizards.revsGUI.screens.CashierScreen;
import com.webdevwizards.revsGUI.screens.LoginScreen;
import com.webdevwizards.revsGUI.screens.ManagerScreen;
import com.webdevwizards.revsGUI.screens.PaymentScreen;
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
    private int preferredWidth;
    private int preferredHeight;


    
    /** 
     * @param args
     */
    public static void main(String[] args) {
        // setup and display the login screen
        FlatLightLaf.setup();
        Controller controller = new Controller();
        controller.initialize();
        controller.switchToLoginScreen();
        controller.getPreferredSize();
        controller.switchFromLoginScreen();

    }

    // constructor
    public Controller() {
        // not necessary
    }

    // initializes the model and all screens
    public void initialize() {
        model = new Model();

        loginScreen = new LoginScreen();
        final JFrame loginFrame = loginScreen.getFrame();
        loginFrame.setVisible(false);
        updateFontSizes(loginFrame, loginFrame);

        cashierScreen = new CashierScreen();
        final JFrame cashierFrame = cashierScreen.getFrame();
        cashierFrame.setVisible(false);

        managerScreen = new ManagerScreen();
        final JFrame managerFrame = managerScreen.getFrame();
        managerFrame.setVisible(false);

        paymentScreen = new PaymentScreen();
        final JFrame paymentFrame = paymentScreen.getFrame();
        paymentFrame.setVisible(false);

        // auto update fontsizes
        loginFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateFontSizes(loginFrame, loginFrame);
            }
        });

        isManager = false;
        pf = new PopupFactory();
        orderItems = new int[10][2];

        // set preferred width and height to maximum screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        preferredWidth = screenSize.width;
        preferredHeight = screenSize.height;
    }


    /*
     * LOGIN SCREEN METHODS
     */
    
    // sets the login screen to visible
    public void switchToLoginScreen() {
        loginScreen.getFrame().setVisible(true);
    }

    // get preferred size of the frame from fullscreen button then set preferred values and the size of the frame
    public void getPreferredSize() {
        JToggleButton fullscreenButton = loginScreen.getFullscreenButton();
        fullscreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fullscreenButton.isSelected()) {
                    // set preferred width and height to half of the screen size
                    preferredWidth = (int) (preferredWidth * 0.5);
                    preferredHeight = (int) (preferredHeight * 0.5);
                    fullscreenButton.setText("Fullscreen");
                } else {
                    // set preferred width and height to full screen size
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    preferredWidth = screenSize.width;
                    preferredHeight = screenSize.height;
                    fullscreenButton.setText("Exit Fullscreen");
                }
                // set the size of the frames to the preferred width and height
                loginScreen.getFrame().setSize(preferredWidth, preferredHeight);
                cashierScreen.getFrame().setSize(preferredWidth, preferredHeight);
                managerScreen.getFrame().setSize(preferredWidth, preferredHeight);
                paymentScreen.getFrame().setSize(preferredWidth, preferredHeight);

                // revalidate and repaint the frame for redraw
                loginScreen.getFrame().revalidate();
                loginScreen.getFrame().repaint();
            }
        });
    }

    // switch to appropriate screen based on login's phone number
    public void switchFromLoginScreen() {
        loginScreen.getLoginButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // if the phone number is valid, switch to the appropriate screen
                if (model.login(loginScreen.getPhoneNumber())) {
                    phoneNumber = loginScreen.getPhoneNumber();

                    // if the user is a manager, switch to the manager screen and populate with defaults, get rid of the login screen
                    if (model.isManager(phoneNumber)) {
                        switchToManagerScreen();
                        managerScreen.getFrame().setSize(preferredWidth, preferredHeight);
                        loginScreen.getFrame().dispose();
                        populateManagerNavBar();
                        populateManagerMainPanel("order");

                        // auto update fonts
                        for (Component c : managerScreen.getFrame().getComponents()) {
                            updateFontSizes(c, managerScreen.getFrame());
                        }
                    } else { // if the user is a cashier, switch to the cashier screen and populate with defaults, get rid of the login screen, and completeOrder
                        switchToCashierScreen();
                        cashierScreen.getFrame().setSize(preferredWidth, preferredHeight);
                        loginScreen.getFrame().dispose();
                        populateCashierNavBar();
                        populateCashierItemPanel("Burgers");
                        populateCashierBottomPanel();
                        populateCashierOrderPanel();
                        // auto update fonts
                        for (Component c : cashierScreen.getFrame().getComponents()) {
                            updateFontSizes(c, cashierScreen.getFrame());
                        }
                        switchFromCashierPanel();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid phone number");
                }
            }
        });
    }


    /*
     * CASHIER SCREEN METHODS
     */

    // sets the cashier screen to visible and sets isManager to false
    public void switchToCashierScreen() {
        isManager = false;
        cashierScreen.getFrame().setVisible(true);
    }
    
    /** 
     * @param category
     */
    public void populateCashierItemPanel(String category) {
        // Get items and sort by category
        ResultSet rs = model.executeQuery("SELECT * FROM menu_items ORDER BY category;"); // TODO EDIT THIS LATER
        JPanel itemsPanel = cashierScreen.getItemsPanel();

        // get mainframe from cashier screen
        JFrame frame = cashierScreen.getFrame();

        // remove current items from the panel so they can be replaced
        itemsPanel.removeAll();

        // try loop to get items from the database safely
        try {

            // while there is another row in the result set
            while (rs.next()) {
                String db_category = rs.getString("category"); // get the category of the current row

                // if the category of the current row is the same as the category we want to display
                if (db_category.equals(category)) {
                    String item_name = rs.getString("item_name");
                    String item_price = rs.getString("item_price");
                    String jlabel_text =  item_name + "  " +"$"+ item_price;
                    StringBuilder item_image = new StringBuilder();

                    // formats image as item_name in lowercase and replaces spaces with underscores
                    for (int i = 0; i < item_name.length(); i++) {
                        char c = item_name.charAt(i);
                        if (Character.isLetterOrDigit(c)) {
                            item_image.append(Character.toLowerCase(c));
                        }
                        else if (c == ' ') {
                            item_image.append('_');
                        }
                    }

                    // append appropriate project root path and file extension
                    String item_image_path = "./images/" + item_image + ".png";

                    // create a new panel to hold the item image and name
                    JPanel itemPanel = new JPanel(new BorderLayout());
                    // System.out.println("Item image: " + item_image_path); // testing
                    // System.out.println("Adding item: " + item_name); // testing

                    // create a new button with an image
                    JButton itemButton = new JButton(new ImageIcon(item_image_path));
                    itemPanel.add(itemButton, BorderLayout.CENTER);

                    // add action listener to the image button to display a popup with the item name and an "Add to Order" button

                    itemButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JPanel popUpPanel = new JPanel();

                            // Get the screen size
                            Dimension frameSize = frame.getSize();

                            // sets the size and style of the popup panel
                            int size = (int) (frameSize.getWidth() / 2);
                            popUpPanel.setPreferredSize(new Dimension(size, size));
                            popUpPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
                            popUpPanel.setLayout(new BoxLayout(popUpPanel, BoxLayout.PAGE_AXIS));

                            // create and style the label and button for the popup
                            JLabel popUpLabel = new JLabel("Item: " + item_name);
                            popUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            JButton orderButton = new JButton("Add to Order");
                            orderButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                            // add action listener to the "Add to Order" button to add the item to the order and close the popup
                            orderButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // System.out.println("Item added to order: " + item_name);
                                    model.getItemID(item_name);
                                    for (int i = 0; i < orderItems.length; i++) {
                                        if (orderItems[i][0] == model.getItemID(item_name) || orderItems[i][0] == 0) {
                                            orderItems[i][0] = model.getItemID(item_name);
                                            orderItems[i][1] = orderItems[i][1] + 1;

                                            // reupdates the order panel
                                            populateCashierOrderPanel();

                                            // break because we found the item in the orderItems array
                                            break;
                                        }
                                    }
                                    po.hide();
                                }
                            });

                            JButton cancelButton = new JButton("Cancel");
                            cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                            // add action listener to the "Cancel" button to close the popup
                            cancelButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    po.hide();
                                }
                            });

                            // add the label and buttons to the popup panel with some vertical spacing
                            popUpPanel.add(Box.createVerticalGlue());
                            popUpPanel.add(popUpLabel);
                            popUpPanel.add(Box.createVerticalGlue());
                            popUpPanel.add(orderButton);
                            popUpPanel.add(Box.createVerticalStrut(30));
                            popUpPanel.add(cancelButton);
                            popUpPanel.add(Box.createVerticalGlue());

                            // sets the location of the popup panel (centered on the main frame)
                            Point frameLocation = frame.getLocation();
                            int x = (int) (frameLocation.getX() + (frameSize.getWidth() - popUpPanel.getPreferredSize().getWidth()) / 2);
                            int y = (int) (frameLocation.getY() + (frameSize.getHeight() - popUpPanel.getPreferredSize().getHeight()) / 2);

                            // creates and shows the popup
                            po = pf.getPopup(frame, popUpPanel, x, y);
                            po.show();
                        }
                    });

                    // create a new label with the item name to go below the center of the image button
                    JLabel itemName = new JLabel(jlabel_text, SwingConstants.CENTER);
                    itemName.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                    itemPanel.add(itemName, BorderLayout.SOUTH);

                    // add the panel to the itemsPanel
                    itemsPanel.add(itemPanel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // revalidate and repaint the itemsPanel for redraw
        updateFontSizes(itemsPanel, frame); // have to do this because otherwise on category change it reverts to normal size
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    // populates the cashier navbar and sets up action listeners for each category button
    public void populateCashierNavBar() {
        ResultSet rs = this.model.executeQuery("SELECT * FROM menu_items ORDER BY category;"); // TODO EDIT THIS LATER
        JPanel navPanel = cashierScreen.getNavPanel();

        // used in the while loop to keep track of the current category
        String current_category = "";

        // try loop to get categories from the database safely
        try {
            while (rs.next()) {
                String db_category = rs.getString("category");
                if (!db_category.equals(current_category)) {
                    // set the current category to the new category
                    current_category = db_category;

                    // get category icon image
                    StringBuilder category_file_name = new StringBuilder();

                    // formats image as category in lowercase and replaces spaces with underscores as well as replaces '&' with "and"
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

                    // get the category image and resize it
                    ImageIcon categoryImage = new ImageIcon("./images/" + category_file_name + ".png");
                    categoryImage = new ImageIcon(categoryImage.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH));

                    // create and style a new button with the category image
                    JButton categoryButton = new JButton(categoryImage);
                    categoryButton.setPreferredSize(new Dimension(100, 100));
                    // System.out.println("Category image: " + category_image_path); // testing

                    // create a new action listener for the category button to populate the item panel with the category's items using a final variable
                    final String current_category_final = current_category;
                    categoryButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            populateCashierItemPanel(current_category_final);
                        }
                    });

                    // add the category button to the navPanel
                    navPanel.add(categoryButton);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // populate cashier order panel with items in order
    public void populateCashierOrderPanel() {
        // remove all items from the orderFieldsPanel if it exists and then from the orderPanel as well
        JPanel orderPanel = cashierScreen.getOrderPanel();
        JPanel orderFieldsPanel = cashierScreen.getOrderFieldsPanel();
        if (orderPanel.getComponentCount() > 0) {
            orderPanel.removeAll();
            orderFieldsPanel.removeAll();
        }

        JLabel orderItemsLabel = new JLabel("Order Items");
        orderItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        orderPanel.add(orderItemsLabel, BorderLayout.NORTH);



        // add items (JTextField) to the orderFieldsPanel by looping through the orderItems array
        for (int i = 0; i < orderItems.length; i++) {
            if (orderItems[i][0] != 0) {
                JTextArea orderItemTextArea = new JTextArea(model.getItemName(orderItems[i][0]) + " x" + String.valueOf(orderItems[i][1]));
                orderItemTextArea.setEditable(false);
                orderItemTextArea.setLineWrap(true);
                orderItemTextArea.setWrapStyleWord(true);
                orderItemTextArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                orderItemTextArea.setPreferredSize(new Dimension(200, 60));
                orderFieldsPanel.add(orderItemTextArea);
            }
        }
        // // create scroll pane so that the text fields can be scrolled if there are too many
        // JScrollPane orderFieldsScrollPane = new JScrollPane(orderFieldsPanel);
        // orderFieldsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // orderFieldsScrollPane.setPreferredSize(new Dimension(200, 550));

        // add the orderFieldsPanel to the orderPanel and revalidate and repaint the orderPanel for redraw
        orderPanel.add(orderFieldsPanel, BorderLayout.CENTER);
        updateFontSizes(orderPanel, cashierScreen.getFrame()); // do this because removing all elements
        orderPanel.revalidate();
        orderPanel.repaint();

        // reinitialize the subtotal label in bottom panel
        populateCashierBottomPanel();
    }

    // populate the bottom panel of the cashier screen with the cashier's name and the subtotal of the order
    public void populateCashierBottomPanel() {
        JPanel bottomPanel = cashierScreen.getBottomPanel();

        // if bottom panel populated with items, remove all items
        if (bottomPanel.getComponentCount() > 0) {
            bottomPanel.removeAll();
        }

        // create a new button to complete the order
        cashierScreen.setOrderCompleteButton(null);
        JButton orderCompleteButton = cashierScreen.getOrderCompleteButton();
        orderCompleteButton.setText("Complete Order");

        // add the cashier's name, the subtotal of the order, and orderComplete button to the bottomPanel with empty horizontal glues for centering
        bottomPanel.add(new JLabel("Cashier Name: " + model.getUserName(phoneNumber)));
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(new JLabel("Subtotal: " + model.sumItemPrices(orderItems)));
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(orderCompleteButton);

        // add event listener to orderComplete button
        cashierScreen.setOrderCompleteButton(orderCompleteButton);
        switchFromCashierPanel();

        // revalidate and repaint the bottomPanel for redraw and add back to the cashierScreen frame
        // auto update fonts
        updateFontSizes(bottomPanel, cashierScreen.getFrame());
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    // complete the cashier's order via orderComplete button, display subtotal, and switch to payment screen
    public void switchFromCashierPanel() {
        JButton orderCompleteButton = cashierScreen.getOrderCompleteButton();

        // add action listener to orderComplete button to display subtotal and switch to payment screen when clicked
        orderCompleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // System.out.println("Order complete"); // testing
                String subtotal = String.valueOf(model.sumItemPrices(orderItems));

                // display total by converting subtotal to float and multiplying by 1.0825 (8.25% tax)
                JOptionPane.showMessageDialog(null, "Total: " + Float.parseFloat(subtotal)*1.0825);

                // insert order into database and display message based on success ; currently hardcoded to "credit"
                if(model.insert_order(subtotal, orderItems,"credit") == true){
                    JOptionPane.showMessageDialog(null, "Order submitted");
                }
                else{
                    JOptionPane.showMessageDialog(null, "Order not submitted");
                }

                // switch to payment screen and dispose of the cashier screen
                switchToPaymentScreen();

                // dispose of the cashier screen
                cashierScreen.getFrame().dispose();
            }
        });
    }


    /*
     * MANAGER SCREEN METHODS
     */

    // sets the manager screen to visible and sets isManager to true
    public void switchToManagerScreen() {
        isManager = true;
        managerScreen.getFrame().setVisible(true);
    }
    // populates the manager screen with a default navbar and adds action listeners for each button
    public void populateManagerNavBar() {
        String[] buttonNames = {"chart", "order", "track", "table"};

        // for each button name, create a new button with an image and add an action listener to populate the main panel with the corresponding content
        for (int i = 0; i < buttonNames.length; i++) {
            final int index = i;
            ImageIcon imageIcon = new ImageIcon("./images/" + buttonNames[i] + ".png");
            imageIcon = new ImageIcon(imageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
            JButton button = new JButton(imageIcon);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    populateManagerMainPanel(buttonNames[index]);
                }
            });
            // add buttons and vertical glue for spacing to the navPanel
            managerScreen.getNavPanel().add(button);
            if (i < buttonNames.length - 1) {
                managerScreen.getNavPanel().add(Box.createVerticalGlue());
            }
        }
        // revalidate and repaint the navPanel for redraw
        managerScreen.getNavPanel().revalidate();
        managerScreen.getNavPanel().repaint();
    }
    /** 
     * @param content
     */
    // populates the manager screen with content
    public void populateManagerMainPanel(String content) {
        JPanel mainPanel = managerScreen.getMainPanel();
        if (mainPanel.getComponentCount() > 0) {
            mainPanel.removeAll();
        }
        if (content.equals("chart")) {
            populateManagerChartPanel();
        }
        else if (content.equals("order")) {
            populateManagerOrderPanel();
        }
        else if (content.equals("track")) {
            populateManagerTrackPanel();
        }
        else if (content.equals("table")) {
            populateManagerTablePanel();

        }
        // revalidate and repaint the mainPanel for redraw
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    // populates the manager screen with a chart // TODO implement chart
    public void populateManagerChartPanel() {
        JPanel mainPanel = managerScreen.getMainPanel();
        JTextArea chartTextArea = new JTextArea("Chart");
        chartTextArea.setEditable(false);
        chartTextArea.setPreferredSize(new Dimension(450, 500));
        mainPanel.add(chartTextArea);
    }

    // populates the manager screen with the order panel
    public void populateManagerOrderPanel() {
        JPanel mainPanel = managerScreen.getMainPanel();
        mainPanel.setLayout(new BorderLayout());

        // Panel for ingredient ID and count
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        //COMBOBOX
        JLabel tableLabel = new JLabel("Table: ");
        String[] dropDownList = {"", "Users", "Manager Orders", "Customer Orders", "Items", "Ingredients"};
        JComboBox comboBox = new JComboBox(dropDownList);

        inputPanel.add(tableLabel);
        inputPanel.add(comboBox);
        

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Table to display results. Used in viewTable
        JTable table = new JTable();

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        

        comboBox.addActionListener(e -> { //Resets boxes to white and then grays out and sets to uneditable the unneeded ones based on the option you select
            if(comboBox.getSelectedItem().equals("Users")){
                usersPopUp(table);
                viewTable(table, 0);
            } else if(comboBox.getSelectedItem().equals("Manager Orders")){
                managerOrdersPopUp(table);
                viewTable(table, 1);
            } else if (comboBox.getSelectedItem().equals("Customer Orders")) {
                viewTable(table, 2);
                customerOrdersPopUp(table);
            } else if (comboBox.getSelectedItem().equals("Items")) {
                itemsPopUp(table);
                viewTable(table, 3);
            } else if(comboBox.getSelectedItem().equals("Ingredients")){
                ingredientsPopUp(table);
                viewTable(table, 4);
            }
        });  
    }

    // populates the manager screen with the track panel
    public void populateManagerTrackPanel() {
        JPanel mainPanel = managerScreen.getMainPanel();

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
    public void TableQuery(String query, JTable table) {
        try {
            ResultSet rs = model.executeQuery(query);

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

        JLabel timeStart = new JLabel("TimeStart");
        JTextField timeStart2 = new JTextField(10);

        JLabel timeEnd = new JLabel("TimeEnd");
        JTextField timeEnd2 = new JTextField(10);

        JLabel timeStamp = new JLabel("TimeStamp");
        JTextField timeStamp2 = new JTextField(10);

        //inputPanel.add(tableLabel);
        inputPanel.add(comboBox);

        inputPanel.add(timeStart);
        inputPanel.add(timeStart2);

        inputPanel.add(timeEnd);
        inputPanel.add(timeEnd2);

        inputPanel.add(timeStamp);
        inputPanel.add(timeStamp2);


        

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Table to display results. Used in viewTable
        JTable table = new JTable();

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        

        comboBox.addActionListener(e -> { //Resets boxes to white and then grays out and sets to uneditable the unneeded ones based on the option you select
            if(comboBox.getSelectedItem().equals("Product Usage")){
                TableQuery("SELECT * FROM menu_items ORDER BY category;", table);
            } else if(comboBox.getSelectedItem().equals("Sales Report")){
                TableQuery("SELECT c_order_to_item_list.item_id as item_id, COUNT(*) as itemCount \r\n" + //
                                        "FROM customer_order JOIN c_order_to_item_list ON customer_order.c_order_id = c_order_to_item_list.c_order_id \r\n" + //
                                        "WHERE DATE_PART('month', c_order_date) = DATE_PART('month', CURRENT_DATE - INTERVAL '1 month')\r\n" + //
                                        "GROUP BY item_id, DATE_PART('month', c_order_date)\r\n" + //
                                        "ORDER BY itemCount DESC;\r\n" + //
                                        "", table);
            } else if (comboBox.getSelectedItem().equals("Excess Report")) {
                TableQuery("SELECT * FROM menu_items ORDER BY category;", table);
            } else if (comboBox.getSelectedItem().equals("Restock Report")) {
                //Adds the items that are currently less than 15 in number to the restock report
                TableQuery("SELECT *\r\n" + //
                                        "\r\n" + //
                                        "FROM ingredients\r\n" + //
                                        "\r\n" + //
                                        "WHERE ingredient_current_stock < 15\r\n" + //
                                        "\r\n" + //
                                        "ORDER BY ingredient_current_stock ASC;", table);
            } else if(comboBox.getSelectedItem().equals("What Sells Together")){
                TableQuery("SELECT * FROM menu_items ORDER BY category;", table);
            }
        });     
    }


    /*
     * PAYMENT SCREEN METHODS
     */
    // sets the payment screen to visible
    public void switchToPaymentScreen() {
        paymentScreen.getFrame().setVisible(true);
    }

    
    /** 
     * @param dateString
     * @return String
     */
    /*
     * HELPER METHODS
     */
    // parses a date string to a format that can be used in SQL queries
    private String parseDate(String dateString) {
        // Implement your date parsing logic here
        dateString = dateString.trim();
        dateString = dateString.replaceAll("/", "-");
        return dateString;
    }
    
    /** 
     * @param table
     * @param tableType
     */
    // updates the table with the latest data from SQL
    // tableType 0 for users, 1 for manager orders, 2 for customer orders, 2 for items, 3 for ingredients
    private void viewTable(JTable table, int tableType) {
        // Fetch data from SQL and view the table
        // remove all mouse listeners from the table before updating it
        MouseListener[] mouseListeners = table.getMouseListeners();
        for (MouseListener mouseListener : mouseListeners) {
            table.removeMouseListener(mouseListener);
        }
        switch (tableType) { //if the function was called to display the menu_item table
            case 0: // USERS
                try {
                    ResultSet resultSet = model.getAllUsers();

                    // make table uneditable
                    DefaultTableModel tableModel = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };

                    // set columns to phone number | name | is manager ; and then populate with appropriate data
                    tableModel.setColumnIdentifiers(new String[]{"phone number", "name", "is manager"});
                    while (resultSet.next()) {
                        Object[] rowData = new Object[3];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(2);
                        rowData[1] = resultSet.getObject(3);
                        rowData[2] = resultSet.getObject(4);
                        tableModel.addRow(rowData); 
                    }
                    table.setModel(tableModel);

                    // set to false so user can't select row, its distracting and not needed
                    table.setRowSelectionAllowed(false);
                    table.setCellSelectionEnabled(false);

                    // change table column widths
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int tableWidth = table.getWidth();
                    table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 3);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                usersPopUp(table);
                break;
            case 1: // MANAGER ORDERS
                try {
                    ResultSet resultSet = model.getAllManagerOrders();

                    // make table uneditable
                    DefaultTableModel tableModel = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    // set columns to phone number | date | time | total ; and then populate with appropriate data
                    tableModel.setColumnIdentifiers(new String[]{"phone number", "date", "time", "total"}); 
                    while (resultSet.next()) {
                        Object[] rowData = new Object[4];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(5);
                        rowData[1] = resultSet.getObject(2);
                        rowData[2] = resultSet.getObject(3);
                        rowData[3] = resultSet.getObject(4);
                        tableModel.addRow(rowData);
                    }
                    table.setModel(tableModel);

                    // set to false so user can't select row, its distracting and not needed
                    table.setRowSelectionAllowed(false);
                    table.setCellSelectionEnabled(false);

                    // change table column widths
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int tableWidth = table.getWidth();
                    table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 4);
                    table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 4);
                    table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 4);
                    table.getColumnModel().getColumn(3).setPreferredWidth(tableWidth / 4);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                managerOrdersPopUp(table);
                break;
            case 2: // CUSTOMER ORDERS
                try {
                    ResultSet resultSet = model.getAllCustomerOrders();

                    // make table uneditable
                    DefaultTableModel tableModel = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    // set columns to date | time  | subtotal | tax | total | payment type ; and then populate with appropriate data
                    tableModel.setColumnIdentifiers(new String[]{"date", "time", "subtotal", "tax", "total", "payment type"});
                    while (resultSet.next()) {
                        Object[] rowData = new Object[6];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(2);
                        rowData[1] = resultSet.getObject(3);
                        rowData[2] = resultSet.getObject(4);
                        rowData[3] = resultSet.getObject(5);
                        rowData[4] = resultSet.getObject(6);
                        rowData[5] = resultSet.getObject(7);
                        tableModel.addRow(rowData);
                    }
                    table.setModel(tableModel);

                    // set to false so user can't select row, its distracting and not needed
                    table.setRowSelectionAllowed(false);
                    table.setCellSelectionEnabled(false);

                    // change table column widths
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int tableWidth = table.getWidth();
                    table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(3).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(4).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(5).setPreferredWidth(tableWidth / 6);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                customerOrdersPopUp(table);
                break;
            case 3: // ITEMS
                try {
                    ResultSet resultSet = model.getAllItemsAndIngredients();

                    // make table uneditable
                    DefaultTableModel tableModel = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };

                    // set columns to name | price | category | ingredients ; and then populate with appropriate data
                    tableModel.setColumnIdentifiers(new String[]{"name", "price", "category", "ingredients"}); // Set column names
                    while (resultSet.next()) {
                        Object[] rowData = new Object[4];
                        rowData[0] = resultSet.getObject(1);
                        rowData[1] = resultSet.getObject(2);
                        rowData[2] = resultSet.getObject(3);
                        rowData[3] = resultSet.getObject(4);
                        tableModel.addRow(rowData);

                    }
                    table.setModel(tableModel);

                    // set to false so user can't select row, its distracting and not needed
                    table.setRowSelectionAllowed(false);
                    table.setCellSelectionEnabled(false);

                    // change table column widths
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int tableWidth = table.getWidth();
                    table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 6);
                    table.getColumnModel().getColumn(3).setPreferredWidth(tableWidth / 2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                itemsPopUp(table);
                break;
            case 4: // INGREDIENTS
                try {
                    ResultSet resultSet = model.getAllIngredients();

                    // make table uneditable
                    DefaultTableModel tableModel = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };

                    // set columns to name | current stock | unit price ; and then populate with appropriate data
                    tableModel.setColumnIdentifiers(new String[]{"name", "current stock", "unit price"}); // Set column names
                    while (resultSet.next()) {
                        Object[] rowData = new Object[3];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(2);
                        rowData[1] = resultSet.getObject(3);
                        rowData[2] = resultSet.getObject(4);
                        tableModel.addRow(rowData);
                    }
                    table.setModel(tableModel); 
                
                    // set to false so user can't select a row, its distracting and not needed
                    table.setRowSelectionAllowed(false);

                    // change table column widths
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int tableWidth = table.getWidth();
                    table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 3);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ingredientsPopUp(table);
                break;
        }
    }

    
    
    /** 
     * @param table
     */
    private void usersPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int id = table.rowAtPoint(p) + 1;
                int tableType = 0;
                if (me.getClickCount() == 2  && table.getSelectedRow() != -1) {
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
                            JDialog updateUserPopupDialog = new JDialog();
                            updateUserPopupDialog.setTitle("Update User");
                            updateUserPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateUserPopupDialog.setLocationRelativeTo(null);

                            // create Container of Dialog's content pane for layout purposes  
                            Container updateUserPopup = updateUserPopupDialog.getContentPane();
                            updateUserPopup.setLayout(new BoxLayout(updateUserPopup, BoxLayout.Y_AXIS));
                            
                            // create and collect phoneNumber label and field 
                            JPanel phoneNumberPanel = new JPanel();
                            JLabel phoneNumberLabel = new JLabel("User Phone Number: ");
                            JTextField phoneNumberField = new JTextField(10);
                            phoneNumberField.setText(model.getObject("users", "user_id", id, "phonenumber"));
                            phoneNumberPanel.add(phoneNumberLabel);
                            phoneNumberPanel.add(phoneNumberField);

                            // create and collect name label and field 
                            JPanel namePanel = new JPanel();
                            JLabel nameLabel = new JLabel("User's Name: ");
                            JTextField nameField = new JTextField(20);
                            nameField.setText(model.getObject("users", "user_id", id, "name"));
                            namePanel.add(nameLabel);
                            namePanel.add(nameField);

                            // create and collect isManager label and field 
                            JPanel isManagerPanel = new JPanel();
                            JLabel isManagerLabel = new JLabel("User Manager Status: ");
                            String[] managerStatus = {"true", "false"};
                            JComboBox isManagerComboBox = new JComboBox(managerStatus);
                            boolean isManager = Boolean.parseBoolean(model.getObject("users", "user_id", id, "ismanager"));
                            isManagerComboBox.setSelectedItem(String.valueOf(isManager));
                            isManagerPanel.add(isManagerLabel);
                            isManagerPanel.add(isManagerComboBox);

                            // create commit button that triggers model.updateCustomerOrder() on click and disposes of dialog
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database and also the table 
                                    String phoneNumber = phoneNumberField.getText();
                                    String name = nameField.getText();
                                    boolean isManager = isManagerComboBox.getSelectedItem().equals("true");
                                    model.updateUser(id, phoneNumber, name, isManager);
                                    updateUserPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and display the popup 
                            updateUserPopup.add(phoneNumberPanel);
                            updateUserPopup.add(namePanel);
                            updateUserPopup.add(isManagerPanel);
                            updateUserPopup.add(commitButton);
                            updateUserPopup.setVisible(true);
                            updateUserPopupDialog.setVisible(true);
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            
                            // create a JDialog
                            JDialog deleteUserPopupDialog = new JDialog();
                            deleteUserPopupDialog.setTitle("Delete User");
                            deleteUserPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            deleteUserPopupDialog.setLocationRelativeTo(null);

                            // create Container of Dialog's content pane for layout purposes
                            Container deleteUserPopup = deleteUserPopupDialog.getContentPane();
                            deleteUserPopup.setLayout(new BoxLayout(deleteUserPopup, BoxLayout.Y_AXIS));
                            JLabel deleteUserLabel = new JLabel("Are you sure you want to delete this user: " + model.getObject("users", "user_id", id, "name") + "?");
                            JButton confirmButton = new JButton("Confirm");

                            // TODO : verify query is correct 
                            // deletes order, gets rid of dialog, and refreshes table 
                            confirmButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    model.deleteUser(id);
                                    deleteUserPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add everything and display dialog 
                            deleteUserPopup.add(deleteUserLabel);
                            deleteUserPopup.add(confirmButton);
                            deleteUserPopup.setVisible(true);
                            deleteUserPopupDialog.setVisible(true);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a JDialog
                            JDialog updateUserPopupDialog = new JDialog();
                            updateUserPopupDialog.setTitle("Update User");
                            updateUserPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateUserPopupDialog.setLocationRelativeTo(null);

                            // create Container of Dialog's content pane for layout purposes  
                            Container updateUserPopup = updateUserPopupDialog.getContentPane();
                            updateUserPopup.setLayout(new BoxLayout(updateUserPopup, BoxLayout.Y_AXIS));
                            
                            // create and collect phoneNumber label and field 
                            JPanel phoneNumberPanel = new JPanel();
                            JLabel phoneNumberLabel = new JLabel("User Phone Number: ");
                            JTextField phoneNumberField = new JTextField(10);
                            phoneNumberField.setText("1234567890");
                            phoneNumberPanel.add(phoneNumberLabel);
                            phoneNumberPanel.add(phoneNumberField);

                            // create and collect name label and field 
                            JPanel namePanel = new JPanel();
                            JLabel nameLabel = new JLabel("User's Name: ");
                            JTextField nameField = new JTextField(20);
                            nameField.setText("<first name> <last name>");
                            namePanel.add(nameLabel);
                            namePanel.add(nameField);

                            // create and collect isManager label and field 
                            JPanel isManagerPanel = new JPanel();
                            JLabel isManagerLabel = new JLabel("User Manager Status: ");
                            String[] managerStatus = {"true", "false"};
                            JComboBox isManagerComboBox = new JComboBox(managerStatus);
                            isManagerComboBox.setSelectedItem("false");
                            isManagerPanel.add(isManagerLabel);
                            isManagerPanel.add(isManagerComboBox);

                            // create commit button that triggers model.updateCustomerOrder() on click and disposes of dialog
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database and also the table 
                                    String phoneNumber = phoneNumberField.getText();
                                    String name = nameField.getText();
                                    boolean isManager = isManagerComboBox.getSelectedItem().equals("true");
                                    model.createUser(phoneNumber, name, isManager);
                                    updateUserPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and display the popup 
                            updateUserPopup.add(phoneNumberPanel);
                            updateUserPopup.add(namePanel);
                            updateUserPopup.add(isManagerPanel);
                            updateUserPopup.add(commitButton);
                            updateUserPopup.setVisible(true);
                            updateUserPopupDialog.setVisible(true);
                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

    
    /** 
     * @param table
     */
    private void managerOrdersPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int id = table.rowAtPoint(p) + 1; // row + 1 because row 0 in Java == row 1 in SQL
                int tableType = 1;

                // if right clicked and not on column header
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
                            JDialog updateOrderPopupDialog = new JDialog();
                            updateOrderPopupDialog.setTitle("Update Order");
                            updateOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateOrderPopupDialog.setLocationRelativeTo(null);

                            // create a Container from the Dialog's content pane for layout purposes
                            Container updateOrderPopup = updateOrderPopupDialog.getContentPane();
                            updateOrderPopup.setLayout(new BoxLayout(updateOrderPopup, BoxLayout.Y_AXIS));
                            
                            // create and collect date label and field 
                            JPanel datePanel = new JPanel();
                            JLabel dateLabel = new JLabel("Order Date: ");
                            JTextField dateField = new JTextField(10);
                            dateField.setText(model.getObject("manager_order", "m_order_id", id, "m_order_date"));
                            datePanel.add(dateLabel);
                            datePanel.add(dateField);

                            // create and collect time label and field 
                            JPanel timePanel = new JPanel();
                            JLabel timeLabel = new JLabel("Order Time: ");
                            JTextField timeField = new JTextField(12);
                            timeField.setText(model.getObject("manager_order", "m_order_id", id, "m_order_time"));
                            timePanel.add(timeLabel);
                            timePanel.add(timeField);

                            // create and collect total label and field 
                            JPanel totalPanel = new JPanel();
                            JLabel totalLabel = new JLabel("Order Total: ");
                            JTextField totalField = new JTextField(7);
                            totalField.setText(model.getObject("manager_order", "m_order_id", id, "m_order_total"));
                            totalPanel.add(totalLabel);
                            totalPanel.add(totalField);

                            // create and collect payment method label and field 
                            JPanel phoneNumberPanel = new JPanel();
                            JLabel phoenNumberLabel = new JLabel("Order Phone Number: ");
                            JTextField phoneNumberField = new JTextField(10);
                            phoneNumberField.setText(model.getObject("manager_order", "m_order_id", id, "phonenumber"));
                            phoneNumberPanel.add(phoenNumberLabel);
                            phoneNumberPanel.add(phoneNumberField);

                            // create commit button that triggers model.updateCustomerOrder() on click and disposes of dialog
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database and also the table 
                                    String date = dateField.getText();
                                    String time = timeField.getText();
                                    Double total = Double.parseDouble(totalField.getText());
                                    String phoneNumber = phoneNumberField.getText();
                                    model.updateManagerOrder(id, date, time, total, phoneNumber);
                                    updateOrderPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and display the popup 
                            updateOrderPopup.add(datePanel);
                            updateOrderPopup.add(timePanel);
                            updateOrderPopup.add(totalPanel);
                            updateOrderPopup.add(phoneNumberPanel);
                            updateOrderPopup.add(commitButton);
                            updateOrderPopup.setVisible(true);
                            updateOrderPopupDialog.setVisible(true);
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            
                            // create a JDialog
                            JDialog deleteOrderPopupDialog = new JDialog();
                            deleteOrderPopupDialog.setTitle("Delete Order");
                            deleteOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            deleteOrderPopupDialog.setLocationRelativeTo(null);

                            // create Container of Dialog's content pane for layout purposes
                            Container deleteOrderPopup = deleteOrderPopupDialog.getContentPane();
                            deleteOrderPopup.setLayout(new BoxLayout(deleteOrderPopup, BoxLayout.Y_AXIS));
                            
                            JLabel deleteOrderLabel = new JLabel("Are you sure you want to delete this order?");
                            JButton confirmButton = new JButton("Confirm");

                            // TODO : verify query is correct 
                            // deletes order, gets rid of dialog, and refreshes table 
                            confirmButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    model.deleteManagerOrder(id);
                                    deleteOrderPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add everything and display dialog 
                            deleteOrderPopup.add(deleteOrderLabel);
                            deleteOrderPopup.add(confirmButton);
                            deleteOrderPopup.setVisible(true);
                            deleteOrderPopupDialog.setVisible(true);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a JDialog
                            JDialog createOrderPopupDialog = new JDialog();
                            createOrderPopupDialog.setTitle("Update Order");
                            createOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            createOrderPopupDialog.setLocationRelativeTo(null);

                            // create a Container from the Dialog's content pane for layout purposes
                            Container createOrderPopup = createOrderPopupDialog.getContentPane();
                            createOrderPopup.setLayout(new BoxLayout(createOrderPopup, BoxLayout.Y_AXIS));
                            
                            // create and collect date label and field 
                            JPanel datePanel = new JPanel();
                            JLabel dateLabel = new JLabel("Order Date: ");
                            JTextField dateField = new JTextField(10);
                            dateField.setText("YYYY-MM-DD");
                            datePanel.add(dateLabel);
                            datePanel.add(dateField);

                            // create and collect time label and field 
                            JPanel timePanel = new JPanel();
                            JLabel timeLabel = new JLabel("Order Time: ");
                            JTextField timeField = new JTextField(12);
                            timeField.setText("HH:MM:SS.sss");
                            timePanel.add(timeLabel);
                            timePanel.add(timeField);

                            // create and collect total label and field 
                            JPanel totalPanel = new JPanel();
                            JLabel totalLabel = new JLabel("Order Total: ");
                            JTextField totalField = new JTextField(7);
                            totalField.setText("0.00");
                            totalPanel.add(totalLabel);
                            totalPanel.add(totalField);

                            // create and collect payment method label and field 
                            JPanel phoneNumberPanel = new JPanel();
                            JLabel phoenNumberLabel = new JLabel("Order Phone Number: ");
                            JTextField phoneNumberField = new JTextField(10);
                            phoneNumberField.setText("1234567890");
                            phoneNumberPanel.add(phoenNumberLabel);
                            phoneNumberPanel.add(phoneNumberField);

                            // create commit button that triggers model.updateCustomerOrder() on click and disposes of dialog
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database and also the table 
                                    String date = dateField.getText();
                                    String time = timeField.getText();
                                    Double total = Double.parseDouble(totalField.getText());
                                    String phoneNumber = phoneNumberField.getText();
                                    model.createManagerOrder(date, time, total, phoneNumber);
                                    createOrderPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and display the popup 
                            createOrderPopup.add(datePanel);
                            createOrderPopup.add(timePanel);
                            createOrderPopup.add(totalPanel);
                            createOrderPopup.add(phoneNumberPanel);
                            createOrderPopup.add(commitButton);
                            createOrderPopup.setVisible(true);
                            createOrderPopupDialog.setVisible(true);
                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

    
    /** 
     * @param table
     */
    private void customerOrdersPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int id = table.rowAtPoint(p) + 1; // row + 1 because row 0 in Java == row 1 in SQL
                int tableType = 2;

                // if right clicked and not on column header  
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
                            JDialog updateOrderPopupDialog = new JDialog();
                            updateOrderPopupDialog.setTitle("Update Order");
                            updateOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateOrderPopupDialog.setLocationRelativeTo(null);

                            // create a Container from the Dialog's content pane for layout purposes
                            Container updateOrderPopup = updateOrderPopupDialog.getContentPane();
                            updateOrderPopup.setLayout(new BoxLayout(updateOrderPopup, BoxLayout.Y_AXIS));
                            
                            // create and collect date label and field 
                            JPanel datePanel = new JPanel();
                            JLabel dateLabel = new JLabel("Order Date: ");
                            JTextField dateField = new JTextField(10);
                            dateField.setText(model.getObject("customer_order", "c_order_id", id, "c_order_date"));
                            datePanel.add(dateLabel);
                            datePanel.add(dateField);

                            // create and collect time label and field 
                            JPanel timePanel = new JPanel();
                            JLabel timeLabel = new JLabel("Order Time: ");
                            JTextField timeField = new JTextField(12);
                            timeField.setText(model.getObject("customer_order", "c_order_id", id, "c_order_time"));
                            timePanel.add(timeLabel);
                            timePanel.add(timeField);

                            // create and collect subtotal label and field 
                            JPanel subtotalPanel = new JPanel();
                            JLabel subtotalLabel = new JLabel("Order Subtotal: ");
                            JTextField subtotalField = new JTextField(7);
                            subtotalField.setText(model.getObject("customer_order", "c_order_id", id, "c_order_subtotal"));
                            subtotalPanel.add(subtotalLabel);
                            subtotalPanel.add(subtotalField);

                            // create and collect tax label and field 
                            JPanel taxPanel = new JPanel();
                            JLabel taxLabel = new JLabel("Order Tax: ");
                            JTextField taxField = new JTextField(6);
                            taxField.setText(model.getObject("customer_order", "c_order_id", id, "c_order_tax"));
                            taxPanel.add(taxLabel);
                            taxPanel.add(taxField);

                            // create and collect total label and field 
                            JPanel totalPanel = new JPanel();
                            JLabel totalLabel = new JLabel("Order Total: ");
                            JTextField totalField = new JTextField(7);
                            totalField.setText(model.getObject("customer_order", "c_order_id", id, "c_order_total"));
                            totalPanel.add(totalLabel);
                            totalPanel.add(totalField);

                            // create and collect payment method label and field 
                            JPanel paymentMethodPanel = new JPanel();
                            JLabel paymentMethodLabel = new JLabel("Payment Method: ");
                            JTextField paymentMethodField = new JTextField(10);
                            paymentMethodField.setText(model.getObject("customer_order", "c_order_id", id, "c_order_payment_type"));
                            paymentMethodPanel.add(paymentMethodLabel);
                            paymentMethodPanel.add(paymentMethodField);

                            // create commit button that triggers model.updateCustomerOrder() on click and disposes of dialog
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database and also the table 
                                    String date = dateField.getText();
                                    String time = timeField.getText();
                                    String subtotal = subtotalField.getText();
                                    String tax = taxField.getText();
                                    String total = totalField.getText();
                                    String paymentMethod = paymentMethodField.getText();
                                    model.updateCustomerOrder(id, date, time, subtotal, tax, total, paymentMethod);
                                    updateOrderPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and display the popup 
                            updateOrderPopup.add(datePanel);
                            updateOrderPopup.add(timePanel);
                            updateOrderPopup.add(subtotalPanel);
                            updateOrderPopup.add(taxPanel);
                            updateOrderPopup.add(totalPanel);
                            updateOrderPopup.add(paymentMethodPanel);
                            updateOrderPopup.add(commitButton);
                            updateOrderPopup.setVisible(true);
                            updateOrderPopupDialog.setVisible(true);
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            // create a JDialog
                            JDialog deleteOrderPopupDialog = new JDialog();
                            deleteOrderPopupDialog.setTitle("Delete Order");
                            deleteOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            deleteOrderPopupDialog.setLocationRelativeTo(null);

                            // create Container of Dialog's content pane for layout purposes
                            Container deleteOrderPopup = deleteOrderPopupDialog.getContentPane();
                            deleteOrderPopup.setLayout(new BoxLayout(deleteOrderPopup, BoxLayout.Y_AXIS));
                            
                            JLabel deleteOrderLabel = new JLabel("Are you sure you want to delete this order?");
                            JButton confirmButton = new JButton("Confirm");

                            // TODO : verify query is correct 
                            // deletes order, gets rid of dialog, and refreshes table 
                            confirmButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    model.deleteCustomerOrder(id);
                                    deleteOrderPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add everything and display dialog 
                            deleteOrderPopup.add(deleteOrderLabel);
                            deleteOrderPopup.add(confirmButton);
                            deleteOrderPopup.setVisible(true);
                            deleteOrderPopupDialog.setVisible(true);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a popup to create the order
                            JDialog createOrderPopupDialog = new JDialog();
                            createOrderPopupDialog.setTitle("Create Order");
                            createOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            createOrderPopupDialog.setLocationRelativeTo(null);

                            // create a Container of Dialog's content pane for layout purposes
                            Container createOrderPopup = createOrderPopupDialog.getContentPane();
                            createOrderPopup.setLayout(new BoxLayout(createOrderPopup, BoxLayout.Y_AXIS));
                            
                            // create and collect date label and field 
                            JPanel datePanel = new JPanel();
                            JLabel dateLabel = new JLabel("Order Date: ");
                            JTextField dateField = new JTextField(10);
                            dateField.setText("YYYY-MM-DD");
                            datePanel.add(dateLabel);
                            datePanel.add(dateField);

                            // create and collect time label and field 
                            JPanel timePanel = new JPanel();
                            JLabel timeLabel = new JLabel("Order Time: ");
                            JTextField timeField = new JTextField(12);
                            timeField.setText("HH:MM:SS.SSS");
                            timePanel.add(timeLabel);
                            timePanel.add(timeField);

                            // create and collect sybtotal label and field 
                            JPanel subtotalPanel = new JPanel();
                            JLabel subtotalLabel = new JLabel("Order Subtotal: ");
                            JTextField subtotalField = new JTextField(7);
                            subtotalField.setText("0.00");
                            subtotalPanel.add(subtotalLabel);
                            subtotalPanel.add(subtotalField);

                            // create and collect tax label and field 
                            JPanel taxPanel = new JPanel();
                            JLabel taxLabel = new JLabel("Order Tax: ");
                            JTextField taxField = new JTextField(6);
                            taxField.setText("0.00");
                            taxPanel.add(taxLabel);
                            taxPanel.add(taxField);

                            // create and collect total label and field 
                            JPanel totalPanel = new JPanel();
                            JLabel totalLabel = new JLabel("Order Total: ");
                            JTextField totalField = new JTextField(7);
                            totalField.setText("0.00");
                            totalPanel.add(totalLabel);
                            totalPanel.add(totalField);

                            // create and collect payemnt method label and field 
                            JPanel paymentMethodPanel = new JPanel();
                            JLabel paymentMethodLabel = new JLabel("Payment Method: ");
                            JTextField paymentMethodField = new JTextField(10);
                            paymentMethodField.setText("cash or credit");
                            paymentMethodPanel.add(paymentMethodLabel);
                            paymentMethodPanel.add(paymentMethodField);

                            // create commit button, update database, and update table 
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database
                                    String date = dateField.getText();
                                    String time = timeField.getText();
                                    String subtotal = subtotalField.getText();
                                    String tax = taxField.getText();
                                    String total = totalField.getText();
                                    String paymentMethod = paymentMethodField.getText();
                                    model.createCustomerOrder(date, time, subtotal, tax, total, paymentMethod);
                                    createOrderPopupDialog.dispose();
                                }
                            });

                            // add all panels to the popup and show it 
                            createOrderPopup.add(datePanel);
                            createOrderPopup.add(timePanel);
                            createOrderPopup.add(subtotalPanel);
                            createOrderPopup.add(taxPanel);
                            createOrderPopup.add(totalPanel);
                            createOrderPopup.add(paymentMethodPanel);
                            createOrderPopup.add(commitButton);
                            createOrderPopup.setVisible(true);
                            createOrderPopupDialog.setVisible(true);
                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

    
    /** 
     * @param table
     */
    private void itemsPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int id = table.rowAtPoint(p) + 1;
                int tableType = 3;

                // if right clicked and not in column header
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
                            JDialog updateItemPopupDialog = new JDialog();
                            updateItemPopupDialog.setTitle("Update Item");
                            updateItemPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateItemPopupDialog.setLocationRelativeTo(null);

                            // create a Container of Dialog's content pane for layout purposes
                            Container updateItemPopup = updateItemPopupDialog.getContentPane();
                            updateItemPopup.setLayout(new BoxLayout(updateItemPopup, BoxLayout.Y_AXIS));

                            // create and collect name label and field 
                            JPanel namePanel = new JPanel();
                            JLabel nameLabel = new JLabel("Item Name: ");
                            JTextField nameField = new JTextField(model.getObject("menu_items", "item_id", id, "item_name"));
                            namePanel.add(nameLabel);
                            namePanel.add(nameField);

                            // create and collect price label and field 
                            JPanel pricePanel = new JPanel();
                            JLabel priceLabel = new JLabel("Item Price: ");
                            JTextField priceField = new JTextField(model.getObject("menu_items", "item_id", id, "item_price"));
                            pricePanel.add(priceLabel);
                            pricePanel.add(priceField);

                            // create and collect category label and ComboBox
                            JPanel categoryPanel = new JPanel();
                            JLabel categoryLabel = new JLabel("Item Category: ");
                            String[] categoryDropDown = {"Choose An Option", "Appetizers", "Beverages", "Burgers", "Limited Time Offer", "Salads", "Sandwiches", "Shakes & More", "Value Meals"};
                            JComboBox categoryComboBox = new JComboBox(categoryDropDown);
                            categoryComboBox.setSelectedItem(model.getObject("menu_items", "item_id", id, "category"));
                            categoryPanel.add(categoryLabel);
                            categoryPanel.add(categoryComboBox);

                            // create a button to commit the new item to the database, dispose of dialog, and update table
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database and also the table 
                                    String name = nameField.getText();
                                    Double price = Double.parseDouble(priceField.getText());
                                    String category = categoryComboBox.getSelectedItem().toString();
                                    model.updateItem(id, name, price, category);
                                    updateItemPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and make it visible 
                            updateItemPopup.add(namePanel);
                            updateItemPopup.add(pricePanel);
                            updateItemPopup.add(categoryPanel);
                            updateItemPopup.add(commitButton);
                            updateItemPopup.setVisible(true);
                            updateItemPopupDialog.setVisible(true);
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a JDialog
                            JDialog deleteItemPopupDialog = new JDialog();
                            deleteItemPopupDialog.setTitle("Delete Item");
                            deleteItemPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            deleteItemPopupDialog.setLocationRelativeTo(null);

                            // create Container of Dialog's content pane for layout purposes
                            Container deleteItemPopup = deleteItemPopupDialog.getContentPane();
                            deleteItemPopup.setLayout(new BoxLayout(deleteItemPopup, BoxLayout.Y_AXIS));
                            
                            JLabel deleteItemLabel = new JLabel("Are you sure you want to delete this item?");
                            JButton confirmButton = new JButton("Confirm");

                            // TODO : verify query is correct 
                            // deletes order, gets rid of dialog, and refreshes table 
                            confirmButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    model.deleteItem(id);  
                                    deleteItemPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add everything and display dialog 
                            deleteItemPopup.add(deleteItemLabel);
                            deleteItemPopup.add(confirmButton);
                            deleteItemPopup.setVisible(true);
                            deleteItemPopupDialog.setVisible(true);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a JDialog 
                            JDialog createItemPopupDialog = new JDialog();
                            createItemPopupDialog.setTitle("Create Item");
                            createItemPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            createItemPopupDialog.setLocationRelativeTo(null);

                            // create a Container of Dialog's content pane for layout purposes
                            Container createItemPopup = createItemPopupDialog.getContentPane();
                            createItemPopup.setLayout(new BoxLayout(createItemPopup, BoxLayout.Y_AXIS));

                            // create and collect name label and field 
                            JPanel namePanel = new JPanel();
                            JLabel nameLabel = new JLabel("Item Name: ");
                            JTextField nameField = new JTextField(20);
                            namePanel.add(nameLabel);
                            namePanel.add(nameField);

                            // create and collect price label and field 
                            JPanel pricePanel = new JPanel();
                            JLabel priceLabel = new JLabel("Item Price: ");
                            JTextField priceField = new JTextField(7);
                            pricePanel.add(priceLabel);
                            pricePanel.add(priceField);

                            // create and collect category label and ComboBox
                            JPanel categoryPanel = new JPanel();
                            JLabel categoryLabel = new JLabel("Item Category: ");
                            String[] categoryDropDown = {"Choose An Option", "Appetizers", "Beverages", "Burgers", "Limited Time Offer", "Salads", "Sandwiches", "Shakes & More", "Value Meals"};
                            JComboBox categoryBox = new JComboBox(categoryDropDown);
                            categoryPanel.add(categoryLabel);
                            categoryPanel.add(categoryBox);

                            // create a button to commit the new item to the database
                            JButton commitButton = new JButton("Commit");

                            // add all panels to the popup and make it visible 
                            createItemPopup.add(namePanel);
                            createItemPopup.add(pricePanel);
                            createItemPopup.add(categoryPanel);
                            createItemPopup.add(commitButton);
                            createItemPopup.setVisible(true);
                            createItemPopupDialog.setVisible(true);

                            // set up frame for ingredient syncing for junction table

                            // window for associated ingredients with a new menu item
                            JFrame attachIngredientsFrame = new JFrame();
                            attachIngredientsFrame.setLayout(new BoxLayout(attachIngredientsFrame.getContentPane(), BoxLayout.PAGE_AXIS));
                            attachIngredientsFrame.setSize(preferredWidth / 3,preferredHeight / 3);
                            attachIngredientsFrame.setLocationRelativeTo(null);
                            attachIngredientsFrame.setTitle("Inventory Association");

                            ResultSet rs = model.getAllIngredients();
                            Vector<String> ingredientNames = null;

                            try{
                                ingredientNames = populateVector(rs);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + ex.getMessage());
                            }

                            // create and collect ingredient label and ComboBox
                            JComboBox ingredientComboBox = new JComboBox(ingredientNames);
                            JPanel panelIngredientList = new JPanel();
                            JTextField ingredientList = new JTextField(25);
                            panelIngredientList.add(new JLabel("Inventory List:"));
                            panelIngredientList.add(ingredientList);
                            panelIngredientList.add(ingredientComboBox);

                            // create and collect quantity label and field 
                            JPanel panelQuantityList = new JPanel();
                            JTextField quantityList = new JTextField(25);
                            panelQuantityList.add(new JLabel("Quantity List:"));
                            panelQuantityList.add(quantityList);

                            // create confirmation button
                            JPanel panelButton = new JPanel();
                            JButton btnConfirmInventoryAttachment = new JButton("Confirm");
                            panelButton.add(btnConfirmInventoryAttachment);

                            // create description
                            JPanel panelDescription = new JPanel();
                            JTextArea description = new JTextArea("1) Make sure the ingredients/inventory are already added\n2) Enter an ingredient only once and in the following format: Grilled Chicken, Hot Dog Bun, Red Onion, \n3) Enter quantities in the same format: 3, 2, 4, ");
                            description.setEditable(false);
                            panelDescription.add(description);
            
                            // TODO : explain
                            AtomicInteger itemID = new AtomicInteger();
                            // make commit button cause ingredient checking phase 
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent event) {
                                    String name = nameField.getText();
                                    Double price = Double.parseDouble(priceField.getText());
                                    String category = categoryBox.getSelectedItem().toString();

                                    // if we can add the item then display the popup and update the table
                                    if(model.addNewItem(name, price, category)){
                                        itemID.set(model.getItemID(name));
                                        attachIngredientsFrame.getContentPane().add(panelIngredientList);
                                        attachIngredientsFrame.getContentPane().add(panelQuantityList);
                                        attachIngredientsFrame.getContentPane().add(panelButton);
                                        attachIngredientsFrame.getContentPane().add(panelDescription);
                                        attachIngredientsFrame.setVisible(true);
                                        viewTable(table, 3);
                                    } else{
                                        JOptionPane.showMessageDialog(null, "Unable to add menu item");
                                    }
                                }
                            });

                            // makes text show up in comma-separated list
                            ingredientComboBox.addActionListener(event -> {
                                String currentText = ingredientList.getText();
                                ingredientList.setText(currentText + ingredientComboBox.getSelectedItem() + ", ");
                            });

                            // make sure both ingredients and their quantities have the same length and then hide the popup and update the table
                            btnConfirmInventoryAttachment.addActionListener(event -> {
                                String[] associatedIngredients = ingredientList.getText().split(", ");
                                String[] quantities = quantityList.getText().split(", ");
                                if(quantities.length != associatedIngredients.length){
                                    JOptionPane.showMessageDialog(null, "Number of Quantities and Ingredients don't match");
                                    return;
                                }
                                try {
                                    if(model.attachAssociatedInventoryToNewItem(itemID.get(), associatedIngredients, quantities)){
                                        JOptionPane.showMessageDialog(null, "New menu item added");
                                    } else{
                                        JOptionPane.showMessageDialog(null, "Unable to attach the selected ingredients");
                                        return;
                                    }
                                    attachIngredientsFrame.dispose();
                                } catch (SQLException ex) {
                                    throw new RuntimeException(ex);
                                }
                                viewTable(table, 3);
                                createItemPopupDialog.dispose();
                            });

                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
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
                            JTextField currentStockField = new JTextField(model.getObject("ingredients", "ingredient_id", id, "ingredient_current_stock"));
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
                                    model.updateIngredient(id, name, currentStock, unitPrice);
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
                        
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a JDialog
                            JDialog deleteIngredientsPopupDialog = new JDialog();
                            deleteIngredientsPopupDialog.setTitle("Delete Ingredient");
                            deleteIngredientsPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            deleteIngredientsPopupDialog.setLocationRelativeTo(null);

                            // create Container of Dialog's content pane for layout purposes
                            Container deleteIngredientsPopup = deleteIngredientsPopupDialog.getContentPane();
                            deleteIngredientsPopup.setLayout(new BoxLayout(deleteIngredientsPopup, BoxLayout.Y_AXIS));
                            
                            JLabel deleteIngredientsLabel = new JLabel("Are you sure you want to delete this order?");
                            JButton confirmButton = new JButton("Confirm");

                            // TODO : verify query is correct 
                            // deletes order, gets rid of dialog, and refreshes table 
                            confirmButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    model.deleteIngredient(id);
                                    deleteIngredientsPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add everything and display dialog 
                            deleteIngredientsPopup.add(deleteIngredientsLabel);
                            deleteIngredientsPopup.add(confirmButton);
                            deleteIngredientsPopup.setVisible(true);
                            deleteIngredientsPopupDialog.setVisible(true);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // create a JDialog 
                            JDialog createIngredientPopupDialog = new JDialog();
                            createIngredientPopupDialog.setTitle("Update Ingredient");
                            createIngredientPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            createIngredientPopupDialog.setLocationRelativeTo(null);

                            // create a Container of Dialog's content pane for layout purposes
                            Container createIngredientPopup = createIngredientPopupDialog.getContentPane();
                            createIngredientPopup.setLayout(new BoxLayout(createIngredientPopup, BoxLayout.Y_AXIS));

                            // create and collect name label and field 
                            JPanel namePanel = new JPanel();
                            JLabel nameLabel = new JLabel("Ingredient Name: ");
                            JTextField nameField = new JTextField(20);
                            namePanel.add(nameLabel);
                            namePanel.add(nameField);

                            // create and collect current stock label and field 
                            JPanel currentStockPanel = new JPanel();
                            JLabel currentStockLabel = new JLabel("Ingredient Current Stock: ");
                            JTextField currentStockField = new JTextField(7);
                            currentStockPanel.add(currentStockLabel);
                            currentStockPanel.add(currentStockField);

                            // create and collect unit price label and ComboBox
                            JPanel unitPricePanel = new JPanel();
                            JLabel unitPriceLabel = new JLabel("Item Unit Price: ");
                            JTextField unitPriceField = new JTextField(7);
                            unitPricePanel.add(unitPriceLabel);
                            unitPricePanel.add(unitPriceField);

                            // create a button to commit the new item to the database, dispose of dialog, and update table
                            JButton commitButton = new JButton("Commit");
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // update the order in the database and also the table 
                                    String name = nameField.getText();
                                    Integer currentStock = Integer.parseInt(currentStockField.getText());
                                    Double unitPrice = Double.parseDouble(unitPriceField.getText());
                                    model.createIngredient(name, currentStock, unitPrice);
                                    createIngredientPopupDialog.dispose();
                                    viewTable(table, tableType);
                                }
                            });

                            // add all panels to the popup and make it visible 
                            createIngredientPopup.add(namePanel);
                            createIngredientPopup.add(currentStockPanel);
                            createIngredientPopup.add(unitPricePanel);
                            createIngredientPopup.add(commitButton);
                            createIngredientPopup.setVisible(true);
                            createIngredientPopupDialog.setVisible(true);
                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

    
    /** 
     * @param c
     * @param f
     */
    // update font size of all components of a panel recursively
    private void updateFontSizes(Component c, JFrame f) {
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                updateFontSizes(child, f);
            }
        }
        if (c instanceof JButton || c instanceof JLabel || c instanceof JTextField || c instanceof JTextArea || c instanceof JToggleButton) {
            Font sourceFont = c.getFont();
            float scale = f.getHeight() / 1000.0f;
            float newSize = sourceFont.getSize() * scale;
            int minSize = 14;
            if (newSize < minSize) {
                newSize = minSize;
            }
            c.setFont(sourceFont.deriveFont(newSize));
            f.revalidate();
            f.repaint();
        }
    }


    private Vector<String> populateVector(ResultSet rs) throws SQLException {
        Vector<String> ingredientList = new Vector<>();
        while(rs.next()){
            ingredientList.add(rs.getString(2));
        }

        return ingredientList;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // to override the abstract method
    }
}
