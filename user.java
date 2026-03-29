package deeplearning;


public class user {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String gender;
 
    public user(String firstName, String lastName, String email, String password, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.gender = gender;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getGender() {
        return gender;
    }

    // Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }    // For saving to file
    @Override
    public String toString() {
        return "First Name: " + firstName + "\n" +
               "Last Name: " + lastName + "\n" +
               "Email: " + email + "\n" +
               "Password: " + password + "\n" +
               "Gender: " + gender + "\n";
    }
}

