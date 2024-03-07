package com.webdevwizards.revsGUI.screens;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * ManagerScreen class creates the components for the manager screen.
 *
 * @author Amol, Caden, Mihir
 */
public class ManagerScreen extends JFrame implements ActionListener {
  /** main frame. */
  JFrame frame;

  /** panel containing navbar. */
  JPanel navPanel;

  /** panel containing manager name and close button. */
  JPanel southPanel;

  /** main panel. */
  JPanel mainPanel;

  /** label for manager name. */
  JLabel managerLabel;

  /** close button. */
  JButton btnClose;

  /** login screen. */
  // private LoginScreen loginScreen;
  // /** controller. */
  // private Controller controller;

  /** Constructor for the ManagerScreen class that creates the frame and panels for the manager. */
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
   * closes the frame when the close button is clicked.
   *
   * @param e ActionEvent
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

  /**
   * returns the frame.
   *
   * @return JFrame frame
   */
  public JFrame getFrame() {
    return frame;
  }

  /**
   * returns the nav panel and creates a new one if it doesnt exist.
   *
   * @return JPanel navigation panel
   */
  public JPanel getNavPanel() {
    if (navPanel == null) {
      navPanel = new JPanel();
      navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
    }
    return navPanel;
  }

  /**
   * returns the main panel.
   *
   * @return JPanel main panel
   */
  public JPanel getMainPanel() {
    return mainPanel;
  }

  /**
   * returns the close button
   *
   * @return JButton close button
   */
  public JButton getOrderButton() {
    return btnClose;
  }

  /**
   * returns the manager label.
   *
   * @return JLabel manager label
   */
  public JLabel getManagerLabel() {
    return managerLabel;
  }
}
