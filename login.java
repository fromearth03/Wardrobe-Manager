package deeplearning;

import javax.swing.*;
import java.awt.*;

// ------------------------- MAIN CLASS (Only main method) -------------------------
public class login {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginUIFrame ui = new LoginUIFrame();
            ui.setVisible(true);
        });
    }
}

// ------------------------- LOGIN UI FRAME -------------------------
class LoginUIFrame extends JFrame {
    public LoginUIFrame() {
        setTitle("Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        LoginBackgroundPanel panel = new LoginBackgroundPanel();
        panel.setLayout(new GridBagLayout());
        add(panel);

        LoginFormPanel loginForm = new LoginFormPanel();
        panel.add(loginForm);
    }
}

// ------------------------- BACKGROUND PANEL (Same as signup) -------------------------
class LoginBackgroundPanel extends JPanel {
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Set a very light blue background
        setBackground(new Color(225, 240, 255));
    }
}

// ------------------------- LOGIN FORM PANEL -------------------------
class LoginFormPanel extends JPanel {
    LoginInputField email = new LoginInputField(70, 160, 520, 60);
    LoginPasswordField password = new LoginPasswordField(70, 300, 520, 60);

    public LoginFormPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(680, 540));
        setBackground(new Color(225, 240, 255)); // Match background

        add(new LoginTitleLabel("Login"));

        add(new LoginTextLabel("Email", 70, 126));
        add(email.getField());

        add(new LoginTextLabel("Password", 70, 266));
        add(password.getField());

        add(new LoginActionButton("Login", 70, 410, 520, 68 , email , password).getButton());
    }
}

// ------------------------- TEXT FIELD COMPONENT (Reused) -------------------------
class LoginInputField {
    private JTextField field;

    public LoginInputField(int x, int y, int w, int h) {
        field = new JTextField();
        field.setBounds(x, y, w, h);
        field.setFont(new Font("SansSerif", Font.PLAIN, 24));
    }

    public JTextField getField() {
        return field;
    }
}

// ------------------------- PASSWORD FIELD COMPONENT (Reused) -------------------------
class LoginPasswordField {
    private JPasswordField field;

    public LoginPasswordField(int x, int y, int w, int h) {
        field = new JPasswordField();
        field.setBounds(x, y, w, h);
        field.setFont(new Font("SansSerif", Font.PLAIN, 24));
    }

    public JPasswordField getField() {
        return field;
    }
}

// ------------------------- TITLE LABEL (Reused) -------------------------
class LoginTitleLabel extends JLabel {
    public LoginTitleLabel(String text) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 46));
        setForeground(new Color(40, 80, 120)); // Deep blue for contrast
        setBounds(238, 30, 300, 58);
    }
}

// ------------------------- TEXT LABEL (Reused) -------------------------
class LoginTextLabel extends JLabel {
    public LoginTextLabel(String text, int x, int y) {
        super(text);
        setFont(new Font("SansSerif", Font.PLAIN, 24));
        setForeground(new Color(40, 80, 120)); // Deep blue for contrast
        setBounds(x, y, 260, 36);
    }
}

// ------------------------- LOGIN BUTTON -------------------------
class LoginActionButton {
    private JButton button;
    UserAccount userAccount = new UserAccount();

    public LoginActionButton(String text, int x, int y, int w, int h , LoginInputField email, LoginPasswordField password) {
        button = new JButton(text) {
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
        userAccount.loadUsersFromDefaultFile();
        button.setBounds(x, y, w, h);
        // Soft blue button with deeper blue border
        button.setBackground(new Color(120, 180, 255));
        button.setForeground(new Color(40, 80, 120));
        button.setFont(new Font("SansSerif", Font.BOLD, 26));
        button.setFocusPainted(false);
        button.setBorder(new LoginRoundedBorder(20, new Color(80, 140, 200)));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.addActionListener(e -> {
            userAccount.loadUsersFromDefaultFile();

            String emailText = email.getField().getText().trim();
            String passwordText = new String(password.getField().getPassword()).trim();

            // Check if the user exists
            user foundUser = userAccount.searchUserByEmail(emailText);
            if (foundUser != null && foundUser.getPassword() != null && foundUser.getPassword().trim().equals(passwordText)) {
                // City selection dialog
                String[] cities = {
                    "Karachi", "Lahore", "Islamabad", "Rawalpindi", "Faisalabad",
                    "Multan", "Peshawar", "Quetta", "Sialkot", "Gujranwala",
                    "Hyderabad", "Bahawalpur", "Sargodha", "Sukkur", "Larkana"
                };
                String selectedCity = showCitySelectionDialog(button, cities);

                if (selectedCity == null || selectedCity.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please select a city to continue.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String username = foundUser.getFirstName() + foundUser.getLastName();
                String [] uname = {username , foundUser.getEmail() , selectedCity};
                mainpage.main(uname);
                Window loginWindow = SwingUtilities.getWindowAncestor(button);
                if (loginWindow != null) {
                    loginWindow.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public JButton getButton() {
        return button;
    }

    private String showCitySelectionDialog(Component parent, String[] cities) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Choose City", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(18, 18));
        dialog.getContentPane().setBackground(new Color(225, 240, 255));
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(parent);

        JLabel titleLabel = new JLabel("Select your location", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        titleLabel.setForeground(new Color(40, 80, 120));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JList<String> cityList = new JList<>(cities);
        cityList.setFont(new Font("SansSerif", Font.PLAIN, 24));
        cityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cityList.setSelectedIndex(0);
        cityList.setFixedCellHeight(44);
        cityList.setBackground(Color.WHITE);
        cityList.setForeground(new Color(40, 80, 120));
        cityList.setSelectionBackground(new Color(120, 180, 255));
        cityList.setSelectionForeground(new Color(40, 80, 120));

        JScrollPane listScrollPane = new JScrollPane(cityList);
        listScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
        listScrollPane.getVerticalScrollBar().setUnitIncrement(24);

        JButton confirmButton = new JButton("Continue");
        confirmButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        confirmButton.setPreferredSize(new Dimension(180, 56));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("SansSerif", Font.PLAIN, 22));
        cancelButton.setPreferredSize(new Dimension(160, 56));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 12));
        actionPanel.setBackground(new Color(225, 240, 255));
        actionPanel.add(confirmButton);
        actionPanel.add(cancelButton);

        final String[] selectedCity = {null};

        confirmButton.addActionListener(event -> {
            selectedCity[0] = cityList.getSelectedValue();
            dialog.dispose();
        });

        cityList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    selectedCity[0] = cityList.getSelectedValue();
                    dialog.dispose();
                }
            }
        });

        cancelButton.addActionListener(event -> dialog.dispose());

        dialog.add(titleLabel, BorderLayout.NORTH);
        dialog.add(listScrollPane, BorderLayout.CENTER);
        dialog.add(actionPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);

        return selectedCity[0];
    }
}

// ------------------------- ROUNDED BORDER (Reused) -------------------------
class LoginRoundedBorder extends javax.swing.border.AbstractBorder {
    private int radius;
    private Color color;

    public LoginRoundedBorder(int radius, Color color) {
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