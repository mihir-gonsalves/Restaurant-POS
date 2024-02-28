package com.webdevwizards.revsGUI.screens;

import javax.swing.*;

public class PaymentScreen extends JFrame {
    public static JFrame frame;

    public PaymentScreen() {
        System.out.println("PaymentScreen constructor");
        frame = new JFrame("Rev's GUI: Payment Screen");
    }

    public JFrame getFrame() {
        return frame;
    }
}