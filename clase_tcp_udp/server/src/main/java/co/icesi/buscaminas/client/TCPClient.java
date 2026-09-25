package co.icesi.buscaminas.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;

public class TCPClient {

    private String host;
    private int port;
    private Gson gson;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder().create();
    }

    public Response sendRequest(Request request) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 1. Serializar el objeto Request a una cadena JSON
            String jsonRequest = gson.toJson(request);

            // 2. Enviar el JSON al servidor seguido de un salto de línea
            writer.write(jsonRequest);
            writer.newLine();
            writer.flush();

            // 3. Leer la línea de respuesta enviada por el servidor
            String jsonResponse = reader.readLine();

            // 4. Deserializar la respuesta JSON a un objeto Response
            if (jsonResponse != null) {
                return gson.fromJson(jsonResponse, Response.class);
            }

        } catch (Exception e) {
            System.err.println("Error de comunicación con el servidor: " + e.getMessage());
        }
        return null;
    }
}