package deeplearning;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DonationUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Donation Suggestions");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setLocationRelativeTo(null);

            // Main panel with BorderLayout
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(225, 240, 255)); // Light blue

            // Title panel
            JLabel title = new JLabel("Items Eligible for Donation", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 40));
            title.setForeground(new Color(40, 80, 120));
            title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
            mainPanel.add(title, BorderLayout.NORTH);

            String username = (args != null && args.length > 0) ? args[0] : "default";
            JPanel itemsPanel = createItemsPanel(username);

            JScrollPane scrollPane = new JScrollPane(itemsPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(24);
            scrollPane.setBorder(null);
            
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }

    private static JPanel createItemsPanel(String username) {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 26, 26));
        panel.setBackground(new Color(200, 220, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] categories = {"Shirts", "Pants", "Shoes", "Cultural Dress"};
        for (String category : categories) {
            File categoryFile = getCategoryTextFile(username, category);
            if (categoryFile.exists() && categoryFile.length() > 0) {
                try {
                    String content = new String(Files.readAllBytes(categoryFile.toPath()));
                    String[] items = content.split("\n\n"); // Split by double newline
                    
                    for (String itemData : items) {
                        if (itemData.trim().isEmpty()) continue;
                        
                        if (isEligibleForDonation(itemData)) {
                            String itemId = extractItemId(itemData);
                            if (itemId != null) {
                                File imageFile = new File(getCategoryDir(username, category), itemId + ".png");
                                if (imageFile.exists()) {
                                    panel.add(createItemPanel(imageFile, itemData, categoryFile));
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (panel.getComponentCount() == 0) {
            JLabel noItemsLabel = new JLabel("No items eligible for donation found", SwingConstants.CENTER);
            noItemsLabel.setForeground(Color.WHITE);
            noItemsLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
            panel.add(noItemsLabel);
        }
        
        return panel;
    }

    private static File getUserDir(String username) {
        return new File(new File(System.getProperty("user.home"), "WardrobeManagerData"), username);
    }

    private static File getCategoryDir(String username, String category) {
        return new File(getUserDir(username), category);
    }

    private static File getCategoryTextFile(String username, String category) {
        return new File(getCategoryDir(username, category), category + ".txt");
    }

    private static boolean isEligibleForDonation(String itemData) {
        LocalDate lastWorn = parseLastWornDate(itemData);
        return lastWorn != null && ChronoUnit.MONTHS.between(lastWorn, LocalDate.now()) >= 6;
    }

    private static LocalDate parseLastWornDate(String itemData) {
        String[] lines = itemData.split("\n");
        for (String line : lines) {
            if (line.startsWith("Last Worn: ")) {
                try {
                    return LocalDate.parse(line.substring("Last Worn: ".length()).trim());
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + line);
                }
            }
        }
        return null;
    }

    private static String extractItemId(String itemData) {
        String[] lines = itemData.split("\n");
        for (String line : lines) {
            if (line.startsWith("ID: ")) {
                return line.substring("ID: ".length()).trim();
            }
        }
        return null;
    }

    private static JPanel createItemPanel(File imageFile, String itemData, File categoryFile) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(225, 240, 255));
        panel.setPreferredSize(new Dimension(380, 470));
        panel.setBorder(BorderFactory.createLineBorder(new Color(120, 180, 255), 2));

        try {
            // Image
            BufferedImage originalImage = ImageIO.read(imageFile);
            Image scaledImage = originalImage.getScaledInstance(320, 320, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(imageLabel, BorderLayout.CENTER);

            // Info
            JTextArea infoArea = new JTextArea(getSimplifiedInfo(itemData));
            infoArea.setEditable(false);
            infoArea.setBackground(new Color(225, 240, 255));
            infoArea.setFont(new Font("SansSerif", Font.PLAIN, 18));
            panel.add(infoArea, BorderLayout.SOUTH);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            buttonPanel.setOpaque(false);

            JButton editDateButton = createStyledButton("Edit Date", new Color(120, 180, 255));
            editDateButton.addActionListener(e -> showDateEditDialog(itemData, categoryFile));
            buttonPanel.add(editDateButton);

            JButton donateButton = createStyledButton("Donate", new Color(255, 120, 120));
            donateButton.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(panel, 
                    "Confirm donation of this item?", 
                    "Confirm Donation", 
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    
                    deleteItem(imageFile, itemData, categoryFile);
                    panel.getParent().remove(panel);
                    panel.getParent().revalidate();
                    panel.getParent().repaint();
                }
            });
            buttonPanel.add(donateButton);

            panel.add(buttonPanel, BorderLayout.NORTH);

        } catch (IOException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading image");
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel);
        }
        return panel;
    }

    private static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(new Color(40, 80, 120));
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return button;
    }

    private static String getSimplifiedInfo(String itemData) {
        StringBuilder info = new StringBuilder();
        String[] lines = itemData.split("\n");
        for (String line : lines) {
            if (line.startsWith("ID: ") || line.startsWith("Type: ") || 
               line.startsWith("Primary Color: ") || line.startsWith("Last Worn: ")) {
                info.append(line).append("\n");
            }
        }
        return info.toString();
    }

   private static void showDateEditDialog(String itemData, File categoryFile) {
    try {
        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Last Worn Date");
        dialog.setLayout(new GridLayout(3, 1, 8, 8));
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);

        // Parse current date from itemData
        LocalDate currentDate = parseLastWornDate(itemData);
        if (currentDate == null) {
            JOptionPane.showMessageDialog(dialog, "Error: No valid date found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Setup date picker with current date
        SpinnerDateModel model = new SpinnerDateModel(
            java.sql.Date.valueOf(currentDate),  // Initial date
            null,                               // No min date
            null,                               // No max date
            java.util.Calendar.DAY_OF_MONTH     // Precision
        );
        JSpinner dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setFont(new Font("SansSerif", Font.PLAIN, 18));

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        saveButton.addActionListener(e -> {
            LocalDate newDate = ((java.util.Date) dateSpinner.getValue())
                .toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
            updateLastWornDate(extractItemId(itemData), newDate, categoryFile);
            dialog.dispose();
        });

        JLabel dateLabel = new JLabel("Select new date:");
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        dialog.add(dateLabel);
        dialog.add(dateSpinner);
        dialog.add(saveButton);
        dialog.setVisible(true);
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error opening date editor.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
   private static void updateLastWornDate(String itemId, LocalDate newDate, File categoryFile) {
    try {
        // Read all lines from the file
        List<String> lines = Files.readAllLines(categoryFile.toPath());
        List<String> updatedLines = new ArrayList<>();
        boolean inTargetItem = false;
        boolean dateUpdated = false;

        for (String line : lines) {
            // Check if we're in the target item block
            if (line.startsWith("ID: " + itemId)) {
                inTargetItem = true;
            }

            // If we're in the target block and find the old date, REPLACE it
            if (inTargetItem && line.startsWith("Last Worn: ")) {
                updatedLines.add("Last Worn: " + newDate); // Update with new date
                dateUpdated = true;
            } 
            // Otherwise, keep the line as-is
            else {
                updatedLines.add(line);
            }

            // Check for end of item block
            if (line.equals("End")) {
                if (inTargetItem && !dateUpdated) {
                    // If no date was found, add it before "End"
                    updatedLines.add("Last Worn: " + newDate);
                }
                inTargetItem = false;
                dateUpdated = false;
            }
        }

        // Write the updated content back to the file
        Files.write(categoryFile.toPath(), updatedLines);
        
        // Optional: Show success message
        JOptionPane.showMessageDialog(
            null,
            "Date updated successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    } catch (IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
            null,
            "Error updating date: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
    private static void deleteItem(File imageFile, String itemData, File categoryFile) {
        try {
            // Delete image file
            if (imageFile.exists()) {
                imageFile.delete();
            }
            
            // Remove from text file
            List<String> fileContent = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(categoryFile))) {
                String line;
                boolean skipBlock = false;
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("ID: ") && line.contains(extractItemId(itemData))) {
                        skipBlock = true;
                    }
                    if (!skipBlock) {
                        fileContent.add(line);
                    }
                    if (line.equals("End")) {
                        skipBlock = false;
                    }
                }
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(categoryFile))) {
                for (String line : fileContent) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting item", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class WrapLayout extends FlowLayout {
        public WrapLayout() {
            super();
        }

        public WrapLayout(int align) {
            super(align);
        }

        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);

                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }

                        if (rowWidth != 0) rowWidth += hgap;
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }

                addRow(dim, rowWidth, rowHeight);
                dim.width += insets.left + insets.right + hgap * 2;
                dim.height += insets.top + insets.bottom + vgap * 2;

                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }
}