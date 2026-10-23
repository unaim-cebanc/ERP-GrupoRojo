import os
import subprocess
import threading

# --- Ajusta estos valores a tu entorno ---
DB_URL = "jdbc:mysql://100.103.202.75:3307/decasport?allowPublicKeyRetrieval=true&useSSL=false"
DB_USER = "root"
DB_PASS = "Base_reto_rojo_67"
JAR_MYSQL = "mysql-connector.jar"   # nombre del conector JDBC en esta carpeta
TIMEOUT = 1                        # segundos; None = sin límite
# En Windows el separador del classpath es ";" en vez de ":"
CLASSPATH = "target/classes" + os.pathsep + open("cp.txt").read().strip()
OPS = {"1": "mostrar", "2": "recepciones", "3": "entregas",
       "4": "fabricacion", "5": "compras"}


def volcar(stream, contador):
    # Imprime cada línea del worker en cuanto llega, sin esperar a que termine
    for linea in stream:
        print(linea, end="", flush=True)
        contador[0] += 1
    stream.close()


def detener(proc, forzar):
    # En Windows "java" es un lanzador que crea la JVM como proceso hijo:
    # terminate()/kill() solo matan el lanzador, así que se cancela todo el árbol.
    # taskkill sin /F no cierra procesos de consola, por eso aquí siempre es forzado.
    if os.name == "nt":
        subprocess.run(["taskkill", "/PID", str(proc.pid), "/T", "/F"], capture_output=True)
    elif forzar:
        proc.kill()
    else:
        proc.terminate()


def ejecutar(op, timeout):
    env = {**os.environ, "DB_URL": DB_URL, "DB_USER": DB_USER, "DB_PASS": DB_PASS}
    proc = subprocess.Popen(
        ["java", "-cp", CLASSPATH, "Worker", op],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,   # errores y salida juntos, en orden
        text=True,
        bufsize=1,                  # buffer por líneas en el lado de Python
        env=env,
    )
    lineas = [0]                    # contador compartido con el hilo lector
    lector = threading.Thread(target=volcar, args=(proc.stdout, lineas), daemon=True)
    lector.start()
    modo = "sin timeout" if timeout is None else f"timeout {timeout:g}s"
    try:
        # El proceso principal espera aquí mientras el hilo va mostrando la salida
        proc.wait(timeout=timeout)
        lector.join()
        print(f"El worker terminó con código {proc.returncode}", flush=True)
    except subprocess.TimeoutExpired:
        detener(proc, forzar=False)     # fase 1: petición amable
        try:
            proc.wait(timeout=3)
        except subprocess.TimeoutExpired:
            detener(proc, forzar=True)  # fase 2: forzado
            proc.wait()
        lector.join()
        print("Consulta cancelada por timeout", flush=True)
    print(f"Líneas recibidas: {lineas[0]} ({modo})", flush=True)


def pedir_timeout(actual):
    txt = input("Nuevo timeout en segundos (0 = sin timeout): ").strip()
    try:
        valor = float(txt)
    except ValueError:
        valor = -1
    if valor < 0:
        print("Valor no válido, se mantiene el actual")
        return actual
    return None if valor == 0 else valor


def main():
    timeout = TIMEOUT
    while True:
        estado = "sin timeout" if timeout is None else f"{timeout:g}s"
        print(f"\n1.Inventario  2.Recepciones  3.Entregas  4.Fabricación  5.Compras  6.Timeout [{estado}]  0.Salir")
        op = input("> ").strip()
        if op == "0":
            break
        if op == "6":
            timeout = pedir_timeout(timeout)
        elif op in OPS:
            ejecutar(OPS[op], timeout)
        else:
            print("Opción incorrecta")


if __name__ == "__main__":
    main()
