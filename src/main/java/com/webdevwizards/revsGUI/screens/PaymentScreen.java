package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class PaymentScreen extends JFrame implements ActionListener {
    private JFrame frame;
    
    private JPanel mainPanel;
    private JPanel customerInfoPanel;
    private JPanel orderPanel;
    private JPanel paymentPanel;
    private JPanel cardPanel;
    private JPanel buttonPanel;
    
    // customerInfoPanel components - this will be completed within Controller.java

    private JLabel lblCustomer;
    
    // private JLabel lblCustomerName;
    // private JLabel lblCustomerNumber;
    
    // orderPanel components - this will be completed within Controller.java
    // image, name, quantity, plus minus buttons, remove button, total

    // paymentPanel components - this will be completed within Controller.java
    // private JLabel lblSubtotal;
    // private JLabel lblTax;
    // private JLabel lblSubtotalAmount;
    // private JLabel lblTaxAmount;

    private JLabel lblTotal;
    private JLabel lblTotalAmount;

    // cardPanel components
    private JButton btnCredit;
    private JButton btnCampus;

    // buttonPanel components
    private JButton btnGoBack;
    private JButton btnCancelOrder;
    private JButton btnPlaceOrder;

    public PaymentScreen() {
        frame = new JFrame("Rev's American Grill: Payment Screen");
        
        // the next line ensures that the X button in the top right will not close the application
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // removing window decorations (title and buttons)
        frame.setUndecorated(true);
        
        // get the size of the screen so that we can fill it with our window completely
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);

        Font font1 = new Font("Arial", Font.PLAIN, 16);

        // the panel will lay compenents out from top to bottom
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // customerInfoPanel
        customerInfoPanel = new JPanel();
        customerInfoPanel.setLayout(new BoxLayout(customerInfoPanel, BoxLayout.X_AXIS));

        lblCustomer = new JLabel("Customer:");
        lblCustomer.setFont(font1);
        lblCustomer.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCustomer.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        // the customer name and number will be set within controller

        // orderPanel - this will be completed within Controller.java
        orderPanel = new JPanel();
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
        orderPanel.setBackground(Color.RED);

        // paymentPanel - this will be completed within Controller.java
        paymentPanel = new JPanel();
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.X_AXIS));
        paymentPanel.setBackground(Color.RED);

        // cardPanel
        cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.X_AXIS));

        // buttonPanel
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(Color.RED);

        btnCancelOrder = new JButton("Cancel Order");
        btnCancelOrder.setFont(font1);
        btnCancelOrder.setAlignmentY(Component.CENTER_ALIGNMENT);
        btnCancelOrder.addActionListener(this);

        btnGoBack = new JButton("Go Back");
        btnGoBack.setFont(font1);
        btnGoBack.setAlignmentY(Component.CENTER_ALIGNMENT);
        btnGoBack.addActionListener(this);

        btnPlaceOrder = new JButton("Place Order");
        btnPlaceOrder.setFont(font1);
        btnPlaceOrder.setAlignmentY(Component.CENTER_ALIGNMENT);
        btnPlaceOrder.addActionListener(this);

        // add panel to frame
        frame.add(mainPanel);
    }

    // if button is pressed
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
        if (s.equals("Close")) {
            frame.dispose();
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public JButton getGoBackButton() {
        return btnGoBack;
    }

    public JButton getCancelOrderButton() {
        return btnCancelOrder;
    }

    public JButton getPlaceOrderButton() {
        return btnPlaceOrder;
    }
}