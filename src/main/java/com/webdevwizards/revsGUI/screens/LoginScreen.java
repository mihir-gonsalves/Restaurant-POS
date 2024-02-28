package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class LoginScreen extends JFrame implements ActionListener {
    private JLabel lblScreen;
    
    private JLabel lblNumber;
    private JTextField phoneNumber;
    
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel inputPanel; // panel for lblNumber and phoneNumber textfield

    private JButton btnLogin; 

    public LoginScreen() {
        frame = new JFrame("Rev's American Grill: Login");
        
        // the next line ensures that the X button in the top right will not close the application
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // removing window decorations (title and buttons)
        frame.setUndecorated(true);
        
        // get the size of the screen so that we can fill it with our window completely
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);


        // the panel will lay compenents out from top to bottom
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // label for screen (replaces title that was removed by setUndecorated)
        lblScreen = new JLabel("Rev's American Grill: Login Screen");
        lblScreen.setAlignmentX(Component.CENTER_ALIGNMENT);

        // the panel will lay compenents out from left to right
        inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        lblNumber = new JLabel("Phone Number:");
        lblNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        phoneNumber = new JTextField(10);
        phoneNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);


        // add components to inputPanel
        inputPanel.add(lblNumber);
        inputPanel.add(phoneNumber);
        inputPanel.add(btnLogin);

        // add components to mainPanel
        mainPanel.add(lblScreen);
        mainPanel.add(inputPanel);

        // add panel to frame
        frame.add(mainPanel);

        frame.setVisible(true);
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

    public JButton getLoginButton() {
        return btnLogin;
    }

    public String getPhoneNumber() {
        return phoneNumber.getText();
    }
}