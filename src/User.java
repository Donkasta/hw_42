import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class User {
    String name;
    Socket socket;
    PrintWriter writer;

    public User(Socket socket, PrintWriter writer) {
        this.name = "User_" + UUID.randomUUID().toString().substring(9, 13).toUpperCase();
        this.socket = socket;
        this.writer = writer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
