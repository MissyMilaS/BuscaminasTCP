# BuscaminasTCP

Proyecto de laboratorio de Computación en Internet I para un Buscaminas distribuido sobre TCP con JSON.

## Estructura

```text
clase_tcp_udp/
├── build.gradle
├── settings.gradle
├── .gitignore
├── README.md
├── server/
│   └── src/main/java/co/icesi/buscaminas/
└── client/
    └── src/main/java/co/icesi/buscaminas/
```

## Requisitos

- Java 17+
- Gradle 8+

## Ejecutar servidor

```bash
cd clase_tcp_udp
./gradlew :server:run --args="12345"
```

## Ejecutar cliente

```bash
cd clase_tcp_udp
./gradlew :client:run --args="localhost 12345"
```

## Protocolo

### Request

```json
{
  "action": "INIT_GAME",
  "data": {
    "n": "8",
    "m": "8",
    "minas": "10"
  }
}
```

### Response

```json
{
  "status": "OK",
  "data": {
    "board": [[{...}]],
    "win": true,
    "gameEnd": false,
    "message": ""
  }
}
```

## Declaración IAG

Durante el desarrollo de este proyecto se utilizó asistencia de IA para:
- analizar patrones de diseño de TCP/JSON,
- revisar el manejo de `ThreadPool`,
- proponer validaciones de entrada,
- depurar errores de serialización y concurrencia,
- verificar que la solución cumpla con el protocolo detallado en la guía práctica.

La validación se realizó revisando el contrato del servidor y el cliente, además de confirmar que la lógica de `markCell`, `gameEnd` y `win` estuviera coherente con el comportamiento del juego.

## Cuestionario conceptual

1. TCP framing: el flujo continuo de bytes necesita delimitación con `\n` y `flush()`. Si no se delimitan los mensajes, el receptor puede bloquear esperando más bytes o leer un stream incompleto.
2. ThreadPool: evita la sobrecarga de `new Thread()` por cliente y permite controlar la concurrencia con un número fijo de hilos.
3. Condiciones de carrera: si dos clientes modifican el mismo tablero sin sincronización, pueden verse estados inconsistentes al mismo tiempo.
4. Conexiones cortas vs persistentes: la conexión corta consume menos recursos por sesión, pero implica mayor latencia y más overhead de establecimiento; la persistente reduce latencia pero usa más descriptores.
5. IAG: la IA se usó como apoyo de diseño y depuración; la validación final fue manual y basada en el protocolo establecido por la guía.

## Notas final

Este repositorio está preparado para una ejecución en dos módulos separados: `server` y `client`, con una interfaz de consola clara y la lógica de juego ajustada a la rúbrica del laboratorio.
