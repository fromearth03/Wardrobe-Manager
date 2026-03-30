package deeplearning;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Files;

public class AvatarApp {

    private static int outfitIndex = 0;
    private static final ArrayList<ImageIcon> outfitImages = new ArrayList<>();
    private static final ArrayList<String> customOutfitPaths = new ArrayList<>();

    static String[] storedArgs;

    public static void main(String[] args) {
        storedArgs = args;
        resetAvatarState();
        // Build user avatar directory
        File appRootDir = new File(System.getProperty("user.home"), "WardrobeManagerData");
        File avatarDir = new File(new File(appRootDir, storedArgs[0]), "Avatars");
        String avatarDirPath = avatarDir.getAbsolutePath();
        if (!avatarDir.exists()) {
            avatarDir.mkdirs();
        }

        // Load custom outfit paths first (from outfits.txt in Avatars directory)
        loadCustomOutfits(avatarDirPath);

        // Load default outfits (from Avatars directory)
        File[] files = avatarDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
        ArrayList<String> defaultOutfits = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                String path = f.getAbsolutePath();
                if (!customOutfitPaths.contains(path)) {
                    defaultOutfits.add(path);
                }
            }
        }

        for (String path : defaultOutfits) {
            outfitImages.add(new ImageIcon(path));
        }

        for (String path : customOutfitPaths) {
            File file = new File(path);
            if (file.exists()) {
                outfitImages.add(new ImageIcon(path));
            }
        }

        JFrame frame = new JFrame("Avatar Customization");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setSize(1400, 900);
        frame.setLayout(new BorderLayout());

        // Avatar panel
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!outfitImages.isEmpty() && outfitIndex >= 0 && outfitIndex < outfitImages.size()) {
                    Image outfit = outfitImages.get(outfitIndex).getImage();
                    int imgWidth = outfit.getWidth(null);
                    int imgHeight = outfit.getHeight(null);
                    int x = (getWidth() - imgWidth) / 2;
                    int y = (getHeight() - imgHeight) / 2;
                    g.drawImage(outfit, x, y, this);
                }
            }
        };
        avatarPanel.setPreferredSize(new Dimension(1100, 800));
        avatarPanel.setBackground(new Color(48, 25, 52));

        // Outfit button panel
        JPanel outfitPanel = new JPanel();
        outfitPanel.setLayout(new BoxLayout(outfitPanel, BoxLayout.Y_AXIS));
        outfitPanel.setBackground(new Color(220, 220, 220));
        int buttonSize = 180;

        JScrollPane scrollPane = new JScrollPane(
            outfitPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setPreferredSize(new Dimension(300, 800));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        Runnable refreshOutfitButtons = () -> {
            outfitPanel.removeAll();
            for (int i = 0; i < outfitImages.size(); i++) {
                int index = i;
                ImageIcon icon = outfitImages.get(i);

                JButton outfitButton = new JButton();
                outfitButton.setIcon(new ImageIcon(
                    icon.getImage().getScaledInstance(buttonSize - 30, buttonSize - 30, Image.SCALE_SMOOTH)
                ));
                outfitButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
                outfitButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
                outfitButton.setFocusPainted(false);
                outfitButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                outfitButton.addActionListener(e -> {
                    outfitIndex = index;
                    avatarPanel.repaint();
                });

                JPanel buttonWrapper = new JPanel();
                buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.X_AXIS));
                buttonWrapper.setOpaque(false);
                buttonWrapper.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
                buttonWrapper.add(Box.createHorizontalGlue());
                buttonWrapper.add(outfitButton);
                buttonWrapper.add(Box.createHorizontalGlue());

                outfitPanel.add(buttonWrapper);
            }
            outfitPanel.revalidate();
            outfitPanel.repaint();
        };

        refreshOutfitButtons.run();

        // Add Outfit button (styled like SignInButton)
        JButton addOutfitButton = new JButton("Add New Avatar") {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        addOutfitButton.setFocusPainted(false);
        addOutfitButton.setBackground(new Color(247, 110, 122)); // Coral red
        addOutfitButton.setForeground(Color.WHITE);
        addOutfitButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        addOutfitButton.setBorder(new AvatarRoundedBorder(20, new Color(247, 110, 122)));
        addOutfitButton.setContentAreaFilled(false);
        addOutfitButton.setOpaque(false);
        addOutfitButton.setPreferredSize(new Dimension(280, 72));

        addOutfitButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Avatar Image");
            styleFileChooser(fileChooser);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // Copy the file to the Avatars directory
                File destFile = new File(avatarDir, selectedFile.getName());
                try {
                    Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    ImageIcon newOutfit = new ImageIcon(destFile.getAbsolutePath());
                    outfitImages.add(newOutfit);
                    customOutfitPaths.add(destFile.getAbsolutePath());
                    saveCustomOutfits(avatarDirPath);
                    outfitIndex = outfitImages.size() - 1;
                    refreshOutfitButtons.run();
                    avatarPanel.repaint();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to add avatar: " + ex.getMessage());
                }
            }
        });

        // Delete Outfit button (styled like SignInButton)
        JButton deleteOutfitButton = new JButton("Delete Avatar") {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        deleteOutfitButton.setFocusPainted(false);
        deleteOutfitButton.setBackground(new Color(247, 110, 122)); // Coral red
        deleteOutfitButton.setForeground(Color.WHITE);
        deleteOutfitButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        deleteOutfitButton.setBorder(new AvatarRoundedBorder(20, new Color(247, 110, 122)));
        deleteOutfitButton.setContentAreaFilled(false);
        deleteOutfitButton.setOpaque(false);
        deleteOutfitButton.setPreferredSize(new Dimension(280, 72));

        deleteOutfitButton.addActionListener(e -> {
            int defaultCount = defaultOutfits.size();
            if (outfitIndex < defaultCount) {
                JOptionPane.showMessageDialog(frame, "Cannot delete default avatars.");
                return;
            }
            int indexToRemove = outfitIndex - defaultCount;
            // Remove image file from disk
            File toDelete = new File(customOutfitPaths.get(indexToRemove));
            if (toDelete.exists()) toDelete.delete();
            outfitImages.remove(outfitIndex);
            customOutfitPaths.remove(indexToRemove);
            saveCustomOutfits(avatarDirPath);
            outfitIndex = Math.max(0, outfitIndex - 1);
            refreshOutfitButtons.run();
            avatarPanel.repaint();
        });

        // Bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245, 245, 245));
        bottomPanel.add(addOutfitButton);
        bottomPanel.add(deleteOutfitButton);

        // Add components
        frame.add(avatarPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void styleFileChooser(JFileChooser chooser) {
        chooser.setPreferredSize(new Dimension(980, 700));
        Font chooserFont = new Font("SansSerif", Font.PLAIN, 18);
        applyFontRecursively(chooser, chooserFont);
    }

    private static void applyFontRecursively(Component component, Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyFontRecursively(child, font);
            }
        }
    }

    private static void loadCustomOutfits(String avatarDirPath) {
        File saveFile = new File(avatarDirPath, "outfits.txt");
        if (saveFile.exists()) {
            try (Scanner scanner = new Scanner(saveFile)) {
                while (scanner.hasNextLine()) {
                    String path = scanner.nextLine().trim();
                    if (!path.isEmpty() && !customOutfitPaths.contains(path)) {
                        File file = new File(path);
                        if (file.exists()) {
                            customOutfitPaths.add(path);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading saved avatars: " + e.getMessage());
            }
        }
    }

    private static void saveCustomOutfits(String avatarDirPath) {
        File saveFile = new File(avatarDirPath, "outfits.txt");
        try (PrintWriter writer = new PrintWriter(saveFile)) {
            for (String path : customOutfitPaths) {
                writer.println(path);
            }
        } catch (IOException e) {
            System.err.println("Error saving avatars: " + e.getMessage());
        }
    }

    private static void resetAvatarState() {
        outfitIndex = 0;
        outfitImages.clear();
        customOutfitPaths.clear();
    }
}

// RoundedBorder class for button styling
class AvatarRoundedBorder extends javax.swing.border.AbstractBorder {
    private int radius;
    private Color color;

    public AvatarRoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius + 1, radius + 1, radius + 1, radius + 1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = insets.top = insets.bottom = radius + 1;
        return insets;
    }
}