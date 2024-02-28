import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;

public class manager {
    private JFrame frame;
    private JPanel leftPanel;
    private JPanel southPanel;
    private CardLayout cardLayout;
    private JPanel cards;
    private JButton icon1, icon2, icon3;
    private ImageIcon icon1Image, icon2Image, icon3Image;

    public manager() {
        frame = new JFrame("Custom UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        icon1Image = new ImageIcon("ritchey.png");
        icon2Image = new ImageIcon("ritchey.png");
        icon3Image = new ImageIcon("ritchey.png");

        icon1 = new JButton(icon1Image);
        icon2 = new JButton(icon2Image);
        icon3 = new JButton(icon3Image);

        icon1.setPreferredSize(new Dimension(500, 500));
        icon2.setPreferredSize(new Dimension(500, 500));
        icon3.setPreferredSize(new Dimension(500, 500));

        icon1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "Card 1");
            }
        });

        icon2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "Card 2");
            }
        });

        icon3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "Card 3");
            }
        });

        leftPanel.add(icon1);
        leftPanel.add(icon2);
        leftPanel.add(icon3);

        southPanel = new JPanel();
        // Add components to the southPanel as per your requirements
        southPanel.add(new JLabel("Cashier Name: Ritchey"));
        southPanel.add(new JLabel(" Order Subtotal: $0.00"));
        southPanel.add(new JButton(" Order Complete"));

        // Create headers for each card
        JLabel header1 = new JLabel("Page 1");
        header1.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel header2 = new JLabel("Pageeeeeee");
        header2.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel header3 = new JLabel("Page627288");
        header3.setFont(new Font("Arial", Font.BOLD, 24));

        // Create panels for each card and add the headers
        JPanel card1 = new JPanel();
        card1.add(header1);
        
        JPanel card2 = new JPanel();
        card2.add(header2);
        
        JPanel card3 = new JPanel();
        card3.add(header3);

        // Add the panels to the cards
        cards.add(card1, "Card 1");
        cards.add(card2, "Card 2");
        cards.add(card3, "Card 3");

        frame.getContentPane().add(leftPanel, BorderLayout.WEST);
        frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(cards, BorderLayout.CENTER);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = frame.getWidth() / 5;
                int height = frame.getHeight() / 5;
                icon1.setIcon(resizeIcon(icon1Image, width, height));
                icon2.setIcon(resizeIcon(icon2Image, width, height));
                icon3.setIcon(resizeIcon(icon3Image, width, height));
                frame.revalidate();
                frame.repaint();
            }
        });

        frame.setVisible(true);
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new manager();
            }
        });
    }
}
