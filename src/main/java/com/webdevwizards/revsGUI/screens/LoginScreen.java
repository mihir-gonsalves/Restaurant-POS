package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginScreen extends JFrame implements ActionListener {
    private JLabel lblScreen;
    
    private JLabel lblNumber;
    private JTextField phoneNumber;
    
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel inputPanel; // panel for lblNumber and phoneNumber textfield

    private JButton btnLogin; 
    private JToggleButton btnFullscreen;

    public LoginScreen() {
        frame = new JFrame("Rev's American Grill: Login");
        
        // the next line ensures that the X button in the top right will not close the application
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // removing window decorations (title and buttons)
        frame.setUndecorated(true);
        
        // get the size of the screen so that we can fill it with our window completely
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);

        Font  font1  = new Font("Arial", Font.BOLD,  24);
        Font  font2  = new Font("Arial", Font.PLAIN,  16);

        // the panel will lay compenents out from top to bottom
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // label for screen (replaces title that was removed by setUndecorated)
        lblScreen = new JLabel("Rev's American Grill: Login Screen");
        lblScreen.setFont(font1);
        lblScreen.setAlignmentX(Component.CENTER_ALIGNMENT);

        // the panel will lay compenents out from left to right
        inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        lblNumber = new JLabel("Phone Number:");
        lblNumber.setFont(font2);
        lblNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        phoneNumber = new JTextField(10);
        phoneNumber.setFont(font2);
        phoneNumber.setMaximumSize(new Dimension(150, 30));
        phoneNumber.setAlignmentX(Component.CENTER_ALIGNMENT);
        // add delay to format phone number so that it isn't so slow and jumpy
        // Timer timer = new Timer(500, new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         formatPhoneNumber();
        //     }
        // });
        // timer.setRepeats(false);

        // // This will format the phone number as the user types it in
        // phoneNumber.getDocument().addDocumentListener(new DocumentListener() {
        //     @Override
        //     public void insertUpdate(DocumentEvent e) {
        //         formatPhoneNumber();
        //     }

        //     @Override
        //     public void removeUpdate(DocumentEvent e) {
        //         formatPhoneNumber();
        //     }

        //     @Override
        //     public void changedUpdate(DocumentEvent e) {
        //         formatPhoneNumber();
        //     }
        // });

        btnLogin = new JButton("Login");
        btnLogin.setFont(font2);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // add components to inputPanel
        // inputPanel.add(lblNumber);
        // inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        // inputPanel.add(phoneNumber);
        // // inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        // inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnFullscreen = new JToggleButton("Exit Fullscreen");
        btnFullscreen.setFont(font2);
        btnFullscreen.setAlignmentX(Component.CENTER_ALIGNMENT);

        // add components to mainPanel with glue in between to center the components
        mainPanel.add(lblScreen);
        mainPanel.add(Box.createVerticalGlue());
        // mainPanel.add(inputPanel);
        mainPanel.add(lblNumber);
        mainPanel.add(phoneNumber);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(btnFullscreen);
        mainPanel.add(Box.createVerticalGlue());

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

    public JToggleButton getFullscreenButton() {
        return btnFullscreen;
    }

    // from XXXXXXXXXX to (XXX) XXX-XXXX
    private void formatPhoneNumber() {
        // String number = phoneNumber.getText().replaceAll("[^\\d]", "");
        // if (number.length() > 0) {
        //     StringBuilder formattedNumber = new StringBuilder("(");
        //     for (int i = 0; i < Math.min(number.length(), 10); i++) {
        //         if (i == 3) {
        //             formattedNumber.append(") ");
        //         } else if (i == 6) {
        //             formattedNumber.append("-");
        //         }
        //         formattedNumber.append(number.charAt(i));
        //     }
        //     phoneNumber.setText(formattedNumber.toString());
        // }
    }
}