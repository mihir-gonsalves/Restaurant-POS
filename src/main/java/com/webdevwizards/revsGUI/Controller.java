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
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
                System.out.println(orderItems);
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
        managerScreen.getNavPanel().add(tableButton);
        managerScreen.getNavPanel().revalidate();
        managerScreen.getNavPanel().repaint();
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
            JTextArea orderTextArea = new JTextArea("Order");
            orderTextArea.setEditable(false);
            orderTextArea.setPreferredSize(new Dimension(450, 500));
            mainPanel.add(orderTextArea);
        }
        else if (content.equals("table")) {
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
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }
}
