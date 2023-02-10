import java.util.UUID;

public class User {
    String name;

    public User() {
        this.name = "User" + UUID.randomUUID().toString().substring(9,13).toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
