package deeplearning;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.awt.RenderingHints;
import java.awt.Shape;

public class profile {
    public static String[] storedArgs;

    public static void main(String[] args) {
        storedArgs = args;
        SwingUtilities.invokeLater(() -> {
            ProfileUI ui = new ProfileUI();
            ui.setVisible(true);
        });
    }
}

class RoundedPanel extends JPanel {
    private int cornerRadius;

    public RoundedPanel(int radius) {
        super();
        cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
    }
}

class ProfileUI extends JFrame {
    public ProfileUI() {
        setTitle("User Profile");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(900, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Main panel with two-color background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Purple top half
                g2d.setColor(new Color(100, 65, 165));
                g2d.fillRect(0, 0, getWidth(), getHeight()/2);
                // White bottom half
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, getHeight()/2, getWidth(), getHeight()/2);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        add(backgroundPanel, BorderLayout.CENTER);
        
        // Rounded white card panel centered in the frame
        RoundedPanel cardPanel = new RoundedPanel(20);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(52, 52, 52, 52));
        cardPanel.setMaximumSize(new Dimension(700, 860));
        
        // Center the card panel vertically
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(cardPanel);
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Profile Panel
        ProfilePanel profilePanel = new ProfilePanel();
        cardPanel.add(profilePanel);
    }
}

class ProfilePanel extends JPanel {
    private JLabel imageLabel;
    private JLabel statusLabel;
    UserAccount userAccount = new UserAccount();

    public ProfilePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        userAccount.loadUsersFromDefaultFile();
        user matchedUser = userAccount.searchUserByEmail(profile.storedArgs[1]);
        if (matchedUser == null) {
            JLabel errorLabel = new JLabel("User not found.");
            errorLabel.setForeground(Color.BLACK);
            add(errorLabel);
            return;
        }

        // Profile image (circular)
        imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape circle = new Ellipse2D.Float(0, 0, 220, 220);
                g2.setClip(circle);
                super.paintComponent(g2);
            }
        };
        imageLabel.setPreferredSize(new Dimension(220, 220));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(imageLabel);
        
        // Status label for profile pic availability
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 20));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(statusLabel);
        add(Box.createVerticalStrut(34));

        // Name
        JLabel nameLabel = new JLabel(matchedUser.getFirstName() + " " + matchedUser.getLastName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 38));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(nameLabel);
        add(Box.createVerticalStrut(34));

        // Email
        JLabel emailLabel = new JLabel("Email: " + matchedUser.getEmail());
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 26));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(emailLabel);
        add(Box.createVerticalStrut(22));

        // Gender
        JLabel genderLabel = new JLabel("Gender: " + matchedUser.getGender());
        genderLabel.setFont(new Font("SansSerif", Font.PLAIN, 26));
        genderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(genderLabel);
        add(Box.createVerticalStrut(34));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(560, 90));

        // Upload button (smaller size)
        JButton uploadBtn = createStyledButton("Upload", new Color(247, 110, 122));
        uploadBtn.addActionListener(e -> selectImage());
        uploadBtn.setPreferredSize(new Dimension(220, 64));
        buttonPanel.add(uploadBtn);

        // Delete button
        JButton deleteBtn = createStyledButton("Delete", new Color(200, 200, 200));
        deleteBtn.addActionListener(e -> deleteProfileImage());
        deleteBtn.setPreferredSize(new Dimension(220, 64));
        buttonPanel.add(deleteBtn);

        add(buttonPanel);

        // Load existing profile image
        loadProfileImage();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 26));
        button.setFocusPainted(false);
        button.setBorder(new ProfileRoundedBorder(15, bgColor));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        styleFileChooser(fileChooser);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                statusLabel.setText("");

                File userFolder = getUserFolder();
                if (!userFolder.exists()) {
                    userFolder.mkdirs();
                }
                File destination = getProfileImageFile();
                Files.copy(selectedFile.toPath(), destination.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deleteProfileImage() {
        File profileImage = getProfileImageFile();
        
        if (profileImage.exists()) {
            if (profileImage.delete()) {
                imageLabel.setIcon(null);
                statusLabel.setText("No profile picture available");
                statusLabel.setForeground(Color.RED);
            }
        } else {
            statusLabel.setText("No profile picture available");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void loadProfileImage() {
        File profileImage = getProfileImageFile();
        if (profileImage.exists()) {
            ImageIcon icon = new ImageIcon(profileImage.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
            statusLabel.setText("");
        } else {
            BufferedImage blankImage = new BufferedImage(220, 220, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = blankImage.createGraphics();
            g2d.setColor(new Color(220, 220, 220));
            g2d.fillOval(0, 0, 220, 220);
            g2d.dispose();
            imageLabel.setIcon(new ImageIcon(blankImage));
            statusLabel.setText("No profile picture available");
            statusLabel.setForeground(Color.RED);
        }
    }

    private File getUserFolder() {
        return new File(new File(System.getProperty("user.home"), "WardrobeManagerData"), profile.storedArgs[0]);
    }

    private File getProfileImageFile() {
        return new File(getUserFolder(), "profile_image.png");
    }

    private void styleFileChooser(JFileChooser chooser) {
        chooser.setPreferredSize(new Dimension(980, 700));
        Font chooserFont = new Font("SansSerif", Font.PLAIN, 18);
        applyFontRecursively(chooser, chooserFont);
    }

    private void applyFontRecursively(Component component, Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyFontRecursively(child, font);
            }
        }
    }
}

class ProfileRoundedBorder extends javax.swing.border.AbstractBorder {
    private int radius;
    private Color color;

    public ProfileRoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius+1, this.radius+1, this.radius+1, this.radius+1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = this.radius+1;
        insets.top = insets.bottom = this.radius+1;
        return insets;
    }
}