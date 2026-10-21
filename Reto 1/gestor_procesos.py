"""Gestión de subprocesos con Popen, PIPE, communicate y cancelación en 2 fases."""
import logging
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path

log = logging.getLogger("gestor")

WORKER = str(Path(__file__).with_name("worker.py"))

_activos: "set[subprocess.Popen]" = set()


@dataclass
class ResultadoProceso:
    nombre: str
    estado: str
    returncode: "int | None"
    stdout: str
    stderr: str
    duracion: float


def iniciar(args: "list[str]") -> subprocess.Popen:
    proc = subprocess.Popen(
        [sys.executable, *args],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    _activos.add(proc)
    log.info("Lanzado PID %s: %s", proc.pid, " ".join(args))
    return proc


def cancelar(proc: subprocess.Popen, gracia: float = 2.0) -> str:
    """Fase 1: terminate(). Fase 2: kill() si no responde."""
    if proc.poll() is not None:
        return "YA_TERMINADO"
    log.warning("Fase 1: terminate() a PID %s", proc.pid)
    proc.terminate()
    try:
        proc.wait(timeout=gracia)
        return "TIMEOUT_TERMINATE"
    except subprocess.TimeoutExpired:
        log.error("Fase 2: PID %s ignora terminate, kill()", proc.pid)
        proc.kill()
        proc.wait()
        return "TIMEOUT_KILL"


def recoger(nombre: str, proc: subprocess.Popen, entrada: "str | None",
            timeout: float, inicio: float, gracia: float = 2.0) -> ResultadoProceso:
    restante = max(0.05, timeout - (time.monotonic() - inicio))
    try:
        out, err = proc.communicate(input=entrada, timeout=restante)
        estado = "OK" if proc.returncode == 0 else "ERROR"
    except subprocess.TimeoutExpired:
        estado = cancelar(proc, gracia)
        out, err = proc.communicate()
    except (OSError, ValueError) as exc:
        log.exception("Fallo de E/S con %s", nombre)
        cancelar(proc, gracia)
        out, err, estado = "", str(exc), "EXCEPCION"
    finally:
        _activos.discard(proc)
    return ResultadoProceso(nombre, estado, proc.returncode, out.strip(),
                            err.strip(), time.monotonic() - inicio)


def ejecutar_en_paralelo(tareas: "list[tuple[str, list[str], str | None]]",
                         timeout: float) -> "list[ResultadoProceso]":
    inicio = time.monotonic()
    lanzados = [(n, iniciar(a), e) for n, a, e in tareas]
    return [recoger(n, p, e, timeout, inicio) for n, p, e in lanzados]


def terminar_todos() -> None:
    for proc in list(_activos):
        cancelar(proc, gracia=1.0)
        _activos.discard(proc)
