package com.webdevwizards.revsGUI.screens;
import com.webdevwizards.revsGUI.database.Model;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PaymentScreen extends JFrame implements ActionListener {
    JFrame frame;
    
    // private JPanel mainPanel;
    JPanel customerInfoPanel;
    JPanel orderPanel;
    JPanel costPanel;
    JPanel cardPanel;
    JPanel buttonPanel;
    
    // orderPanel components - this will be completed within Controller.java
    // image, name, quantity, plus minus buttons, remove button, total

    // costPanel components - this will be completed within Controller.java
    // private JLabel lblSubtotal;
    // private JLabel lblTax;
    // private JLabel lblSubtotalAmount;
    // private JLabel lblTaxAmount;

    // cardPanel components
    ImageIcon cashIcon;
    ImageIcon cardIcon;
    JButton btnCredit;
    JButton btnCampus;

    // buttonPanel components
    JButton btnGoBack;
    JButton btnCancelOrder;
    JButton btnPlaceOrder;

    public PaymentScreen() {
        frame = new JFrame("Rev's American Grill: Payment Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // the next line ensures that the X button in the top right will not close the application
        //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // removing window decorations (title and buttons)
        //frame.setUndecorated(true);
        
        // get the size of the screen so that we can fill it with our window completely
        // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // frame.setSize(screenSize.width, screenSize.height);

        Font font1 = new Font("Arial", Font.PLAIN, 48);

        // the panel will lay compenents out from top to bottom
        // mainPanel = new JPanel();
        // mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // customerInfoPanel
        customerInfoPanel = new JPanel();
        customerInfoPanel.setLayout(new BoxLayout(customerInfoPanel, BoxLayout.X_AXIS));
        
        // the customer name and number will be set within controller

        // orderPanel - this will be completed within Controller.java
        orderPanel = new JPanel();
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
        orderPanel.setBackground(Color.RED);

        // costPanel - this will be completed within Controller.java
        costPanel = new JPanel();
        costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.X_AXIS));
        costPanel.setBackground(Color.RED);

        // cardPanel
        cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.X_AXIS));

        cashIcon = new ImageIcon("./images/chart.png");
        cardIcon = new ImageIcon("./images/order.png");

        cashIcon = new ImageIcon(cashIcon.getImage().getScaledInstance(600, 300, Image.SCALE_DEFAULT));
        cardIcon = new ImageIcon(cardIcon.getImage().getScaledInstance(600, 300, Image.SCALE_DEFAULT));

        btnCredit = new JButton(cardIcon);
        btnCredit.setText("Credit Card");
        btnCredit.setPreferredSize(new Dimension(600, 300));
        btnCredit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCredit.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        btnCampus = new JButton(cashIcon);
        btnCampus.setText("Campus Card");
        btnCampus.setPreferredSize(new Dimension(600, 300));
        btnCampus.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnCampus.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        cardPanel.add(Box.createHorizontalGlue());
        cardPanel.add(btnCredit);
        cardPanel.add(Box.createHorizontalGlue());
        cardPanel.add(btnCampus);
        cardPanel.add(Box.createHorizontalGlue());

        // buttonPanel
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(new Color(80, 0, 0));
        buttonPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // btnCancelOrder = new JButton("Cancel Order");
        // btnCancelOrder.setFont(font1);
        // btnCancelOrder.setAlignmentX(Component.LEFT_ALIGNMENT);
        // btnCancelOrder.setAlignmentY(Component.CENTER_ALIGNMENT);
        // btnCancelOrder.addActionListener(this);
        // buttonPanel.add(Box.createHorizontalGlue());
        // buttonPanel.add(btnCancelOrder);
        // buttonPanel.add(Box.createHorizontalGlue());
        // buttonPanel.add(Box.createHorizontalGlue());
        // buttonPanel.add(Box.createHorizontalGlue());
        // buttonPanel.add(Box.createHorizontalGlue());
        // buttonPanel.add(Box.createHorizontalGlue());

        // btnGoBack = new JButton("Go Back");
        // btnGoBack.setFont(font1);
        // btnGoBack.setAlignmentY(Component.CENTER_ALIGNMENT);
        // btnGoBack.addActionListener(this);
        // buttonPanel.add(btnGoBack);
        // buttonPanel.add(Box.createHorizontalGlue());

        // btnPlaceOrder = new JButton("Place Order");
        // btnPlaceOrder.setFont(font1);
        // btnPlaceOrder.setAlignmentY(Component.CENTER_ALIGNMENT);
        // btnPlaceOrder.addActionListener(this);
        // buttonPanel.add(btnPlaceOrder);
        // buttonPanel.add(Box.createHorizontalGlue());

        // add panel to frame
        frame.add(customerInfoPanel);
        frame.add(orderPanel);
        frame.add(costPanel);
        frame.add(Box.createVerticalGlue());
        frame.add(cardPanel);
        frame.add(buttonPanel);

        frame.pack();
        // // frame.setVisible(true);
    }

    
    /** 
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Cancel Order") || s.equals("Go Back") || s.equals("Place Order") || s.equals("Credit Card") || s.equals("Campus Card")){
            frame.dispose();
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    // public JPanel getMainPaymentPanel() {
    //     return mainPanel;
    // }

    public JPanel getCustomerInfoPanel() {
        return customerInfoPanel;
    }

    public JPanel getPaymentOrderPanel() {
        return orderPanel;
    }

    public JPanel getPaymentCostPanel() {
        return costPanel;
    }

    public JPanel getPaymentCardPanel() {
        return cardPanel;
    }

    public JPanel getPaymentButtonPanel() {
        return buttonPanel;
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
