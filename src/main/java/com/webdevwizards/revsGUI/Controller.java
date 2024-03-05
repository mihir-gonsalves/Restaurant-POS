package com.webdevwizards.revsGUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
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
                viewTable(table, 0);
                usersPopUp(table);
            } else if(comboBox.getSelectedItem().equals("Manager Orders")){
                viewTable(table, 1);
                managerOrdersPopUp(table);
            } else if (comboBox.getSelectedItem().equals("Customer Orders")) {
                viewTable(table, 2);
                customerOrdersPopUp(table);
            } else if (comboBox.getSelectedItem().equals("Items")) {
                viewTable(table, 3);
                itemsPopUp(table);
            } else if(comboBox.getSelectedItem().equals("Ingredients")){
                viewTable(table, 4);
                ingredientsPopUp(table);
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
    // populates the manager screen with a table of CRUD operations for each table
    public void populateManagerTablePanel() {
        JPanel mainPanel = managerScreen.getMainPanel();
        mainPanel.setLayout(new GridLayout(4, 4));
        StringBuilder tableName = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int remainder = i % 4;
            if (remainder == 0) {
                tableName.append("Create ");
            }
            else if (remainder == 1) {
                tableName.append("Read ");
            }
            else if (remainder == 2) {
                tableName.append("Update ");
            }
            else if (remainder == 3) {
                tableName.append("Delete ");
            }
            if (i < 4) {
                tableName.append("Users");
            }
            else if (i < 8) {
                tableName.append("Orders");
            }
            else if (i < 12) {
                tableName.append("Items");
            }
            else if (i < 16) {
                tableName.append("Ingredients");
            }
            JButton tableButton = new JButton(tableName.toString());
            tableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Table: " + tableButton.getText());
                }
            });
            mainPanel.add(tableButton);
            tableName.setLength(0);
        }
    }


    /*
     * PAYMENT SCREEN METHODS
     */
    // sets the payment screen to visible
    public void switchToPaymentScreen() {
        paymentScreen.getFrame().setVisible(true);
    }

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
    // updates the table with the latest data from SQL
    // tableType 0 for users, 1 for manager orders, 2 for customer orders, 2 for items, 3 for ingredients
    private void viewTable(JTable table, int tableType) {
        // Fetch data from SQL and view the table
        // remove all mouse listeners from the table before updating it
        MouseListener[] mouseListeners = table.getMouseListeners();
        for (MouseListener mouseListener : mouseListeners) {
            table.removeMouseListener(mouseListener);
            System.out.println("Removed mouse listener" + mouseListener.toString());
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
                    tableModel.setColumnIdentifiers(new String[]{"phone number", "name", "is manager"}); // Set column names

                    while (resultSet.next()) {
                        Object[] rowData = new Object[3];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(2);
                        rowData[1] = resultSet.getObject(3);
                        rowData[2] = resultSet.getObject(4);
                        tableModel.addRow(rowData);//add it to table
                    }
                    table.setModel(tableModel); // Set the updated table model

                    // set to false so user can't select row, its distracting and not needed
                    table.setRowSelectionAllowed(false);
                    table.setCellSelectionEnabled(false);

                    // change table column widths
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int tableWidth = table.getWidth();
                    table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 3);
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break; // TODO
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
                    tableModel.setColumnIdentifiers(new String[]{"phone number", "date", "time", "total"}); // Set column names

                    while (resultSet.next()) {
                        Object[] rowData = new Object[4];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(5);
                        rowData[1] = resultSet.getObject(2);
                        rowData[2] = resultSet.getObject(3);
                        rowData[3] = resultSet.getObject(4);
                        tableModel.addRow(rowData);//add it to table
                    }
                    table.setModel(tableModel); // Set the updated table model

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
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break; // TODO
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
                    tableModel.setColumnIdentifiers(new String[]{"date", "time", "subtotal", "tax", "total", "payment type"}); // Set column names

                    while (resultSet.next()) {
                        Object[] rowData = new Object[6];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(2);
                        rowData[1] = resultSet.getObject(3);
                        rowData[2] = resultSet.getObject(4);
                        rowData[3] = resultSet.getObject(5);
                        rowData[4] = resultSet.getObject(6);
                        rowData[5] = resultSet.getObject(7);
                        tableModel.addRow(rowData);//add it to table
                    }
                    table.setModel(tableModel); // Set the updated table model

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
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break; // TODO
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
                    tableModel.setColumnIdentifiers(new String[]{"name", "price", "category", "ingredients"}); // Set column names

                    while (resultSet.next()) {
                        Object[] rowData = new Object[4];
                        rowData[0] = resultSet.getObject(1);
                        rowData[1] = resultSet.getObject(2);
                        rowData[2] = resultSet.getObject(3);
                        rowData[3] = resultSet.getObject(4);
                        tableModel.addRow(rowData);//add it to table

                    }
                    table.setModel(tableModel); // Set the updated table model

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
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                    tableModel.setColumnIdentifiers(new String[]{"name", "current stock", "unit price"}); // Set column names

                    while (resultSet.next()) {
                        Object[] rowData = new Object[3];
                        // don't access first column because it contains ID which is not needed
                        rowData[0] = resultSet.getObject(2);
                        rowData[1] = resultSet.getObject(3);
                        rowData[2] = resultSet.getObject(4);
                        tableModel.addRow(rowData);//add it to table
                    }
                    table.setModel(tableModel); // Set the updated table model
                
                    // set to false so user can't select a row, its distracting and not needed
                    table.setRowSelectionAllowed(false);

                    // change table column widths
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int tableWidth = table.getWidth();
                    table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 3);
                    table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 3);
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
    }

    private void usersPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                int tableType = 0;
                if (me.getButton() == MouseEvent.BUTTON3 && table.getSelectedRow() != -1) {
                    System.out.println("tableType: " + tableType + " Right click on row " + row);

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
                            System.out.println("Update " + tableType);
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Delete " + tableType);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Create " + tableType);
                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

    private void managerOrdersPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                int tableType = 1;
                if (me.getButton() == MouseEvent.BUTTON3 && table.getSelectedRow() != -1) {
                    System.out.println("tableType: " + tableType + " Right click on row " + row);

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

                            // create a popup to update the order
                            System.out.println("Update " + tableType);
                            JDialog updateOrderPopup = new JDialog();
                            updateOrderPopup.setTitle("Update Order");
                            updateOrderPopup.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateOrderPopup.setLocationRelativeTo(null);
                            updateOrderPopup.setLayout(new BoxLayout(updateOrderPopup, BoxLayout.Y_AXIS));
                            
                            // add type : manager || customer
                            JPanel typePanel = new JPanel(); 
                            JLabel typeLabel = new JLabel("Order Type: ");
                            String[] typeDropDown = {"", "Manager", "Customer"};
                            JComboBox typeBox = new JComboBox(typeDropDown);
                            typePanel.add(typeLabel);
                            typePanel.add(typeBox);
                            updateOrderPopup.add(typePanel);

                            // add 

                            
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Delete " + tableType);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Create " + tableType);
                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

    private void customerOrdersPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                int tableType = 1;
                if (me.getButton() == MouseEvent.BUTTON3 && table.getSelectedRow() != -1) {
                    System.out.println("tableType: " + tableType + " Right click on row " + row);

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

                            // create a popup to update the order
                            System.out.println("Update " + tableType);
                            JDialog updateOrderPopupDialog = new JDialog();
                            updateOrderPopupDialog.setTitle("Update Order");
                            updateOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            updateOrderPopupDialog.setLocationRelativeTo(null);

                            Container updateOrderPopup = updateOrderPopupDialog.getContentPane();
                            updateOrderPopup.setLayout(new BoxLayout(updateOrderPopup, BoxLayout.Y_AXIS));
                            
                            // add order date 
                            JPanel datePanel = new JPanel();
                            JLabel dateLabel = new JLabel("Order Date: ");
                            JTextField dateField = new JTextField(10);
                            System.out.println("row: " + (row + 1));
                            dateField.setText(model.getObject("customer_order", "c_order_id", row + 1, "c_order_date"));
                            datePanel.add(dateLabel);
                            datePanel.add(dateField);

                            // add order time
                            JPanel timePanel = new JPanel();
                            JLabel timeLabel = new JLabel("Order Time: ");
                            JTextField timeField = new JTextField(12);
                            timeField.setText(model.getObject("customer_order", "c_order_id", row + 1, "c_order_time"));
                            timePanel.add(timeLabel);
                            timePanel.add(timeField);

                            // add order subtotal
                            JPanel subtotalPanel = new JPanel();
                            JLabel subtotalLabel = new JLabel("Order Subtotal: ");
                            JTextField subtotalField = new JTextField(7);
                            subtotalField.setText(model.getObject("customer_order", "c_order_id", row + 1, "c_order_subtotal"));
                            subtotalPanel.add(subtotalLabel);
                            subtotalPanel.add(subtotalField);

                            // add order tax
                            JPanel taxPanel = new JPanel();
                            JLabel taxLabel = new JLabel("Order Tax: ");
                            JTextField taxField = new JTextField(6);
                            taxField.setText(model.getObject("customer_order", "c_order_id", row + 1, "c_order_tax"));
                            taxPanel.add(taxLabel);
                            taxPanel.add(taxField);

                            // add order total
                            JPanel totalPanel = new JPanel();
                            JLabel totalLabel = new JLabel("Order Total: ");
                            JTextField totalField = new JTextField(7);
                            totalField.setText(model.getObject("customer_order", "c_order_id", row + 1, "c_order_total"));
                            totalPanel.add(totalLabel);
                            totalPanel.add(totalField);

                            // add order payment method
                            JPanel paymentMethodPanel = new JPanel();
                            JLabel paymentMethodLabel = new JLabel("Payment Method: ");
                            JTextField paymentMethodField = new JTextField(10);
                            paymentMethodField.setText(model.getObject("customer_order", "c_order_id", row + 1, "c_order_payment_type"));
                            paymentMethodPanel.add(paymentMethodLabel);
                            paymentMethodPanel.add(paymentMethodField);

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
                                    model.updateCustomerOrder(row + 1, date, time, subtotal, tax, total, paymentMethod);
                                    updateOrderPopupDialog.dispose();
                                }
                            });

                            // add all Panels to the popup
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
                            System.out.println("Delete " + tableType);

                            JDialog deleteOrderPopupDialog = new JDialog();
                            deleteOrderPopupDialog.setTitle("Delete Order");
                            deleteOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            deleteOrderPopupDialog.setLocationRelativeTo(null);

                            Container deleteOrderPopup = deleteOrderPopupDialog.getContentPane();
                            deleteOrderPopup.setLayout(new BoxLayout(deleteOrderPopup, BoxLayout.Y_AXIS));
                            
                            JLabel deleteOrderLabel = new JLabel("Are you sure you want to delete this order?");
                            JButton confirmButton = new JButton("Confirm");
                            confirmButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    model.deleteCustomerOrder(row + 1);
                                    deleteOrderPopupDialog.dispose();
                                }
                            });
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
                            System.out.println("Create " + tableType);
                            JDialog createOrderPopupDialog = new JDialog();
                            createOrderPopupDialog.setTitle("Create Order");
                            createOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            createOrderPopupDialog.setLocationRelativeTo(null);

                            Container createOrderPopup = createOrderPopupDialog.getContentPane();
                            createOrderPopup.setLayout(new BoxLayout(createOrderPopup, BoxLayout.Y_AXIS));
                            
                            // add order date 
                            JPanel datePanel = new JPanel();
                            JLabel dateLabel = new JLabel("Order Date: ");
                            JTextField dateField = new JTextField(10);
                            dateField.setText("YYYY-MM-DD");
                            datePanel.add(dateLabel);
                            datePanel.add(dateField);

                            // add order time
                            JPanel timePanel = new JPanel();
                            JLabel timeLabel = new JLabel("Order Time: ");
                            JTextField timeField = new JTextField(12);
                            timeField.setText("HH:MM:SS.SSS");
                            timePanel.add(timeLabel);
                            timePanel.add(timeField);

                            // add order subtotal
                            JPanel subtotalPanel = new JPanel();
                            JLabel subtotalLabel = new JLabel("Order Subtotal: ");
                            JTextField subtotalField = new JTextField(7);
                            subtotalField.setText("0.00");
                            subtotalPanel.add(subtotalLabel);
                            subtotalPanel.add(subtotalField);

                            // add order tax
                            JPanel taxPanel = new JPanel();
                            JLabel taxLabel = new JLabel("Order Tax: ");
                            JTextField taxField = new JTextField(6);
                            taxField.setText("0.00");
                            taxPanel.add(taxLabel);
                            taxPanel.add(taxField);

                            // add order total
                            JPanel totalPanel = new JPanel();
                            JLabel totalLabel = new JLabel("Order Total: ");
                            JTextField totalField = new JTextField(7);
                            totalField.setText("0.00");
                            totalPanel.add(totalLabel);
                            totalPanel.add(totalField);

                            // add order payment method
                            JPanel paymentMethodPanel = new JPanel();
                            JLabel paymentMethodLabel = new JLabel("Payment Method: ");
                            JTextField paymentMethodField = new JTextField(10);
                            paymentMethodField.setText("cash or credit");
                            paymentMethodPanel.add(paymentMethodLabel);
                            paymentMethodPanel.add(paymentMethodField);

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

                            // add all Panels to the popup
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

    private void itemsPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                int tableType = 3;
                if (me.getButton() == MouseEvent.BUTTON3 && table.getSelectedRow() != -1) {
                    System.out.println("tableType: " + tableType + " Right click on row " + row);

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
                            System.out.println("Update " + tableType);
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Delete " + tableType);
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Create " + tableType);
                            // create a popup to create the item
                            JDialog createItemPopupDialog = new JDialog();
                            createItemPopupDialog.setTitle("Create Item");
                            createItemPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                            createItemPopupDialog.setLocationRelativeTo(null);

                            Container createItemPopup = createItemPopupDialog.getContentPane();
                            createItemPopup.setLayout(new BoxLayout(createItemPopup, BoxLayout.Y_AXIS));

                            // create a panel for item name
                            JPanel namePanel = new JPanel();
                            JLabel nameLabel = new JLabel("Item Name: ");
                            JTextField nameField = new JTextField(20);
                            namePanel.add(nameLabel);
                            namePanel.add(nameField);

                            // create a panel for item price
                            JPanel pricePanel = new JPanel();
                            JLabel priceLabel = new JLabel("Item Price: ");
                            JTextField priceField = new JTextField(7);
                            pricePanel.add(priceLabel);
                            pricePanel.add(priceField);

                            // create a panel for item category
                            JPanel categoryPanel = new JPanel();
                            JLabel categoryLabel = new JLabel("Item Category: ");
                            String[] categoryDropDown = {"Choose An Option", "Appetizers", "Beverages", "Burgers", "Limited Time Offer", "Salads", "Sandwiches", "Shakes & More", "Value Meals"};
                            JComboBox categoryBox = new JComboBox(categoryDropDown);
                            categoryPanel.add(categoryLabel);
                            categoryPanel.add(categoryBox);

                            // create a button to commit the new item to the database
                            JButton commitButton = new JButton("Commit");

                            // add all panels to the popup
                            createItemPopup.add(namePanel);
                            createItemPopup.add(pricePanel);
                            createItemPopup.add(categoryPanel);
                            createItemPopup.add(commitButton);
                            createItemPopup.setVisible(true);
                            createItemPopupDialog.setVisible(true);



                            //Window for associated ingredients with a new menu item
                            JFrame attachIngredientsFrame = new JFrame();
                            attachIngredientsFrame.setLayout(new BoxLayout(attachIngredientsFrame.getContentPane(), BoxLayout.PAGE_AXIS));
                            attachIngredientsFrame.setSize(600, 200);
                            attachIngredientsFrame.setTitle("Inventory Association");

                            ResultSet rs = model.getAllIngredients();
                            Vector<String> ingredientNames = null;

                            try{
                                ingredientNames = populateVector(rs);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null, "Error executing SQL query: " + ex.getMessage());
                            }

                            JComboBox ingredientComboBox = new JComboBox(ingredientNames);
                            JPanel panelIngredientList = new JPanel();
                            JTextField ingredientList = new JTextField(25);
                            panelIngredientList.add(new JLabel("Inventory List:"));
                            panelIngredientList.add(ingredientList);
                            panelIngredientList.add(ingredientComboBox);

                            JPanel panelQuantityList = new JPanel();
                            JTextField quantityList = new JTextField(25);
                            panelQuantityList.add(new JLabel("Quantity List:"));
                            panelQuantityList.add(quantityList);

                            JPanel panelButton = new JPanel();
                            JButton btnConfirmInventoryAttachment = new JButton("Confirm");
                            panelButton.add(btnConfirmInventoryAttachment);

                            JPanel panelDescription = new JPanel();
                            JTextArea description = new JTextArea("1) Make sure the ingredients/inventory are already added\n2) Enter an ingredient only once and in the following format: Grilled Chicken, Hot Dog Bun, Red Onion, \n3) Enter quantities in the same format: 3, 2, 4, ");
                            description.setEditable(false);
                            panelDescription.add(description);
            
                            AtomicInteger itemID = new AtomicInteger();
                            
                            commitButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent event) {
                                    String name = nameField.getText();
                                    Double price = Double.parseDouble(priceField.getText());
                                    String category = categoryBox.getSelectedItem().toString();
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

                            ingredientComboBox.addActionListener(event -> {
                                String currentText = ingredientList.getText();
                                ingredientList.setText(currentText + ingredientComboBox.getSelectedItem() + ", ");
                            });

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

    private void ingredientsPopUp(JTable table) {
        // add event listener so rows can be selected
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                int tableType = 4;
                if (me.getButton() == MouseEvent.BUTTON3 && table.getSelectedRow() != -1) {
                    System.out.println("tableType: " + tableType + " Right click on row " + row);

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
                            System.out.println("Update order");
                        }
                    });
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Delete order");
                        }
                    });
                    createItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Create order");
                        }
                    });
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

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
