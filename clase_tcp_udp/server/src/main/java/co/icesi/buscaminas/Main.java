package co.icesi.buscaminas;

import co.icesi.buscaminas.controllers.TCPController;
import co.icesi.buscaminas.services.ServicesImpl;

public class Main {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 12345;

        ServicesImpl services = new ServicesImpl();
        TCPController controller = new TCPController(services, port);
        controller.startService();
    }
}
