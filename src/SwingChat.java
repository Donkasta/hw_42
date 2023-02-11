import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class SwingChat {
    private static final Map<Socket, User> USER_LIST = new HashMap<>();

    public static void handle(Socket socket) {
        try {
            USER_LIST.put(socket, new User(socket, getWriter(socket)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Подключен клиент: %s%n", USER_LIST.get(socket).name);
        // создадим объекты через которые будем читать
        // запросы от клиента и отправлять ответы

        try (socket;
             Scanner reader = getReader(socket)
        ) {
            sendEveryone(USER_LIST.get(socket).name + " присоеденился чату!", socket);
            while (true) {
                String message = USER_LIST.get(socket).name + ": " + reader.nextLine().trim().strip();
                if (isEmptyMsg(message) || isQuitMsg(message)) {
                    break;
                } else if (message.contains("/list")) {
                    printUserList(socket);
                } else {
                    sendEveryone(message, socket);
                }
            }
        } catch (
                NoSuchElementException ex) {
            // если scanner не сможет ничего прочитать из потока,
            // то будет исключение
            System.out.println("Клиент закрыл соединение!");
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        String fmt = USER_LIST.get(socket).name + " покинул чат.";
        System.out.printf("%s" + fmt + "%n", USER_LIST.get(socket).name);
        try {
            sendEveryone(fmt, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        USER_LIST.remove(socket);
    }

    private static PrintWriter getWriter(Socket socket)
            throws IOException {
        OutputStream stream = socket.getOutputStream();
        return new PrintWriter(stream);
    }

    private static Scanner getReader(Socket socket) throws IOException {
        InputStream stream = socket.getInputStream();
        InputStreamReader input = new InputStreamReader(stream, StandardCharsets.UTF_8);
        return new Scanner(input);
    }

    private static boolean isQuitMsg(String message) {
        return "bye".equalsIgnoreCase(message);
    }

    private static boolean isEmptyMsg(String message) {
        return message == null || message.isBlank();
    }

    private static void sendEveryone(String response, Socket socket) throws IOException {
        for (Map.Entry<Socket, User> writer : USER_LIST.entrySet()) {
            if (socket != writer.getValue().socket) {
                writer.getValue().writer.write(response);
                writer.getValue().writer.write(System.lineSeparator());
                writer.getValue().writer.flush();
            }
        }
    }

    private static void printUserList(Socket socket) throws IOException {
        for (Map.Entry<Socket, User> kv : USER_LIST.entrySet()) {
            USER_LIST.get(socket).writer.write(kv.getValue().name);
            USER_LIST.get(socket).writer.write(System.lineSeparator());
            USER_LIST.get(socket).writer.flush();
        }
    }
}
