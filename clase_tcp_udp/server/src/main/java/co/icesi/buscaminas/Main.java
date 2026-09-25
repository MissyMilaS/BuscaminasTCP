package co.icesi.buscaminas;

import co.icesi.buscaminas.controllers.TCPController;
import co.icesi.buscaminas.services.ServicesImpl;

public class Main {

    public static void main(String[] args) {
        // 1. Instanciar los servicios centrales del Buscaminas
        ServicesImpl serv = new ServicesImpl();

        // 2. Crear e iniciar el controlador TCP en el puerto 12345
        TCPController iceController = new TCPController(serv);
        iceController.startService();
    }
}