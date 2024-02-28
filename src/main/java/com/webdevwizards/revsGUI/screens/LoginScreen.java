package com.webdevwizards.revsGUI.screens;

import java.sql.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

/*
  TODO:
  1) Change credentials for your own team's database
  2) Change SQL command to a relevant query that retrieves a small amount of data
  3) Create a JTextArea object using the queried data
  4) Add the new object to the JPanel p
*/

public class LoginScreen extends JFrame implements ActionListener {
    static JTextField phoneNumberField;
    static JFrame frame;
    static JPanel panel;
    static JButton btnLogin;
    private Runnable switchToCashierScreenCallback;
    private Runnable switchToManagerScreenCallback;


    public LoginScreen()
    {
        // create a new frame
        frame = new JFrame("Login");

        // create a panel
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // add actionlistener to button
        phoneNumberField = new JTextField(20);
        phoneNumberField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // create login button
        btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // add components to panel
        panel.add(Box.createVerticalGlue());  // Add vertical glue before the components
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneNumberField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));  // Add some space between the components
        panel.add(btnLogin);
        panel.add(Box.createVerticalGlue());  // Add vertical glue after the components

        // add panel to frame
        frame.add(panel);

        // set the size of frame
        frame.setSize(400, 400);

        frame.setVisible(true);

        //closing the connection
        btnLogin
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
        return phoneNumberField.getText();
    }

}