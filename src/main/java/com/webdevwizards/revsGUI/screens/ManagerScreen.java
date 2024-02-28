package com.webdevwizards.revsGUI.screens;

import javax.swing.*;
import java.awt.event.*;

public class ManagerScreen extends JFrame implements ActionListener{
    JFrame frame;

    public ManagerScreen() {
        frame = new JFrame("Rev's GUI: Manager Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        System.out.println("ManagerScreen constructor");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle the action event here
        System.out.println("Action event occurred: " + e.getActionCommand());
    }

    public JFrame getFrame() {
        return frame;
    }

}
