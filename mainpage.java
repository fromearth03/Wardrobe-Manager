package deeplearning;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// ------------------------- MAIN CLASS -------------------------
public class mainpage {
    public static String[] storedArgs; // Store args here
    public static void main(String[] args) {
        storedArgs = normalizeArgs(args);
        SwingUtilities.invokeLater(() -> {
            DashboardUI ui = new DashboardUI();
            ui.setVisible(true);
        });
    }

    private static String[] normalizeArgs(String[] args) {
        String username = "GuestUser";
        String email = "guest@example.com";
        String city = "Karachi";

        if (args != null) {
            if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
                username = args[0].trim();
            }
            if (args.length > 1 && args[1] != null && !args[1].trim().isEmpty()) {
                email = args[1].trim();
            }
            if (args.length > 2 && args[2] != null && !args[2].trim().isEmpty()) {
                city = args[2].trim();
            }
        }

        return new String[] {username, email, city};
    }

    public static String[] safeArgs() {
        if (storedArgs == null || storedArgs.length < 3) {
            storedArgs = normalizeArgs(storedArgs);
        }
        return storedArgs;
    }
}

// ------------------------- MAIN UI FRAME -------------------------
class DashboardUI extends JFrame {
    public DashboardUI() {
        setTitle("Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        DashboardBackgroundPanel panel = new DashboardBackgroundPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(225, 240, 255)); // Light blue
        add(panel);

        DashboardPanel form = new DashboardPanel();
        panel.add(form);
    }
}

// ------------------------- BACKGROUND PANEL -------------------------
class DashboardBackgroundPanel extends JPanel {
    private Image backgroundImage;

    public DashboardBackgroundPanel() {
        java.io.File appRootDir = new java.io.File(System.getProperty("user.home"), "WardrobeManagerData");
        java.io.File backgroundFile = new java.io.File(appRootDir, "dashboard-bg.png");
        backgroundImage = backgroundFile.exists() ? new ImageIcon(backgroundFile.getAbsolutePath()).getImage() : null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

// ------------------------- DASHBOARD PANEL -------------------------
class DashboardPanel extends JPanel {
    private final int panelWidth;
    private final int panelHeight;
    private final int buttonWidth;
    private final int buttonHeight;

    public DashboardPanel() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        panelWidth = Math.max(700, Math.min(920, (int) (screen.width * 0.46)));
        panelHeight = Math.max(760, Math.min(980, (int) (screen.height * 0.80)));
        buttonWidth = Math.max(420, Math.min(560, (int) (panelWidth * 0.70)));
        buttonHeight = Math.max(68, Math.min(84, (int) (panelHeight * 0.095)));

        setLayout(null);
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(Color.WHITE); // White panel
        setOpaque(true);

        add(new DashboardTitle("Welcome to Your Dashboard", panelWidth));

        int buttonX = (panelWidth - buttonWidth) / 2;
        int startY = Math.max(130, (int) (panelHeight * 0.20));
        int gap = Math.max(18, (int) (panelHeight * 0.03));

        add(new DashboardButton("Closet", buttonX, startY, buttonWidth, buttonHeight, new ClosetHandler()).getButton());
        add(new DashboardButton("Styling Expert", buttonX, startY + (buttonHeight + gap), buttonWidth, buttonHeight, new StylingHandler()).getButton());
        add(new DashboardButton("Donation", buttonX, startY + 2 * (buttonHeight + gap), buttonWidth, buttonHeight, new DonationHandler()).getButton());
        add(new DashboardButton("Calendar", buttonX, startY + 3 * (buttonHeight + gap), buttonWidth, buttonHeight, new CalendarHandler()).getButton());
        add(new DashboardButton("Future Updates", buttonX, startY + 4 * (buttonHeight + gap), buttonWidth, buttonHeight, new WeatherHandler()).getButton());
        add(new DashboardButton("Account", buttonX, startY + 5 * (buttonHeight + gap), buttonWidth, buttonHeight, new AccountHandler()).getButton());
    }
}

// ------------------------- DASHBOARD TITLE -------------------------
class DashboardTitle extends JLabel {
    public DashboardTitle(String text, int panelWidth) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 36));
        setForeground(new Color(40, 80, 120));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBounds(30, 38, panelWidth - 60, 56);
    }
}

// ------------------------- DASHBOARD BUTTON -------------------------
class DashboardButton {
    private JButton button;

    public DashboardButton(String text, int x, int y, int width, int height, ActionListener listener) {
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
        button.setBounds(x, y, width, height);
        button.setBackground(new Color(120, 180, 255));
        button.setForeground(new Color(40, 80, 120));
        button.setFont(new Font("SansSerif", Font.BOLD, 26));
        button.setFocusPainted(false);
        button.setBorder(new DashboardRoundedBorder(20, new Color(80, 140, 200)));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.addActionListener(listener);
    }

    public JButton getButton() {
        return button;
    }
}

// ------------------------- ROUNDED BORDER -------------------------
class DashboardRoundedBorder extends javax.swing.border.AbstractBorder {
    private int radius;
    private Color color;

    public DashboardRoundedBorder(int radius, Color color) {
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

// ------------------------- INDIVIDUAL EVENT HANDLERS -------------------------

class ClosetHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        wardrobe.main(mainpage.safeArgs());
    }
}

class StylingHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        App.main(mainpage.safeArgs());
    }
}

class InspirationHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "Loading Inspiration Board...", "Inspiration", JOptionPane.INFORMATION_MESSAGE);
    }
}

class CalendarHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String[] args = mainpage.safeArgs();
        String []cin = {args[0] , "", args[2]};
        calendar.main (cin);
    }
}

class DonationHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        DonationUI.main(mainpage.safeArgs());
    }
}

class WeatherHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        AvatarApp.main(mainpage.safeArgs());
    }
}

class AccountHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        profile.main(mainpage.safeArgs());
    }
}
