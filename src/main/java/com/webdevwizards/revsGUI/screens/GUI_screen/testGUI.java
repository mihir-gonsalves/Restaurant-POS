import javax.swing.*;
import java.awt.*;

public class testGUI {
    public static ImageIcon resizeIcon(String iconPath, int width, int height) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Point of Sale");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Left panel for categories
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new GridLayout(7, 1)); // Changed to GridLayout for icons
            // Add icons for each category
            leftPanel.setPreferredSize(new Dimension(100, 600));
            for (int i = 0; i <= 6; i++) {
               // leftPanel.add(new JButton(new ImageIcon("icon_" + i + ".png")));
               leftPanel.add(new JButton(resizeIcon("icon_" + i + ".png", 70, 70)));
            }

            // Middle panel for items
            JPanel middlePanel = new JPanel(new BorderLayout()); // Changed to BorderLayout
            middlePanel.setPreferredSize(new Dimension(600, 600));
            
            // Added search bar at the top
            JTextField searchBar = new JTextField("Enter an item name to search");
            middlePanel.add(searchBar, BorderLayout.NORTH);
            
            // Changed layout to GridLayout for items 

JPanel itemsPanel = new JPanel(new GridLayout(3, 3)); // Increase grid size as needed

// Wrap itemsPanel in a JScrollPane
JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);

middlePanel.add(itemsScrollPane, BorderLayout.CENTER); // Changed this line to add the scroll pane instead of itemsPanel directly
            // Create a panel for each item (only add items to some cells)
            JPanel itemPanel1 = new JPanel(new BorderLayout());
            JButton itemButton1 = new JButton(new ImageIcon("burger_1.png"));
            itemPanel1.add(itemButton1, BorderLayout.CENTER);
            JLabel itemName1 = new JLabel("Cheeseburger", SwingConstants.CENTER);
            itemPanel1.add(itemName1, BorderLayout.SOUTH);
            itemsPanel.add(itemPanel1);
            
            JPanel itemPanel2 = new JPanel(new BorderLayout());
            JButton itemButton2 = new JButton(new ImageIcon("patty_melt.png"));
            itemPanel2.add(itemButton2, BorderLayout.CENTER);
            JLabel itemName2 = new JLabel("Patty Melt", SwingConstants.CENTER);
            itemPanel2.add(itemName2, BorderLayout.SOUTH);
            itemsPanel.add(itemPanel2);
            

            // Add empty panels for future modifications
            for(int i=0; i<7; i++) { // Adjust this number based on how many cells you want to leave empty
                itemsPanel.add(new JPanel());
            }
            
            middlePanel.add(itemsPanel, BorderLayout.CENTER);

            // Right panel for order items
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BorderLayout()); // Changed to BorderLayout for better organization
            rightPanel.setPreferredSize(new Dimension(200, 600));

            JLabel orderItemsLabel = new JLabel("Order Items");
            rightPanel.add(orderItemsLabel, BorderLayout.NORTH);

            // Panel for adding text fields
            JPanel textFieldsPanel = new JPanel();
            textFieldsPanel.setLayout(new GridLayout(3, 1)); // Use GridLayout to organize text fields

            // Add empty fields for ordered items
            for (int i = 0; i < 3; i++) {
                textFieldsPanel.add(new JTextField());
            }

            rightPanel.add(textFieldsPanel, BorderLayout.CENTER);

            // Bottom panel for cashier and subtotal
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

            
            bottomPanel.add(new JLabel("Cashier Name: Ritchey"));
            bottomPanel.add(new JLabel(" Order Subtotal: $0.00"));
            bottomPanel.add(new JButton(" Order Complete"));

            frame.add(leftPanel, BorderLayout.WEST);
            frame.add(middlePanel, BorderLayout.CENTER);
            frame.add(rightPanel, BorderLayout.EAST);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            frame.pack();
            frame.setVisible(true);
        });
    }
}
