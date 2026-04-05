package onlineboard.network;

import javafx.application.Platform;
import onlineboard.MainApp;
import onlineboard.util.JsonConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String host;
    private int port;
    private MainApp mainApp;

    private BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    public Client(String host, int port, MainApp mainApp) {
        this.host = host;
        this.port = port;
        this.mainApp = mainApp;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void disconnect() throws IOException {
        if (out != null) {
            out.println(JsonConverter.toJson(Map.of("command", "LOGOUT")));
            out.close();
        }

        if (in != null) {
            in.close();
        }

        if (socket != null) {
            socket.close();
        }
    }

    public String sendRequestAndWait(String request) throws IOException, InterruptedException {
        System.out.println("Sending request: " + request);
        out.println(request);
        return responseQueue.take();
    }

    @Override
    public void run() {
        try {
            connect();
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from server: " + message);
                Map<String, Object> messageMap = JsonConverter.jsonToMap(message);

                if (messageMap.containsKey("event")) {
                    Platform.runLater(() -> mainApp.handleServerUpdate(messageMap));
                    continue;
                }

                try {
                    responseQueue.put(message);
                } catch (InterruptedException e) {
                    System.err.println("Error adding response to queue: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Connection to server closed: " + e.getMessage());
        }
    }
}