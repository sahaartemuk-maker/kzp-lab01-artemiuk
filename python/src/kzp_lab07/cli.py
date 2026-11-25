"""Командний рядок: python -m kzp_lab07.cli <input.csv> <report.txt>."""

import argparse
import sys
from pathlib import Path

from .errors import DataError
from .loader import load_records
from .reporting import format_report, write_report

EXIT_OK = 0
EXIT_DATA_ERROR = 1
EXIT_IO_ERROR = 2


def build_parser() -> argparse.ArgumentParser:
    """Створює парсер аргументів CLI."""
    parser = argparse.ArgumentParser(
        prog="kzp-lab07", description="Звіт за CSV спортивних тренувань (ЛР7, варіант 2)"
    )
    parser.add_argument("input", type=Path, help="вхідний CSV (UTF-8)")
    parser.add_argument("output", type=Path, help="файл текстового звіту")
    return parser


def main(argv: list[str] | None = None) -> int:
    """Точка входу; повертає код завершення 0/1/2."""
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(encoding="utf-8")
    args = build_parser().parse_args(argv)
    if args.input.resolve() == args.output.resolve():
        print("Помилка: вхідний і вихідний файли мають бути різними", file=sys.stderr)
        return EXIT_DATA_ERROR
    try:
        records = load_records(args.input)
        text = format_report(records)
        write_report(args.output, text)
    except DataError as error:
        print(f"Помилка даних: {error}", file=sys.stderr)
        return EXIT_DATA_ERROR
    except OSError as error:
        print(f"Помилка файлу: {error}", file=sys.stderr)
        return EXIT_IO_ERROR
    print(text, end="")
    print(f"Звіт записано: {args.output}")
    return EXIT_OK


if __name__ == "__main__":
    sys.exit(main())
