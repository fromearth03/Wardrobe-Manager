package deeplearning;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.border.AbstractBorder;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;


public class wardrobe {
   public static String[] storedArgs; // Store args here
    public static void main(String[] args) {
        storedArgs = args; // Store args for later use
        
        // Create all necessary folders for new user
        createUserFolders(args[0]);
        
        SwingUtilities.invokeLater(() -> {
            OutfitUI ui = new OutfitUI();
            ui.setVisible(true);
        });
    }

    private static void createUserFolders(String username) {
        File userDir = getUserDir(username);
        String[] categories = {"Shirts", "Pants", "Shoes", "Cultural Dress"};
        
        // Create user directory if it doesn't exist
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        
        // Create category directories and empty text files
        for (String category : categories) {
            File categoryDir = getCategoryDir(username, category);
            if (!categoryDir.exists()) {
                categoryDir.mkdirs();
            }
            
            // Create empty text file if it doesn't exist
            File textFile = getCategoryTextFile(username, category);
            if (!textFile.exists()) {
                try {
                    textFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Create geminiconversationhistory.txt if it doesn't exist
        File historyFile = getGeminiHistoryFile(username);
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static File getUserDir(String username) {
        return new File(new File(System.getProperty("user.home"), "WardrobeManagerData"), username);
    }

    static File getCategoryDir(String username, String category) {
        return new File(getUserDir(username), category);
    }

    static File getCategoryTextFile(String username, String category) {
        return new File(getCategoryDir(username, category), category + ".txt");
    }

    static File getGeminiHistoryFile(String username) {
        return new File(getUserDir(username), "geminiconversationhistory.txt");
    }

    static void styleFileChooser(JFileChooser chooser) {
        chooser.setPreferredSize(new Dimension(1200, 820));
        chooser.setMinimumSize(new Dimension(1100, 760));
        Font chooserFont = new Font("SansSerif", Font.PLAIN, 20);
        applyFontRecursively(chooser, chooserFont);
        SwingUtilities.updateComponentTreeUI(chooser);
    }

    private static void applyFontRecursively(Component component, Font font) {
        component.setFont(font);

        if (component instanceof JList) {
            JList<?> list = (JList<?>) component;
            if (list.getFixedCellHeight() < 32) {
                list.setFixedCellHeight(32);
            }
        }

        if (component instanceof JTable) {
            JTable table = (JTable) component;
            if (table.getRowHeight() < 32) {
                table.setRowHeight(32);
            }
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyFontRecursively(child, font);
            }
        }
    }

    static File showLargeFileChooser(Component parent, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        styleFileChooser(chooser);

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog pickerDialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        pickerDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pickerDialog.setLayout(new BorderLayout());
        pickerDialog.add(chooser, BorderLayout.CENTER);
        pickerDialog.setSize(1400, 920);
        pickerDialog.setLocationRelativeTo(parent);

        final File[] selected = {null};
        chooser.addActionListener(event -> {
            String command = event.getActionCommand();
            if (JFileChooser.APPROVE_SELECTION.equals(command)) {
                selected[0] = chooser.getSelectedFile();
                pickerDialog.dispose();
            } else if (JFileChooser.CANCEL_SELECTION.equals(command)) {
                pickerDialog.dispose();
            }
        });

        pickerDialog.setVisible(true);
        return selected[0];
    }
}

class OutfitUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    FileWriter fileWriter = null;
    FileReader fileReader = null;
     private static int shirtid = 0;
    private static int pantsid = 0;
    private static int shoesid = 0;
    private static int culturalDressid = 0;
    Scanner input = new Scanner(System.in);


    public OutfitUI() {
        setTitle("Create Outfit");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        OutfitBackground background = new OutfitBackground();
        background.setLayout(new BorderLayout());
        add(background);

        TitleLabel title = new TitleLabel("Create Outfit");
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(225, 240, 255));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        titlePanel.add(title);
        background.add(titlePanel, BorderLayout.NORTH);

        OutfitPanel outfitPanel = new OutfitPanel(this);
        background.add(outfitPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(200, 220, 255));
        background.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(createPanel("Shirts"), "Shirts");
        contentPanel.add(createPanel("Pants"), "Pants");
        contentPanel.add(createPanel("Shoes"), "Shoes");
        contentPanel.add(createPanel("Cultural Dress"), "Cultural Dress");
    }

    private JPanel createPanel(String name) {
    JPanel imageContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
    imageContainer.setBackground(new Color(200, 220, 255));

    JLabel label = new JLabel("Add " + name);
    label.setForeground(Color.WHITE);
    label.setFont(new Font("SansSerif", Font.BOLD, 24));
    imageContainer.add(label);

    // Create the Add button but don't add it yet
    JButton addButton = createAddButton(imageContainer , name);

    // Load existing images from folder (BEFORE adding addButton)
    loadExistingImages(imageContainer, name, addButton);

    // Now add the Add button after existing images
    imageContainer.add(addButton);

    JScrollPane scrollPane = new JScrollPane(imageContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.setBorder(null);

    JPanel panelWithScroll = new JPanel(new BorderLayout());
    panelWithScroll.setBackground(new Color(130, 90, 174));
    panelWithScroll.add(scrollPane, BorderLayout.CENTER);
    return panelWithScroll;
}

    private void loadExistingImages(JPanel imageContainer, String category, JButton addButton) {
           try {
    BufferedReader reader = new BufferedReader(new FileReader(
        wardrobe.getCategoryTextFile(wardrobe.storedArgs[0], category)));

    String line;
    int maxId = 0; // Track the maximum ID found
    while ((line = reader.readLine()) != null) {
        String[] parts = line.split(" ");
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                switch (category) {
                    case "Shirts":
                        if (part.startsWith("Shir")) {
                            int id = Integer.parseInt(part.substring(4)); // Extract the numeric part of the ID
                            maxId = Math.max(maxId, id);
                        }
                        break;
                    case "Pants":
                        if (part.startsWith("Pan")) {
                            int id = Integer.parseInt(part.substring(3)); // Extract the numeric part of the ID
                            maxId = Math.max(maxId, id);
                        }
                        break;
                    case "Shoes":
                        if (part.startsWith("Shoe")) {
                            int id = Integer.parseInt(part.substring(4)); // Extract the numeric part of the ID
                            maxId = Math.max(maxId, id);
                        }
                        break;
                    case "Cultural Dress":
                        if (part.startsWith("CD")) {
                            int id = Integer.parseInt(part.substring(2)); // Extract the numeric part of the ID
                            maxId = Math.max(maxId, id);
                        }
                        break;
                }
            }
        }
    }
    reader.close();

    // Set the next ID based on the maximum ID found
    switch (category) {
        case "Shirts":
            shirtid = maxId + 1;
            break;
        case "Pants":
            pantsid = maxId + 1;
            break;
        case "Shoes":
            shoesid = maxId + 1;
            break;
        case "Cultural Dress":
            culturalDressid = maxId + 1;
            break;
    }
} catch (Exception e) {
    e.printStackTrace();
}
    try {
        // Get the image directory
        File folder = wardrobe.getCategoryDir(wardrobe.storedArgs[0], category);
        if (!folder.exists()) return;

        // Get all image files (only .png files)
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) return;

        // To store image panels temporarily
        List<JPanel> imagePanels = new ArrayList<>();
        int imageWidth = 300; // Width of each image panel
        int imageHeight = 360; // Height of each image panel
        int hgap = 20; // Horizontal gap
        int vgap = 20; // Vertical gap
        int containerWidth = 1500; // Set a wider container for fullscreen layouts

        // Add images to the panel
        for (File file : files) {
            BufferedImage originalImage = ImageIO.read(file);
            Image scaledImage = getHighQualityScaledImage(originalImage, 300, 300);

            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setPreferredSize(new Dimension(300, 300));

            JButton deleteButton = new JButton("Delete");
            deleteButton.setPreferredSize(new Dimension(180, 50));
            deleteButton.setBackground(new Color(255, 94, 94));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorder(new WardrobeRoundedBorder(15, new Color(255, 94, 94)));

            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setBackground(new Color(130, 90, 174));
            imagePanel.setPreferredSize(new Dimension(imageWidth, imageHeight));
            imagePanel.add(imageLabel, BorderLayout.CENTER);
            imagePanel.add(deleteButton, BorderLayout.SOUTH);

            // Delete action for image
            deleteButton.addActionListener(e -> {   
                String fileName = file.getName();
                        String id = fileName.substring(0, fileName.lastIndexOf('.'));  // e.g., Shir1
                    deleteLinesInRange(
                        wardrobe.getCategoryTextFile(wardrobe.storedArgs[0], category).getAbsolutePath(), 
                        id, 
                        "End"
                    );
                imageContainer.remove(imagePanel);
                imageContainer.revalidate();
                imageContainer.repaint();
                if (file.exists()) file.delete();
            });

            // Add the image panel to the list
            imagePanels.add(imagePanel);
        }

        // Setup the layout to wrap images
        WrapLayout wrapLayout = new WrapLayout(FlowLayout.LEFT, hgap, vgap);
        imageContainer.setLayout(wrapLayout);

        // Remove the "Add" button temporarily
        imageContainer.remove(addButton);

        // Add images to the container
        for (JPanel panel : imagePanels) {
            imageContainer.add(panel);
        }

        // Re-add the "Add" button at the end
        imageContainer.add(addButton);

        // Dynamically calculate the preferred size of the container
        int rows = (int) Math.ceil((double) (imagePanels.size() + 1) / (containerWidth / (imageWidth + hgap)));
        int preferredHeight = rows * (imageHeight + vgap);
        imageContainer.setPreferredSize(new Dimension(containerWidth, preferredHeight));

        // Refresh the layout to accommodate the "Add" button at the end
        imageContainer.revalidate();
        imageContainer.repaint();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
        
private JButton createAddButton(JPanel imageContainer, String category) {
    JButton button = new JButton("+") {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            g2.fillOval(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            Font font = new Font("SansSerif", Font.BOLD, 44);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getAscent();
            g2.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 4);
            g2.dispose();
        }
    };
    button.setPreferredSize(new Dimension(94, 94));
    button.setFocusPainted(false);
    button.setContentAreaFilled(false);
    button.setOpaque(false);
    button.setBorderPainted(false);

    button.addActionListener(e -> {
        // Open a dialog box
        JDialog dialog = new JDialog((Frame) null, "Add New " + category, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(1120, 980);
        dialog.setLocationRelativeTo(null);
        
        // Set background to gray
        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBackground(new Color(74, 50, 104));
        JScrollPane formScrollPane = new JScrollPane(contentPane);
        formScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formScrollPane.getVerticalScrollBar().setUnitIncrement(24);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dialog.setContentPane(formScrollPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 18, 18, 18);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("SansSerif", Font.BOLD, 24);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 22);
        Dimension labelSize = new Dimension(320, 44);
        Dimension fieldSize = new Dimension(560, 64);
        Dimension listSize = new Dimension(560, 180);

        // Create components with white text for labels
        JLabel primaryColorLabel = new JLabel("Primary Color:");
        primaryColorLabel.setForeground(Color.WHITE);
        primaryColorLabel.setFont(labelFont);
        primaryColorLabel.setPreferredSize(labelSize);
        String[] colorOptions = {
            "BLACK", "WHITE", "BLUE", "RED", "GREEN", "BROWN", "YELLOW", "GRAY", "PURPLE",
            "ORANGE", "PINK", "MAROON", "BEIGE", "GOLD", "SILVER"
        };
        String[] patternOptions = {"PLAIN", "STRIPED", "CHECKERED", "FLORAL", "PRINTED", "EMBROIDERED"};
        String[] fabricOptions = {"COTTON", "DENIM", "WOOL", "SILK", "LINEN", "LEATHER", "POLYESTER", "CHIFFON", "VELVET", "KNIT"};
        String[] seasonOptions = {"SPRING", "SUMMER", "FALL", "WINTER"};
        String[] fitOptions = {"SLIM", "REGULAR", "LOOSE", "SKINNY", "TAILORED"};
        
        JComboBox<String> primaryColorComboBox = new JComboBox<>();
        for (String color : colorOptions) {
            primaryColorComboBox.addItem(color);
        }
        primaryColorComboBox.setFont(inputFont);
        primaryColorComboBox.setPreferredSize(fieldSize);

        JLabel accentColorsLabel = new JLabel("Accent Colors:");
        accentColorsLabel.setForeground(Color.WHITE);
        accentColorsLabel.setFont(labelFont);
        accentColorsLabel.setPreferredSize(labelSize);
        
        JList<String> accentColorsList = new JList<>(colorOptions);
        accentColorsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        accentColorsList.setFont(inputFont);
        accentColorsList.setVisibleRowCount(5);
        accentColorsList.setFixedCellHeight(38);
        accentColorsList.setBackground(Color.WHITE);
        accentColorsList.setForeground(new Color(40, 80, 120));
        accentColorsList.setSelectionBackground(new Color(255, 167, 94));
        accentColorsList.setSelectionForeground(Color.BLACK);
        JScrollPane accentColorsScrollPane = new JScrollPane(accentColorsList);
        accentColorsScrollPane.setPreferredSize(listSize);
        accentColorsScrollPane.setMinimumSize(listSize);
        accentColorsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel typeLabel = new JLabel("Select " + category + " Type:");
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setFont(labelFont);
        typeLabel.setPreferredSize(labelSize);

        JComboBox<String> typeComboBox = new JComboBox<>();
        switch (category) {
            case "Shirts":
                String[] shirtTypes = {"TSHIRT", "DRESS_SHIRT", "POLO", "HENLEY", "CASUAL_SHIRT", "KURTA", "KAMEEZ", "LONG_SLEEVE", "SHORT_SLEEVE", "TUNIC"};
                for (String type : shirtTypes) {
                    typeComboBox.addItem(type);
                }
                break;
            case "Pants":
                String[] pantsTypes = {"JEANS", "CHINOS", "DRESS_PANTS", "JOGGERS", "SHORTS", "CARGO", "SWEATPANTS", "SHALWAR"};
                for (String type : pantsTypes) {
                    typeComboBox.addItem(type);
                }
                break;
            case "Shoes":
                String[] shoeTypes = {"SNEAKERS", "FORMAL", "LOAFERS", "BOOTS", "SANDALS", "SLIPPERS", "KHUSSA", "PESHAWERI", "CHAPPAL", "MOJARI", "OXFORDS", "BROGUES"};
                for (String type : shoeTypes) {
                    typeComboBox.addItem(type);
                }
                break;
            case "Cultural Dress":
                String[] culturalTypes = {"SHALWAR", "KAMEEZ", "KURTA", "WAISTCOAT", "SHERWANI", "ACHKAN", "DUPATTA", "CHADAR", "LEHNGA", "SAARI"};
                for (String type : culturalTypes) {
                    typeComboBox.addItem(type);
                }
                break;
            default:
                typeComboBox.addItem("Unknown");
                break;
        }
        typeComboBox.setFont(inputFont);
        typeComboBox.setPreferredSize(fieldSize);

        JLabel sizeLabel = new JLabel("Size:");
        sizeLabel.setForeground(Color.WHITE);
        sizeLabel.setFont(labelFont);
        sizeLabel.setPreferredSize(labelSize);
        JTextField sizeField = new JTextField();
        sizeField.setFont(inputFont);
        sizeField.setPreferredSize(fieldSize);

        JLabel patternLabel = new JLabel("Pattern:");
        patternLabel.setForeground(Color.WHITE);
        patternLabel.setFont(labelFont);
        patternLabel.setPreferredSize(labelSize);
        JComboBox<String> patternComboBox = new JComboBox<>();
        for (String pattern : patternOptions) {
            patternComboBox.addItem(pattern);
        }
        patternComboBox.setFont(inputFont);
        patternComboBox.setPreferredSize(fieldSize);

        JLabel fabricLabel = new JLabel("Fabric:");
        fabricLabel.setForeground(Color.WHITE);
        fabricLabel.setFont(labelFont);
        fabricLabel.setPreferredSize(labelSize);
        JComboBox<String> fabricComboBox = new JComboBox<>();
        for (String fabric : fabricOptions) {
            fabricComboBox.addItem(fabric);
        }
        fabricComboBox.setFont(inputFont);
        fabricComboBox.setPreferredSize(fieldSize);

        JLabel seasonLabel = new JLabel("Seasons:");
        seasonLabel.setForeground(Color.WHITE);
        seasonLabel.setFont(labelFont);
        seasonLabel.setPreferredSize(labelSize);
        JList<String> seasonList = new JList<>(seasonOptions);
        seasonList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        seasonList.setFont(inputFont);
        seasonList.setVisibleRowCount(5);
        seasonList.setFixedCellHeight(38);
        seasonList.setBackground(Color.WHITE);
        seasonList.setForeground(new Color(40, 80, 120));
        seasonList.setSelectionBackground(new Color(255, 167, 94));
        seasonList.setSelectionForeground(Color.BLACK);
        JScrollPane seasonScrollPane = new JScrollPane(seasonList);
        seasonScrollPane.setPreferredSize(listSize);
        seasonScrollPane.setMinimumSize(listSize);
        seasonScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel fitLabel = new JLabel("Fit:");
        fitLabel.setForeground(Color.WHITE);
        fitLabel.setFont(labelFont);
        fitLabel.setPreferredSize(labelSize);
        JComboBox<String> fitComboBox = new JComboBox<>();
        for (String fit : fitOptions) {
            fitComboBox.addItem(fit);
        }
        fitComboBox.setFont(inputFont);
        fitComboBox.setPreferredSize(fieldSize);

        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setForeground(Color.WHITE);
        notesLabel.setFont(labelFont);
        notesLabel.setPreferredSize(labelSize);
        JTextField notesField = new JTextField();
        notesField.setFont(inputFont);
        notesField.setPreferredSize(fieldSize);

        JLabel tagsLabel = new JLabel("Tags (comma-separated):");
        tagsLabel.setForeground(Color.WHITE);
        tagsLabel.setFont(labelFont);
        tagsLabel.setPreferredSize(labelSize);
        JTextField tagsField = new JTextField();
        tagsField.setFont(inputFont);
        tagsField.setPreferredSize(fieldSize);

        JLabel fileLabel = new JLabel("Image:");
        fileLabel.setForeground(Color.WHITE);
        fileLabel.setFont(labelFont);
        fileLabel.setPreferredSize(labelSize);
        JButton fileButton = new JButton("Choose File") {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        fileButton.setBackground(new Color(255, 167, 94));
        fileButton.setForeground(Color.WHITE);
        fileButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        fileButton.setPreferredSize(new Dimension(220, 56));
        fileButton.setFocusPainted(false);
        fileButton.setContentAreaFilled(false);
        fileButton.setOpaque(false);
        fileButton.setBorder(new WardrobeRoundedBorder(20, new Color(255, 167, 94)));

        JLabel filePathLabel = new JLabel("No file selected");
        filePathLabel.setForeground(Color.WHITE);
        filePathLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        final File[] selectedFile = {null};

        fileButton.addActionListener(fileEvent -> {
            File chosenFile = wardrobe.showLargeFileChooser(dialog, "Select Image File");
            if (chosenFile != null) {
                selectedFile[0] = chosenFile;
                filePathLabel.setText(selectedFile[0].getName());
            }
        });

        // Add components to the dialog
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPane.add(typeLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(typeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(primaryColorLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(primaryColorComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(accentColorsLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(accentColorsScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(sizeLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(sizeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(patternLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(patternComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(fabricLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(fabricComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(seasonLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(seasonScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(fitLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(fitComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(notesLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(notesField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(tagsLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(tagsField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(fileLabel, gbc);
        gbc.gridx = 1;
        contentPane.add(fileButton, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        contentPane.add(filePathLabel, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;

        // Save button
        JButton saveButton = new JButton("Save") {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        saveButton.setBackground(new Color(255, 167, 94));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        saveButton.setPreferredSize(new Dimension(240, 64));
        saveButton.setFocusPainted(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setOpaque(false);
        saveButton.setBorder(new WardrobeRoundedBorder(20, new Color(255, 167, 94)));
        saveButton.addActionListener(saveEvent -> {

            try {
                fileWriter = new FileWriter(wardrobe.getCategoryTextFile(wardrobe.storedArgs[0], category), true);
            }catch (IOException ex) {
                ex.printStackTrace();
            }
            // Validation checks
            if (sizeField.getText().isEmpty() || selectedFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields and select an image.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Double.parseDouble(sizeField.getText());
            } catch (NumberFormatException exx) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid size.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (notesField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter notes.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (tagsField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter tags.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get values from the dialog
            String primaryColor = (String) primaryColorComboBox.getSelectedItem();
            List<String> accentColors = accentColorsList.getSelectedValuesList();
            if(accentColors.isEmpty()) {
              JOptionPane.showMessageDialog(dialog, "please select a accent color", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String size = sizeField.getText();
            String pattern = (String) patternComboBox.getSelectedItem();
            String fabric = (String) fabricComboBox.getSelectedItem();
            List<String> seasons = seasonList.getSelectedValuesList();
            String fit = (String) fitComboBox.getSelectedItem();
            String notes = notesField.getText();
            List<String> tags = Arrays.asList(tagsField.getText().split("\\s*,\\s*"));
            LocalDate lastWornDate = LocalDate.now();
            String selectedType = (String) typeComboBox.getSelectedItem();

            String itemId;
            switch (category) {
                case "Shirts":
                    shirtid++;
                    itemId = "Shir" + shirtid;
                    break;
                case "Pants":
                    pantsid++;
                    itemId = "Pan" + pantsid;
                    break;
                case "Shoes":
                    shoesid++;
                    itemId = "Shoe" + shoesid;
                    break;
                case "Cultural Dress":
                    culturalDressid++;
                    itemId = "CD" + culturalDressid;
                    break;
                default:
                    JOptionPane.showMessageDialog(dialog, "Unknown category: " + category, "Error", JOptionPane.ERROR_MESSAGE);
                    try {
                        if (fileWriter != null) {
                            fileWriter.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return;
            }

            String itemText = "ID: " + itemId + "\n" +
                    "Primary Color: " + primaryColor + "\n" +
                    "Accent Colors: " + accentColors + "\n" +
                    "Size: " + size + "\n" +
                    "Pattern: " + pattern + "\n" +
                    "Fabric: " + fabric + "\n" +
                    "Seasons: " + seasons + "\n" +
                    "Fit: " + fit + "\n" +
                    "Notes: " + notes + "\n" +
                    "Tags: " + tags + "\n" +
                    "Last Worn: " + lastWornDate + "\n" +
                    "Type: " + selectedType + "\n" +
                    "End\n\n";

            try {
                fileWriter.write(itemText);
                fileWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    fileWriter.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            // Save the image
            try {
                File saveDir = wardrobe.getCategoryDir(wardrobe.storedArgs[0], category);
                File savedFile;
                if (!saveDir.exists()) saveDir.mkdirs();
                if(category.equalsIgnoreCase("Shirts"))
                savedFile = new File(saveDir,  "Shir" + shirtid + ".png");
                else if(category.equalsIgnoreCase("Pants"))
                savedFile = new File(saveDir,  "Pan" + pantsid + ".png");
                else if(category.equalsIgnoreCase("Shoes"))
                savedFile = new File(saveDir,  "Shoe" + shoesid + ".png");
                else if (category.equalsIgnoreCase("Cultural Dress"))
                savedFile = new File(saveDir,  "CD" + culturalDressid + ".png");
                else
                savedFile = new File(saveDir,  selectedFile[0].getName());
                ImageIO.write(ImageIO.read(selectedFile[0]), "png", savedFile);

                // Create image panel
                BufferedImage originalImage = ImageIO.read(savedFile);
                Image scaledImage = getHighQualityScaledImage(originalImage, 300, 300);

                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageLabel.setPreferredSize(new Dimension(300, 300));

                JButton deleteButton = new JButton("Delete");
                deleteButton.setPreferredSize(new Dimension(180, 50));
                deleteButton.setBackground(new Color(255, 94, 94));
                deleteButton.setForeground(Color.WHITE);
                deleteButton.setFocusPainted(false);
                deleteButton.setBorder(new WardrobeRoundedBorder(15, new Color(255, 94, 94)));

                JPanel imagePanel = new JPanel(new BorderLayout());
                imagePanel.setBackground(new Color(130, 90, 174));
                imagePanel.setPreferredSize(new Dimension(300, 360));
                imagePanel.add(imageLabel, BorderLayout.CENTER);
                imagePanel.add(deleteButton, BorderLayout.SOUTH);

                deleteButton.addActionListener(del -> {
                        String fileName = savedFile.getName();
                        String id = fileName.substring(0, fileName.lastIndexOf('.'));  // e.g., Shir1
                    deleteLinesInRange(
                        wardrobe.getCategoryTextFile(wardrobe.storedArgs[0], category).getAbsolutePath(), 
                        id, 
                        "End"
                    );
                    imageContainer.remove(imagePanel);
                    imageContainer.revalidate();
                    imageContainer.repaint();
                    savedFile.delete();// Delete from text file
                    
                });

                // Add the new image to the container
                imageContainer.remove(button);
                imageContainer.add(imagePanel);
                imageContainer.add(button);
                imageContainer.revalidate();
                imageContainer.repaint();

                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(saveButton, gbc);

        dialog.setVisible(true);
    });

    return button;
}
//Deleting data from file
    public static void deleteLinesInRange(String filePath, String startString, String endString) {
    List<String> updatedLines = new ArrayList<>();
    boolean inDeletionRange = false;

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        String line;
        boolean foundStart = false;

        while ((line = reader.readLine()) != null) {
            if (!inDeletionRange && line.contains(startString)) {
                // Found the start of the range to delete
                inDeletionRange = true;
                foundStart = true;
                continue; // Skip this line (don't add to updatedLines)
            }

            if (inDeletionRange) {
                if (line.contains(endString)) {
                    // Found the end of the range to delete
                    inDeletionRange = false;
                }
                continue; // Skip all lines within the deletion range
            }

            // If we're not in deletion range, keep the line
            updatedLines.add(line);
        }

        if (foundStart && inDeletionRange) {
            // If we found the start but never found the end, that's a problem
            System.err.println("Warning: Found start but no matching end for: " + startString);
        }
    } catch (IOException e) {
        e.printStackTrace();
        return; // Don't proceed if there was an error reading
    }

    // Write the filtered content back to the file
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
        for (String updatedLine : updatedLines) {
            writer.write(updatedLine);
            writer.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    private Image getHighQualityScaledImage(BufferedImage originalImage, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        return scaledImage;
    }

    public void showPanel(String name) {
        cardLayout.show(contentPanel, name);
    }
}

class OutfitBackground extends JPanel {
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(225, 240, 255));
    }
}

class OutfitPanel extends JPanel {
    public OutfitPanel(OutfitUI ui) {
        setLayout(null);
        setPreferredSize(new Dimension(420, 760));
        setBackground(new Color(225, 240, 255));

        int startY = 110;
        int gap = 105;

        add(createButton("Shirts", startY, ui));
        add(createButton("Pants", startY + gap, ui));
        add(createButton("Shoes", startY + 2 * gap, ui));
        add(createButton("Cultural Dress", startY + 3 * gap, ui));
    }

    private JButton createButton(String name, int y, OutfitUI ui) {
        JButton button = new OutfitButton(name, 45, y).getButton();
        button.addActionListener(e -> ui.showPanel(name));
        return button;
    }
}

class TitleLabel extends JLabel {
    public TitleLabel(String text) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 32));
        setForeground(new Color(40, 80, 120));
    }
}

class OutfitButton {
    private JButton button;

    public OutfitButton(String text, int x, int y) {
        button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        button.setBounds(x, y, 320, 66);
        button.setBackground(new Color(120, 180, 255));
        button.setForeground(new Color(40, 80, 120));
        button.setFont(new Font("SansSerif", Font.BOLD, 26));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new WardrobeRoundedBorder(20, new Color(80, 140, 200)));
    }

    public JButton getButton() {
        return button;
    }
}

class WardrobeRoundedBorder extends AbstractBorder {
    private int radius;
    private Color color;

    public WardrobeRoundedBorder(int radius, Color color) {
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
    public boolean isBorderOpaque() {
        return true;
    }
}

class WrapLayout extends FlowLayout {
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
            int targetWidth = target.getParent() instanceof JScrollPane
                ? ((JScrollPane) target.getParent().getParent()).getViewport().getWidth()
                : target.getWidth();

            if (targetWidth == 0)
                targetWidth = Integer.MAX_VALUE;

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