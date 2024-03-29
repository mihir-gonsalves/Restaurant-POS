package com.webdevwizards.revsGUI;

// import com.formdev.flatlaf.FlatLightLaf;
import com.webdevwizards.revsGUI.database.Model;
import com.webdevwizards.revsGUI.screens.CashierScreen;
import com.webdevwizards.revsGUI.screens.LoginScreen;
import com.webdevwizards.revsGUI.screens.ManagerScreen;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Controller class for the Rev's GUI This class is used to control the flow of the program and handle all user input.
 *
 * @author Amol, Caden, Carson, Kevin, Jesung, and Mihir
 */
public class Controller implements ActionListener {
  private Model model;
  private LoginScreen loginScreen;
  private CashierScreen cashierScreen;
  private ManagerScreen managerScreen;
  private boolean isManager;
  private Popup po;
  private PopupFactory pf;
  private int[][] orderItems;
  private String phoneNumber;
  private int preferredWidth;
  private int preferredHeight;
  private boolean managerfirst = true;
  private boolean cashierfirst = true;

  /** Constructor for the Controller class that creates a new instance of the Controller class. */
  public Controller() {}

  /**
   * This method is used to create a new instance of the Controller class.
   *
   * @param args is the command line arguments
   */
  public static void main(String[] args) {
    // FlatLightLaf.setup();
    Controller controller = new Controller();
    controller.initialize();
    controller.switchToLoginScreen();
    controller.getPreferredSize();
    controller.switchFromLoginScreen();
  }

  /** This method is used to initialize the model and all screens on program start. */
  public void initialize() {
    model = new Model();

    loginScreen = new LoginScreen();
    final JFrame loginFrame = loginScreen.getFrame();
    loginFrame.setVisible(false);
    updateFontSizes(loginFrame, loginFrame);

    cashierScreen = new CashierScreen();
    final JFrame cashierFrame = cashierScreen.getFrame();
    cashierFrame.setVisible(false);

    managerScreen = new ManagerScreen();
    final JFrame managerFrame = managerScreen.getFrame();
    managerFrame.setVisible(false);

    // auto update fontsizes
    loginFrame.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            updateFontSizes(loginFrame, loginFrame);
          }
        });

    isManager = false;
    pf = new PopupFactory();
    orderItems = new int[15][2];

    // set preferred width and height to maximum screen size
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    preferredWidth = screenSize.width;
    preferredHeight = screenSize.height;
  }

  /**
   * get preferred size of the frame from fullscreen button then set preferred values and the size of the frames so that all frames have a consistent size.
   */
  public void getPreferredSize() {
    JToggleButton fullscreenButton = loginScreen.getFullscreenButton();
    fullscreenButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (fullscreenButton.isSelected()) {
              // set preferred width and height to half of the screen size
              preferredWidth = (int) (preferredWidth * 0.5);
              preferredHeight = (int) (preferredHeight * 0.5);
              fullscreenButton.setText("Fullscreen");
            } else {
              // set preferred width and height to full screen size
              Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
              preferredWidth = screenSize.width;
              preferredHeight = screenSize.height;
              fullscreenButton.setText("Exit Fullscreen");
            }
            // set the size of the frames to the preferred width and height
            loginScreen.getFrame().setSize(preferredWidth, preferredHeight);
            cashierScreen.getFrame().setSize(preferredWidth, preferredHeight);
            managerScreen.getFrame().setSize(preferredWidth, preferredHeight);

            // revalidate and repaint the frame for redraw
            loginScreen.getFrame().revalidate();
            loginScreen.getFrame().repaint();
          }
        });
  }

  /**
   * update font size of all components of a panel recursively, not used constantly is mainly used for testing.
   *
   * @param c is the component that needs to be updated
   * @param f is the frame that the component is in
   */
  private void updateFontSizes(Component c, JFrame f) {
    if (c instanceof Container) {
      for (Component child : ((Container) c).getComponents()) {
        updateFontSizes(child, f);
      }
    }
    if (c instanceof JButton
        || c instanceof JLabel
        || c instanceof JTextField
        || c instanceof JTextArea
        || c instanceof JToggleButton
        || c instanceof JTable) {
      Font sourceFont = c.getFont();
      float scale = f.getHeight() / 1000.0f;
      float newSize = sourceFont.getSize() * scale;
      int minSize = 14;
      if (newSize < minSize) {
        newSize = minSize;
      }
      c.setFont(sourceFont.deriveFont(newSize));
      f.revalidate();
      f.repaint();
    }
  }

  /** This is used to dispose the manager screen, this was created for testing purposes. */
  public void disposeManager() {
    managerScreen.getFrame().dispose();
  }

  /** This is used to clear the current order if it is canceled. */
  public void clearOrder() {
    for (int i = 0; i < orderItems.length; i++) {
      orderItems[i][0] = 0;
      orderItems[i][1] = 0;
    }
    populateCashierOrderPanel();
  }

  /* ------------------------------------------------- LOGIN SCREEN METHODS --------------------------------------- */

  /** sets the login screen to visible for program startup. */
  public void switchToLoginScreen() {
    loginScreen.getFrame().setVisible(true);
  }

  /** switch to appropriate screen (manager or cashier) based on login's phone number. */
  public void switchFromLoginScreen() {
    loginScreen
        .getLoginButton()
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {

                // if the phone number is valid, switch to the appropriate screen
                if (model.login(loginScreen.getPhoneNumber())) {
                  phoneNumber = loginScreen.getPhoneNumber();

                  // if the user is a manager, switch to the manager screen and populate with
                  // defaults, get rid of the login screen
                  if (model.isManager(phoneNumber)) {
                    switchToManagerScreen();
                    managerScreen.getFrame().setSize(preferredWidth, preferredHeight);
                    loginScreen.getFrame().dispose();
                    if (managerfirst) {
                      populateManagerNavBar();
                      populateManagerMainPanel("chart");
                      managerfirst = false;
                    }
                    for (Component c : managerScreen.getFrame().getComponents()) {
                      updateFontSizes(c, managerScreen.getFrame());
                    }
                    // auto update fonts
                  } else { // if the user is a cashier, switch to the cashier screen and populate
                    // with defaults, get rid of the login screen, and completeOrder
                    switchToCashierScreen();
                    cashierScreen.getFrame().setSize(preferredWidth, preferredHeight);
                    loginScreen.getFrame().dispose();
                    if (cashierfirst) {
                      populateCashierNavBar();
                      populateCashierItemPanel("Burgers");
                      populateCashierBottomPanel();
                      populateCashierOrderPanel();
                      cashierfirst = false;
                    }
                    // auto update fonts
                    for (Component c : cashierScreen.getFrame().getComponents()) {
                      updateFontSizes(c, cashierScreen.getFrame());
                    }
                    switchFromCashierPanel();
                  }
                } else {
                  JOptionPane.showMessageDialog(null, "Invalid phone number");
                }
              }
            });
  }

  /* ------------------------------------------------- CASHIER SCREEN METHODS --------------------------------------- */

  /** sets the cashier screen to visible and sets isManager to false. */
  public void switchToCashierScreen() {
    isManager = false;
    cashierScreen.getFrame().setVisible(true);
  }

  /**
   * draws the menu items on the cashier screen for use.
   *
   * @param category used to print correct items in the category
   */
  public void populateCashierItemPanel(String category) {
    // Get items and sort by category
    ResultSet rs =
        model.executeQuery("SELECT * FROM menu_items ORDER BY category;");
    JPanel itemsPanel = cashierScreen.getItemsPanel();
    Font font12 = new Font("Arial", Font.PLAIN, 12);

    // get mainframe from cashier screen
    JFrame frame = cashierScreen.getFrame();

    // remove current items from the panel so they can be replaced
    itemsPanel.removeAll();

    // try loop to get items from the database safely
    try {
      boolean isTodayinRange;
      // while there is another row in the result set
      while (rs.next()) {
        String db_category = rs.getString("category"); // get the category of the current row
        String date_range = rs.getString("date_range");
        if (date_range == null) {
          isTodayinRange = true;
        } else {
          isTodayinRange = isTodayWithinDateRange(date_range);
        }
        // if the category of the current row is the same as the category we want to display
        if (db_category.equals(category) && isTodayinRange) {
          String item_name = rs.getString("item_name");
          String item_price = rs.getString("item_price");

          String jlabel_text = item_name + "  " + "$" + item_price;
          // jlabel_text.setFont(font12);
          StringBuilder item_image = new StringBuilder();

          // formats image as item_name in lowercase and replaces spaces with underscores
          for (int i = 0; i < item_name.length(); i++) {
            char c = item_name.charAt(i);
            if (Character.isLetterOrDigit(c)) {
              item_image.append(Character.toLowerCase(c));
            } else if (c == ' ') {
              item_image.append('_');
            }
          }

          // append appropriate project root path and file extension
          String item_image_path = "./images/" + item_image + ".png";

          // create a new panel to hold the item image and name
          JPanel itemPanel = new JPanel(new BorderLayout());
          // System.out.println("Item image: " + item_image_path); // testing
          // System.out.println("Adding item: " + item_name); // testing

          // create a new button with an image
          JLabel itemImage = new JLabel(new ImageIcon(item_image_path));
          itemImage.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

          itemPanel.add(itemImage, BorderLayout.CENTER);

          // create a new label with the item name to go below the center of the image button
          JLabel itemName = new JLabel(jlabel_text, SwingConstants.CENTER);
          itemName.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
          itemName.setFont(font12);
          itemPanel.add(itemName, BorderLayout.SOUTH);

          // add the panel to the itemsPanel
          itemsPanel.add(itemPanel);

          // add action listener to the image button to display a popup with the item name and an
          // "Add to Order" button

          itemPanel.addMouseListener(
              new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                  model.getItemID(item_name);
                  for (int i = 0; i < orderItems.length; i++) {
                    if (orderItems[i][0] == model.getItemID(item_name) || orderItems[i][0] == 0) {
                      orderItems[i][0] = model.getItemID(item_name);
                      orderItems[i][1] = orderItems[i][1] + 1;

                      // reupdates the order panel
                      populateCashierOrderPanel();

                      // break because we found the item in the orderItems array
                      break;
                    }
                  }
                }
              });
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // revalidate and repaint the itemsPanel for redraw
    updateFontSizes(
        itemsPanel,
        frame); // have to do this because otherwise on category change it reverts to normal size
    itemsPanel.revalidate();
    itemsPanel.repaint();
  }

  /** populates the cashier navbar and sets up action listeners for each category button. */
  public void populateCashierNavBar() {
    ResultSet rs =
        this.model.executeQuery(
            "SELECT * FROM menu_items ORDER BY category;"); 
    JPanel navPanel = cashierScreen.getNavPanel();

    // used in the while loop to keep track of the current category
    String current_category = "";

    // try loop to get categories from the database safely
    try {
      while (rs.next()) {
        String db_category = rs.getString("category");
        if (!db_category.equals(current_category)) {
          // set the current category to the new category
          current_category = db_category;

          // get category icon image
          StringBuilder category_file_name = new StringBuilder();

          // formats image as category in lowercase and replaces spaces with underscores as well as
          // replaces '&' with "and"
          for (int i = 0; i < current_category.length(); i++) {
            char c = current_category.charAt(i);
            if (Character.isLetter(c)) {
              category_file_name.append(Character.toLowerCase(c));
            } else if (c == ' ') {
              category_file_name.append('_');
            } else if (c == '&') {
              category_file_name.append("and");
            }
          }

          // get the category image and resize it
          ImageIcon categoryImage = new ImageIcon("./images/" + category_file_name + ".png");
          categoryImage =
              new ImageIcon(categoryImage.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH));

          // create and style a new button with the category image
          JButton categoryButton = new JButton(categoryImage);
          categoryButton.setPreferredSize(new Dimension(100, 100));
          // System.out.println("Category image: " + category_image_path); // testing

          // create a new action listener for the category button to populate the item panel with
          // the category's items using a final variable
          final String current_category_final = current_category;
          categoryButton.addActionListener(
              new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                  populateCashierItemPanel(current_category_final);
                }
              });

          // add the category button to the navPanel
          navPanel.add(categoryButton);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This is used for testing, used when disposing the cashier screen when cancelling and restarting
   * order.
   */
  private void cleanCashierOrderPanel() {
    JPanel orderPanel = cashierScreen.getOrderPanel();
    JPanel orderFieldsPanel = cashierScreen.getOrderFieldsPanel();
    if (orderPanel.getComponentCount() > 0) {
      orderPanel.removeAll();
      orderFieldsPanel.removeAll();
    }
  }

  /** populate cashier order panel with items selected for order. */
  public void populateCashierOrderPanel() {
    // remove all items from the orderFieldsPanel if it exists and then from the orderPanel as well
    JPanel orderPanel = cashierScreen.getOrderPanel();
    JPanel plusMinusPanel;
    JPanel orderFieldsPanel = cashierScreen.getOrderFieldsPanel();
    if (orderPanel.getComponentCount() > 0) {
      orderPanel.removeAll();
      orderFieldsPanel.removeAll();
    }

    JLabel orderItemsLabel = new JLabel("Order Items");
    orderItemsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    orderItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    orderPanel.add(orderItemsLabel, BorderLayout.NORTH);

    // add items (JTextField) to the orderFieldsPanel by looping through the orderItems array
    for (int i = 0; i < orderItems.length; i++) {
      if (orderItems[i][0] != 0) {
        final int index = i;

        JTextArea orderItemTextArea =
            new JTextArea(
                model.getItemName(orderItems[i][0]) + " x" + String.valueOf(orderItems[i][1]));
        orderItemTextArea.setEditable(false);
        orderItemTextArea.setLineWrap(true);
        orderItemTextArea.setWrapStyleWord(true);
        orderItemTextArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        orderItemTextArea.setPreferredSize(new Dimension(200, 60));

        plusMinusPanel = new JPanel();
        plusMinusPanel.setLayout(new BoxLayout(plusMinusPanel, BoxLayout.X_AXIS));

        JButton removeItemButton = new JButton("-");
        removeItemButton.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                if (orderItems[index][1] > 1) {
                  orderItems[index][1] = orderItems[index][1] - 1;
                  populateCashierOrderPanel();
                } else {
                  orderItems[index][0] = 0;
                  orderItems[index][1] = 0;
                  populateCashierOrderPanel();
                }
              }
            });

        JButton addItemButton = new JButton("+");
        addItemButton.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                orderItems[index][1] = orderItems[index][1] + 1;
                populateCashierOrderPanel();
              }
            });

        plusMinusPanel.add(removeItemButton);
        plusMinusPanel.add(addItemButton);

        orderFieldsPanel.add(orderItemTextArea);
        orderFieldsPanel.add(plusMinusPanel);
      }
    }

    // add the orderFieldsPanel to the orderPanel and revalidate and repaint the orderPanel for
    // redraw
    orderPanel.add(orderFieldsPanel, BorderLayout.CENTER);
    updateFontSizes(orderPanel, cashierScreen.getFrame()); // do this because removing all elements
    orderPanel.revalidate();
    orderPanel.repaint();

    // reinitialize the subtotal label in bottom panel
    populateCashierBottomPanel();
  }

  /**
   * populate the bottom panel of the cashier screen with the cashier's name and the subtotal of the
   * order.
   */
  public void populateCashierBottomPanel() {
    JPanel bottomPanel = cashierScreen.getBottomPanel();
    Font font24 = new Font("Arial", Font.PLAIN, 24);

    // if bottom panel populated with items, remove all items
    if (bottomPanel.getComponentCount() > 0) {
      bottomPanel.removeAll();
    }

    // create a new button to complete the order
    cashierScreen.setOrderCompleteButton(null);
    JButton returnButton = new JButton("Return to Login");
    returnButton.setFont(font24);
    returnButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            clearOrder();
            cashierScreen.getFrame().dispose();
            switchToLoginScreen();
          }
        });

    JButton orderCompleteButton = cashierScreen.getOrderCompleteButton();
    orderCompleteButton.setText("Complete Order");
    orderCompleteButton.setFont(font24);

    // get the subtotal of the order and calculate the tax and total
    double subtotal = model.sumItemPrices(orderItems);
    double tax = subtotal * 0.0825;
    double total = subtotal * 1.0825;

    double roundedSubtotal = ((long) (subtotal * 1e2)) / 1e2;
    double roundedTax = ((long) (tax * 1e2)) / 1e2;
    double roundedTotal = ((long) (total * 1e2)) / 1e2;

    // create a new label with the cashier's name and the subtotal of the order, we do this so we
    // can update the component's font
    JLabel orderCashier =
        new JLabel(" Cashier Name: " + model.getUserName(phoneNumber) + " -- " + phoneNumber);
    orderCashier.setFont(font24);
    orderCashier.setForeground(Color.WHITE); // Set foreground color to white
    JLabel orderPrice =
        new JLabel(
            "Subtotal: "
                + roundedSubtotal
                + " +  "
                + roundedTax
                + " (tax)"
                + "                Total - $"
                + roundedTotal);
    orderPrice.setFont(font24);
    orderPrice.setForeground(Color.WHITE); // Set foreground color to white

    // add the cashier's name, the subtotal of the order, and orderComplete button to the
    // bottomPanel with empty horizontal glues for centering
    bottomPanel.add(returnButton);
    bottomPanel.add(orderCashier);
    bottomPanel.add(Box.createHorizontalGlue());
    bottomPanel.add(orderPrice);
    bottomPanel.add(Box.createHorizontalGlue());
    bottomPanel.add(orderCompleteButton);

    bottomPanel.setBackground(new Color(80, 0, 0));

    // add event listener to orderComplete button
    cashierScreen.setOrderCompleteButton(orderCompleteButton);
    switchFromCashierPanel();

    // revalidate and repaint the bottomPanel for redraw and add back to the cashierScreen frame
    // auto update fonts
    updateFontSizes(bottomPanel, cashierScreen.getFrame());
    bottomPanel.revalidate();
    bottomPanel.repaint();
  }

  /**
   * complete the cashier's order via orderComplete button, display subtotal, and switch to payment
   * screen.
   */
  public void switchFromCashierPanel() {
    JButton orderCompleteButton = cashierScreen.getOrderCompleteButton();

    double dSubtotal = model.sumItemPrices(orderItems);
    double dTax = dSubtotal * 0.0825;
    double dTotal = dSubtotal * 1.0825;

    double roundedSubtotal = ((long) (dSubtotal * 1e2)) / 1e2;
    double roundedTax = ((long) (dTax * 1e2)) / 1e2;
    double roundedTotal = ((long) (dTotal * 1e2)) / 1e2;

    final String[] paymentType = {"filler"};

    // add action listener to orderComplete button to display subtotal and switch to payment screen
    // when clicked
    orderCompleteButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String subtotal = String.valueOf(roundedSubtotal);

            // create pop up to ask for payment methods
            JPanel popUpPanel = new JPanel();
            JPanel cardPanel = new JPanel();
            cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Get the screen size
            Dimension frameSize = cashierScreen.getFrame().getSize();

            // sets the size and style of the popup panel
            int size = (int) (frameSize.getWidth() / 3);
            popUpPanel.setPreferredSize(new Dimension(size, size));
            popUpPanel.setBorder(BorderFactory.createLineBorder(new Color(246, 182, 12), 8));
            popUpPanel.setLayout(new BoxLayout(popUpPanel, BoxLayout.PAGE_AXIS));

            // create and style the label and button for the popup
            JLabel popUpLabel = new JLabel("Cart");
            popUpLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            popUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            ImageIcon creditImage = new ImageIcon("./images/credit-card.png");
            creditImage =
                new ImageIcon(
                    creditImage.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            JButton creditButton = new JButton(creditImage);
            creditButton.setPreferredSize(new Dimension(300, 200));
            cardPanel.add(creditButton);

            ImageIcon cashImage = new ImageIcon("./images/student-card.png");
            cashImage =
                new ImageIcon(cashImage.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            JButton cashButton = new JButton(cashImage);
            cashButton.setPreferredSize(new Dimension(300, 200));
            cardPanel.add(Box.createHorizontalGlue());
            cardPanel.add(cashButton);

            JButton finishOrder = new JButton("Finish Order");
            finishOrder.setFont(new Font("Arial", Font.PLAIN, 12));
            finishOrder.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton returnButton = new JButton("Continue Ordering");
            returnButton.setFont(new Font("Arial", Font.PLAIN, 12));
            returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            // String paymentType = "credit"; // TODO

            creditButton.addActionListener(
                new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                    paymentType[0] = "credit";
                  }
                });

            cashButton.addActionListener(
                new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                    paymentType[0] = "cash";
                  }
                });

            finishOrder.addActionListener(
                new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                    if (paymentType[0].equals("filler")) {
                      JOptionPane.showMessageDialog(null, "Please select a payment method");
                    } else {
                      // insert order into database and display message based on success
                      if (model.insert_order(subtotal, orderItems, paymentType[0])) {
                        JOptionPane.showMessageDialog(
                            null,
                            "Order submitted, your order number is: " + model.getOrderNumber());
                        orderItems = new int[15][2];
                        // dispose of the cashier screen
                        po.hide();
                        clearOrder();
                        cashierScreen.getFrame().dispose();
                        switchToLoginScreen();

                      } else {
                        JOptionPane.showMessageDialog(
                            null, "Sorry, we are out of ingredients for this order.");
                      }
                    }
                  }
                });

            returnButton.addActionListener(
                new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                    po.hide();
                  }
                });

            popUpPanel.add(Box.createVerticalGlue());
            popUpPanel.add(popUpLabel);
            popUpPanel.add(Box.createVerticalGlue());
            popUpPanel.add(cardPanel);
            popUpPanel.add(Box.createVerticalStrut(30));
            popUpPanel.add(finishOrder);
            popUpPanel.add(Box.createVerticalGlue());
            popUpPanel.add(returnButton);
            popUpPanel.add(Box.createVerticalGlue());

            // set the popup panel to the center of the screen
            Point frameLocation = cashierScreen.getFrame().getLocation();
            int x =
                (int)
                    (frameLocation.getX()
                        + (frameSize.getWidth() - popUpPanel.getPreferredSize().getWidth()) / 2);
            int y =
                (int)
                    (frameLocation.getY()
                        + (frameSize.getHeight() - popUpPanel.getPreferredSize().getHeight()) / 2);

            // creates and shows the popup
            po = pf.getPopup(cashierScreen.getFrame(), popUpPanel, x, y);
            po.show();
          }
        });
  }

  /* ------------------------------------------------- MANAGER SCREEN METHODS --------------------------------------- */

  /** Activated when manager number is input to login screen. */
  public void switchToManagerScreen() {
    isManager = true;
    managerScreen.getFrame().setVisible(true);
  }

  /**
   * populates the manager screen with a default navbar and adds action listeners for each button.
   */
  public void populateManagerNavBar() {
    String[] buttonNames = {"chart", "order", "track", "table"};

    // for each button name, create a new button with an image and add an action listener to
    // populate the main panel with the corresponding content
    for (int i = 0; i < buttonNames.length; i++) {
      final int index = i;
      ImageIcon imageIcon = new ImageIcon("./images/" + buttonNames[i] + ".png");
      imageIcon =
          new ImageIcon(imageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
      JButton button = new JButton(imageIcon);
      button.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              populateManagerMainPanel(buttonNames[index]);
            }
          });
      // add buttons and vertical glue for spacing to the navPanel
      managerScreen.getNavPanel().add(button);
      if (i < buttonNames.length - 1) {
        managerScreen.getNavPanel().add(Box.createVerticalGlue());
      }
    }
    // revalidate and repaint the navPanel for redraw
    managerScreen.getNavPanel().revalidate();
    managerScreen.getNavPanel().repaint();
  }

  /**
   * populates the manager screen with content.
   *
   * @param content is the content to populate the manager screen with
   */
  public void populateManagerMainPanel(String content) {
    JPanel mainPanel = managerScreen.getMainPanel();
    if (mainPanel.getComponentCount() > 0) {
      mainPanel.removeAll();
    }
    if (content.equals("chart")) {
      populateManagerChartPanel();
    } else if (content.equals("order")) {
      populateManagerOrderPanel();
    } else if (content.equals("track")) {
      populateManagerTrackPanel();
    } else if (content.equals("table")) {
      populateManagerTablePanel();
    }

    // add manager name
    managerScreen.getManagerLabel().setText("Manager Name: " + model.getUserName(phoneNumber));

    JButton returnButton = new JButton("Return to Login");

    returnButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            managerScreen.getFrame().dispose();
            switchToLoginScreen();
          }
        });
    managerScreen.getOrderButton().add(returnButton);

    // revalidate and repaint the mainPanel for redraw
    mainPanel.revalidate();
    mainPanel.repaint();
  }

  /** populates the manager screen with the order history. */
  public void populateManagerOrderPanel() {
    JPanel mainPanel = managerScreen.getMainPanel();
    mainPanel.setLayout(new BorderLayout());

    // Panel for ingredient ID and count
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new FlowLayout());

    JLabel ingredientIdLabel = new JLabel("Ingredient Name:");
    JTextField ingredientNameField = new JTextField(10);

    JLabel countLabel = new JLabel("Count:");
    JTextField countField = new JTextField(10);

    JButton commitButton = new JButton("Purchase");

    inputPanel.add(ingredientIdLabel);
    inputPanel.add(ingredientNameField);
    inputPanel.add(countLabel);
    inputPanel.add(countField);
    inputPanel.add(commitButton);

    mainPanel.add(inputPanel, BorderLayout.NORTH);

    // Table to display results
    JTable table = new JTable();
    JScrollPane scrollPane = new JScrollPane(table);
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    updateOrderTable(table);
    // Add action listener to commit button
    commitButton.addActionListener(
        e -> {
          // Retrieve ingredient ID and count from text fields
          Integer ingredientId = model.getIngredientID(ingredientNameField.getText());
          String count = countField.getText();

          try {
            model.addIngredientCount(ingredientId, Integer.parseInt(count));
            JOptionPane.showMessageDialog(null, "Stock updated");
          } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Stock not updated");
            ex.printStackTrace();
          }
          // Perform commit action here
          // You may want to update the table based on the committed data
          updateOrderTable(table);
        });
  }

  /** Populates the manager screen with the order panel. */
  public void populateManagerTablePanel() {
    JPanel mainPanel = managerScreen.getMainPanel();
    mainPanel.setLayout(new BorderLayout());

    // header panel
    JPanel headerPanel = new JPanel();
    headerPanel.add(new JLabel("Table"));

    // Panel for ingredient ID and count
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new FlowLayout());

    // COMBOBOX
    JLabel tableLabel = new JLabel("Table: ");
    String[] dropDownList = {
      "", "Users", "Manager Orders", "Customer Orders", "Items", "Ingredients"
    };
    JComboBox comboBox = new JComboBox(dropDownList);

    inputPanel.add(tableLabel);
    inputPanel.add(comboBox);

    mainPanel.add(inputPanel, BorderLayout.NORTH);

    // Table to display results. Used in viewTable
    JTable table = new JTable();

    JScrollPane scrollPane = new JScrollPane(table);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    comboBox.addActionListener(
        e -> { // Resets boxes to white and then grays out and sets to uneditable the unneeded ones
          // based on the option you select
          if (comboBox.getSelectedItem().equals("Users")) {
            usersPopUp(table);
            viewTable(table, 0);
          } else if (comboBox.getSelectedItem().equals("Manager Orders")) {
            managerOrdersPopUp(table);
            viewTable(table, 1);
          } else if (comboBox.getSelectedItem().equals("Customer Orders")) {
            viewTable(table, 2);
            customerOrdersPopUp(table);
          } else if (comboBox.getSelectedItem().equals("Items")) {
            itemsPopUp(table);
            viewTable(table, 3);
          } else if (comboBox.getSelectedItem().equals("Ingredients")) {
            ingredientsPopUp(table);
            viewTable(table, 4);
          }
        });
  }

  /** populates the manager screen with the track panel. */
  public void populateManagerTrackPanel() {
    JPanel mainPanel = managerScreen.getMainPanel();

    // add a header panel
    JPanel headerPanel = new JPanel();
    headerPanel.add(new JLabel("Track Orders"));

    // Add start date text area
    JPanel startDatePanel = new JPanel();
    startDatePanel.setLayout(new FlowLayout());
    JLabel startDateLabel = new JLabel("Start Date yyyy-mm-dd:");
    JTextField startDateField = new JTextField(10);
    startDatePanel.add(startDateLabel);
    startDatePanel.add(startDateField);
    mainPanel.add(startDatePanel);

    // Add end date text area
    JPanel endDatePanel = new JPanel();
    endDatePanel.setLayout(new FlowLayout());
    JLabel endDateLabel = new JLabel("End Date yyyy-mm-dd:");
    JTextField endDateField = new JTextField(10);
    endDatePanel.add(endDateLabel);
    endDatePanel.add(endDateField);
    mainPanel.add(endDatePanel);

    // Add a table
    JTable table = new JTable(); // Initialize your table
    table.setSize(1000, 400);
    JScrollPane scrollPane = new JScrollPane(table);
    mainPanel.add(scrollPane);
    String[] columnNames = {"ID", "Date", "Time", "Subtotal", "Tax", "Total", "Payment Method"};
    DefaultTableModel tablemodel = new DefaultTableModel(columnNames, 0);

    // Add an action listener to a button to fetch data from SQL based on dates
    JButton fetchDataButton = new JButton("Fetch Data");
    fetchDataButton.addActionListener(
        e -> {
          // Fetch data from SQL table based on start and end dates
          String startDate = parseDate(startDateField.getText());
          String endDate = parseDate(endDateField.getText());
          // Query SQL table using startDate and endDate
          ResultSet rs = model.getOrderDaytoDay(startDate, endDate);
          try {
            while (rs.next()) {
              Object[] row = new Object[7]; // Assuming 7 columns in the result set
              for (int i = 0; i < row.length; i++) {
                row[i] = rs.getObject(i + 1); // Columns are 1-indexed in ResultSet
              }
              tablemodel.addRow(row);
            }
            table.setModel(tablemodel);

          } catch (Exception ex) {
            ex.printStackTrace();
          }
        });
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(Box.createVerticalStrut(10)); // Add some spacing
    mainPanel.add(scrollPane);
    mainPanel.add(Box.createVerticalStrut(10)); // Add some spacing
    mainPanel.add(fetchDataButton);
  }

  /** populates the manager screen with a table of CRUD operations for each table. */
  public void populateManagerChartPanel() {
    JPanel mainPanel = managerScreen.getMainPanel();
    mainPanel.setLayout(new BorderLayout());

    // header panel
    JPanel headerPanel = new JPanel();
    headerPanel.add(new JLabel("Chart"));

    // Panel for ingredient ID and count
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new FlowLayout());

    // COMBOBOX
    JLabel tableLabel = new JLabel("Query Select: ");
    String[] dropDownList = {
      "Product Usage", "Sales Report", "Excess Report", "Restock Report", "What Sells Together"
    };
    JComboBox comboBox = new JComboBox(dropDownList);

    JLabel timeStart = new JLabel("Start Date yyyy-mm-dd:");
    JTextField timeStart2 = new JTextField(10);

    JLabel timeEnd = new JLabel("End Date yyyy-mm-dd:");
    JTextField timeEnd2 = new JTextField(10);

    JButton fetchButton = new JButton("Fetch Data");

    // inputPanel.add(tableLabel);
    inputPanel.add(comboBox);

    inputPanel.add(timeStart);
    inputPanel.add(timeStart2);

    inputPanel.add(timeEnd);
    inputPanel.add(timeEnd2);

    timeStart2.setText("2022-02-01"); // our table's start date it begins from 2022 feb first
    timeEnd2.setText((new Date(System.currentTimeMillis())).toString()); // current time

    inputPanel.add(fetchButton);

    mainPanel.add(inputPanel, BorderLayout.NORTH);

    // Table to display results. Used in viewTable
    JTable table = new JTable();

    JScrollPane scrollPane = new JScrollPane(table);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    // String sql = "SELECT * FROM users WHERE phonenumber = ?";
    // PreparedStatement pstmt = conn.prepareStatement(sql);
    // pstmt.setString(1, phoneNumber);
    fetchButton.addActionListener(
        e -> {
          // retrieve start and end dates from text fields
          String startDate = timeStart2.getText();
          String endDate = timeEnd2.getText();

          // set up the table based on the selected option's resultset
          if (comboBox.getSelectedItem().equals("Product Usage")) {
            TableQuery(model.getProductUsage(startDate, endDate), table, 1);

          } else if (comboBox.getSelectedItem().equals("Sales Report")) {

            TableQuery(model.getSalesReport(startDate, endDate), table, 1);

          } else if (comboBox.getSelectedItem().equals("Excess Report")) {
            ResultSet Rs = model.findExcess(timeStart2.getText());
            TableQuery(Rs, table, 1);
          } else if (comboBox.getSelectedItem().equals("Restock Report")) {
            // Adds the items that are currently less than 15 in number to the restock report
            ResultSet Rs = model.findRestock();
            TableQuery(Rs, table, 1);
          } else if (comboBox.getSelectedItem().equals("What Sells Together")) {
            ResultSet Rs = model.findPair(timeStart2.getText(), timeEnd2.getText());
            TableQuery(Rs, table, 1);
          }
          // updateFontSizes(table, managerScreen.getFrame());
          table.setFont(new Font("Arial", Font.PLAIN, preferredHeight / 65));
        });
  }

  /**
   * This method is used to update the table with the latest data from SQL.
   *
   * @param rs is the result set to be displayed in the table
   * @param table is the table to display the result set
   * @param start_num is the starting column number to display
   */
  public void TableQuery(ResultSet rs, JTable table, int start_num) {
    try {
      // make table uneditable
      DefaultTableModel tableModel =
          new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
              return false;
            }
          };

      if (rs != null) {
        ResultSetMetaData data = rs.getMetaData();
        int cols = data.getColumnCount();
        // clear if data exists
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        // create columns with count
        for (int i = start_num; i <= cols; i++) {
          tableModel.addColumn(data.getColumnName(i));
        }

        // add rows
        while (rs.next()) {
          Vector<Object> v = new Vector<Object>();
          for (int i = start_num; i <= cols; i++) {
            v.add(rs.getObject(i));
          }
          tableModel.addRow(v);
        }
      }
      table.setModel(tableModel);
    } catch (SQLException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Failed to fetch data: " + e.getMessage());
    }
  }

  /* ------------------------------------------------- MANAGER HELPER METHODS --------------------------------------- */

  /**
   * parses a date string to a format that can be used in SQL queries.
   *
   * @param dateString is the date for which data will be fetched
   * @return String is the date in the format yyyy-mm-dd so that queries are consistent
   */
  private String parseDate(String dateString) {
    // Implement your date parsing logic here
    dateString = dateString.trim();
    dateString = dateString.replaceAll("/", "-");
    return dateString;
  }

  /**
   * updates the table with the latest data from SQL.
   *
   * @param table is the table to display the result set
   * @param tableType 0 for users, 1 for manager orders, 2 for customer orders, 2 for items, 3 for
   *     ingredients
   */
  private void viewTable(JTable table, int tableType) {
    // Fetch data from SQL and view the table
    // remove all mouse listeners from the table before updating it
    MouseListener[] mouseListeners = table.getMouseListeners();
    for (MouseListener mouseListener : mouseListeners) {
      table.removeMouseListener(mouseListener);
    }
    switch (tableType) { // if the function was called to display the menu_item table
      case 0: // USERS
        try {
          ResultSet resultSet = model.getAllUsers();

          // make table uneditable
          DefaultTableModel tableModel =
              new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                  return false;
                }
              };

          // set columns to phone number | name | is manager ; and then populate with appropriate
          // data
          tableModel.setColumnIdentifiers(new String[] {"phone number", "name", "is manager"});
          while (resultSet.next()) {
            Object[] rowData = new Object[3];
            // don't access first column because it contains ID which is not needed
            rowData[0] = resultSet.getObject(2);
            rowData[1] = resultSet.getObject(3);
            rowData[2] = resultSet.getObject(4);
            tableModel.addRow(rowData);
          }
          table.setModel(tableModel);

          // set to false so user can't select row, its distracting and not needed
          table.setRowSelectionAllowed(false);
          table.setCellSelectionEnabled(false);

          // change table column widths
          table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          int tableWidth = table.getWidth();
          table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 3);
          table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 3);
          table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 3);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        usersPopUp(table);
        break;
      case 1: // MANAGER ORDERS
        try {
          ResultSet resultSet = model.getAllManagerOrders();

          // make table uneditable
          DefaultTableModel tableModel =
              new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                  return false;
                }
              };
          // set columns to phone number | date | time | total ; and then populate with appropriate
          // data
          tableModel.setColumnIdentifiers(new String[] {"phone number", "date", "time", "total"});
          while (resultSet.next()) {
            Object[] rowData = new Object[4];
            // don't access first column because it contains ID which is not needed
            rowData[0] = resultSet.getObject(5);
            rowData[1] = resultSet.getObject(2);
            rowData[2] = resultSet.getObject(3);
            rowData[3] = resultSet.getObject(4);
            tableModel.addRow(rowData);
          }
          table.setModel(tableModel);

          // set to false so user can't select row, its distracting and not needed
          table.setRowSelectionAllowed(false);
          table.setCellSelectionEnabled(false);

          // change table column widths
          table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          int tableWidth = table.getWidth();
          table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 4);
          table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 4);
          table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 4);
          table.getColumnModel().getColumn(3).setPreferredWidth(tableWidth / 4);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        managerOrdersPopUp(table);
        break;
      case 2: // CUSTOMER ORDERS
        try {
          ResultSet resultSet = model.getAllCustomerOrders();

          // make table uneditable
          DefaultTableModel tableModel =
              new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                  return false;
                }
              };
          // set columns to date | time  | subtotal | tax | total | payment type ; and then populate
          // with appropriate data
          tableModel.setColumnIdentifiers(
              new String[] {"date", "time", "subtotal", "tax", "total", "payment type"});
          while (resultSet.next()) {
            Object[] rowData = new Object[6];
            // don't access first column because it contains ID which is not needed
            rowData[0] = resultSet.getObject(2);
            rowData[1] = resultSet.getObject(3);
            rowData[2] = resultSet.getObject(4);
            rowData[3] = resultSet.getObject(5);
            rowData[4] = resultSet.getObject(6);
            rowData[5] = resultSet.getObject(7);
            tableModel.addRow(rowData);
          }
          table.setModel(tableModel);

          // set to false so user can't select row, its distracting and not needed
          table.setRowSelectionAllowed(false);
          table.setCellSelectionEnabled(false);

          // change table column widths
          table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          int tableWidth = table.getWidth();
          table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(3).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(4).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(5).setPreferredWidth(tableWidth / 6);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        customerOrdersPopUp(table);
        break;
      case 3: // ITEMS
        try {
          ResultSet resultSet = model.getAllItemsAndIngredients();

          // make table uneditable
          DefaultTableModel tableModel =
              new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                  return false;
                }
              };

          // set columns to name | price | category | ingredients ; and then populate with
          // appropriate data
          tableModel.setColumnIdentifiers(
              new String[] {"name", "price", "category", "ingredients"}); // Set column names
          while (resultSet.next()) {
            Object[] rowData = new Object[4];
            rowData[0] = resultSet.getObject(1);
            rowData[1] = resultSet.getObject(2);
            rowData[2] = resultSet.getObject(3);
            rowData[3] = resultSet.getObject(4);
            tableModel.addRow(rowData);
          }
          table.setModel(tableModel);

          // set to false so user can't select row, its distracting and not needed
          table.setRowSelectionAllowed(false);
          table.setCellSelectionEnabled(false);

          // change table column widths
          table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          int tableWidth = table.getWidth();
          table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 6);
          table.getColumnModel().getColumn(3).setPreferredWidth(tableWidth / 2);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        itemsPopUp(table);
        break;
      case 4: // INGREDIENTS
        try {
          ResultSet resultSet = model.getAllIngredients();

          // make table uneditable
          DefaultTableModel tableModel =
              new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                  return false;
                }
              };

          // set columns to name | current stock | unit price ; and then populate with appropriate
          // data
          tableModel.setColumnIdentifiers(
              new String[] {"name", "current stock", "unit price"}); // Set column names
          while (resultSet.next()) {
            Object[] rowData = new Object[3];
            // don't access first column because it contains ID which is not needed
            rowData[0] = resultSet.getObject(2);
            rowData[1] = resultSet.getObject(3);
            rowData[2] = resultSet.getObject(4);
            tableModel.addRow(rowData);
          }
          table.setModel(tableModel);

          // set to false so user can't select a row, its distracting and not needed
          table.setRowSelectionAllowed(false);

          // change table column widths
          table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          int tableWidth = table.getWidth();
          table.getColumnModel().getColumn(0).setPreferredWidth(tableWidth / 3);
          table.getColumnModel().getColumn(1).setPreferredWidth(tableWidth / 3);
          table.getColumnModel().getColumn(2).setPreferredWidth(tableWidth / 3);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        ingredientsPopUp(table);
        break;
    }
    // updateFontSizes(table, managerScreen.getFrame());
    table.setFont(new Font("Arial", Font.PLAIN, preferredHeight / 65));
  }

  /**
   * Creates a pop up menu for CRUD.
   *
   * @param table is the table to display the result set
   */
  private void usersPopUp(JTable table) {
    // add event listener so rows can be selected
    table.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent me) {
            JTable table = (JTable) me.getSource();
            Point p = me.getPoint();
            int id =
                model.getIDFromRow(
                    "users", "user_id", table.rowAtPoint(p) + 1, table.getRowCount());
            int tableType = 0;
            if (me.getClickCount() == 2 && table.getSelectedRow() != -1) {
              // create a popup menu for CRUD operations
              JPopupMenu popupMenu = new JPopupMenu();

              // create items for menu
              JMenuItem updateItem = new JMenuItem("Update");
              JMenuItem deleteItem = new JMenuItem("Delete");
              JMenuItem createItem = new JMenuItem("Create");

              // add items to menu
              popupMenu.add(updateItem);
              popupMenu.add(deleteItem);
              popupMenu.add(createItem);

              // add action listeners to items
              updateItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog updateUserPopupDialog = new JDialog();
                      updateUserPopupDialog.setTitle("Update User");
                      updateUserPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      updateUserPopupDialog.setLocationRelativeTo(null);

                      // create Container of Dialog's content pane for layout purposes
                      Container updateUserPopup = updateUserPopupDialog.getContentPane();
                      updateUserPopup.setLayout(new BoxLayout(updateUserPopup, BoxLayout.Y_AXIS));

                      // create and collect phoneNumber label and field
                      JPanel phoneNumberPanel = new JPanel();
                      JLabel phoneNumberLabel = new JLabel("User Phone Number: ");
                      JTextField phoneNumberField = new JTextField(10);
                      phoneNumberField.setText(
                          model.getObject("users", "user_id", id, "phonenumber"));
                      phoneNumberPanel.add(phoneNumberLabel);
                      phoneNumberPanel.add(phoneNumberField);

                      // create and collect name label and field
                      JPanel namePanel = new JPanel();
                      JLabel nameLabel = new JLabel("User's Name: ");
                      JTextField nameField = new JTextField(20);
                      nameField.setText(model.getObject("users", "user_id", id, "name"));
                      namePanel.add(nameLabel);
                      namePanel.add(nameField);

                      // create and collect isManager label and field
                      JPanel isManagerPanel = new JPanel();
                      JLabel isManagerLabel = new JLabel("User Manager Status: ");
                      String[] managerStatus = {"true", "false"};
                      JComboBox isManagerComboBox = new JComboBox(managerStatus);
                      boolean isManager =
                          Boolean.parseBoolean(
                              model.getObject("users", "user_id", id, "ismanager"));
                      isManagerComboBox.setSelectedItem(String.valueOf(isManager));
                      isManagerPanel.add(isManagerLabel);
                      isManagerPanel.add(isManagerComboBox);

                      // create commit button that triggers model.updateCustomerOrder() on click and
                      // disposes of dialog
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database and also the table
                              String phoneNumber = phoneNumberField.getText();
                              String name = nameField.getText();
                              boolean isManager =
                                  isManagerComboBox.getSelectedItem().equals("true");
                              model.updateUser(id, phoneNumber, name, isManager);
                              updateUserPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and display the popup
                      updateUserPopup.add(phoneNumberPanel);
                      updateUserPopup.add(namePanel);
                      updateUserPopup.add(isManagerPanel);
                      updateUserPopup.add(commitButton);
                      updateUserPopup.setVisible(true);
                      updateUserPopupDialog.setVisible(true);
                    }
                  });
              deleteItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                      // create a JDialog
                      JDialog deleteUserPopupDialog = new JDialog();
                      deleteUserPopupDialog.setTitle("Delete User");
                      deleteUserPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      deleteUserPopupDialog.setLocationRelativeTo(null);

                      // create Container of Dialog's content pane for layout purposes
                      Container deleteUserPopup = deleteUserPopupDialog.getContentPane();
                      deleteUserPopup.setLayout(new BoxLayout(deleteUserPopup, BoxLayout.Y_AXIS));
                      JLabel deleteUserLabel =
                          new JLabel(
                              "Are you sure you want to delete this user: "
                                  + model.getObject("users", "user_id", id, "name")
                                  + "?");
                      JButton confirmButton = new JButton("Confirm");

                      // TODO : verify query is correct
                      // deletes order, gets rid of dialog, and refreshes table
                      confirmButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              model.deleteUser(id);
                              deleteUserPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add everything and display dialog
                      deleteUserPopup.add(deleteUserLabel);
                      deleteUserPopup.add(confirmButton);
                      deleteUserPopup.setVisible(true);
                      deleteUserPopupDialog.setVisible(true);
                    }
                  });
              createItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog updateUserPopupDialog = new JDialog();
                      updateUserPopupDialog.setTitle("Update User");
                      updateUserPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      updateUserPopupDialog.setLocationRelativeTo(null);

                      // create Container of Dialog's content pane for layout purposes
                      Container updateUserPopup = updateUserPopupDialog.getContentPane();
                      updateUserPopup.setLayout(new BoxLayout(updateUserPopup, BoxLayout.Y_AXIS));

                      // create and collect phoneNumber label and field
                      JPanel phoneNumberPanel = new JPanel();
                      JLabel phoneNumberLabel = new JLabel("User Phone Number: ");
                      JTextField phoneNumberField = new JTextField(10);
                      phoneNumberField.setText("1234567890");
                      phoneNumberPanel.add(phoneNumberLabel);
                      phoneNumberPanel.add(phoneNumberField);

                      // create and collect name label and field
                      JPanel namePanel = new JPanel();
                      JLabel nameLabel = new JLabel("User's Name: ");
                      JTextField nameField = new JTextField(20);
                      nameField.setText("<first name> <last name>");
                      namePanel.add(nameLabel);
                      namePanel.add(nameField);

                      // create and collect isManager label and field
                      JPanel isManagerPanel = new JPanel();
                      JLabel isManagerLabel = new JLabel("User Manager Status: ");
                      String[] managerStatus = {"true", "false"};
                      JComboBox isManagerComboBox = new JComboBox(managerStatus);
                      isManagerComboBox.setSelectedItem("false");
                      isManagerPanel.add(isManagerLabel);
                      isManagerPanel.add(isManagerComboBox);

                      // create commit button that triggers model.updateCustomerOrder() on click and
                      // disposes of dialog
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database and also the table
                              String phoneNumber = phoneNumberField.getText();
                              String name = nameField.getText();
                              boolean isManager =
                                  isManagerComboBox.getSelectedItem().equals("true");
                              model.createUser(phoneNumber, name, isManager);
                              updateUserPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and display the popup
                      updateUserPopup.add(phoneNumberPanel);
                      updateUserPopup.add(namePanel);
                      updateUserPopup.add(isManagerPanel);
                      updateUserPopup.add(commitButton);
                      updateUserPopup.setVisible(true);
                      updateUserPopupDialog.setVisible(true);
                    }
                  });
              popupMenu.show(me.getComponent(), me.getX(), me.getY());
            }
          }
        });
  }

  /**
   * Fetch data from SQL and update the table.
   *
   * @param table is the table to display information
   */
  private void updateOrderTable(JTable table) {
    try {
      ResultSet resultSet = model.getAllIngredients();
      DefaultTableModel tableModel = new DefaultTableModel();
      tableModel.setColumnIdentifiers(
          new String[] {"name", "current stock", "unit price"}); // Set column names

      while (resultSet.next()) {
        Object[] rowData = new Object[3];
        rowData[0] = resultSet.getObject(2);
        rowData[1] = resultSet.getObject(3);
        rowData[2] = resultSet.getObject(4);
        tableModel.addRow(rowData); // add it to table
      }

      table.setModel(tableModel); // Set the updated table model
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Creates a pop up menu for manager CRUD.
   *
   * @param table is the table to display info
   */
  private void managerOrdersPopUp(JTable table) {
    // add event listener so rows can be selected
    table.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent me) {
            JTable table = (JTable) me.getSource();
            Point p = me.getPoint();
            int id =
                model.getIDFromRow(
                    "manager_order",
                    "m_order_id",
                    table.rowAtPoint(p) + 1,
                    table.getRowCount()); // row + 1 because row 0 in Java == row 1 in SQL
            int tableType = 1;

            // if right clicked and not on column header
            if (me.getClickCount() == 2 && table.getSelectedRow() != -1) {
              // create a popup menu for CRUD operations
              JPopupMenu popupMenu = new JPopupMenu();

              // create items for menu
              JMenuItem updateItem = new JMenuItem("Update");
              JMenuItem deleteItem = new JMenuItem("Delete");
              JMenuItem createItem = new JMenuItem("Create");

              // add items to menu
              popupMenu.add(updateItem);
              popupMenu.add(deleteItem);
              popupMenu.add(createItem);

              // add action listeners to items
              updateItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog updateOrderPopupDialog = new JDialog();
                      updateOrderPopupDialog.setTitle("Update Order");
                      updateOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      updateOrderPopupDialog.setLocationRelativeTo(null);

                      // create a Container from the Dialog's content pane for layout purposes
                      Container updateOrderPopup = updateOrderPopupDialog.getContentPane();
                      updateOrderPopup.setLayout(new BoxLayout(updateOrderPopup, BoxLayout.Y_AXIS));

                      // create and collect date label and field
                      JPanel datePanel = new JPanel();
                      JLabel dateLabel = new JLabel("Order Date: ");
                      JTextField dateField = new JTextField(10);
                      dateField.setText(
                          model.getObject("manager_order", "m_order_id", id, "m_order_date"));
                      datePanel.add(dateLabel);
                      datePanel.add(dateField);

                      // create and collect time label and field
                      JPanel timePanel = new JPanel();
                      JLabel timeLabel = new JLabel("Order Time: ");
                      JTextField timeField = new JTextField(12);
                      timeField.setText(
                          model.getObject("manager_order", "m_order_id", id, "m_order_time"));
                      timePanel.add(timeLabel);
                      timePanel.add(timeField);

                      // create and collect total label and field
                      JPanel totalPanel = new JPanel();
                      JLabel totalLabel = new JLabel("Order Total: ");
                      JTextField totalField = new JTextField(7);
                      totalField.setText(
                          model.getObject("manager_order", "m_order_id", id, "m_order_total"));
                      totalPanel.add(totalLabel);
                      totalPanel.add(totalField);

                      // create and collect payment method label and field
                      JPanel phoneNumberPanel = new JPanel();
                      JLabel phoenNumberLabel = new JLabel("Order Phone Number: ");
                      JTextField phoneNumberField = new JTextField(10);
                      phoneNumberField.setText(
                          model.getObject("manager_order", "m_order_id", id, "phonenumber"));
                      phoneNumberPanel.add(phoenNumberLabel);
                      phoneNumberPanel.add(phoneNumberField);

                      // create commit button that triggers model.updateCustomerOrder() on click and
                      // disposes of dialog
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database and also the table
                              String date = dateField.getText();
                              String time = timeField.getText();
                              Double total = Double.parseDouble(totalField.getText());
                              String phoneNumber = phoneNumberField.getText();
                              model.updateManagerOrder(id, date, time, total, phoneNumber);
                              updateOrderPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and display the popup
                      updateOrderPopup.add(datePanel);
                      updateOrderPopup.add(timePanel);
                      updateOrderPopup.add(totalPanel);
                      updateOrderPopup.add(phoneNumberPanel);
                      updateOrderPopup.add(commitButton);
                      updateOrderPopup.setVisible(true);
                      updateOrderPopupDialog.setVisible(true);
                    }
                  });
              deleteItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                      // create a JDialog
                      JDialog deleteOrderPopupDialog = new JDialog();
                      deleteOrderPopupDialog.setTitle("Delete Order");
                      deleteOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      deleteOrderPopupDialog.setLocationRelativeTo(null);

                      // create Container of Dialog's content pane for layout purposes
                      Container deleteOrderPopup = deleteOrderPopupDialog.getContentPane();
                      deleteOrderPopup.setLayout(new BoxLayout(deleteOrderPopup, BoxLayout.Y_AXIS));

                      JLabel deleteOrderLabel =
                          new JLabel("Are you sure you want to delete this order?");
                      JButton confirmButton = new JButton("Confirm");

                      // TODO : verify query is correct
                      // deletes order, gets rid of dialog, and refreshes table
                      confirmButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              model.deleteManagerOrder(id);
                              deleteOrderPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add everything and display dialog
                      deleteOrderPopup.add(deleteOrderLabel);
                      deleteOrderPopup.add(confirmButton);
                      deleteOrderPopup.setVisible(true);
                      deleteOrderPopupDialog.setVisible(true);
                    }
                  });
              createItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog createOrderPopupDialog = new JDialog();
                      createOrderPopupDialog.setTitle("Update Order");
                      createOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      createOrderPopupDialog.setLocationRelativeTo(null);

                      // create a Container from the Dialog's content pane for layout purposes
                      Container createOrderPopup = createOrderPopupDialog.getContentPane();
                      createOrderPopup.setLayout(new BoxLayout(createOrderPopup, BoxLayout.Y_AXIS));

                      // create and collect date label and field
                      JPanel datePanel = new JPanel();
                      JLabel dateLabel = new JLabel("Order Date: ");
                      JTextField dateField = new JTextField(10);
                      dateField.setText("YYYY-MM-DD");
                      datePanel.add(dateLabel);
                      datePanel.add(dateField);

                      // create and collect time label and field
                      JPanel timePanel = new JPanel();
                      JLabel timeLabel = new JLabel("Order Time: ");
                      JTextField timeField = new JTextField(12);
                      timeField.setText("HH:MM:SS.sss");
                      timePanel.add(timeLabel);
                      timePanel.add(timeField);

                      // create and collect total label and field
                      JPanel totalPanel = new JPanel();
                      JLabel totalLabel = new JLabel("Order Total: ");
                      JTextField totalField = new JTextField(7);
                      totalField.setText("0.00");
                      totalPanel.add(totalLabel);
                      totalPanel.add(totalField);

                      // create and collect payment method label and field
                      JPanel phoneNumberPanel = new JPanel();
                      JLabel phoenNumberLabel = new JLabel("Order Phone Number: ");
                      JTextField phoneNumberField = new JTextField(10);
                      phoneNumberField.setText("1234567890");
                      phoneNumberPanel.add(phoenNumberLabel);
                      phoneNumberPanel.add(phoneNumberField);

                      // create commit button that triggers model.updateCustomerOrder() on click and
                      // disposes of dialog
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database and also the table
                              String date = dateField.getText();
                              String time = timeField.getText();
                              Double total = Double.parseDouble(totalField.getText());
                              String phoneNumber = phoneNumberField.getText();
                              model.createManagerOrder(date, time, total, phoneNumber);
                              createOrderPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and display the popup
                      createOrderPopup.add(datePanel);
                      createOrderPopup.add(timePanel);
                      createOrderPopup.add(totalPanel);
                      createOrderPopup.add(phoneNumberPanel);
                      createOrderPopup.add(commitButton);
                      createOrderPopup.setVisible(true);
                      createOrderPopupDialog.setVisible(true);
                    }
                  });
              popupMenu.show(me.getComponent(), me.getX(), me.getY());
            }
          }
        });
  }

  /**
   * Creates a pop up menu for customer CRUD.
   *
   * @param table is the table to display info
   */
  private void customerOrdersPopUp(JTable table) {
    // add event listener so rows can be selected
    table.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent me) {
            JTable table = (JTable) me.getSource();
            Point p = me.getPoint();
            int id =
                model.getIDFromRow(
                    "customer_order",
                    "c_order_id",
                    table.rowAtPoint(p) + 1,
                    table.getRowCount()); // row + 1 because row 0 in Java == row 1 in SQL
            int tableType = 2;

            // if right clicked and not on column header
            if (me.getClickCount() == 2 && table.getSelectedRow() != -1) {
              // create a popup menu for CRUD operations
              JPopupMenu popupMenu = new JPopupMenu();

              // create items for menu
              JMenuItem updateItem = new JMenuItem("Update");
              JMenuItem deleteItem = new JMenuItem("Delete");
              JMenuItem createItem = new JMenuItem("Create");

              // add items to menu
              popupMenu.add(updateItem);
              popupMenu.add(deleteItem);
              popupMenu.add(createItem);

              // add action listeners to items
              updateItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog updateOrderPopupDialog = new JDialog();
                      updateOrderPopupDialog.setTitle("Update Order");
                      updateOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      updateOrderPopupDialog.setLocationRelativeTo(null);

                      // create a Container from the Dialog's content pane for layout purposes
                      Container updateOrderPopup = updateOrderPopupDialog.getContentPane();
                      updateOrderPopup.setLayout(new BoxLayout(updateOrderPopup, BoxLayout.Y_AXIS));

                      // create and collect date label and field
                      JPanel datePanel = new JPanel();
                      JLabel dateLabel = new JLabel("Order Date: ");
                      JTextField dateField = new JTextField(10);
                      dateField.setText(
                          model.getObject("customer_order", "c_order_id", id, "c_order_date"));
                      datePanel.add(dateLabel);
                      datePanel.add(dateField);

                      // create and collect time label and field
                      JPanel timePanel = new JPanel();
                      JLabel timeLabel = new JLabel("Order Time: ");
                      JTextField timeField = new JTextField(12);
                      timeField.setText(
                          model.getObject("customer_order", "c_order_id", id, "c_order_time"));
                      timePanel.add(timeLabel);
                      timePanel.add(timeField);

                      // create and collect subtotal label and field
                      JPanel subtotalPanel = new JPanel();
                      JLabel subtotalLabel = new JLabel("Order Subtotal: ");
                      JTextField subtotalField = new JTextField(7);
                      subtotalField.setText(
                          model.getObject("customer_order", "c_order_id", id, "c_order_subtotal"));
                      subtotalPanel.add(subtotalLabel);
                      subtotalPanel.add(subtotalField);

                      // create and collect tax label and field
                      JPanel taxPanel = new JPanel();
                      JLabel taxLabel = new JLabel("Order Tax: ");
                      JTextField taxField = new JTextField(6);
                      taxField.setText(
                          model.getObject("customer_order", "c_order_id", id, "c_order_tax"));
                      taxPanel.add(taxLabel);
                      taxPanel.add(taxField);

                      // create and collect total label and field
                      JPanel totalPanel = new JPanel();
                      JLabel totalLabel = new JLabel("Order Total: ");
                      JTextField totalField = new JTextField(7);
                      totalField.setText(
                          model.getObject("customer_order", "c_order_id", id, "c_order_total"));
                      totalPanel.add(totalLabel);
                      totalPanel.add(totalField);

                      // create and collect payment method label and field
                      JPanel paymentMethodPanel = new JPanel();
                      JLabel paymentMethodLabel = new JLabel("Payment Method: ");
                      JTextField paymentMethodField = new JTextField(10);
                      paymentMethodField.setText(
                          model.getObject(
                              "customer_order", "c_order_id", id, "c_order_payment_type"));
                      paymentMethodPanel.add(paymentMethodLabel);
                      paymentMethodPanel.add(paymentMethodField);

                      // create commit button that triggers model.updateCustomerOrder() on click and
                      // disposes of dialog
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database and also the table
                              String date = dateField.getText();
                              String time = timeField.getText();
                              String subtotal = subtotalField.getText();
                              String tax = taxField.getText();
                              String total = totalField.getText();
                              String paymentMethod = paymentMethodField.getText();
                              model.updateCustomerOrder(
                                  id, date, time, subtotal, tax, total, paymentMethod);
                              updateOrderPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and display the popup
                      updateOrderPopup.add(datePanel);
                      updateOrderPopup.add(timePanel);
                      updateOrderPopup.add(subtotalPanel);
                      updateOrderPopup.add(taxPanel);
                      updateOrderPopup.add(totalPanel);
                      updateOrderPopup.add(paymentMethodPanel);
                      updateOrderPopup.add(commitButton);
                      updateOrderPopup.setVisible(true);
                      updateOrderPopupDialog.setVisible(true);
                    }
                  });
              deleteItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                      // create a JDialog
                      JDialog deleteOrderPopupDialog = new JDialog();
                      deleteOrderPopupDialog.setTitle("Delete Order");
                      deleteOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      deleteOrderPopupDialog.setLocationRelativeTo(null);

                      // create Container of Dialog's content pane for layout purposes
                      Container deleteOrderPopup = deleteOrderPopupDialog.getContentPane();
                      deleteOrderPopup.setLayout(new BoxLayout(deleteOrderPopup, BoxLayout.Y_AXIS));

                      JLabel deleteOrderLabel =
                          new JLabel("Are you sure you want to delete this order?");
                      JButton confirmButton = new JButton("Confirm");

                      // TODO : verify query is correct
                      // deletes order, gets rid of dialog, and refreshes table
                      confirmButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              model.deleteCustomerOrder(id);
                              deleteOrderPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add everything and display dialog
                      deleteOrderPopup.add(deleteOrderLabel);
                      deleteOrderPopup.add(confirmButton);
                      deleteOrderPopup.setVisible(true);
                      deleteOrderPopupDialog.setVisible(true);
                    }
                  });
              createItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a popup to create the order
                      JDialog createOrderPopupDialog = new JDialog();
                      createOrderPopupDialog.setTitle("Create Order");
                      createOrderPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      createOrderPopupDialog.setLocationRelativeTo(null);

                      // create a Container of Dialog's content pane for layout purposes
                      Container createOrderPopup = createOrderPopupDialog.getContentPane();
                      createOrderPopup.setLayout(new BoxLayout(createOrderPopup, BoxLayout.Y_AXIS));

                      // create and collect date label and field
                      JPanel datePanel = new JPanel();
                      JLabel dateLabel = new JLabel("Order Date: ");
                      JTextField dateField = new JTextField(10);
                      dateField.setText("YYYY-MM-DD");
                      datePanel.add(dateLabel);
                      datePanel.add(dateField);

                      // create and collect time label and field
                      JPanel timePanel = new JPanel();
                      JLabel timeLabel = new JLabel("Order Time: ");
                      JTextField timeField = new JTextField(12);
                      timeField.setText("HH:MM:SS.SSS");
                      timePanel.add(timeLabel);
                      timePanel.add(timeField);

                      // create and collect sybtotal label and field
                      JPanel subtotalPanel = new JPanel();
                      JLabel subtotalLabel = new JLabel("Order Subtotal: ");
                      JTextField subtotalField = new JTextField(7);
                      subtotalField.setText("0.00");
                      subtotalPanel.add(subtotalLabel);
                      subtotalPanel.add(subtotalField);

                      // create and collect tax label and field
                      JPanel taxPanel = new JPanel();
                      JLabel taxLabel = new JLabel("Order Tax: ");
                      JTextField taxField = new JTextField(6);
                      taxField.setText("0.00");
                      taxPanel.add(taxLabel);
                      taxPanel.add(taxField);

                      // create and collect total label and field
                      JPanel totalPanel = new JPanel();
                      JLabel totalLabel = new JLabel("Order Total: ");
                      JTextField totalField = new JTextField(7);
                      totalField.setText("0.00");
                      totalPanel.add(totalLabel);
                      totalPanel.add(totalField);

                      // create and collect payemnt method label and field
                      JPanel paymentMethodPanel = new JPanel();
                      JLabel paymentMethodLabel = new JLabel("Payment Method: ");
                      JTextField paymentMethodField = new JTextField(10);
                      paymentMethodField.setText("cash or credit");
                      paymentMethodPanel.add(paymentMethodLabel);
                      paymentMethodPanel.add(paymentMethodField);

                      // create commit button, update database, and update table
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database
                              String date = dateField.getText();
                              String time = timeField.getText();
                              String subtotal = subtotalField.getText();
                              String tax = taxField.getText();
                              String total = totalField.getText();
                              String paymentMethod = paymentMethodField.getText();
                              model.createCustomerOrder(
                                  date, time, subtotal, tax, total, paymentMethod);
                              createOrderPopupDialog.dispose();
                            }
                          });

                      // add all panels to the popup and show it
                      createOrderPopup.add(datePanel);
                      createOrderPopup.add(timePanel);
                      createOrderPopup.add(subtotalPanel);
                      createOrderPopup.add(taxPanel);
                      createOrderPopup.add(totalPanel);
                      createOrderPopup.add(paymentMethodPanel);
                      createOrderPopup.add(commitButton);
                      createOrderPopup.setVisible(true);
                      createOrderPopupDialog.setVisible(true);
                    }
                  });
              popupMenu.show(me.getComponent(), me.getX(), me.getY());
            }
          }
        });
  }

  /**
   * Creates a pop up menu for items CRUD.
   *
   * @param table is the table to display info
   */
  private void itemsPopUp(JTable table) {
    // add event listener so rows can be selected
    table.addMouseListener(
        new MouseAdapter() {
          public void mousePressed(MouseEvent me) {
            JTable table = (JTable) me.getSource();
            Point p = me.getPoint();
            int id =
                model.getIDFromRow(
                    "menu_items", "item_id", table.rowAtPoint(p) + 1, table.getRowCount());

            int tableType = 3;

            // if right clicked and not in column header
            if (me.getClickCount() == 2 && table.getSelectedRow() != -1) {
              // create a popup menu for CRUD operations
              JPopupMenu popupMenu = new JPopupMenu();

              // create items for menu
              JMenuItem updateItem = new JMenuItem("Update");
              JMenuItem deleteItem = new JMenuItem("Delete");
              JMenuItem createItem = new JMenuItem("Create");

              // add items to menu
              popupMenu.add(updateItem);
              popupMenu.add(deleteItem);
              popupMenu.add(createItem);

              // add action listeners to items
              updateItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog updateItemPopupDialog = new JDialog();
                      updateItemPopupDialog.setTitle("Update Item");
                      updateItemPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      updateItemPopupDialog.setLocationRelativeTo(null);

                      // create a Container of Dialog's content pane for layout purposes
                      Container updateItemPopup = updateItemPopupDialog.getContentPane();
                      updateItemPopup.setLayout(new BoxLayout(updateItemPopup, BoxLayout.Y_AXIS));

                      // create and collect name label and field
                      JPanel namePanel = new JPanel();
                      JLabel nameLabel = new JLabel("Item Name: ");
                      JTextField nameField =
                          new JTextField(model.getObject("menu_items", "item_id", id, "item_name"));
                      namePanel.add(nameLabel);
                      namePanel.add(nameField);

                      // create and collect price label and field
                      JPanel pricePanel = new JPanel();
                      JLabel priceLabel = new JLabel("Item Price: ");
                      JTextField priceField =
                          new JTextField(
                              model.getObject("menu_items", "item_id", id, "item_price"));
                      pricePanel.add(priceLabel);
                      pricePanel.add(priceField);

                      // create and collect category label and ComboBox
                      JPanel categoryPanel = new JPanel();
                      JLabel categoryLabel = new JLabel("Item Category: ");
                      String[] categoryDropDown = {
                        "Choose An Option",
                        "Appetizers",
                        "Beverages",
                        "Burgers",
                        "Limited Time Offer",
                        "Salads",
                        "Sandwiches",
                        "Shakes & More",
                        "Value Meals"
                      };
                      JComboBox categoryComboBox = new JComboBox(categoryDropDown);
                      categoryComboBox.setSelectedItem(
                          model.getObject("menu_items", "item_id", id, "category"));
                      categoryPanel.add(categoryLabel);
                      categoryPanel.add(categoryComboBox);

                      // create and collect initial date and ending date
                      JPanel datePanel = new JPanel();
                      JLabel startDate = new JLabel("Start Date: ");
                      JTextField startDateField =
                          new JTextField(
                              model.getObject("menu_items", "item_id", id, "lower(date_range)"));
                      JLabel endDate = new JLabel("End Date: ");
                      JTextField endDateField =
                          new JTextField(
                              model.getObject("menu_items", "item_id", id, "upper(date_range)"));
                      datePanel.add(startDate);
                      datePanel.add(startDateField);
                      datePanel.add(endDate);
                      datePanel.add(endDateField);

                      JPanel descriptionPanel = new JPanel();
                      JTextArea description =
                          new JTextArea("Please enter dates in the following format: YYYY-MM-DD");
                      description.setEditable(false);
                      descriptionPanel.add(description);

                      // create a button to commit the new item to the database, dispose of dialog,
                      // and update table
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database and also the table
                              boolean flagDate;
                              String startDateString = "";
                              String endDateString = "";
                              String name = nameField.getText();
                              Double price = Double.parseDouble(priceField.getText());
                              String category = categoryComboBox.getSelectedItem().toString();
                              if (startDateField.getText().trim().isEmpty()
                                  || endDateField
                                      .getText()
                                      .trim()
                                      .isEmpty()) { // If either field is empty don't add to the
                                // date_range column
                                flagDate = false;
                              } else {
                                flagDate = true;
                                startDateString = startDateField.getText();
                                endDateString = endDateField.getText();
                              }
                              model.updateItem(
                                  id,
                                  name,
                                  price,
                                  category,
                                  startDateString,
                                  endDateString,
                                  flagDate);
                              updateItemPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and make it visible
                      updateItemPopup.add(namePanel);
                      updateItemPopup.add(pricePanel);
                      updateItemPopup.add(categoryPanel);
                      updateItemPopup.add(datePanel);
                      updateItemPopup.add(descriptionPanel);
                      updateItemPopup.add(commitButton);
                      updateItemPopup.setVisible(true);
                      updateItemPopupDialog.setVisible(true);
                    }
                  });
              deleteItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog deleteItemPopupDialog = new JDialog();
                      deleteItemPopupDialog.setTitle("Delete Item");
                      deleteItemPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      deleteItemPopupDialog.setLocationRelativeTo(null);

                      // create Container of Dialog's content pane for layout purposes
                      Container deleteItemPopup = deleteItemPopupDialog.getContentPane();
                      deleteItemPopup.setLayout(new BoxLayout(deleteItemPopup, BoxLayout.Y_AXIS));

                      JLabel deleteItemLabel =
                          new JLabel("Are you sure you want to delete this item?");
                      JButton confirmButton = new JButton("Confirm");

                      // TODO : verify query is correct
                      // deletes order, gets rid of dialog, and refreshes table
                      confirmButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              model.deleteItem(id);
                              deleteItemPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add everything and display dialog
                      deleteItemPopup.add(deleteItemLabel);
                      deleteItemPopup.add(confirmButton);
                      deleteItemPopup.setVisible(true);
                      deleteItemPopupDialog.setVisible(true);
                    }
                  });
              createItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog createItemPopupDialog = new JDialog();
                      createItemPopupDialog.setTitle("Create Item");
                      createItemPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      createItemPopupDialog.setLocationRelativeTo(null);

                      // create a Container of Dialog's content pane for layout purposes
                      Container createItemPopup = createItemPopupDialog.getContentPane();
                      createItemPopup.setLayout(new BoxLayout(createItemPopup, BoxLayout.Y_AXIS));

                      // create and collect name label and field
                      JPanel namePanel = new JPanel();
                      JLabel nameLabel = new JLabel("Item Name: ");
                      JTextField nameField = new JTextField(20);
                      namePanel.add(nameLabel);
                      namePanel.add(nameField);

                      // create and collect price label and field
                      JPanel pricePanel = new JPanel();
                      JLabel priceLabel = new JLabel("Item Price: ");
                      JTextField priceField = new JTextField(7);
                      pricePanel.add(priceLabel);
                      pricePanel.add(priceField);

                      // create and collect category label and ComboBox
                      JPanel categoryPanel = new JPanel();
                      JLabel categoryLabel = new JLabel("Item Category: ");
                      String[] categoryDropDown = {
                        "Choose An Option",
                        "Appetizers",
                        "Beverages",
                        "Burgers",
                        "Limited Time Offer",
                        "Salads",
                        "Sandwiches",
                        "Shakes & More",
                        "Value Meals"
                      };
                      JComboBox categoryBox = new JComboBox(categoryDropDown);
                      categoryPanel.add(categoryLabel);
                      categoryPanel.add(categoryBox);

                      JPanel datePanel = new JPanel();
                      JLabel startDate = new JLabel("Start Date: ");
                      JTextField startDateField = new JTextField(10);
                      JLabel endDate = new JLabel("End Date: ");
                      JTextField endDateField = new JTextField(10);
                      datePanel.add(startDate);
                      datePanel.add(startDateField);
                      datePanel.add(endDate);
                      datePanel.add(endDateField);

                      JPanel descriptionPanel = new JPanel();
                      JTextArea dateDescription =
                          new JTextArea("Please enter dates in the following format: YYYY-MM-DD");
                      dateDescription.setEditable(false);
                      descriptionPanel.add(dateDescription);

                      // create a button to commit the new item to the database
                      JButton commitButton = new JButton("Commit");

                      // add all panels to the popup and make it visible
                      createItemPopup.add(namePanel);
                      createItemPopup.add(pricePanel);
                      createItemPopup.add(categoryPanel);
                      createItemPopup.add(datePanel);
                      createItemPopup.add(descriptionPanel);
                      createItemPopup.add(commitButton);
                      createItemPopup.setVisible(true);
                      createItemPopupDialog.setVisible(true);

                      // set up frame for ingredient syncing for junction table

                      // window for associated ingredients with a new menu item
                      JFrame attachIngredientsFrame = new JFrame();
                      attachIngredientsFrame.setLayout(
                          new BoxLayout(
                              attachIngredientsFrame.getContentPane(), BoxLayout.PAGE_AXIS));
                      attachIngredientsFrame.setSize(preferredWidth / 2, preferredHeight / 3);
                      attachIngredientsFrame.setLocationRelativeTo(null);
                      attachIngredientsFrame.setTitle("Inventory Association");

                      ResultSet rs = model.getAllIngredients();
                      Vector<String> ingredientNames = null;

                      try {
                        ingredientNames = populateVector(rs);
                      } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                            null, "Error executing SQL query: " + ex.getMessage());
                      }

                      // create and collect ingredient label and ComboBox
                      JComboBox ingredientComboBox = new JComboBox(ingredientNames);
                      JPanel panelIngredientList = new JPanel();
                      JTextField ingredientList = new JTextField(25);
                      panelIngredientList.add(new JLabel("Inventory List:"));
                      panelIngredientList.add(ingredientList);
                      panelIngredientList.add(ingredientComboBox);

                      // create and collect quantity label and field
                      JPanel panelQuantityList = new JPanel();
                      JTextField quantityList = new JTextField(25);
                      panelQuantityList.add(new JLabel("Quantity List:"));
                      panelQuantityList.add(quantityList);

                      // create confirmation button
                      JPanel panelButton = new JPanel();
                      JButton btnConfirmInventoryAttachment = new JButton("Confirm");
                      panelButton.add(btnConfirmInventoryAttachment);

                      // create description
                      JPanel panelDescription = new JPanel();
                      JTextArea description =
                          new JTextArea(
                              "1) Make sure the ingredients/inventory are already added\n"
                                  + "2) Enter an ingredient only once and in the following format:"
                                  + " Grilled Chicken, Hot Dog Bun, Red Onion, \n"
                                  + "3) Enter quantities in the same format: 3, 2, 4, ");
                      description.setEditable(false);
                      panelDescription.add(description);

                      // TODO : explain
                      AtomicInteger itemID = new AtomicInteger();
                      // make commit button cause ingredient checking phase
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                              boolean flagDate;
                              String startDateString = "";
                              String endDateString = "";
                              String name = nameField.getText();
                              Double price = Double.parseDouble(priceField.getText());
                              String category = categoryBox.getSelectedItem().toString();
                              if (startDateField.getText().trim().isEmpty()
                                  || endDateField
                                      .getText()
                                      .trim()
                                      .isEmpty()) { // If either field is empty don't add to the
                                // date_range column
                                flagDate = false;
                              } else {
                                flagDate = true;
                                startDateString = startDateField.getText();
                                endDateString = endDateField.getText();
                              }

                              // if we can add the item then display the popup and update the table
                              if (model.addNewItem(
                                  name,
                                  price,
                                  category,
                                  startDateString,
                                  endDateString,
                                  flagDate)) {
                                itemID.set(model.getItemID(name));
                                attachIngredientsFrame.getContentPane().add(panelIngredientList);
                                attachIngredientsFrame.getContentPane().add(panelQuantityList);
                                attachIngredientsFrame.getContentPane().add(panelButton);
                                attachIngredientsFrame.getContentPane().add(panelDescription);
                                attachIngredientsFrame.setVisible(true);
                                viewTable(table, 3);
                              } else {
                                JOptionPane.showMessageDialog(null, "Unable to add menu item");
                              }
                            }
                          });

                      // makes text show up in comma-separated list
                      ingredientComboBox.addActionListener(
                          event -> {
                            String currentText = ingredientList.getText();
                            ingredientList.setText(
                                currentText + ingredientComboBox.getSelectedItem() + ", ");
                          });

                      // make sure both ingredients and their quantities have the same length and
                      // then hide the popup and update the table
                      btnConfirmInventoryAttachment.addActionListener(
                          event -> {
                            String[] associatedIngredients = ingredientList.getText().split(", ");
                            String[] quantities = quantityList.getText().split(", ");
                            if (quantities.length != associatedIngredients.length) {
                              JOptionPane.showMessageDialog(
                                  null, "Number of Quantities and Ingredients don't match");
                              return;
                            }
                            try {
                              if (model.attachAssociatedInventoryToNewItem(
                                  itemID.get(), associatedIngredients, quantities)) {
                                JOptionPane.showMessageDialog(null, "New menu item added");
                              } else {
                                JOptionPane.showMessageDialog(
                                    null, "Unable to attach the selected ingredients");
                                return;
                              }
                              attachIngredientsFrame.dispose();
                            } catch (SQLException ex) {
                              throw new RuntimeException(ex);
                            }
                            viewTable(table, 3);
                            createItemPopupDialog.dispose();
                          });
                    }
                  });
              popupMenu.show(me.getComponent(), me.getX(), me.getY());
            }
          }
        });
  }

  /**
   * Creates a pop up menu for ingredients CRUD.
   *
   * @param table is the table to display info
   */
  private void ingredientsPopUp(JTable table) {
    // add event listener so rows can be selected
    table.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent me) {
            JTable table = (JTable) me.getSource();
            Point p = me.getPoint();
            int id =
                model.getIDFromRow(
                    "ingredients",
                    "ingredient_id",
                    table.rowAtPoint(p) + 1,
                    table.getRowCount()); // row + 1 becaues row 0 in Java == row 1 in SQL
            int tableType = 4;

            // if right clicked and not column header
            if (me.getClickCount() == 2 && table.getSelectedRow() != -1) {
              // create a popup menu for CRUD operations
              JPopupMenu popupMenu = new JPopupMenu();

              // create items for menu
              JMenuItem updateItem = new JMenuItem("Update");
              JMenuItem deleteItem = new JMenuItem("Delete");
              JMenuItem createItem = new JMenuItem("Create");

              // add items to menu
              popupMenu.add(updateItem);
              popupMenu.add(deleteItem);
              popupMenu.add(createItem);

              // add action listeners to items
              updateItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog updateIngredientPopupDialog = new JDialog();
                      updateIngredientPopupDialog.setTitle("Update Ingredient");
                      updateIngredientPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      updateIngredientPopupDialog.setLocationRelativeTo(null);

                      // create a Container of Dialog's content pane for layout purposes
                      Container updateIngredientPopup =
                          updateIngredientPopupDialog.getContentPane();
                      updateIngredientPopup.setLayout(
                          new BoxLayout(updateIngredientPopup, BoxLayout.Y_AXIS));

                      // create and collect name label and field
                      JPanel namePanel = new JPanel();
                      JLabel nameLabel = new JLabel("Ingredient Name: ");
                      JTextField nameField =
                          new JTextField(
                              model.getObject(
                                  "ingredients", "ingredient_id", id, "ingredient_name"));
                      namePanel.add(nameLabel);
                      namePanel.add(nameField);

                      // create and collect current stock label and field
                      JPanel currentStockPanel = new JPanel();
                      JLabel currentStockLabel = new JLabel("Ingredient Current Stock: ");
                      String previousStock =
                          model.getObject(
                              "ingredients", "ingredient_id", id, "ingredient_current_stock");
                      JTextField currentStockField = new JTextField(previousStock);
                      currentStockPanel.add(currentStockLabel);
                      currentStockPanel.add(currentStockField);

                      // create and collect unit price label and ComboBox
                      JPanel unitPricePanel = new JPanel();
                      JLabel unitPriceLabel = new JLabel("Ingredient Unit Price: ");
                      JTextField unitPriceField =
                          new JTextField(
                              model.getObject(
                                  "ingredients", "ingredient_id", id, "ingredient_unit_price"));
                      unitPricePanel.add(unitPriceLabel);
                      unitPricePanel.add(unitPriceField);

                      // create a button to commit the new ingredient to the database, dispose of
                      // dialog, and update table
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the ingredient in the database and also the table
                              String name = nameField.getText();
                              Integer currentStock = Integer.parseInt(currentStockField.getText());
                              Double unitPrice = Double.parseDouble(unitPriceField.getText());
                              model.updateIngredient(
                                  id,
                                  name,
                                  currentStock,
                                  unitPrice,
                                  (previousStock != currentStockField.getText()),
                                  phoneNumber);
                              updateIngredientPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and make it visible
                      updateIngredientPopup.add(namePanel);
                      updateIngredientPopup.add(currentStockPanel);
                      updateIngredientPopup.add(unitPricePanel);
                      updateIngredientPopup.add(commitButton);
                      updateIngredientPopup.setVisible(true);
                      updateIngredientPopupDialog.setVisible(true);
                    }
                  });
              deleteItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog deleteIngredientsPopupDialog = new JDialog();
                      deleteIngredientsPopupDialog.setTitle("Delete Ingredient");
                      deleteIngredientsPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      deleteIngredientsPopupDialog.setLocationRelativeTo(null);

                      // create Container of Dialog's content pane for layout purposes
                      Container deleteIngredientsPopup =
                          deleteIngredientsPopupDialog.getContentPane();
                      deleteIngredientsPopup.setLayout(
                          new BoxLayout(deleteIngredientsPopup, BoxLayout.Y_AXIS));

                      JLabel deleteIngredientsLabel =
                          new JLabel("Are you sure you want to delete this order?");
                      JButton confirmButton = new JButton("Confirm");

                      // TODO : verify query is correct
                      // deletes order, gets rid of dialog, and refreshes table
                      confirmButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              model.deleteIngredient(id);
                              deleteIngredientsPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add everything and display dialog
                      deleteIngredientsPopup.add(deleteIngredientsLabel);
                      deleteIngredientsPopup.add(confirmButton);
                      deleteIngredientsPopup.setVisible(true);
                      deleteIngredientsPopupDialog.setVisible(true);
                    }
                  });
              createItem.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      // create a JDialog
                      JDialog createIngredientPopupDialog = new JDialog();
                      createIngredientPopupDialog.setTitle("Update Ingredient");
                      createIngredientPopupDialog.setSize(preferredWidth / 3, preferredHeight / 3);
                      createIngredientPopupDialog.setLocationRelativeTo(null);

                      // create a Container of Dialog's content pane for layout purposes
                      Container createIngredientPopup =
                          createIngredientPopupDialog.getContentPane();
                      createIngredientPopup.setLayout(
                          new BoxLayout(createIngredientPopup, BoxLayout.Y_AXIS));

                      // create and collect name label and field
                      JPanel namePanel = new JPanel();
                      JLabel nameLabel = new JLabel("Ingredient Name: ");
                      JTextField nameField = new JTextField(20);
                      namePanel.add(nameLabel);
                      namePanel.add(nameField);

                      // create and collect current stock label and field
                      JPanel currentStockPanel = new JPanel();
                      JLabel currentStockLabel = new JLabel("Ingredient Current Stock: ");
                      JTextField currentStockField = new JTextField(7);
                      currentStockPanel.add(currentStockLabel);
                      currentStockPanel.add(currentStockField);

                      // create and collect unit price label and ComboBox
                      JPanel unitPricePanel = new JPanel();
                      JLabel unitPriceLabel = new JLabel("Item Unit Price: ");
                      JTextField unitPriceField = new JTextField(7);
                      unitPricePanel.add(unitPriceLabel);
                      unitPricePanel.add(unitPriceField);

                      // create a button to commit the new item to the database, dispose of dialog,
                      // and update table
                      JButton commitButton = new JButton("Commit");
                      commitButton.addActionListener(
                          new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                              // update the order in the database and also the table
                              String name = nameField.getText();
                              Integer currentStock = Integer.parseInt(currentStockField.getText());
                              Double unitPrice = Double.parseDouble(unitPriceField.getText());
                              model.createIngredient(name, currentStock, unitPrice);
                              createIngredientPopupDialog.dispose();
                              viewTable(table, tableType);
                            }
                          });

                      // add all panels to the popup and make it visible
                      createIngredientPopup.add(namePanel);
                      createIngredientPopup.add(currentStockPanel);
                      createIngredientPopup.add(unitPricePanel);
                      createIngredientPopup.add(commitButton);
                      createIngredientPopup.setVisible(true);
                      createIngredientPopupDialog.setVisible(true);
                    }
                  });
              popupMenu.show(me.getComponent(), me.getX(), me.getY());
            }
          }
        });
  }

  /**
   * Populates a vector with the ingredient names from the result set.
   *
   * @param rs is the result set
   * @return a vector of ingredient names
   * @throws SQLException if there is an error with the SQL query
   */
  private Vector<String> populateVector(ResultSet rs) throws SQLException {
    Vector<String> ingredientList = new Vector<>();
    while (rs.next()) {
      ingredientList.add(rs.getString(2));
    }

    return ingredientList;
  }

  /**
   * Checks if today's date is within a given date range.
   *
   * @param dateRange is the date range to check
   * @return true if today's date is within the date range, false otherwise
   */
  public static boolean isTodayWithinDateRange(String dateRange) {
    // Parse the date range string
    String[] dateRangeParts = dateRange.substring(1, dateRange.length() - 1).split(",");
    String startDateString = dateRangeParts[0].trim();
    String endDateString = dateRangeParts[1].trim();

    // Convert to LocalDate objects
    LocalDate startDate = LocalDate.parse(startDateString, DateTimeFormatter.ISO_DATE);
    LocalDate endDate = LocalDate.parse(endDateString, DateTimeFormatter.ISO_DATE);

    // Get today's date
    LocalDate today = LocalDate.now();

    // Check if today is within the date range
    return !today.isBefore(startDate) && today.isBefore(endDate);
  }

  /**
   * @param e
   */
  /* ------------------------------------------ MISCELLANEOUS -------------------------------------- */

  /**
   * used to simply override the abstract method in the ActionListener interface.
   *
   * @param e is the action event
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // to override the abstract method
  }
}
