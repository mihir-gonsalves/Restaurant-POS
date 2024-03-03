package com.webdevwizards.revsGUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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
        String[] dropDownList = {"Choose An Option", "Add New Inventory", "Add New Menu Item", "Update Inventory", "Update Menu Item"};
        JComboBox comboBox = new JComboBox(dropDownList);

        JLabel IdLabel = new JLabel("ID:");
        JTextField IdField = new JTextField(10);

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField(10);

        JLabel countLabel = new JLabel("Add Stock:");
        JTextField countField = new JTextField(10);

        JLabel priceLabel = new JLabel("Cost:");
        JTextField priceField = new JTextField(10);

        JLabel categoryLabel = new JLabel("Category:");
        JTextField categoryField = new JTextField(10);

        JButton commitButton = new JButton("Commit");

        inputPanel.add(comboBox);
        inputPanel.add(IdLabel);
        inputPanel.add(IdField);
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(countLabel);
        inputPanel.add(countField);
        inputPanel.add(priceLabel);
        inputPanel.add(priceField);
        inputPanel.add(categoryLabel);
        inputPanel.add(categoryField);
        inputPanel.add(commitButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Table to display results. Used in viewTable
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        comboBox.addActionListener(e -> { //Resets boxes to white and then grays out and sets to uneditable the unneeded ones based on the option you select
            priceField.setBackground(Color.white); categoryField.setBackground(Color.white); nameField.setBackground(Color.white);
            countField.setBackground(Color.white); priceField.setBackground(Color.white); IdField.setBackground(Color.white);

            priceField.setEditable(true); categoryField.setEditable(true); nameField.setEditable(true);
            countField.setEditable(true); priceField.setEditable(true); IdField.setEditable(true);
            
            if(comboBox.getSelectedItem().equals("Add New Inventory")){
                categoryField.setEditable(false); categoryField.setBackground(Color.lightGray); categoryField.setText("");
                IdField.setEditable(false); IdField.setBackground(Color.lightGray); IdField.setText("");
                viewTable(table, false);
            } else if(comboBox.getSelectedItem().equals("Add New Menu Item")){
                IdField.setEditable(false); IdField.setBackground(Color.lightGray); IdField.setText("");
                countField.setEditable(false); countField.setBackground(Color.lightGray); countField.setText("");
                viewTable(table, true);
            } else if(comboBox.getSelectedItem().equals("Update Inventory")){
                categoryField.setEditable(false); categoryField.setBackground(Color.lightGray); categoryField.setText("");
                viewTable(table, false);
            } else if(comboBox.getSelectedItem().equals("Update Menu Item")){
                countField.setEditable(false); countField.setBackground(Color.lightGray); countField.setText("");
                viewTable(table, true);
            }
        });

        commitButton.addActionListener(e -> { //Verifying inputs and then choosing action
            int ID = 0; int count = 0; Double price = 0.00; String category = ""; String name = "";

            boolean flagID, flagCount, flagCategory, flagPrice, flagName; //The flags are used so we can optionally update some fields in the SQL statement (see updateMenuItem and updateInventory SQL statements)
            if(IdField.getText().trim().isEmpty()){
                flagID = false;
            } else{
                flagID = true;
                ID = Integer.parseInt(IdField.getText());
            }

            if(priceField.getText().trim().isEmpty()){
                flagPrice = false;
            } else{
                flagPrice = true;
                price = Double.parseDouble(priceField.getText());
            }

            if(categoryField.getText().trim().isEmpty()){
                flagCategory = false;
            } else{
                flagCategory = true;
                category = categoryField.getText();
            }

            if(countField.getText().trim().isEmpty()){
                flagCount = false;
            } else{
                flagCount = true;
                count = Integer.parseInt(countField.getText());
            }

            if(nameField.getText().trim().isEmpty()){
                flagName = false;
            } else{
                flagName = true;
                name = nameField.getText();
            }

            if(comboBox.getSelectedItem().equals("Add New Menu Item")){ //Need to do some more work here: Phase 4
                if(flagName == false || flagPrice == false || flagCategory == false){ //If any of these fields don't have a value
                    JOptionPane.showMessageDialog(null, "Please make sure all fields have values");
                    return;
                }
                if(model.addNewItem(name, price, category)){
                    JOptionPane.showMessageDialog(null, "New menu item added");
                    viewTable(table, true);
                } else{
                    JOptionPane.showMessageDialog(null, "Unable to add menu item");
                }

            } else if(comboBox.getSelectedItem().equals("Update Inventory")){
                if(flagID == false){
                    JOptionPane.showMessageDialog(null, "Enter a Valid ID value");
                    return;
                }
                if(model.updateInventory(ID, name, count, price, phoneNumber, flagName, flagCount, flagPrice)){
                    JOptionPane.showMessageDialog(null, "Inventory Updated");
                    viewTable(table, false);
                } else{
                    JOptionPane.showMessageDialog(null, "Unable to update inventory");
                }
            } else if(comboBox.getSelectedItem().equals("Add New Inventory")){
                if(flagName == false || flagPrice == false || flagCount == false){
                    JOptionPane.showMessageDialog(null, "Please make sure all fields have values");
                    return;
                }
                if(model.addNewInventory(name, count, price)){
                    JOptionPane.showMessageDialog(null, "New Inventory Added");
                    viewTable(table, false);
                } else{
                    JOptionPane.showMessageDialog(null, "Unable to add new inventory");
                }
            } else if(comboBox.getSelectedItem().equals("Update Menu Item")){
                if(flagID == false){
                    JOptionPane.showMessageDialog(null, "Enter a Valid ID value");
                    return;
                }
                if(model.updateMenuItem(ID, name, price, category, flagName, flagPrice, flagCategory)){
                    JOptionPane.showMessageDialog(null, "Menu Item Updated");
                    viewTable(table, true);
                } else{
                    JOptionPane.showMessageDialog(null, "Unable to update menu item");
                }
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
    private void viewTable(JTable table, boolean isItemTable) {
        // Fetch data from SQL and view the table
        if(isItemTable){ //if the function was called to display the menu_item table
            try {
                ResultSet resultSet = model.getAllMenuItems();
                DefaultTableModel tableModel = new DefaultTableModel();
                tableModel.setColumnIdentifiers(new String[]{"item_id", "item_name", "item_price", "category"}); // Set column names

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
        }else{ //else if the function was called to display the ingredient table
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

    @Override
    public void actionPerformed(ActionEvent e) {
        // to override the abstract method
    }
}
