package helpers;

import java.io.*;
import java.util.Properties;
import java.util.function.Supplier;

public class UserPersistence {

	// Reusing credentials without enforcing test execution order
    private static final String FILE_PATH = "target/last-user.properties";

    public static void save(UserData user) {
        try {
            Properties props = new Properties();
            props.setProperty("firstName", user.firstName);
            props.setProperty("lastName", user.lastName);
            props.setProperty("email", user.email);
            props.setProperty("password", user.password);

            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            try (FileOutputStream out = new FileOutputStream(file)) {
                props.store(out, "Last registered user");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public static LoadResult loadOrCreate(Supplier<UserData> userCreator) {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            UserData user = userCreator.get();
            save(user);
            return new LoadResult(user, false); 
        }

        try (FileInputStream in = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(in);

            UserData user = new UserData();
            user.firstName = props.getProperty("firstName");
            user.lastName  = props.getProperty("lastName");
            user.email     = props.getProperty("email");
            user.password  = props.getProperty("password");

            if (user.email == null || user.password == null) {
                throw new RuntimeException("User file is corrupted");
            }

            return new LoadResult(user, true); // loaded from file
        } catch (IOException e) {
            throw new RuntimeException("Failed to load user", e);
        }
    }

    public static class LoadResult {
        public final UserData user;
        public final boolean fromFile;

        public LoadResult(UserData user, boolean fromFile) {
            this.user = user;
            this.fromFile = fromFile;
        }
    }
}
