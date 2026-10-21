"""Proceso hijo. Modos: normal, lento (responde a SIGTERM), terco (lo ignora), error."""
import hashlib
import json
import signal
import sys
import time


def log_err(msg: str) -> None:
    print(f"[worker] {msg}", file=sys.stderr)
    sys.stderr.flush()


def main() -> int:
    modo = sys.argv[1] if len(sys.argv) > 1 else "normal"

    if modo == "terco":
        signal.signal(signal.SIGTERM, signal.SIG_IGN)

    if modo in ("lento", "terco"):
        log_err(f"modo {modo}: simulando tarea de 60 s")
        for _ in range(600):
            time.sleep(0.1)
        return 0

    if modo == "error":
        log_err("fallo simulado")
        return 3

    entrada = sys.stdin.read()
    try:
        datos = json.loads(entrada)
        numeros = [float(n) for n in datos["numeros"]]
    except (json.JSONDecodeError, KeyError, TypeError, ValueError) as exc:
        log_err(f"entrada inválida: {exc}")
        return 2

    resultado = {
        "suma": sum(numeros),
        "cantidad": len(numeros),
        "sha256_entrada": hashlib.sha256(entrada.encode()).hexdigest(),
    }
    print(json.dumps(resultado))
    sys.stdout.flush()
    return 0


if __name__ == "__main__":
    sys.exit(main())
