"""Читання CSV і перетворення рядків на типізовані записи."""

import csv
import io
import math
from datetime import date
from pathlib import Path

from .errors import DataError

REQUIRED_FIELDS = ("date", "kind", "duration", "distance", "pulse")

Record = dict[str, object]


def _text(row: dict[str, str | None], field: str, row_number: int) -> str:
    """Повертає обрізаний текст поля або DataError, якщо значення немає."""
    value = row.get(field)
    if value is None or not value.strip():
        raise DataError(f"Рядок {row_number}: поле {field} порожнє")
    return value.strip()


def _non_negative_int(row: dict[str, str | None], field: str, row_number: int) -> int:
    """Перетворює поле на ціле число >= 0."""
    raw = _text(row, field, row_number)
    try:
        value = int(raw)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле {field} має бути цілим числом") from error
    if value < 0:
        raise DataError(f"Рядок {row_number}: поле {field} не може бути від'ємним")
    return value


def _non_negative_float(row: dict[str, str | None], field: str, row_number: int) -> float:
    """Перетворює поле на скінченне дійсне число >= 0."""
    raw = _text(row, field, row_number)
    try:
        value = float(raw)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле {field} має бути числом") from error
    if not math.isfinite(value):
        raise DataError(f"Рядок {row_number}: поле {field} має бути скінченним числом")
    if value < 0:
        raise DataError(f"Рядок {row_number}: поле {field} не може бути від'ємним")
    return value


def _iso_date(row: dict[str, str | None], row_number: int) -> str:
    """Перевіряє дату у форматі ISO YYYY-MM-DD і повертає її текстом."""
    raw = _text(row, "date", row_number)
    try:
        date.fromisoformat(raw)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле date має формат YYYY-MM-DD") from error
    return raw


def parse_row(row: dict[str, str | None], row_number: int) -> Record:
    """Перетворює один рядок DictReader на запис із типізованими значеннями."""
    if None in row:
        raise DataError(f"Рядок {row_number}: забагато полів")
    return {
        "date": _iso_date(row, row_number),
        "kind": " ".join(_text(row, "kind", row_number).split()),
        "duration": _non_negative_int(row, "duration", row_number),
        "distance": _non_negative_float(row, "distance", row_number),
        "pulse": _non_negative_int(row, "pulse", row_number),
    }


def parse_records(text: str) -> list[Record]:
    """Розбирає CSV-текст; порожній текст або неправильний заголовок -> DataError."""
    reader = csv.DictReader(io.StringIO(text, newline=""))
    if reader.fieldnames is None:
        raise DataError("CSV порожній: немає рядка заголовка")
    missing = [field for field in REQUIRED_FIELDS if field not in reader.fieldnames]
    if missing:
        raise DataError(f"Рядок 1: відсутні стовпці {', '.join(missing)}")
    try:
        # Номер 2 — перший рядок даних після заголовка.
        return [parse_row(row, number) for number, row in enumerate(reader, start=2)]
    except csv.Error as error:
        raise DataError(f"Рядок {reader.line_num}: пошкоджений CSV") from error


def load_records(path: Path) -> list[Record]:
    """Читає CSV-файл через DictReader (UTF-8, newline="") і повертає записи."""
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            text = source.read()
    except UnicodeDecodeError as error:
        raise DataError(f"Файл {path} не є коректним UTF-8") from error
    return parse_records(text)
