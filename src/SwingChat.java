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

        try (socket;
             Scanner reader = getReader(socket)
        ) {
            sendEveryone(USER_LIST.get(socket).getName() + " присоеденился к чату!", socket);
            while (true) {
                String message = reader.nextLine().strip();

                if (isQuitMsg(message)) {
                    sendToUser(socket, "Вы покинули чат.");
                    break;
                } else if (isEmptyMsg(message)) {
                    sendToUser(socket, "Сообщение не может быть пустым.");
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
                        sendEveryone("Пользователь " + USER_LIST.get(socket).getName() +
                                " теперь известен как " + newName, socket);
                        changeName(socket, newName);

                    } catch (NullPointerException e) {
                        sendToUser(socket, "Имя не может пустым, с пробелами или" +
                                " быть таким же \nкак у другого пользователя.");
                    }
                } else if (message.contains("/whisper")) {
                    String newMessage = message.replace("/whisper", "").strip();
                    try {
                        if (findUser(newMessage.strip())) {

                            for (Map.Entry<Socket, User> user : USER_LIST.entrySet()) {
                                if (newMessage.contains(user.getValue().getName())) {

                                    String updateMessage = newMessage.replace(
                                            user.getValue().getName(), "").strip();
                                    sendToUser(user.getKey(), "(Личное сообщение) " +
                                            USER_LIST.get(socket).getName() + ": " + updateMessage);
                                }
                            }
                        } else {
                            throw new NullPointerException();
                        }
                    } catch (NullPointerException e) {
                        sendToUser(socket, "Пустое сообщение или пользователь не найден.");
                    }
                } else {
                    sendEveryone("(Всем) " + USER_LIST.get(socket).getName() + ": " + message, socket);
                }
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Клиент закрыл соединение!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fmt = USER_LIST.get(socket).getName() + " покинул чат.";
        System.out.printf(fmt + "%n");
        try {
            sendEveryone(fmt, socket);
        } catch (
                IOException e) {
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

    private static boolean findUser(String newMessage) {
        int firstIndex = newMessage.indexOf(" ");
        if (firstIndex > 0) {
            StringBuilder name = new StringBuilder(newMessage.strip());
            String username = String.valueOf(name.replace(firstIndex, newMessage.length(), ""));
            return USER_LIST.entrySet().stream()
                    .anyMatch(e -> e.getValue().getName().contains(username.strip()));
        } else return false;
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
