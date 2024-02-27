package com.webdevwizards.revsGUI.screens;
import com.webdevwizards.revsGUI.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class testGUI extends JFrame implements ActionListener{
    JFrame frame;
    Popup po;
    PopupFactory pf;
    private static final String INSERT_ORDER_QUERY = "INSERT INTO customer_order (c_order_id, c_order_date, c_order_time, c_order_subtotal, c_order_tax, c_order_total, c_order_payment_type) VALUES (?, ?, ?, ?, ?, ?, ?)";

    
    public testGUI() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Point of Sale");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Left panel for categories
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new GridLayout(7, 1)); // Changed to GridLayout for icons
            // Add icons for each category
            leftPanel.setPreferredSize(new Dimension(100, 600));
            for (int i = 0; i <= 6; i++) {
               // leftPanel.add(new JButton(new ImageIcon("icon_" + i + ".png")));
               leftPanel.add(new JButton(resizeIcon("./images/icon_" + i + ".png", 70, 70)));
            }

            // Middle panel for items
            JPanel middlePanel = new JPanel(new BorderLayout()); // Changed to BorderLayout
            middlePanel.setPreferredSize(new Dimension(600, 600));
            
            // Added search bar at the top
            JTextField searchBar = new JTextField("Enter an item name to search");
            middlePanel.add(searchBar, BorderLayout.NORTH);
            
            // Changed layout to GridLayout for items 

            JPanel itemsPanel = new JPanel(new GridLayout(3, 3)); // Increase grid size as needed

            // Wrap itemsPanel in a JScrollPane
            JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);

            middlePanel.add(itemsScrollPane, BorderLayout.CENTER); // Changed this line to add the scroll pane instead of itemsPanel directly
            // Create a panel for each item (only add items to some cells)
            // Arrays for programmatic creation of items
            String[] imagePaths = {"./images/burger_1.png", "./images/patty_melt.png"};
            String[] itemNames = {"Cheeseburger", "Patty Melt"};
            pf = new PopupFactory();

            for (int i = 0; i < imagePaths.length; i++) {
                // Create a new panel with BorderLayout
                JPanel itemPanel = new JPanel(new BorderLayout());

                // Create a new button with an image
                JButton itemButton = new JButton(new ImageIcon(imagePaths[i]));
                itemPanel.add(itemButton, BorderLayout.CENTER);

                // Create a final variable to use in the ActionListener
                final int index = i;
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
                        JLabel popUpLabel = new JLabel("Item: " + itemNames[index]);
                        popUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        JButton popUpButton = new JButton("Add to Order");
                        popUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                        popUpButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                System.out.println("Item added to order: " + itemNames[index]);
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
                JLabel itemName = new JLabel(itemNames[i], SwingConstants.CENTER);
                itemPanel.add(itemName, BorderLayout.SOUTH);

                // Add the panel to the itemsPanel
                itemsPanel.add(itemPanel);
            }
                        

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

            JButton orderCompleteButton = new JButton("Order Complete");
            orderCompleteButton.addActionListener(this);
            bottomPanel.add(orderCompleteButton);

            frame.add(leftPanel, BorderLayout.WEST);
            frame.add(middlePanel, BorderLayout.CENTER);
            frame.add(rightPanel, BorderLayout.EAST);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            frame.pack();
            frame.setVisible(true);
        });
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        System.out.println("Button clicked: ");
        if (e.getActionCommand().equals("Order Complete")) {
            
            System.out.println("Order Complete button clicked");
            // Connect to the database and insert order details
            try {
                DatabaseManager.initialize();
                // Add your additional functionality here
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ORDER_QUERY)) {
                
                // Get cashier name and order subtotal from GUI components
                String cashierName = "Ritchey"; // Assuming static cashier name for now
                String subtotal = "0.00"; // Assuming initial subtotal for now

                // Set parameters for the prepared statement
                preparedStatement.setInt(1, 1000000); // Replace "Item Name" with actual item name
                java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
                preparedStatement.setDate(2, currentDate);

                // Set current time
                java.sql.Time currentTime = new java.sql.Time(System.currentTimeMillis());
                preparedStatement.setTime(3, currentTime);
                
                // Set subtotal
                preparedStatement.setDouble(4, Double.parseDouble(subtotal));
                // Set tax
                preparedStatement.setDouble(5, 0.0825 * Double.parseDouble(subtotal));
                // Set total
                preparedStatement.setDouble(6, 1.0825 * Double.parseDouble(subtotal));
                // Set payment type
                preparedStatement.setString(7, "Credit");
                // Execute the query
                preparedStatement.executeUpdate();

                // Display success message
                JOptionPane.showMessageDialog(frame, "Order placed successfully!");
                pf = new PopupFactory();
                po = pf.getPopup(frame, new JLabel("Order Complete"), 50, 50);
                po.show();
            } catch (SQLException ex) {
                ex.printStackTrace();
                // Display error message
                JOptionPane.showMessageDialog(frame, "Error placing order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static ImageIcon resizeIcon(String iconPath, int width, int height) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}
