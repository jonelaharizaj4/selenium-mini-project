package helpers;

public class UserFactory {
	
	// Factory for generating unique test users to avoid registration conflicts
    public static UserData createUniqueUser() {
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";

        return new UserData("Test", "User", email, password);
    }
}
