package co.icesi.buscaminas.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;


public class MainClient {

    public static void main(String[] args) {
        TCPClient client = new TCPClient("localhost", 12345);
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        System.out.println("   CLIENTE BUSCAMINAS TCP   ");

        while (!exit) {
            System.out.println("\n--- MENU DE ACCIONES ---");
            System.out.println("1. Iniciar nuevo juego (INIT_GAME)");
            System.out.println("2. Obtener tablero actual (GET_BOARD)");
            System.out.println("3. Seleccionar casilla (SELECT_CELL)");
            System.out.println("4. Marcar/Desmarcar bandera (MARK_CELL)");
            System.out.println("5. Revelar todo (SOW_ALL)");
            System.out.println("6. Salir");
            System.out.print("Elige una opcion: ");

            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("Por favor ingresa un número válido.");
                continue;
            }

            int option = scanner.nextInt();
            Request rq = new Request();
            rq.data = new HashMap<>();

            switch (option) {
                case 1:
                    rq.action = "INIT_GAME";
                    System.out.print("Filas (n): ");
                    rq.data.put("n", String.valueOf(scanner.nextInt()));
                    System.out.print("Columnas (m): ");
                    rq.data.put("m", String.valueOf(scanner.nextInt()));
                    System.out.print("Cantidad de minas: ");
                    rq.data.put("minas", String.valueOf(scanner.nextInt()));
                    break;

                case 2:
                    rq.action = "GET_BOARD";
                    break;

                case 3:
                    rq.action = "SELECT_CELL";
                    System.out.print("Fila (i): ");
                    rq.data.put("i", String.valueOf(scanner.nextInt()));
                    System.out.print("Columna (j): ");
                    rq.data.put("j", String.valueOf(scanner.nextInt()));
                    break;

                case 4:
                    rq.action = "MARK_CELL";
                    System.out.print("Fila (i): ");
                    rq.data.put("i", String.valueOf(scanner.nextInt()));
                    System.out.print("Columna (j): ");
                    rq.data.put("j", String.valueOf(scanner.nextInt()));
                    break;

                case 5:
                    rq.action = "SOW_ALL";
                    break;

                case 6:
                    exit = true;
                    continue;

                default:
                    System.out.println("Opción no válida.");
                    continue;
            }

            Response response = client.sendRequest(rq);
            if (response != null) {
                System.out.println("\n[Servidor] Estado: " + response.status);
                if (response.data != null && response.data.containsKey("board")) {
                    printFormattedBoard(response.data.get("board"));
                }
                if (response.data != null && Boolean.TRUE.equals(response.data.get("gameEnd"))) {
                    if (Boolean.TRUE.equals(response.data.get("win"))) {
                        System.out.println("\n¡Ganaste! El juego ha terminado.");
                    } else {
                        System.out.println("\n¡Perdiste! El juego ha terminado.");
                    }
                    break;
                }
            } else {
                System.out.println("\n[Error] No se recibió respuesta del servidor.");
            }
        }

        scanner.close();
        System.out.println("¡Cliente finalizado!");
    }

    @SuppressWarnings("unchecked")
    private static void printFormattedBoard(Object boardObject) {
        if (!(boardObject instanceof List)) return;

        List<List<Map<String, Object>>> board = (List<List<Map<String, Object>>>) boardObject;
        System.out.println("\n--- TABLERO ---");

        System.out.print("   ");
        for (int j = 0; j < board.get(0).size(); j++) {
            System.out.print(j + " ");
        }
        System.out.println();

        for (int i = 0; i < board.size(); i++) {
            System.out.print(i + "  ");
            List<Map<String, Object>> row = board.get(i);
            for (Map<String, Object> cell : row) {
                boolean hide = Boolean.TRUE.equals(cell.get("hide"));
                boolean showAll = Boolean.TRUE.equals(cell.get("showAll"));
                boolean isMarked = Boolean.TRUE.equals(cell.get("isMarked"));
                boolean isLandMine = Boolean.TRUE.equals(cell.get("isLandMine"));
                Double valueDouble = (Double) cell.get("value");
                int value = valueDouble != null ? valueDouble.intValue() : 0;

                if (showAll) {
                    if (isLandMine) {
                        System.out.print("* ");
                    } else {
                        System.out.print(value + " ");
                    }
                } else if (isMarked) {
                    System.out.print("F ");
                } else if (hide) {
                    System.out.print("- ");
                } else if (isLandMine) {
                    System.out.print("* ");
                } else {
                    System.out.print(value + " ");
                }
            }
            System.out.println();
        }
    }
}
