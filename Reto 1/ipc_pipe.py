"""Comunicación IPC con multiprocessing.Pipe entre el proceso padre y varios hijos."""
import hashlib
import json
import logging
import multiprocessing as mp
import os
import sys
import time

log = logging.getLogger("ipc")


def _checksum(obj: dict) -> str:
    return hashlib.sha256(json.dumps(obj, sort_keys=True).encode()).hexdigest()


def _contar_primos(desde: int, hasta: int) -> int:
    def es_primo(n: int) -> bool:
        if n < 2:
            return False
        i = 2
        while i * i <= n:
            if n % i == 0:
                return False
            i += 1
        return True
    return sum(1 for n in range(desde, hasta) if es_primo(n))


def trabajador(conn, ident: int, desde: int, hasta: int, retardo: float = 0.0) -> None:
    try:
        conn.send({"tipo": "progreso", "id": ident, "pid": os.getpid(), "msg": "iniciado"})
        time.sleep(retardo)
        carga = {"id": ident, "desde": desde, "hasta": hasta,
                 "primos": _contar_primos(desde, hasta)}
        conn.send({"tipo": "resultado", "carga": carga, "sha256": _checksum(carga)})
    except Exception as exc:
        conn.send({"tipo": "error", "id": ident, "msg": repr(exc)})
    finally:
        conn.close()
        sys.stdout.flush()
        sys.stderr.flush()


def ejecutar_pipes(rangos: "list[tuple[int, int, float]]", timeout: float,
                   gracia: float = 1.0) -> "list[dict]":
    ctx = mp.get_context("spawn")
    hijos = []
    for i, (d, h, retardo) in enumerate(rangos):
        rx, tx = ctx.Pipe(duplex=False)
        p = ctx.Process(target=trabajador, args=(tx, i, d, h, retardo), daemon=True)
        p.start()
        tx.close()  # el padre cierra su copia del extremo de escritura
        hijos.append((i, p, rx))

    resultados = []
    inicio = time.monotonic()
    for i, p, rx in hijos:
        res = {"id": i, "estado": "SIN_RESPUESTA"}
        try:
            while True:
                restante = timeout - (time.monotonic() - inicio)
                if restante <= 0 or not rx.poll(restante):
                    raise TimeoutError
                msg = rx.recv()
                if msg["tipo"] == "progreso":
                    log.info("Hijo %s (PID %s): %s", i, msg["pid"], msg["msg"])
                elif msg["tipo"] == "resultado":
                    ok = _checksum(msg["carga"]) == msg["sha256"]
                    res = {"id": i, "estado": "OK" if ok else "CORRUPTO", **msg["carga"]}
                    break
                else:
                    res = {"id": i, "estado": "ERROR", "detalle": msg["msg"]}
                    break
        except TimeoutError:
            log.warning("Hijo %s excede el timeout", i)
            res["estado"] = "TIMEOUT"
        except (EOFError, OSError):
            res["estado"] = "PIPE_CERRADO"
        finally:
            rx.close()
            p.join(timeout=gracia)
            if p.is_alive():
                p.terminate()
                p.join(timeout=gracia)
                if p.is_alive():
                    p.kill()
                    p.join()
        resultados.append(res)
    return resultados
