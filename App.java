package deeplearning;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

class App {
    private static final File APP_BASE_DIRECTORY = new File(System.getProperty("user.home"), "WardrobeManagerData");
    private static final String DEFAULT_OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static String conversationHistory = "";
    private static String latestGeminiAnswer = "";
     
   
    public static String wingemini () {
        ArrayList<Forecast> weeklyForecast = Weather.getforecast();
        if (weeklyForecast.isEmpty()) {
            return "No weather data available for the next 7 days.";
        }

        StringBuilder weather = new StringBuilder("7-day weather forecast:\n");
        for (Forecast f : weeklyForecast) {
            weather.append("- ")
                .append(String.format("%04d-%02d-%02d", f.getYear(), f.getMonth(), f.getDate()))
                .append(": ")
                .append(f.getCondition())
                .append(", min ")
                .append(String.format("%.1f", f.getMintemp()))
                .append("°C, max ")
                .append(String.format("%.1f", f.getMaxtemp()))
                .append("°C\n");
        }
        return weather.toString();
    }
    
    static String [] storedArgs ;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a username as an argument.");
            System.exit(1);
        }
        storedArgs = args;
        String username = args[0];
        conversationHistory = loadUserConversationHistory(username);

        JFrame frame = new JFrame("AI Styling");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen like wardrobe
        frame.getContentPane().setBackground(new Color(225, 240, 255)); // Light blue background

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(225, 240, 255));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(225, 240, 255));
        JLabel titleLabel = new JLabel("AI Styling Assistant");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 44));
        titleLabel.setForeground(new Color(40, 80, 120));
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Center panel for output
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(200, 220, 255));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(new Color(240, 248, 255)); // Very light blue
        outputArea.setForeground(new Color(40, 80, 120));
        outputArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        outputArea.setFont(new Font("SansSerif", Font.PLAIN, 28));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(1600, 900));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Input panel at bottom
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(200, 220, 255));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User input components
        JLabel userInputLabel = new JLabel("Your Question:");
        userInputLabel.setForeground(new Color(40, 80, 120));
        userInputLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(userInputLabel, gbc);

        JTextField userInputField = new JTextField(30);
        userInputField.setFont(new Font("SansSerif", Font.PLAIN, 24));
        userInputField.setPreferredSize(new Dimension(820, 64));
        userInputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 180, 255), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(userInputField, gbc);

        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setForeground(new Color(40, 80, 120));
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        inputPanel.add(dateLabel, gbc);

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setValue(new Date());
        dateSpinner.setFont(new Font("SansSerif", Font.PLAIN, 22));
        dateSpinner.setPreferredSize(new Dimension(300, 56));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(dateSpinner, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton sendButton = createStyledButton("Send");
        JButton historyButton = createStyledButton("Add Event");
        JButton exitButton = createStyledButton("Exit");

        buttonPanel.add(sendButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(exitButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        centerPanel.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        // Load and display closet details initially
        String closetDetails = loadUserClosetDetails(username);
        outputArea.setText("Your Closet Details:\n\n" + closetDetails + 
                         "\n\nHow can I help you with your wardrobe today?");

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userInput = userInputField.getText().trim();
                Date selectedDateValue = (Date) dateSpinner.getValue();
                LocalDate selectedDate = selectedDateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if (userInput.equalsIgnoreCase("exit")) {
                    System.exit(0);
                }

                outputArea.setText("Processing your request...");
                
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    protected Void doInBackground() throws Exception {
                        try {
                            String closetDetails = loadUserClosetDetails(username);
                            String selectedDateEvent = loadCalendarEventForDate(username, selectedDate);
                            String selectedDateEventContext = selectedDateEvent.isEmpty()
                                ? "No stored event found for this date."
                                : selectedDateEvent;

                            String contextPrompt = "You are a fashion assistant helping with wardrobe choices. " +
                                "Here is the user's closet inventory:\n" + closetDetails + "\n\n" +
                                "Previous conversation history:\n" + conversationHistory + "\n\n" +
                                "7-day weather forecast (must be considered):\n" + wingemini() + "\n\n" +
                                "Selected date: " + selectedDate + "\n" +
                                "Stored event on selected date:\n" + selectedDateEventContext + "\n\n" +
                                "Current request: " + userInput +
                                "\n\nProvide outfit recommendations that explicitly consider wardrobe inventory, 7-day weather forecast, and the selected-date event details.";

                                String escapedPrompt = escapeJson(contextPrompt);
                                String requestBody = "{" +
                                    "\"model\":\"openai/gpt-4o-mini\"," +
                                    "\"messages\":[" +
                                    "{\"role\":\"system\",\"content\":\"You are a helpful fashion assistant. Always consider provided weather forecast, wardrobe inventory, and selected-date event details before suggesting outfits.\"}," +
                                    "{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}" +
                                    "]" +
                                    "}";

                                String apiKey = resolveOpenRouterApiKey();
                                if (apiKey == null || apiKey.isBlank()) {
                                    outputArea.setText("OpenRouter API key is not configured. Set OPENROUTER_API_KEY (env or JVM property) or add it to ~/WardrobeManagerData/openrouter.properties.");
                                    return null;
                                }
                                String apiUrl = resolveOpenRouterApiUrl();

                            URL url = java.net.URI.create(apiUrl).toURL();
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                            conn.setRequestProperty("HTTP-Referer", "http://localhost");
                            conn.setRequestProperty("X-OpenRouter-Title", "Wardrobe Manager");
                            conn.setConnectTimeout(10000);
                            conn.setReadTimeout(30000);
                            conn.setDoOutput(true);

                            try (OutputStream os = conn.getOutputStream()) {
                                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                                os.write(input, 0, input.length);
                            }

                            int statusCode = conn.getResponseCode();
                            InputStream stream = (statusCode >= 200 && statusCode < 300)
                                    ? conn.getInputStream()
                                    : conn.getErrorStream();

                            StringBuilder response = new StringBuilder();
                            if (stream != null) {
                                try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                                    String inputLine;
                                    while ((inputLine = in.readLine()) != null) {
                                        response.append(inputLine);
                                    }
                                }
                            }
                            conn.disconnect();

                            String responseBody = response.toString();
                            if (statusCode < 200 || statusCode >= 300) {
                                outputArea.setText("Styling assistant API error (" + statusCode + "): " + extractApiErrorMessage(responseBody));
                                return null;
                            }

                            String formattedResponse = extractTextFromResponse(responseBody);
                            conversationHistory = updateHistory(conversationHistory, userInput, formattedResponse);
                            saveConversationToFile(username, userInput, formattedResponse);
                            latestGeminiAnswer = formattedResponse;
                            outputArea.setText(formattedResponse);
                        } catch (Exception apiException) {
                            outputArea.setText("Request failed: " + apiException.getMessage());
                        }
                        return null;
                    }
                };

                worker.execute();
            }
        });
        

        historyButton.addActionListener(new ActionListener() {
            //String [] cin = {username , latestGeminiAnswer , App.storedArgs[2]};
            public void actionPerformed(ActionEvent e) {
                String [] cin = {username , latestGeminiAnswer ,App.storedArgs[2]};
                calendar.main(cin);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
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
        button.setBackground(new Color(120, 180, 255)); // Soft blue
        button.setForeground(new Color(40, 80, 120));
        button.setFont(new Font("SansSerif", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new AppRoundedBorder(20, new Color(80, 140, 200)));
        button.setPreferredSize(new Dimension(260, 72));
        return button;
    }

    private static String loadUserClosetDetails(String username) {
        StringBuilder closetDetails = new StringBuilder("Closet Details for " + username + ":\n\n");

        File userDir = getUserDirectory(username);
        if (!userDir.exists() || !userDir.isDirectory()) {
            return "No closet details found for user: " + username;
        }

        // Categories to load
        String[] categories = {"Shirts", "Pants", "Shoes", "Cultural Dress"};
        for (String category : categories) {
            File textFile = getCategoryTextFile(username, category);
            
            if (textFile.exists()) {
                closetDetails.append("=== ").append(category).append(" ===\n");
                try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        closetDetails.append(line).append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                closetDetails.append("\n");
            }
        }

        return closetDetails.toString();
    }

    private static String extractTextFromResponse(String response) {
        try {
            StringBuilder result = new StringBuilder();
            String[] markers = {"\"content\":\"", "\"text\":\""};

            for (String marker : markers) {
                int index = 0;
                while (index < response.length()) {
                    int start = response.indexOf(marker, index);
                    if (start == -1) {
                        break;
                    }
                    start += marker.length();
                    int end = start;
                    boolean escaped = false;
                    while (end < response.length()) {
                        char currentChar = response.charAt(end);
                        if (currentChar == '"' && !escaped) {
                            break;
                        }
                        escaped = currentChar == '\\' && !escaped;
                        if (currentChar != '\\') {
                            escaped = false;
                        }
                        end++;
                    }

                    if (end > start) {
                        String rawText = response.substring(start, end);
                        String decodedText = rawText
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\");
                        if (!decodedText.trim().isEmpty()) {
                            result.append(decodedText).append("\n");
                        }
                    }
                    index = end + 1;
                }

                if (result.length() > 0) {
                    break;
                }
            }

            String extractedText = result.toString().trim();
            if (extractedText.isEmpty()) {
                return "No response generated. Please try again.";
            }
            return extractedText;
        } catch (Exception e) {
            return "Error processing response: " + e.getMessage();
        }
    }

    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String extractApiErrorMessage(String response) {
        if (response == null || response.isEmpty()) {
            return "Empty error response from server.";
        }
        String marker = "\"message\":\"";
        int start = response.indexOf(marker);
        if (start == -1) {
            return response;
        }
        start += marker.length();
        int end = response.indexOf('"', start);
        if (end == -1) {
            return response;
        }
        return response.substring(start, end)
                .replace("\\n", " ")
                .replace("\\\"", "\"")
                .trim();
    }

    private static String updateHistory(String history, String userInput, String response) {
        // Keep the history to a reasonable length
        String[] lines = history.split("\n");
        if (lines.length > 50) { // Keep last 50 lines
            StringBuilder shortened = new StringBuilder();
            int start = lines.length - 25; // Keep last 25 lines
            for (int i = start; i < lines.length; i++) {
                shortened.append(lines[i]).append("\n");
            }
            history = shortened.toString();
        }
        return history + "User: " + userInput + "\nGemini: " + response + "\n\n";
    }

    private static void saveConversationToFile(String username, String userInput, String response) {
        File userDir = getUserDirectory(username);
        if (!userDir.exists()) {
            if (!userDir.mkdirs()) {
                System.err.println("Failed to create user directory: " + userDir.getAbsolutePath());
                return;
            }
        }

        File historyFile = getConversationHistoryFile(username);
        try (FileWriter writer = new FileWriter(historyFile, true)) {
            writer.write("User: " + userInput + "\n");
            writer.write("Gemini: " + response + "\n");
            writer.write("--------------------------------------------------\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadUserConversationHistory(String username) {
        File historyFile = getConversationHistoryFile(username);

        StringBuilder history = new StringBuilder();

        if (historyFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    history.append(line).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return history.toString();
    }

    private static File getUserDirectory(String username) {
        return new File(APP_BASE_DIRECTORY, username);
    }

    private static File getCategoryTextFile(String username, String category) {
        return new File(new File(getUserDirectory(username), category), category + ".txt");
    }

    private static File getConversationHistoryFile(String username) {
        return new File(getUserDirectory(username), "geminiconversationhistory.txt");
    }

    private static String loadCalendarEventForDate(String username, LocalDate selectedDate) {
        File calendarDir = new File(getUserDirectory(username), "Calendar");
        if (!calendarDir.exists() || !calendarDir.isDirectory()) {
            return "";
        }

        String monthFileName = selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + selectedDate.getYear() + ".txt";
        File monthFile = new File(calendarDir, monthFileName);
        if (!monthFile.exists()) {
            return "";
        }

        String identifier = selectedDate.getDayOfMonth() + Integer.toString(selectedDate.getMonthValue()) + selectedDate.getYear();
        StringBuilder event = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(monthFile))) {
            String line;
            boolean capture = false;
            while ((line = reader.readLine()) != null) {
                if (!capture && line.startsWith(identifier)) {
                    capture = true;
                    continue;
                }
                if (capture && line.matches("^\\d{1,2}\\d{1,2}\\d{4}.*")) {
                    break;
                }
                if (capture) {
                    event.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            return "";
        }

        return event.toString().trim();
    }

    private static String resolveOpenRouterApiKey() {
        String fromRuntime = firstNonBlank(
            System.getenv("OPENROUTER_API_KEY"),
            System.getProperty("OPENROUTER_API_KEY")
        );
        if (fromRuntime != null) {
            return fromRuntime;
        }

        File localConfigFile = new File(APP_BASE_DIRECTORY, "openrouter.properties");
        if (!localConfigFile.exists()) {
            return null;
        }

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(localConfigFile)) {
            properties.load(input);
            return firstNonBlank(
                properties.getProperty("OPENROUTER_API_KEY"),
                properties.getProperty("openrouter.api.key")
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String resolveOpenRouterApiUrl() {
        String configured = firstNonBlank(
            System.getenv("OPENROUTER_API_URL"),
            System.getProperty("OPENROUTER_API_URL")
        );
        return configured == null ? DEFAULT_OPENROUTER_URL : configured;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}

class AppRoundedBorder extends javax.swing.border.AbstractBorder {
    private int radius;
    private Color color;

    public AppRoundedBorder(int radius, Color color) {
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