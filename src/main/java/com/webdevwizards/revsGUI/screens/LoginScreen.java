package com.webdevwizards.revsGUI.screens;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import java.awt.event.KeyAdapter;  

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class LoginScreen extends JFrame implements ActionListener {
    private JLabel lblScreen;
    
    private JLabel lblNumber;
    private JTextField phoneNumber;
    
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel inputPanel; // panel for lblNumber, phoneNumber, and btnLogin (below) 

    private JButton btnLogin; 
    private JToggleButton btnFullscreen;

    public LoginScreen() {
        frame = new JFrame("Rev's American Grill: Login");
        
        // the next line ensures that the X button in the top right will not close the application
        //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // removing window decorations (title and buttons)
        //frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // get the size of the screen so that we can fill it with our window completely
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);

        Font font1 = new Font("Arial", Font.BOLD, 32);
        Font font2 = new Font("Arial", Font.PLAIN, 16);

        // the panel will lay compenents out from top to bottom
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(180, 20, 20, 20));

        // label for screen (replaces title that was removed by setUndecorated)
        lblScreen = new JLabel("Welcome to Rev's American Grill");
        lblScreen.setAlignmentX(Component.CENTER_ALIGNMENT);

        // label decoration
        Border emptyBorder = BorderFactory.createEmptyBorder(40, 40, 40, 40);

        lblScreen.setFont(font1);
        lblScreen.setForeground(new Color(246, 182, 12));
        lblScreen.setBackground(new Color(80, 0, 0));
        lblScreen.setOpaque(true);

        Border existingBorder = lblScreen.getBorder();
        Border totalBorder = BorderFactory.createCompoundBorder(existingBorder, emptyBorder);
        lblScreen.setBorder(totalBorder);

        // the panel will lay compenents out from left to right
        inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        lblNumber = new JLabel("Phone Number:");
        lblNumber.setFont(font2);
        lblNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        phoneNumber = new JTextField(10);
        phoneNumber.setFont(font2);
        phoneNumber.setMaximumSize(new Dimension(140, 30));
        phoneNumber.setAlignmentX(Component.CENTER_ALIGNMENT);
                // Added a KeyListener to the phoneNumber field
                phoneNumber.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            // Perform login action
                            btnLogin.doClick();
                        }
                    }
                });

        // code below limits the number of characters that can be input to 10 and restricts the character type to numbers
        ((AbstractDocument) phoneNumber.getDocument()).setDocumentFilter(new DocumentFilter() {
            final int maxCharacters = 10;

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrset)
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int futureLength = currentLength - length + text.length();

                if (futureLength <= maxCharacters && text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrset);
                } else {
                    Toolkit.getDefaultToolkit().beep(); // BEEP BEEP BEEP BEEP BEEP on invalids lol
                }
            }
        });

        btnLogin = new JButton("Login");
        btnLogin.setFont(font2);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // add components to inputPanel
        inputPanel.add(lblNumber);
        inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        inputPanel.add(phoneNumber);
        inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        inputPanel.add(btnLogin);
        inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnFullscreen = new JToggleButton("Exit Fullscreen");
        btnFullscreen.setFont(font2);
        btnFullscreen.setAlignmentX(Component.CENTER_ALIGNMENT);

        // add components to mainPanel with glue in between to center the components
        mainPanel.add(lblScreen);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(btnFullscreen);
        mainPanel.add(Box.createVerticalGlue());

        // add panel to frame
        frame.add(mainPanel);

        frame.setVisible(true);
    }

    
    /** 
     * @param e
     */
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
}