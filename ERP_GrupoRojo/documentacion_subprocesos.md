# Gestión de subprocesos en el ERP Grupo Rojo

## 1. Visión general

La aplicación se divide en dos procesos independientes:

| Proceso | Lenguaje | Archivo | Función |
|---|---|---|---|
| Proceso principal (lanzador) | Python | `main.py` | Muestra el menú, crea el subproceso, controla el tiempo y cancela si hace falta. |
| Subproceso (worker) | Java | `Worker.java` | Ejecuta una consulta de solo lectura contra la base de datos MySQL e imprime el resultado. |

El proceso principal nunca consulta la base de datos directamente. Delega cada consulta en un subproceso, de modo que, si la consulta se bloquea o tarda demasiado, el menú sigue bajo control y puede cancelarla.

```
 main.py (padre)                         Worker (hijo, JVM)
 ┌──────────────┐   Popen(args, env)    ┌──────────────────┐
 │ menú         │ ────────────────────► │ lee DB_URL/USER/ │
 │ hilo lector  │ ◄──── stdout+stderr ─ │ PASS del entorno │
 │ wait(timeout)│        (PIPE)         │ consulta MySQL   │
 └──────────────┘                       └──────────────────┘
        │ TimeoutExpired → terminate() → (3 s) → kill()
```

## 2. Creación del subproceso (`subprocess.Popen`)

```python
proc = subprocess.Popen(
    ["java", "-cp", CLASSPATH, "Worker", op],
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True,
    bufsize=1,
    env=env,
)
```

- **`Popen` no bloquea.** Devuelve el control inmediatamente, por lo que el padre puede seguir trabajando (leer la salida, vigilar el tiempo) mientras el hijo se ejecuta.
- **Lista de argumentos.** Se pasa como lista y no como cadena, así no interviene el intérprete de comandos (`shell=False`) y no hay riesgo de inyección.
- **`stdout=PIPE`.** Redirige la salida estándar del worker a una tubería que lee el padre.
- **`stderr=STDOUT`.** Mezcla los errores con la salida normal en la misma tubería y conserva el orden en que se produjeron.
- **`text=True`.** Convierte los bytes en cadenas de texto.
- **`bufsize=1`.** Usa búfer por líneas, de modo que cada línea queda disponible en cuanto el worker la emite.
- **`env`.** Las credenciales de la base de datos (`DB_URL`, `DB_USER`, `DB_PASS`) se pasan como variables de entorno del hijo. No van como argumentos de línea de comandos, porque estos son visibles para otros procesos del sistema.

## 3. Lectura de la salida en tiempo real (hilo + `flush`)

Mientras el hilo principal espera al subproceso, un hilo secundario (`threading.Thread`, `daemon=True`) lee la tubería línea a línea:

```python
def volcar(stream):
    for linea in stream:
        print(linea, end="", flush=True)
    stream.close()
```

Esto es necesario porque, si el padre solo esperara a que el hijo terminase, la tubería podría llenarse y bloquear al hijo, y además el usuario no vería nada hasta el final.

### Vaciado explícito del búfer (`flush`)

Cuando la salida de un programa va a una tubería o a un archivo, no a un terminal, el sistema la acumula en un búfer y solo la envía cuando se llena o el programa termina. Se vacía de forma explícita en los dos extremos:

- **Python:** `print(..., flush=True)`. Cada línea llega al terminal en cuanto se recibe. Si se cancela por timeout, el usuario ve la salida parcial ya producida.
- **Java:** `System.out.flush()` al final de `Worker.main`. Garantiza que el hijo entrega al padre todo lo que tenía pendiente antes de terminar.

## 4. Control del tiempo límite (`timeout`)

```python
try:
    proc.wait(timeout=timeout)
    lector.join()
    print(f"El worker terminó con código {proc.returncode}", flush=True)
except subprocess.TimeoutExpired:
    ...  # protocolo de cancelación (apartado 5)
```

- `wait(timeout=N)` espera como máximo N segundos. Si el hijo no ha terminado, lanza la excepción `TimeoutExpired`.
- Si termina a tiempo, se espera al hilo lector (`join`) para no perder líneas y se informa del **código de retorno** (`0` = éxito).
- El timeout es configurable desde la opción 6 del menú. El valor `0` equivale a `None`, es decir, **sin timeout**: el padre espera indefinidamente. Esto permite comparar el comportamiento con y sin límite de tiempo.

## 5. Protocolo de cancelación en dos fases (`terminate` / `kill`)

Al producirse `TimeoutExpired`, el hijo sigue vivo y hay que detenerlo. No se mata de golpe, sino en dos fases:

```python
except subprocess.TimeoutExpired:
    proc.terminate()            # fase 1: petición amable
    try:
        proc.wait(timeout=3)
    except subprocess.TimeoutExpired:
        proc.kill()             # fase 2: forzado
        proc.wait()
    lector.join()
    print("Consulta cancelada por timeout", flush=True)
```

| Fase | Método | Qué hace | Cuándo se usa |
|---|---|---|---|
| 1 | `terminate()` | Pide al proceso que se detenga (en POSIX envía `SIGTERM`). El proceso puede interceptarlo, cerrar la conexión y liberar recursos. | Siempre primero. |
| Espera | `wait(timeout=3)` | Da 3 segundos de cortesía para que el proceso termine solo. | Tras `terminate()`. |
| 2 | `kill()` | Mata el proceso de forma inmediata e incondicional (en POSIX envía `SIGKILL`, que no se puede interceptar). | Solo si el proceso no respondió a la fase 1. |

Después de cada fase se llama a `wait()` para **recoger el estado del proceso** y evitar que quede como proceso zombi. Por último, `lector.join()` espera a que el hilo lector termine, lo cual ocurre al cerrarse la tubería cuando el hijo muere.

**Nota sobre Windows:** el sistema donde se ha desarrollado y probado es Windows. Allí `terminate()` y `kill()` se implementan ambos con `TerminateProcess`, que mata el proceso al instante, por lo que la fase 1 no ofrece una parada "amable" real. El protocolo está igualmente implementado porque es el correcto y portable: en Linux y macOS sí se distingue entre `SIGTERM` y `SIGKILL`.

## 6. Resumen de excepciones y estados

| Situación | Resultado |
|---|---|
| El worker termina antes del límite | Se muestra toda la salida y el código de retorno. |
| El worker supera el límite y responde a `terminate()` | Se muestra la salida parcial y "Consulta cancelada por timeout". |
| El worker supera el límite y no responde a `terminate()` | Se aplica `kill()` tras 3 s; mismo mensaje. |
| Sin timeout (valor `0`) | El padre espera hasta que el worker termina. |

## 7. Pruebas realizadas

Tiempos medidos ejecutando cada operación sin límite (incluyen unos 2 s de arranque de la JVM y conexión a MySQL):

| Operación | Tiempo | Con timeout 1 s | Con timeout 4 s | Sin timeout |
|---|---|---|---|---|
| Inventario | 2,45 s | Cancelada | Termina | Termina |
| Recepciones | 3,22 s | Cancelada | Termina | Termina |
| Entregas | 3,40 s | Cancelada | Termina | Termina |
| Fabricación | 2,02 s | Cancelada | Termina | Termina |
| Compras | 5,13 s | Cancelada | Cancelada | Termina |

Los tiempos varían según la red. En las pruebas con 1 s se observó además que la salida parcial se muestra antes de cancelar, lo que confirma que el `flush` funciona.

## 8. Limitaciones conocidas

- Java solo vacía su búfer al final, por lo que, cuando la salida va a una tubería, las líneas pueden llegar a Python agrupadas. Añadir `flush()` tras cada impresión haría la salida línea a línea.
- Con `terminate()` el worker no cierra de forma ordenada la conexión a la base de datos, porque no tiene un manejador de señales; el servidor la libera al detectar la caída.
