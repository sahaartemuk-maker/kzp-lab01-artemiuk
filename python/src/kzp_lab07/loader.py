"""Читання CSV і перетворення рядків на об'єкти-тренування."""

import csv
import io
from datetime import date
from pathlib import Path

from .errors import DataError
from .models import Cycling, GeneralTraining, Running, Training

REQUIRED_FIELDS = ("date", "kind", "duration", "distance", "pulse")


def training_class(kind: str) -> type[Training]:
    """Вибирає підтип за видом: «Біг…» → Running, «Велосипед…» → Cycling, інше → General."""
    lowered = kind.casefold()
    if lowered.startswith("біг"):
        return Running
    if lowered.startswith("велосипед"):
        return Cycling
    return GeneralTraining


def _text(row: dict[str, str | None], field: str, row_number: int) -> str:
    """Повертає обрізаний текст поля або DataError, якщо значення немає."""
    value = row.get(field)
    if value is None or not value.strip():
        raise DataError(f"Рядок {row_number}: поле {field} порожнє")
    return value.strip()


def _convert(row: dict[str, str | None], field: str, row_number: int, kind: type) -> object:
    """Перетворює текст поля на int/float/date з повідомленням про рядок."""
    raw = _text(row, field, row_number)
    try:
        if kind is int:
            return int(raw)
        if kind is date:
            return date.fromisoformat(raw)
        return float(raw)
    except ValueError as error:
        expected = {int: "цілим числом", float: "числом", date: "у форматі YYYY-MM-DD"}[kind]
        raise DataError(f"Рядок {row_number}: поле {field} має бути {expected}") from error


def build_training(row: dict[str, str | None], row_number: int) -> Training:
    """Factory: рядок DictReader → об'єкт підтипу; інваріанти перевіряють властивості."""
    if None in row:
        raise DataError(f"Рядок {row_number}: забагато полів")
    values = {
        "day": _convert(row, "date", row_number, date),
        "kind": _text(row, "kind", row_number),
        "duration": _convert(row, "duration", row_number, int),
        "distance": _convert(row, "distance", row_number, float),
        "pulse": _convert(row, "pulse", row_number, int),
    }
    cls = training_class(str(values["kind"]))
    try:
        return cls(**values)  # type: ignore[arg-type]
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: {error}") from error


def parse_records(text: str) -> list[Training]:
    """Розбирає CSV-текст; порожній текст або неправильний заголовок -> DataError."""
    reader = csv.DictReader(io.StringIO(text, newline=""))
    if reader.fieldnames is None:
        raise DataError("CSV порожній: немає рядка заголовка")
    missing = [field for field in REQUIRED_FIELDS if field not in reader.fieldnames]
    if missing:
        raise DataError(f"Рядок 1: відсутні стовпці {', '.join(missing)}")
    try:
        # Номер 2 — перший рядок даних після заголовка.
        return [build_training(row, number) for number, row in enumerate(reader, start=2)]
    except csv.Error as error:
        raise DataError(f"Рядок {reader.line_num}: пошкоджений CSV") from error


def load_records(path: Path) -> list[Training]:
    """Читає CSV-файл (UTF-8, newline="") і повертає список об'єктів."""
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            text = source.read()
    except UnicodeDecodeError as error:
        raise DataError(f"Файл {path} не є коректним UTF-8") from error
    return parse_records(text)
