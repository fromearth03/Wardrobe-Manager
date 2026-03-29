package deeplearning;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class UserAccount {
     ArrayList<user> users = new ArrayList<>();

    private static final File APP_DATA_DIR = new File(System.getProperty("user.home"), "WardrobeManagerData");
    private static final File PRIMARY_USERS_FILE = new File(APP_DATA_DIR, "Usersdata.txt");
    private static final File LEGACY_USERS_FILE = new File("Usersdata.txt");

    public void addUser(user newUser) {
        users.add(newUser);
    }

    public void removeUser(user userToRemove) {
        users.remove(userToRemove);
    }

    public int getUserCount() {
        return users.size();
    }

    public user searchUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalizedEmail = email.trim();
        for (user u : users) {
            if (u.getEmail() != null && u.getEmail().trim().equalsIgnoreCase(normalizedEmail)) {
                return u;
            }
        }
        return null; // User not found
    }

    public void loadUsersFromFile(String filename) {
        users.clear();
        loadUsersFromGivenFile(new File(filename));
    }

    public void loadUsersFromDefaultFile() {
        users.clear();
        if (PRIMARY_USERS_FILE.exists()) {
            loadUsersFromGivenFile(PRIMARY_USERS_FILE);
        }
        if (LEGACY_USERS_FILE.exists()) {
            loadUsersFromGivenFile(LEGACY_USERS_FILE);
        }
    }

    public void appendUserToDefaultFile(user newUser) throws IOException {
        if (!APP_DATA_DIR.exists() && !APP_DATA_DIR.mkdirs()) {
            throw new IOException("Failed to create app data directory: " + APP_DATA_DIR.getAbsolutePath());
        }
        try (FileWriter writer = new FileWriter(PRIMARY_USERS_FILE, true)) {
            writer.write(newUser.toString());
            writer.flush();
        }
    }

    public File getDefaultUsersFile() {
        return PRIMARY_USERS_FILE;
    }

    private void loadUsersFromGivenFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String firstName = "", lastName = "", email = "", password = "", gender = "";

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("First Name:")) {
                    firstName = line.substring("First Name:".length()).trim();
                } else if (line.startsWith("Last Name:")) {
                    lastName = line.substring("Last Name:".length()).trim();
                } else if (line.startsWith("Email:")) {
                    email = line.substring("Email:".length()).trim();
                } else if (line.startsWith("Password:")) {
                    password = line.substring("Password:".length()).trim();
                } else if (line.startsWith("Gender:")) {
                    gender = line.substring("Gender:".length()).trim();
                    // End of user block — add user
                    user loadedUser = new user(firstName, lastName, email, password, gender);
                    if (searchUserByEmail(email) == null) {
                        addUser(loadedUser);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


