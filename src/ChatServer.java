import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Сервер запущен...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Новый клиент подключился: " + socket);
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    public static void broadcastMessage(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Клиент отключился: " + client);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Получено сообщение: " + message);
                    broadcastMessage(message, this);
                }
            } catch (IOException e) {
                System.err.println("Ошибка клиента: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии сокета: " + e.getMessage());
                }
                removeClient(this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        @Override
        public String toString() {
            return socket.toString();
        }
    }
}