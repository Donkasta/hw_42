import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();

    private EchoServer(int port) {
        this.port = port;
    }

    static EchoServer bindToPort(int port) {
        return new EchoServer(port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (!server.isClosed()) {
                Socket clientSocket = server.accept();
                pool.submit(() -> handle(clientSocket));
            }
        } catch (IOException e) {
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }

    private static void handle(Socket socket) {
        System.out.printf("Подключен клиент: %s%n", socket);
        // создадим объекты через которые будем читать
        // запросы от клиента и отправлять ответы
        try (socket;
             Scanner reader = getReader(socket);
             PrintWriter writer = getWriter(socket)) {
            sendResponse("Привет " + socket, writer);
            while (true) {
                String message = reader.nextLine();
                if (isEmptyMsg(message) || isQuitMsg(message)) {

                    break;
                }
                // отправим ответ
                sendResponse(message.toUpperCase(), writer);
            }
        } catch (NoSuchElementException ex) {
            // если scanner не сможет ничего прочитать из потока,
            // то будет исключение
            System.out.println("Клиент закрыл соединение!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Клиент отключен: %s%n", socket);
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

    private static void sendResponse(String response, Writer writer) throws IOException {
        writer.write(response);
        writer.write(System.lineSeparator());
        writer.flush();
    }

}