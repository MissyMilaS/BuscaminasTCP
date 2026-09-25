package co.icesi.buscaminas.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;

public class TCPClient {
    private final String host;
    private final int port;
    private final Gson gson;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder().create();
    }

    public Response sendRequest(Request request) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonRequest = gson.toJson(request);
            writer.write(jsonRequest);
            writer.newLine();
            writer.flush();

            String jsonResponse = reader.readLine();
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return null;
            }
            return gson.fromJson(jsonResponse, Response.class);
        } catch (IOException e) {
            System.err.println("Error de comunicación con el servidor: " + e.getMessage());
            return null;
        }
    }
}
