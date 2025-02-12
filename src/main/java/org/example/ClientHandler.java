package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable, IObserver {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private IObserverable server;
    private String name;
    ChatServerLogic csl = new ChatServerLogic();
    ChatModerator moderator;


    public ClientHandler(Socket clientSocket, IObserverable server) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;
        this.name = "Guest";
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
    String message;
    List<ClientHandler> clients = csl.getClients();

    try {
        while ((message = in.readLine()) != null) {
            if (message.startsWith("#JOIN")) {
                this.name = message.split(" ")[1];
                server.broadcast(name + " has joined the server.");
            } else if (message.startsWith("#MESSAGE")) {
                server.broadcast(name + " says: " + message.replace("MESSAGE ", ""));
            } else if (message.startsWith("#LEAVE")){
                server.broadcast(name + " has left the server.");
                break;
            } else if (message.startsWith("#GETLIST")) {
                for (ClientHandler client : clients) {
                    out.println(client.name + " is connected to the server.");
                }
            } else if (message.startsWith("#PRIVATE")){
                String[] parts = message.split(" ", 3); // Splits message into 3 parts. "#PRIVATE", "name", "message"
                String targetClient = parts[1];
                String targetMessage = parts[2];
                boolean foundTarget = false;

                for (ClientHandler client : clients) {
                    if(client.name.equalsIgnoreCase(targetClient)){
                        client.notify(name + " whispers privately: " + targetMessage);
                        foundTarget = true;
                        out.println(name + " whispers '" + targetMessage + "' to " + targetClient + ".");
                        break;
                    }
                    if (!foundTarget){
                        out.println("400 Bad Request");
                        out.println("Client with name '" + targetClient + "' does not exist.");
                        out.println("Correct request is: #PRIVATE <name> <message>");
                    }
                }
            } else if (message.startsWith("#PRIVATESUBLIST")) {
                String[] parts = message.split(" ", 10); // Splits message into "#PRIVATESUBLIST", "name", "name", "name" etc. last index is "message"

                if (parts.length < 3) {
                    out.println("400 Bad Request");
                    out.println("Correct request is: #PRIVATESUBLIST <name>, <name>, <name>, <...>, <message>");
                }
                String[] targets = parts[1].split(",\\s*");
                String targetMessage = parts[parts.length - 1];

                List<String> targetList = Arrays.asList(targets);

                for (ClientHandler client : clients) {
                    if(targetList.contains(client.name)){
                    client.notify(name + " whispers privately: " + targetMessage);
                    }
                }

            } else if (message.startsWith("#HELP")) {
                out.println("Available commands:");
                out.println("#JOIN <name>");
                out.println("#MESSAGE <message>");
                out.println("#LEAVE");
                out.println("#GETLIST");
                out.println("#PRIVATE <name> <message>");
                out.println("#PRIVATESUBLIST <name>, <name>, <name>, <...>, <message>");
            }
        }

    }catch(IOException ex){
        // TODO: handle
    }
    }

    @Override
    public void notify(String message) {
    }
}
