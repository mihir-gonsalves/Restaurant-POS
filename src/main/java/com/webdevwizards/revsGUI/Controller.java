package com.webdevwizards.revsGUI;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
                    controller.model.setPhoneNumber(controller.loginScreen.getPhoneNumber());
                    if (controller.model.isManager()) {
                        controller.switchToManagerScreen();
                    } else {
                        controller.model.setPhoneNumber(controller.loginScreen.getPhoneNumber());
                        controller.switchToCashierScreen();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid phone number");
                }
            }
        });

        if (controller.isManager) {

        }
        else {
            controller.populateNavBar();
            controller.populateItemPanel("Burgers");
            controller.completeOrder();
        }


        

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

    public void populateNavBar() {
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

    public void completeOrder() {
        JButton orderCompleteButton = cashierScreen.getOrderCompleteButton();
        orderCompleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // System.out.println("Order complete");
                JOptionPane.showMessageDialog(null, "Order complete");
                cashierScreen.setVisible(false);
                switchToPaymentScreen();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }
}
