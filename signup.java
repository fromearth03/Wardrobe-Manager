package deeplearning;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.AbstractBorder;
import java.io.IOException;

// ------------------------- MAIN CLASS (Only main method) -------------------------
public class signup {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SignupMainUI ui = new SignupMainUI();
            ui.setVisible(true);
        });
    }
}
// ------------------------- MAIN UI FRAME -------------------------
class SignupMainUI extends JFrame {
    public SignupMainUI() {
        setTitle("Create Account");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Allow maximized
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        SignupBackgroundPanel panel = new SignupBackgroundPanel();
        panel.setLayout(new GridBagLayout()); // Center contents
        add(panel);

        SignupFormPanel form = new SignupFormPanel();
        panel.add(form);
    }
}

// ------------------------- BACKGROUND PANEL -------------------------
class SignupBackgroundPanel extends JPanel {
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(225, 240, 255)); // Light blue
    }
}
// ------------------------- FORM PANEL (Encapsulated Fields) -------------------------
class SignupFormPanel extends JPanel {
    SignupInputField firstName = new SignupInputField(80, 165, 260, 58);
    SignupInputField lastName = new SignupInputField(360, 165, 260, 58);
    SignupInputField email = new SignupInputField(80, 305, 540, 58);
    SignupPasswordField password = new SignupPasswordField(80, 445, 540, 58);

    public SignupFormPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(760, 980));
        setBackground(new Color(225, 240, 255)); // Match background

        add(new SignupTitleLabel("Create Account"));

        add(new SignupTextLabel("First name", 80, 130));
        add(new SignupTextLabel("Last name", 360, 130));
        add(firstName.getField());
        add(lastName.getField());

        add(new SignupTextLabel("Email", 80, 270));
        add(email.getField());

        add(new SignupTextLabel("Password", 80, 410));
        add(password.getField());

        SignupGenderPanel genderPanel = new SignupGenderPanel(80, 560);
        add(genderPanel.getLabel());
        add(genderPanel.getMaleButton());
        add(genderPanel.getFemaleButton());
        add(genderPanel.getOtherButton());

        add(new SignupActionButton("Sign In", 80, 710, 540, 72, firstName , lastName, email, password , genderPanel).getButton());
        add(new SignupLoginButton("Already Have an account? Login", 80, 800, 540, 66).getButton());
    }
}
// ------------------------- TITLE LABEL -------------------------
class SignupTitleLabel extends JLabel {
    public SignupTitleLabel(String text) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 50));
        setForeground(new Color(40, 80, 120)); // Deep blue
        setBounds(180, 36, 420, 62);
        setHorizontalAlignment(SwingConstants.CENTER);
    }
}

// ------------------------- TEXT LABEL -------------------------
class SignupTextLabel extends JLabel {
    public SignupTextLabel(String text, int x, int y) {
        super(text);
        setFont(new Font("SansSerif", Font.PLAIN, 24));
        setForeground(new Color(40, 80, 120)); // Deep blue
        setBounds(x, y, 260, 36);
    }
}

// ------------------------- TEXT FIELD COMPONENT -------------------------
class SignupInputField {
    private JTextField field;

    public SignupInputField(int x, int y, int w, int h) {
        field = new JTextField();
        field.setBounds(x, y, w, h);
        field.setFont(new Font("SansSerif", Font.PLAIN, 24));
    }

    public JTextField getField() {
        return field;
    }
}

// ------------------------- PASSWORD FIELD COMPONENT -------------------------
class SignupPasswordField {
    private JPasswordField field;

    public SignupPasswordField(int x, int y, int w, int h) {
        field = new JPasswordField();
        field.setBounds(x, y, w, h);
        field.setFont(new Font("SansSerif", Font.PLAIN, 24));
    }

    public JPasswordField getField() {
        return field;
    }
}

// ------------------------- GENDER PANEL -------------------------
class SignupGenderPanel {
    private JLabel label;
    private JRadioButton male, female, other;
    private ButtonGroup group;

    public SignupGenderPanel(int x, int y) {
        label = new JLabel("Gender:");
        label.setBounds(x, y, 140, 44);
        label.setForeground(new Color(40, 80, 120));
        label.setFont(new Font("SansSerif", Font.PLAIN, 24));

        male = new JRadioButton("Male");
        female = new JRadioButton("Female");
        other = new JRadioButton("Other");

        Color bgColor = new Color(225, 240, 255);
        Color fgColor = new Color(40, 80, 120);

        male.setBackground(bgColor);
        female.setBackground(bgColor);
        other.setBackground(bgColor);

        male.setForeground(fgColor);
        female.setForeground(fgColor);
        other.setForeground(fgColor);

        male.setFont(new Font("SansSerif", Font.PLAIN, 22));
        female.setFont(new Font("SansSerif", Font.PLAIN, 22));
        other.setFont(new Font("SansSerif", Font.PLAIN, 22));

        male.setBounds(x + 150, y, 120, 44);
        female.setBounds(x + 290, y, 140, 44);
        other.setBounds(x + 450, y, 140, 44);

        group = new ButtonGroup();
        group.add(male);
        group.add(female);
        group.add(other);
    }

    public JLabel getLabel() { return label; }
    public JRadioButton getMaleButton() { return male; }
    public JRadioButton getFemaleButton() { return female; }
    public JRadioButton getOtherButton() { return other; }
    public String getSelectedGender() {
        if (male.isSelected()) return "Male";
        if (female.isSelected()) return "Female";
        if (other.isSelected()) return "Other";
        return "Unspecified";
    }
}

// ------------------------- SIGN IN BUTTON -------------------------
class SignupActionButton {
    private JButton button;
    private SignupInputField field;
    UserAccount userAccount = new UserAccount();

    public SignupActionButton(String text, int x, int y, int w, int h, SignupInputField firstName, SignupInputField lastName, SignupInputField email, SignupPasswordField password, SignupGenderPanel genderPanel) {
        this.field = firstName;

        button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        button.setBounds(x, y, w, h);
        button.setBackground(new Color(120, 180, 255));  // Soft blue
        button.setForeground(new Color(40, 80, 120));
        button.setFont(new Font("SansSerif", Font.BOLD, 27));
        button.setFocusPainted(false);
        button.setBorder(new SignupRoundedBorder(25, new Color(80, 140, 200)));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        userAccount.loadUsersFromDefaultFile();

        // Action listener for creating new user (sign up)
        button.addActionListener(e -> {
            userAccount.loadUsersFromDefaultFile();

            String firstNameValue = firstName.getField().getText().trim();
            String lastNameValue = lastName.getField().getText().trim();
            String emailValue = email.getField().getText().trim();
            String passwordValue = new String(password.getField().getPassword()).trim();

            user Newuser = new user(firstNameValue, lastNameValue, emailValue, passwordValue, genderPanel.getSelectedGender());

            // Validate fields
            if (firstNameValue.isEmpty() || lastNameValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill in all fields.");
                return;
            }

            if (!emailValue.contains("@")) {
                JOptionPane.showMessageDialog(null, "Please enter a valid email address.");
                return;
            }

            if (passwordValue.length() < 8) {
                JOptionPane.showMessageDialog(null, "Password must be at least 8 characters long.");
                return;
            }
            if (genderPanel.getSelectedGender().equals("Unspecified")) {
                JOptionPane.showMessageDialog(null, "Please select one gender option.");
                return;
            }
            if(firstNameValue.equals("admin") && lastNameValue.equals("admin")) {
                JOptionPane.showMessageDialog(null, "Admin account cannot be created.");
                return;
            }

            // Check if user already exists
            if(userAccount.searchUserByEmail(emailValue) != null) {
                JOptionPane.showMessageDialog(null, "User already exists.");
                user temp = userAccount.searchUserByEmail(emailValue);
                if(temp.getFirstName().equalsIgnoreCase(firstNameValue) && temp.getLastName().equalsIgnoreCase(lastNameValue))
                JOptionPane.showMessageDialog(null, "Name already take please choose any other name.");
                return;
            }
           
            // Save user to file
            try {
    userAccount.appendUserToDefaultFile(Newuser);
    
    String userDirectoryName = firstNameValue + lastNameValue;
    java.io.File userDirectory = new java.io.File(
        new java.io.File(System.getProperty("user.home"), "WardrobeManagerData"),
        userDirectoryName
    );
    if (!userDirectory.exists() && !userDirectory.mkdirs()) {
        throw new IOException("Failed to create user directory: " + userDirectory.getAbsolutePath());
    }
} catch (IOException ex) {
    ex.printStackTrace();
}

            userAccount.addUser(Newuser);
            JOptionPane.showMessageDialog(null, "Account created successfully Now you can Login.");
            login.main(null);
            Window signupWindow = SwingUtilities.getWindowAncestor(button);
            if (signupWindow != null) {
                signupWindow.dispose();
            }
        });
    }

    public JButton getButton() {
        return button;
    }
}

    
class SignupRoundedBorder extends AbstractBorder {
    private int radius;
    private Color color;

    public SignupRoundedBorder(int radius) {
        this(radius, Color.GRAY); // default color
    }

    public SignupRoundedBorder(int radius, Color color) {
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
        insets.left = insets.right = insets.top = insets.bottom = this.radius+1;
        return insets;
    }
}
class SignupLoginButton {
    private JButton button;

    public SignupLoginButton(String text, int x, int y, int w, int h) {
        button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        button.setBounds(x, y, w, h);
        button.setBackground(new Color(120, 180, 255));  // Soft blue
        button.setForeground(new Color(40, 80, 120));
        button.setFont(new Font("SansSerif", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setBorder(new SignupRoundedBorder(25, new Color(80, 140, 200)));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        // Action listener for login (only login logic here)
        button.addActionListener(e -> {
            login.main(null);
            Window signupWindow = SwingUtilities.getWindowAncestor(button);
            if (signupWindow != null) {
                signupWindow.dispose();
            }
        });
    }

    public JButton getButton() {
        return button;
    }
}
