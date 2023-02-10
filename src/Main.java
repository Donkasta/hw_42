import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        EchoServer.bindToPort(8766).run();

//       String name = "User" + UUID.randomUUID().toString().substring(9,13).toUpperCase();;
//        System.out.println(name);
    }


}