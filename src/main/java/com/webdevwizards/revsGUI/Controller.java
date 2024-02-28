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

import com.formdev.flatlaf.FlatDarkLaf;
import com.webdevwizards.revsGUI.screens.*;

import main.java.com.webdevwizards.revsGUI.screens.CashierScreen;
import main.java.com.webdevwizards.revsGUI.screens.LoginScreen;
import main.java.com.webdevwizards.revsGUI.screens.ManagerScreen;
import main.java.com.webdevwizards.revsGUI.screens.TestScreen;

import com.webdevwizards.revsGUI.database.Model;

public class Controller implements ActionListener{
    private Model model;
    private LoginScreen loginScreen;
    private CashierScreen cashierScreen;
    private ManagerScreen managerScreen;
    private TestScreen testScreen;
    private boolean isManager;


    public static void main(String[] args) {
        // setup and display the login screen
        FlatDarkLaf.setup();
        Controller controller = new Controller();
        controller.initialize();
        switchToLoginScreen();

        // switch to appropriate screen based on login's phone number
        controller.loginScreen.getLoginButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.model.login(controller.loginScreen.getPhoneNumber())) {
                    controller.loginScreen.dispose();
                    if (controller.model.isManager()) {
                        controller.model.setPhoneNumber(controller.loginScreen.getPhoneNumber());
                        switchToManagerScreen();
                    } else {
                        controller.model.setPhoneNumber(controller.loginScreen.getPhoneNumber());
                        switchToCashierScreen();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid phone number");
                }
            }
        });

        if (controller.isManager) {

        }
        else {

        }


        

    }

    public Controller() {
        // TODO Auto-generated constructor stub
    }

    public void initialize() {
        this.model = new Model();
        this.loginScreen = new LoginScreen();
        this.cashierScreen = new CashierScreen();
        this.managerScreen = new ManagerScreen();
        this.testScreen = new TestScreen();
        this.isManager = false;
    }

    public static void switchToLoginScreen() {
        this.loginScreen.setVisible(true);
    }

    public static void switchToCashierScreen() {
        this.isManager = false;
        this.cashierScreen.setVisible(true);
    }

    public static void switchToManagerScreen() {
        this.isManager = true;
        this.managerScreen.setVisible(true);
    }

    public static void switchToTestScreen() {
        this.testScreen.setVisible(true);
    }

    public void populateItemPanel(String category) {
        // Get items and sort by category
        ResultSet rs = this.model.executeQuery("SELECT * FROM menu_items ORDER BY category;");
        JPanel itemsPanel = cashierScreen.getItemsPanel();
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
                            Dimension frameSize = loginScreen.getFrame().getSize();
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

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }
}
