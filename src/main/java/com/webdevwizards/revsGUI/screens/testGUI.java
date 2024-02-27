package com.webdevwizards.revsGUI.screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.webdevwizards.revsGUI.database.DatabaseManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class testGUI extends JFrame implements ActionListener{
    JFrame frame;
    Popup po;
    PopupFactory pf;
    JPanel itemsPanel;
    JPanel navPanel;
    
    
    public testGUI() {
        frame = new JFrame("Point of Sale");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Left panel for categories
        navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(8, 1)); // Changed to GridLayout for icons
        // Add icons for each category
        navPanel.setPreferredSize(new Dimension(100, 600));
        // for (int i = 0; i <= 6; i++) {
        //     // leftPanel.add(new JButton(new ImageIcon("icon_" + i + ".png")));
        //     navPanel.add(new JButton(resizeIcon("./images/icon_" + i + ".png", 70, 70)));
        // }

        populateNavBar();

        // Middle panel for items
        JPanel middlePanel = new JPanel(new BorderLayout()); // Changed to BorderLayout
        middlePanel.setPreferredSize(new Dimension(600, 600));
        
        // Added search bar at the top
        JTextField searchBar = new JTextField("Enter an item name to search");
        middlePanel.add(searchBar, BorderLayout.NORTH);
        
        // Changed layout to GridLayout for items 
        itemsPanel = new JPanel(new GridLayout(3, 2)); // Increase grid size as needed

        // Wrap itemsPanel in a JScrollPane
        JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);

        middlePanel.add(itemsScrollPane, BorderLayout.CENTER); // Changed this line to add the scroll pane instead of itemsPanel directly

        // Used in function populateItemPanel
        pf = new PopupFactory();

        // Populate the items panel with items with category "Burgers"
        populateItemPanel("Burgers"); // change this to interact with navbar buttons
        
        
        // Add empty panels for future modifications
        for(int i=0; i<7; i++) { // Adjust this number based on how many cells you want to leave empty
            itemsPanel.add(new JPanel());
        }
        
        middlePanel.add(itemsPanel, BorderLayout.CENTER);

        // Right panel for order items
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout()); // Changed to BorderLayout for better organization
        rightPanel.setPreferredSize(new Dimension(200, 600));

        JLabel orderItemsLabel = new JLabel("Order Items");
        rightPanel.add(orderItemsLabel, BorderLayout.NORTH);

        // Panel for adding text fields
        JPanel textFieldsPanel = new JPanel();
        textFieldsPanel.setLayout(new GridLayout(3, 1)); // Use GridLayout to organize text fields

        // Add empty fields for ordered items
        for (int i = 0; i < 3; i++) {
            textFieldsPanel.add(new JTextField());
        }

        rightPanel.add(textFieldsPanel, BorderLayout.CENTER);

        // Bottom panel for cashier and subtotal
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        
        bottomPanel.add(new JLabel("Cashier Name: Ritchey"));
        bottomPanel.add(new JLabel(" Order Subtotal: $0.00"));
        bottomPanel.add(new JButton(" Order Complete"));

        frame.add(navPanel, BorderLayout.WEST);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Order Complete")) {
            // Create a new popup
            pf = new PopupFactory();
            po = pf.getPopup(frame, new JLabel("Order Complete"), 50, 50);
            po.show();
        }
    }

    public static ImageIcon resizeIcon(String iconPath, int width, int height) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public void populateNavBar() {
        DatabaseManager db = new DatabaseManager();
        ResultSet rs = db.executeQuery("SELECT * FROM menu_items ORDER BY category;");
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

    public void populateItemPanel(String category) {
        // Get items and sort by category
        DatabaseManager db = new DatabaseManager();
        ResultSet rs = db.executeQuery("SELECT * FROM menu_items ORDER BY category;");
        itemsPanel.removeAll();
        
        try {
            while (rs.next()) {
                String db_category = rs.getString("category");
                if (db_category.equals(category)) {
                    String item_name = rs.getString("item_name");
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
                    JLabel itemName = new JLabel(item_name, SwingConstants.CENTER);
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
}
