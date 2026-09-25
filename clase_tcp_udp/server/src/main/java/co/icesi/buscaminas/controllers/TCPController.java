package co.icesi.buscaminas.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;
import co.icesi.buscaminas.model.Cell;
import co.icesi.buscaminas.services.ServicesImpl;

public class TCPController {
    private final ServicesImpl services;
    private final ServerSocket serverSocket;
    private final ExecutorService executor;
    private final Gson gson;
    private boolean running;

    public TCPController(ServicesImpl services) {
        this(services, 12345);
    }

    public TCPController(ServicesImpl services, int port) {
        this.services = services;
        try {
            this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            this.executor = Executors.newFixedThreadPool(5);
            this.gson = new GsonBuilder().create();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo iniciar el servidor TCP en el puerto " + port, e);
        }
        this.running = true;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public void startService() {
        System.out.println("TCP Service started on port " + serverSocket.getLocalPort());
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.execute(new TCPClientHandler(clientSocket, services));
            } catch (Exception e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }

        try {
            executor.shutdown();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class TCPClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ServicesImpl services;

        public TCPClientHandler(Socket clientSocket, ServicesImpl services) {
            this.clientSocket = clientSocket;
            this.services = services;
        }

        @Override
        public void run() {
            try {
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                String line = reader.readLine();
                if (line == null || line.isBlank()) {
                    writer.write(gson.toJson(new Response()));
                    writer.newLine();
                    writer.flush();
                    return;
                }

                Request rq = gson.fromJson(line, Request.class);
                Response response = new Response();
                response.data = new HashMap<>();

                if (rq == null || rq.action == null || rq.data == null) {
                    response.status = "ERROR";
                    response.data.put("message", "Solicitud inválida: action/data faltantes");
                    writer.write(gson.toJson(response));
                    writer.newLine();
                    writer.flush();
                    return;
                }

                Map<String, String> data = rq.data;
                switch (rq.action) {
                    case "SELECT_CELL": {
                        int i = Integer.parseInt(data.get("i"));
                        int j = Integer.parseInt(data.get("j"));
                        try {
                            boolean resp = services.selectCell(i, j);
                            response.status = "OK";
                            response.data.put("win", resp);
                            response.data.put("gameEnd", resp);
                        } catch (Exception e) {
                            response.status = "ERROR";
                            response.data.put("gameEnd", true);
                            response.data.put("win", false);
                            response.data.put("message", e.getMessage());
                        }
                        Cell[][] board = services.printBoard();
                        response.data.put("board", board);
                        break;
                    }
                    case "SOW_ALL":
                        services.showAll(true);
                        response.status = "OK";
                        response.data.put("board", services.printBoard());
                        break;
                    case "GET_BOARD":
                        response.status = "OK";
                        response.data.put("board", services.printBoard());
                        break;
                    case "INIT_GAME": {
                        int i = Integer.parseInt(data.get("n"));
                        int j = Integer.parseInt(data.get("m"));
                        int m = Integer.parseInt(data.get("minas"));
                        services.initGame(i, j, m);
                        response.status = "OK";
                        response.data.put("board", services.printBoard());
                        break;
                    }
                    case "MARK_CELL": {
                        int mi = Integer.parseInt(data.get("i"));
                        int mj = Integer.parseInt(data.get("j"));
                        try {
                            services.markCell(mi, mj);
                            response.status = "OK";
                        } catch (Exception e) {
                            response.status = "ERROR";
                            response.data.put("message", e.getMessage());
                        }
                        response.data.put("board", services.printBoard());
                        break;
                    }
                    default:
                        response.status = "ERROR";
                        response.data.put("message", "Acción no soportada: " + rq.action);
                        break;
                }

                writer.write(gson.toJson(response));
                writer.newLine();
                writer.flush();
                writer.close();
                reader.close();
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
