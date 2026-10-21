"""Reto PSP: subprocesos (Popen), IPC (Pipe), timeouts y señales."""
import argparse
import json
import logging
import signal
import sys

import gestor_procesos as gp
import ipc_pipe

log = logging.getLogger("main")


def instalar_senales() -> None:
    def manejador(signum, _frame):
        log.warning("Señal %s recibida: cancelando procesos hijos", signal.Signals(signum).name)
        gp.terminar_todos()
        sys.stdout.flush()
        sys.exit(130)

    signal.signal(signal.SIGINT, manejador)
    signal.signal(signal.SIGTERM, manejador)


def parte_subprocesos(timeout: float) -> "list[gp.ResultadoProceso]":
    entrada = json.dumps({"numeros": [1, 2, 3, 4.5, 10]})
    tareas = [
        ("normal", ["-u", gp.WORKER, "normal"], entrada),
        ("entrada_invalida", ["-u", gp.WORKER, "normal"], "esto no es json"),
        ("fallo", ["-u", gp.WORKER, "error"], None),
        ("lento (terminate)", ["-u", gp.WORKER, "lento"], None),
        ("terco (kill)", ["-u", gp.WORKER, "terco"], None),
    ]
    return gp.ejecutar_en_paralelo(tareas, timeout)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--timeout", type=float, default=3.0, help="segundos por tanda")
    args = ap.parse_args()

    logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(name)s] %(message)s")
    instalar_senales()

    print("== 1) Subprocesos con Popen/PIPE/communicate/timeout ==")
    for r in parte_subprocesos(args.timeout):
        print(f"{r.nombre:20} estado={r.estado:18} rc={r.returncode} "
              f"t={r.duracion:.2f}s out={r.stdout[:60]!r}")
    sys.stdout.flush()

    print("\n== 2) IPC con multiprocessing.Pipe ==")
    rangos = [(0, 20000, 0.0), (20000, 40000, 0.0), (0, 10, 10.0)]
    for r in ipc_pipe.ejecutar_pipes(rangos, args.timeout):
        print(r)
    sys.stdout.flush()
    return 0


if __name__ == "__main__":
    sys.exit(main())
