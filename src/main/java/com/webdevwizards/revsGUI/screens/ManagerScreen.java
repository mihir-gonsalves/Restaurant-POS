package com.webdevwizards.revsGUI.screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.ResultSet;

import javax.imageio.ImageIO;
import java.io.File;
import com.webdevwizards.revsGUI.screens.LoginScreen;
import com.webdevwizards.revsGUI.Controller;



public class ManagerScreen extends JFrame implements ActionListener{
    JFrame frame;
    JPanel navPanel;
    JPanel southPanel;
    JPanel mainPanel;
    JLabel managerLabel;
    JButton btnClose;
    private LoginScreen loginScreen;
    private Controller controller;


    public ManagerScreen() {
        frame = new JFrame("Rev's GUI: Manager Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        mainPanel = new JPanel(new GridLayout(3, 3));
        mainPanel.setPreferredSize(new Dimension(450, 500));

        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setPreferredSize(new Dimension(120, 600));
        navPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        

        southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
        southPanel.setPreferredSize(new Dimension(600, 100));
        southPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        // Add components to the southPanel as per your requirements
        southPanel.add(Box.createHorizontalGlue());
        managerLabel = new JLabel("Manager Name: ");
        southPanel.add(managerLabel);
        southPanel.add(Box.createHorizontalGlue());
        btnClose = new JButton("Close");
        southPanel.add(btnClose);

        frame.add(navPanel, BorderLayout.WEST);
        frame.add(southPanel, BorderLayout.SOUTH);
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.pack();
    }

    
    /** 
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Close")) {
            frame.dispose();
        }
    }


    
    /*
     * GETTERS AND SETTERS
     */
    public JFrame getFrame() {
        return frame;
    }
    public JPanel getNavPanel() {
        if (navPanel == null) {
            navPanel = new JPanel();
            navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        }
        return navPanel;
    }
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JButton getOrderButton (){
        return btnClose;
    }

    public JLabel getManagerLabel() {
        return managerLabel;
    }
}
