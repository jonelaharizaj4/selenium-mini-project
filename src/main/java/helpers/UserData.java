package helpers;

public class UserData {

    public String firstName;
    public String lastName;
    public String email;
    public String password;

    public UserData() {
    }

    public UserData(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
}
