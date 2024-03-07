package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CashierScreen extends JFrame {
  JFrame frame;
  JPanel itemsPanel;
  JPanel navPanel;
  JPanel orderPanel;
  JPanel orderFieldsPanel;
  JPanel bottomPanel;
  JButton orderCompleteButton;

  public CashierScreen() {
    frame = new JFrame("Rev's GUI: Cashier Screen");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Left panel for navigational categories
    navPanel = new JPanel();
    navPanel.setLayout(new GridLayout(8, 1)); // Changed to GridLayout for icons
    navPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

    // Middle panel for items
    JPanel middlePanel = new JPanel(new BorderLayout()); // Changed to BorderLayout
    // middlePanel.setPreferredSize(new Dimension(600, 600));

    // Changed layout to GridLayout for items
    itemsPanel = new JPanel(new GridLayout(3, 2)); // Increase grid size as needed

    // Wrap itemsPanel in a JScrollPane
    JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);

    middlePanel.add(
        itemsScrollPane,
        BorderLayout
            .CENTER); // Changed this line to add the scroll pane instead of itemsPanel directly

    middlePanel.add(itemsPanel, BorderLayout.CENTER);

    middlePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

    // Right panel for order items
    orderPanel = new JPanel();
    orderPanel.setLayout(new BorderLayout()); // Changed to BorderLayout for better organization
    orderPanel.setPreferredSize(new Dimension(200, 600));
    orderPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

    // Panel for adding text fields
    orderFieldsPanel = new JPanel();
    orderFieldsPanel.setLayout(new BoxLayout(orderFieldsPanel, BoxLayout.Y_AXIS));

    // create bottom panel
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

    frame.add(navPanel, BorderLayout.WEST);
    frame.add(middlePanel, BorderLayout.CENTER);
    frame.add(orderPanel, BorderLayout.EAST);
    frame.add(bottomPanel, BorderLayout.SOUTH);

    frame.pack();
  }

  /*
   * GETTERS AND SETTERS
   */

  /**
   * returns the frame
   *
   * @return JFrame frame
   */
  public JFrame getFrame() {
    return frame;
  }

  /**
   * returns the items panel
   *
   * @return JPanel items panel
   */
  public JPanel getItemsPanel() {
    return itemsPanel;
  }

  /**
   * returns the navigation panel
   *
   * @return JPanel navigation panel
   */
  public JPanel getNavPanel() {
    return navPanel;
  }

  /**
   * returns the order panel
   *
   * @return JPanel order panel
   */
  public JPanel getOrderPanel() {
    return orderPanel;
  }

  /**
   * returns the bottom panel and creates a new one if it doesnt exist
   *
   * @return JPanel bottom panel
   */
  public JPanel getBottomPanel() {
    if (bottomPanel == null) {
      bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
      bottomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }
    return bottomPanel;
  }

  /**
   * returns the order fields panel and creates a new one if it doesnt exist
   *
   * @return JPanel order fields panel
   */
  public JPanel getOrderFieldsPanel() {
    if (orderFieldsPanel == null) {
      orderFieldsPanel = new JPanel();
      orderFieldsPanel.setLayout(new BoxLayout(orderFieldsPanel, BoxLayout.Y_AXIS));
    }
    return orderFieldsPanel;
  }

  /**
   * returns the order complete button and creates a new one if it doesnt exist
   *
   * @return JButton order complete button
   */
  public JButton getOrderCompleteButton() {
    if (orderCompleteButton == null) {
      orderCompleteButton = new JButton("Order Complete");
    }
    return orderCompleteButton;
  }

  /**
   * sets the order complete button
   *
   * @param JButton order complete button
   */
  public void setOrderCompleteButton(JButton orderCompleteButton) {
    this.orderCompleteButton = orderCompleteButton;
  }
}
