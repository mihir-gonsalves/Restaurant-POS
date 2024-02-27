package com.webdevwizards.revsGUI.screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class testGUI extends JFrame implements ActionListener{
    JFrame frame;
    Popup po;
    PopupFactory pf;
    
    
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
            bottomPanel.add(new JButton(" Order Complete"));

            frame.add(leftPanel, BorderLayout.WEST);
            frame.add(middlePanel, BorderLayout.CENTER);
            frame.add(rightPanel, BorderLayout.EAST);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            frame.pack();
            frame.setVisible(true);
        });
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
}
