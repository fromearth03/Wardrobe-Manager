package deeplearning;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Weather {
    private static final String OPEN_METEO_GEOCODE_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String OPEN_METEO_FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    public static ArrayList<Forecast> getforecast() {
        ArrayList<Forecast> forecastList = new ArrayList<>();
        try {
            String cityValue = resolveCity();
            if (cityValue == null || cityValue.isBlank()) {
                return forecastList;
            }
            fetchOpenMeteoForecast(cityValue, forecastList);
        } catch (Exception e) {
            System.out.println("Open-Meteo weather error: " + e.getMessage());
        }
        return forecastList;
    }

    private static String resolveCity() {
        try {
            if (calendar.storedArgs != null && calendar.storedArgs.length >= 3) {
                String value = calendar.storedArgs[2];
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
            if (mainpage.storedArgs != null && mainpage.storedArgs.length >= 3) {
                String value = mainpage.storedArgs[2];
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
            if (App.storedArgs != null && App.storedArgs.length >= 3) {
                String value = App.storedArgs[2];
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void fetchOpenMeteoForecast(String city, ArrayList<Forecast> forecastList) {
        try {
            String geocodeUrl = OPEN_METEO_GEOCODE_URL
                + "?name=" + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&count=1&language=en&format=json";

            String geoJson = httpGet(geocodeUrl);
            if (geoJson == null || geoJson.isEmpty()) {
                return;
            }

            Matcher geoMatcher = Pattern.compile("\\\"latitude\\\"\\s*:\\s*([-0-9.]+).*?\\\"longitude\\\"\\s*:\\s*([-0-9.]+)", Pattern.DOTALL).matcher(geoJson);
            if (!geoMatcher.find()) {
                System.out.println("Open-Meteo geocoding found no results for city: " + city);
                return;
            }

            String latitude = geoMatcher.group(1);
            String longitude = geoMatcher.group(2);

            String forecastUrl = OPEN_METEO_FORECAST_URL
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&daily=weathercode,temperature_2m_max,temperature_2m_min"
                + "&forecast_days=7&timezone=auto";

            String forecastJson = httpGet(forecastUrl);
            if (forecastJson == null || forecastJson.isEmpty()) {
                return;
            }

            parseOpenMeteoForecast(forecastJson, forecastList);
        } catch (Exception e) {
            System.out.println("Open-Meteo fetch error: " + e.getMessage());
        }
    }

    private static String httpGet(String requestUrl) {
        try {
            URL url = java.net.URI.create(requestUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            int status = conn.getResponseCode();
            java.io.InputStream stream = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
            conn.disconnect();

            if (status < 200 || status >= 300) {
                System.out.println("Weather request failed (" + status + "): " + response);
                return null;
            }

            return response.toString();
        } catch (Exception e) {
            System.out.println("Weather request exception: " + e.getMessage());
            return null;
        }
    }

    private static void parseOpenMeteoForecast(String json, ArrayList<Forecast> forecastList) {
        Matcher dailyMatcher = Pattern.compile("\\\"daily\\\"\\s*:\\s*\\{(.*?)\\}\\s*(,|$)", Pattern.DOTALL).matcher(json);
        if (!dailyMatcher.find()) {
            return;
        }

        String daily = dailyMatcher.group(1);
        ArrayList<String> dates = extractQuotedArray(daily, "time");
        ArrayList<Double> maxTemps = extractNumberArray(daily, "temperature_2m_max");
        ArrayList<Double> minTemps = extractNumberArray(daily, "temperature_2m_min");
        ArrayList<Double> weatherCodes = extractNumberArray(daily, "weathercode");

        int total = Math.min(Math.min(dates.size(), maxTemps.size()), Math.min(minTemps.size(), weatherCodes.size()));
        for (int i = 0; i < total; i++) {
            try {
                LocalDate date = LocalDate.parse(dates.get(i));
                int code = weatherCodes.get(i).intValue();
                forecastList.add(new Forecast(
                    date.getDayOfMonth(),
                    weatherCodeToText(code),
                    minTemps.get(i),
                    maxTemps.get(i),
                    date.getYear(),
                    date.getMonthValue(),
                    ""
                ));
            } catch (Exception ignored) {
            }
        }
    }

    private static ArrayList<String> extractQuotedArray(String source, String key) {
        ArrayList<String> values = new ArrayList<>();
        Matcher keyMatcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(source);
        if (!keyMatcher.find()) {
            return values;
        }

        String arrayBody = keyMatcher.group(1);
        Matcher valueMatcher = Pattern.compile("\\\"(.*?)\\\"").matcher(arrayBody);
        while (valueMatcher.find()) {
            values.add(valueMatcher.group(1));
        }
        return values;
    }

    private static ArrayList<Double> extractNumberArray(String source, String key) {
        ArrayList<Double> values = new ArrayList<>();
        Matcher keyMatcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(source);
        if (!keyMatcher.find()) {
            return values;
        }

        String[] parts = keyMatcher.group(1).split(",");
        for (String part : parts) {
            try {
                values.add(Double.parseDouble(part.trim()));
            } catch (Exception ignored) {
            }
        }
        return values;
    }

    private static String weatherCodeToText(int code) {
        switch (code) {
            case 0:
                return "Clear sky";
            case 1:
            case 2:
            case 3:
                return "Partly cloudy";
            case 45:
            case 48:
                return "Fog";
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                return "Drizzle";
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
                return "Rain";
            case 71:
            case 73:
            case 75:
            case 77:
                return "Snow";
            case 80:
            case 81:
            case 82:
                return "Rain showers";
            case 85:
            case 86:
                return "Snow showers";
            case 95:
            case 96:
            case 99:
                return "Thunderstorm";
            default:
                return "Weather update";
        }
    }
}

class Forecast {
    private String iconUrl;
    private int date;
    private String condition;
    private double maxtemp;
    private double mintemp;
    private int year;
    private int month;

    // Constructor
    public Forecast(int date, String condition, double mintemp, double maxtemp, int year, int month, String iconUrl) {
        this.date = date;
        this.condition = condition;
        this.maxtemp = maxtemp;
        this.mintemp = mintemp;
        this.year = year;
        this.month = month;
        this.iconUrl = iconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getMaxtemp() {
        return maxtemp;
    }

    public void setMaxtemp(double maxtemp) {
        this.maxtemp = maxtemp;
    }

    public double getMintemp() {
        return mintemp;
    }

    public void setMintemp(double mintemp) {
        this.mintemp = mintemp;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}

public class calendar {
    static String[] storedArgs;
    private static YearMonth currentYearMonth = YearMonth.now();
    private static JPanel calendarPanel;
    private static JLabel monthLabel;

    public static void main(String[] args) {
        storedArgs = args;

        JFrame frame = new JFrame("Calendar");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(960, 760);
        frame.setLocationRelativeTo(null);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(225, 240, 255)); // Light blue

        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");

        // Customize arrow buttons
        styleArrowButton(prevButton);
        styleArrowButton(nextButton);

        // Centered month label
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Serif", Font.BOLD, 44));
        monthLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        // Calendar panel (7 columns: Sun to Sat)
        calendarPanel = new JPanel(new GridLayout(0, 7, 10, 10));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        calendarPanel.setBackground(new Color(225, 240, 255)); // Light blue

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(225, 240, 255));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        updateCalendar();

        // Navigation listeners
        prevButton.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        nextButton.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        frame.setVisible(true);
    }

    private static void updateCalendar() {
        calendarPanel.removeAll();

        File baseDir = getUserDir(calendar.storedArgs[0]);
        if (!baseDir.exists()) {
            if (baseDir.mkdirs()) {
                System.out.println("Directory created: " + baseDir.getAbsolutePath());
            } else {
                System.out.println("Failed to create directory: " + baseDir.getAbsolutePath());
            }
        } else {
            System.out.println("Directory already exists: " + baseDir.getAbsolutePath());
        }

        // Month/year label
        String monthYear = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                " / " + currentYearMonth.getYear();
        monthLabel.setText(monthYear);

        // Day names
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            dayLabel.setForeground(new Color(40, 80, 120));
            calendarPanel.add(dayLabel);
        }

        ArrayList<Forecast> forecast = new ArrayList<>();
        forecast.addAll(Weather.getforecast());
        forecast.trimToSize();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int startDay = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        int shift = (startDay % 7); // shift so Sunday = 0

        // Empty labels before first day
        for (int i = 0; i < shift; i++) {
            calendarPanel.add(new JLabel(""));
        }

        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            int clickedDay = day;
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("SansSerif", Font.BOLD, 24));
            Forecast matchedForecast = null;
            for (Forecast f : forecast) {
                if (f.getDate() == day &&
                        f.getMonth() == currentYearMonth.getMonthValue() &&
                        f.getYear() == currentYearMonth.getYear()) {
                    matchedForecast = f;
                    break; // Stop once match is found
                }
            }

            if (matchedForecast != null) {
                String condition = matchedForecast.getCondition();
                String compactCondition = shortenCondition(condition);
                dayButton.setText("<html><div style='text-align:center;line-height:1.2;'><b>" + day + "</b><br><span style='font-size:15px;'>" + escapeHtml(compactCondition) + "</span></div></html>");
                dayButton.setToolTipText(condition + " (" + matchedForecast.getMintemp() + "°C-" + matchedForecast.getMaxtemp() + "°C)");
            }

            dayButton.setFocusPainted(false);
            dayButton.setBackground(Color.WHITE);
            dayButton.setForeground(new Color(40, 80, 120));
            dayButton.setBorder(BorderFactory.createLineBorder(new Color(120, 180, 255)));
            dayButton.setMargin(new Insets(2, 2, 2, 2));

            // Highlight today
            if (today.getYear() == currentYearMonth.getYear() &&
                    today.getMonth() == currentYearMonth.getMonth() &&
                    today.getDayOfMonth() == day) {
                dayButton.setBackground(new Color(120, 180, 255));
                dayButton.setForeground(new Color(40, 80, 120));
                dayButton.setFont(new Font("SansSerif", Font.BOLD, 26));
            }

            File subfolder = getCalendarDir(calendar.storedArgs[0]);
            String file = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + currentYearMonth.getYear() + ".txt";

            // Create the subfolder if it doesn't exist
            if (!subfolder.exists()) {
                boolean created = subfolder.mkdirs(); // Create directories if they don't exist
                if (created) {
                    System.out.println("Subfolder created successfully.");
                } else {
                    System.out.println("Failed to create subfolder.");
                    return;
                }
            }

            // Remove Button
            JButton removeButton = new JButton("Remove");
            removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            dayButton.addActionListener(e -> {
                File filedel = new File(subfolder, file);
                String identifier = clickedDay + Integer.toString(currentYearMonth.getMonthValue()) + currentYearMonth.getYear();

                // 1. Make textArea editable so user can add/edit data
                JTextArea textArea = new JTextArea(15, 56);
                textArea.setEditable(true); // Allow editing
                textArea.setFont(new Font("SansSerif", Font.PLAIN, 20));
                textArea.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(180, 180, 180), 2, true), // true for rounded corners
                BorderFactory.createEmptyBorder(8, 8, 8, 8) // padding inside the border
                ));
                JScrollPane scrollPane = new JScrollPane(textArea);

                // 2. Try to load existing data for this date
                StringBuilder existingData = new StringBuilder();
                try {
                    List<String> lines = Files.readAllLines(filedel.toPath());
                    boolean capture = false;
                    for (String line : lines) {
                        if (line.startsWith(identifier)) {
                            capture = true;
                            continue;
                        }
                        if (capture && line.matches("^\\d{1,2}\\d{1,2}\\d{4}.*")) {
                            break;
                        }
                        if (capture) {
                            existingData.append(line).append("\n");
                        }
                    }
                } catch (IOException eread) {
                    // File may not exist yet, that's fine
                }

                // 3. If user passed data in storedArgs[1] and no existing data, show it
                String userData = (storedArgs.length > 1 && storedArgs[1] != null) ? storedArgs[1] : "";
                // Prevent closet details or empty string from being shown as event
                if (existingData.length() == 0 && !userData.isEmpty() && !userData.trim().toLowerCase().startsWith("your closet details")) {
                    textArea.setText(userData);
                } else if (existingData.length() > 0) {
                    textArea.setText(existingData.toString());
                } else {
                    textArea.setText(""); // Show blank if nothing
                }

                JFrame actionFrame = new JFrame(clickedDay + " " + currentYearMonth.getMonth() + " " + currentYearMonth.getYear());
                actionFrame.setSize(920, 680);
                actionFrame.setLocationRelativeTo(null);
                actionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                actionFrame.setResizable(false);

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBackground(new Color(240, 240, 240));
                panel.add(scrollPane);
                panel.add(Box.createVerticalStrut(10));

                JButton addButton = new JButton("Add");
                addButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Disable addButton if textArea is empty
                addButton.setEnabled(!textArea.getText().trim().isEmpty());

                // Listen for changes in textArea to enable/disable addButton
                textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
                    private void changed() {
                        addButton.setEnabled(!textArea.getText().trim().isEmpty());
                    }
                });

                addButton.addActionListener(addEvent -> {
                    String contentToSave = textArea.getText().trim();
                    try {
                        // Remove any existing block for this identifier (override previous event)
                        List<String> lines = new ArrayList<>();
                        if (filedel.exists()) {
                            lines = Files.readAllLines(filedel.toPath());
                        }
                        StringBuilder updatedContent = new StringBuilder();
                        boolean skip = false;
                        for (String line : lines) {
                            if (line.startsWith(identifier)) {
                                skip = true;
                                continue;
                            }
                            if (skip && line.matches("^\\d{1,2}\\d{1,2}\\d{4}.*")) {
                                skip = false;
                            }
                            if (!skip) {
                                updatedContent.append(line).append(System.lineSeparator());
                            }
                        }
                        // Write cleaned content back (removes previous event for this date)
                        Files.write(filedel.toPath(), updatedContent.toString().getBytes());

                        // Append new block (always only one event per date)
                        try (FileWriter writer = new FileWriter(filedel, true)) {
                            writer.write(identifier + System.lineSeparator());
                            writer.write(contentToSave + System.lineSeparator());
                        }
                        JOptionPane.showMessageDialog(actionFrame, "Event saved!");
                    } catch (IOException eadd) {
                        JOptionPane.showMessageDialog(actionFrame, "Error writing to file: " + eadd.getMessage());
                    }
                });

                removeButton.addActionListener(removeEvent -> {





                    textArea.setForeground(Color.WHITE); //Talha is ko dekh lai
                    
                    try {
                        // 1. Read all lines from the file if it exists
                        List<String> lines = new ArrayList<>();
                        if (filedel.exists()) {
                            lines = Files.readAllLines(filedel.toPath());
                        }

                        // 2. Remove existing block that starts with the identifier
                        StringBuilder updatedContent = new StringBuilder();
                        boolean skip = false;
                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);

                            if (line.startsWith(identifier)) {
                                skip = true;
                                continue;
                            }

                            // Check if the line is the start of the next block
                            if (skip && line.matches("^\\d{1,2}\\d{1,2}\\d{4}.*")) {
                                skip = false; // Stop skipping — new block
                            }

                            if (!skip) {
                                updatedContent.append(line).append(System.lineSeparator());
                            }
                        }

                        // 3. Write the cleaned content back
                        Files.write(filedel.toPath(), updatedContent.toString().getBytes());

                    } catch (IOException eadd) {
                        System.out.println("Error in remove from file " + eadd.getMessage());
                    }
                });

                // Set the background color and foreground color.
                Color buttonBackColor = Color.WHITE; // White, the original panel color.
                addButton.setBackground(buttonBackColor);
                removeButton.setBackground(buttonBackColor);
                addButton.setForeground(Color.BLACK); //set font color
                removeButton.setForeground(Color.BLACK);

                // Set the font.
                addButton.setFont(new Font("Arial", Font.PLAIN, 20));
                removeButton.setFont(new Font("Arial", Font.PLAIN, 20));

                // Make buttons same size.
                Dimension buttonSize = new Dimension(170, 50);
                addButton.setPreferredSize(buttonSize);
                removeButton.setPreferredSize(buttonSize);
                addButton.setMaximumSize(buttonSize);
                removeButton.setMaximumSize(buttonSize);
                addButton.setMinimumSize(buttonSize);
                removeButton.setMinimumSize(buttonSize);

                //Remove outline and focus highlight
                addButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // Light gray border
                removeButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                addButton.setFocusPainted(false);
                removeButton.setFocusPainted(false);

                // Add buttons to the panel
                panel.add(Box.createVerticalStrut(20)); // spacing
                panel.add(addButton);
                panel.add(Box.createVerticalStrut(10)); // spacing
                panel.add(removeButton);

                // Add panel to frame
                actionFrame.setContentPane(panel);

                // Show the window
                actionFrame.setVisible(true);
            });

            calendarPanel.add(dayButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private static File getUserDir(String username) {
        return new File(new File(System.getProperty("user.home"), "WardrobeManagerData"), username);
    }

    private static File getCalendarDir(String username) {
        return new File(getUserDir(username), "Calendar");
    }

    private static void styleArrowButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 42));
        button.setForeground(new Color(40, 80, 120));
        button.setPreferredSize(new Dimension(110, 90));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
    }

    private static String shortenCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) return "Weather";
        String trimmed = condition.trim();
        return trimmed.length() <= 18 ? trimmed : trimmed.substring(0, 17) + "…";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}