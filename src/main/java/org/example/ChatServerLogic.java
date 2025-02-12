package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatServerLogic implements IObserverable {
    private static volatile IObserverable server = getInstance();
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Random random = new Random();
    private PrintWriter out;


    ChatServerLogic() {
    }

    public static synchronized IObserverable getInstance() {
        if (server == null) {
            server = new ChatServerLogic();
        }
        return server;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    @Override
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.notify(message);
        }
    }

    public static void main(String[] args) {
        new ChatServerLogic().startServer(12345);
    }

    private void sendRandomMessages(){
        String[] messages = {"Hej!", "Ha' en god dag!", "Jeg kan lide bananer", "#Datamatikerlyfe", "Ja da", "Hvad er klokken?", "Hvad tid har vi fri?", "Hent lige en monner til mig"};
        int randomDelay = random.nextInt(10000);

        new Thread(() -> {
            try {
                Thread.sleep(randomDelay);
                ClientHandler randomClient = clients.get(random.nextInt(clients.size()));
                randomClient.notify(randomClient.getName() + " skriver random: " + messages[random.nextInt(messages.length)]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started at port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
                clients.add(clientHandler);
            }
        } catch (IOException e) {
            // TODO: Handle
        }
    }
}
