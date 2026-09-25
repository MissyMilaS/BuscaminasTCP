package co.icesi.buscaminas.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;

public class MainClient {
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12345;

        TCPClient client = new TCPClient(host, port);
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        boolean gameStarted = false;
        boolean gameFinished = false;

        System.out.println("=============================================");
        System.out.println("BUSCAMINAS DISTRIBUIDO - CLIENTE TCP");
        System.out.println("=============================================");

        while (!exit) {
            System.out.println("\n[1] Iniciar nueva partida");
            System.out.println("[2] Destapar celda");
            System.out.println("[3] Marcar / Desmarcar bandera");
            System.out.println("[4] Consultar tablero");
            System.out.println("[5] Rendirse y revelar tablero");
            System.out.println("[6] Salir");
            System.out.print("Seleccione una opción: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Entrada no válida. Ingrese un número.");
                scanner.next();
                continue;
            }

            int option = scanner.nextInt();
            Request request = new Request();
            request.data = new HashMap<>();

            switch (option) {
                case 1:
                    request.action = "INIT_GAME";
                    request.data.put("n", String.valueOf(readInt(scanner, "Filas (n): ")));
                    request.data.put("m", String.valueOf(readInt(scanner, "Columnas (m): ")));
                    request.data.put("minas", String.valueOf(readInt(scanner, "Cantidad de minas: ")));
                    gameStarted = true;
                    gameFinished = false;
                    break;
                case 2:
                    if (!gameStarted || gameFinished) {
                        System.out.println("Debe iniciar una partida primero.");
                        continue;
                    }
                    request.action = "SELECT_CELL";
                    request.data.put("i", String.valueOf(readInt(scanner, "Fila (i): ")));
                    request.data.put("j", String.valueOf(readInt(scanner, "Columna (j): ")));
                    break;
                case 3:
                    if (!gameStarted || gameFinished) {
                        System.out.println("Debe iniciar una partida primero.");
                        continue;
                    }
                    request.action = "MARK_CELL";
                    request.data.put("i", String.valueOf(readInt(scanner, "Fila (i): ")));
                    request.data.put("j", String.valueOf(readInt(scanner, "Columna (j): ")));
                    break;
                case 4:
                    if (!gameStarted) {
                        System.out.println("Debe iniciar una partida primero.");
                        continue;
                    }
                    request.action = "GET_BOARD";
                    break;
                case 5:
                    if (!gameStarted) {
                        System.out.println("Debe iniciar una partida primero.");
                        continue;
                    }
                    request.action = "SOW_ALL";
                    gameFinished = true;
                    break;
                case 6:
                    exit = true;
                    continue;
                default:
                    System.out.println("Opción no válida.");
                    continue;
            }

            Response response = client.sendRequest(request);
            if (response == null) {
                System.out.println("No se recibió respuesta del servidor.");
                continue;
            }

            System.out.println("\n[Servidor] Estado: " + response.status);

            if (response.data != null) {
                Object gameEnd = response.data.get("gameEnd");
                Object win = response.data.get("win");
                Object message = response.data.get("message");

                if (gameEnd != null && win != null) {
                    boolean end = Boolean.TRUE.equals(gameEnd);
                    boolean victory = Boolean.TRUE.equals(win);
                    if (end && victory) {
                        System.out.println(GREEN + "¡VICTORIA! Has terminado el tablero." + RESET);
                        gameFinished = true;
                    } else if (end && !victory) {
                        System.out.println(RED + "¡DERROTA! Explotaste una mina." + RESET);
                        request.action = "SOW_ALL";
                        Response reveal = client.sendRequest(request);
                        if (reveal != null && reveal.data != null && reveal.data.containsKey("board")) {
                            printFormattedBoard(reveal.data.get("board"));
                        }
                        gameFinished = true;
                    }
                }

                if (message != null) {
                    System.out.println("[Servidor] " + message);
                }

                if (response.data.containsKey("board")) {
                    printFormattedBoard(response.data.get("board"));
                }
            }
        }

        scanner.close();
        System.out.println("¡Cliente finalizado!");
    }

    private static int readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
            System.out.println("Ingrese un número entero válido.");
            scanner.next();
        }
    }

    @SuppressWarnings("unchecked")
    private static void printFormattedBoard(Object boardObject) {
        if (!(boardObject instanceof List)) {
            return;
        }

        List<List<Map<String, Object>>> board = (List<List<Map<String, Object>>>) boardObject;
        if (board.isEmpty() || board.get(0).isEmpty()) {
            System.out.println("Tablero vacío.");
            return;
        }

        System.out.println("\n--- TABLERO ---");
        System.out.print("    ");
        for (int j = 0; j < board.get(0).size(); j++) {
            System.out.printf("%2d ", j);
        }
        System.out.println();

        for (int i = 0; i < board.size(); i++) {
            System.out.printf("%2d  ", i);
            List<Map<String, Object>> row = board.get(i);
            for (Map<String, Object> cell : row) {
                boolean hide = Boolean.TRUE.equals(cell.get("hide"));
                boolean showAll = Boolean.TRUE.equals(cell.get("showAll"));
                boolean isMarked = Boolean.TRUE.equals(cell.get("isMarked"));
                boolean isLandMine = Boolean.TRUE.equals(cell.get("isLandMine"));
                Object valueObj = cell.get("value");
                int value = valueObj instanceof Number ? ((Number) valueObj).intValue() : 0;

                if (isMarked) {
                    System.out.print(YELLOW + " [M]" + RESET);
                } else if (showAll && isLandMine) {
                    System.out.print(RED + " [*]" + RESET);
                } else if (hide && !showAll) {
                    System.out.print(" [.] ");
                } else if (isLandMine) {
                    System.out.print(RED + " [*]" + RESET);
                } else {
                    System.out.print(" [" + value + "]");
                }
            }
            System.out.println();
        }
    }
}
