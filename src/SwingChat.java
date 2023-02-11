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
        System.out.printf("Подключен клиент: %s%n", USER_LIST.get(socket).getName());
        // создадим объекты через которые будем читать
        // запросы от клиента и отправлять ответы

        try (socket;
             Scanner reader = getReader(socket)
        ) {
            sendEveryone(USER_LIST.get(socket).getName() + " присоеденился чату!", socket);
            while (true) {
                String message = reader.nextLine().strip();

                if (isEmptyMsg(message) || isQuitMsg(message)) {
                    break;
                } else if (message.contains("/list")) {
                    printUserList(socket);
                } else if (message.contains("/name")) {
                    String newName = message.replace("/name", "").strip();
                    try {

                        if (isEmptyMsg(newName) || newName.contains(" ")) {
                            throw new NullPointerException();
                        }
                        for (Map.Entry<Socket, User> usernames : USER_LIST.entrySet()) {
                            if (newName.contains(usernames.getValue().getName())) {
                                throw new NullPointerException();
                            }
                        }

                        sendToUser(socket, "Вы теперь известны как " + newName);
                        sendEveryone("Пользователь" + USER_LIST.get(socket).getName() +
                                        "теперь известен как " + newName,
                                socket);
                        changeName(socket, newName);

                    } catch (NullPointerException e) {
                        sendToUser(socket, "Имя не может пустым, с пробелами или" +
                                " быть таким же как у другого пользователя.");
                    }
                } else if (message.contains("/whisper")) {

                    String newMessage = message.replace("/whisper", "").strip();
                    for (Map.Entry<Socket, User> username : USER_LIST.entrySet()) {
                        if (newMessage.contains(username.getValue().getName())) {
                            String updateMessage = newMessage.replace(
                                    username.getValue().getName(), "").strip();
                            sendToUser(username.getValue().getSocket(),
                                    USER_LIST.get(socket).getName() + ": " + updateMessage);
                        }
                    }
                } else {
                    sendEveryone(USER_LIST.get(socket).getName() + ": " + message, socket);
                }
            }
        } catch (
                NoSuchElementException ex) {
            System.out.println("Клиент закрыл соединение!");
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        String fmt = USER_LIST.get(socket).getName() + " покинул чат.";
        System.out.printf("%s" + fmt + "%n", USER_LIST.get(socket).getName());
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
            if (socket != writer.getValue().getSocket()) {
                writer.getValue().getWriter().write(response);
                writer.getValue().getWriter().write(System.lineSeparator());
                writer.getValue().getWriter().flush();
            }
        }
    }

    private static void printUserList(Socket socket) throws IOException {
        for (Map.Entry<Socket, User> kv : USER_LIST.entrySet()) {
            sendToUser(socket, kv.getValue().getName());
        }
    }

    private static void changeName(Socket socket, String newName) {
        USER_LIST.get(socket).setName(newName);
    }

    private static void sendToUser(Socket socket, String message) {
        USER_LIST.get(socket).getWriter().write(message);
        USER_LIST.get(socket).getWriter().write(System.lineSeparator());
        USER_LIST.get(socket).getWriter().flush();
    }
}
