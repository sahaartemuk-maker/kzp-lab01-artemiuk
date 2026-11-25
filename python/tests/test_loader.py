"""Тести завантаження: CSV → об'єкти підтипів."""

from datetime import date
from pathlib import Path

import pytest

from kzp_lab07 import (
    Cycling,
    DataError,
    GeneralTraining,
    Running,
    load_records,
    parse_records,
    training_class,
)

HEADER = "date,kind,duration,distance,pulse\n"


def test_valid_row_becomes_object() -> None:
    """Коректний рядок перетворюється на Running із типізованими полями."""
    records = parse_records(HEADER + "2026-09-01,  Біг ,45,8.5,152\n")
    assert records == [Running(date(2026, 9, 1), "Біг", 45, 8.5, 152)]


def test_quoted_field_with_comma() -> None:
    """Поле з комою в лапках — один вид, підтип Running."""
    record = parse_records(HEADER + '2026-09-07,"Біг, інтервали",40,6.0,168\n')[0]
    assert record.kind == "Біг, інтервали"
    assert type(record) is Running


@pytest.mark.parametrize(
    ("kind", "cls"),
    [
        ("Біг", Running),
        ("біг, інтервали", Running),
        ("Велосипед", Cycling),
        ("Йога", GeneralTraining),
        ("Плавання", GeneralTraining),
    ],
)
def test_training_class(kind: str, cls: type) -> None:
    """Factory вибирає підтип лише на межі завантаження."""
    assert training_class(kind) is cls


def test_load_control_file(input_csv: Path) -> None:
    """8 записів, дублікати видів і всі три підтипи."""
    records = load_records(input_csv)
    assert len(records) == 8
    assert [r.kind for r in records].count("Біг") == 2
    assert {type(r) for r in records} == {Running, Cycling, GeneralTraining}


def test_header_only_gives_empty_list() -> None:
    """Лише заголовок — коректний порожній набір."""
    assert parse_records(HEADER) == []


def test_empty_text_is_error() -> None:
    """Порожній файл (немає заголовка) — DataError."""
    with pytest.raises(DataError, match="порожній"):
        parse_records("")


def test_missing_column() -> None:
    """Відсутній обов'язковий стовпець — DataError із назвою поля."""
    with pytest.raises(DataError, match="pulse"):
        parse_records("date,kind,duration,distance\n2026-09-01,Біг,45,8.5\n")


@pytest.mark.parametrize(
    ("line", "message"),
    [
        ("2026-09-01,Біг,сорок,8.5,152", "Рядок 2: поле duration має бути цілим"),
        ("2026-09-01,Біг,45,abc,152", "Рядок 2: поле distance має бути числом"),
        ("2026-09-01,Біг,45,nan,152", "Рядок 2: поле distance має бути скінченним"),
        ("2026-09-01,Біг,-5,8.5,152", "Рядок 2: поле duration не може бути від'ємним"),
        ("2026-09-01,,45,8.5,152", "Рядок 2: поле kind порожнє"),
        ("2026-09-01,Біг,45,8.5", "Рядок 2: поле pulse порожнє"),
        ("01.09.2026,Біг,45,8.5,152", "Рядок 2: поле date"),
        ("2026-09-01,Біг,45,8.5,152,зайве", "Рядок 2: забагато полів"),
    ],
)
def test_bad_values(line: str, message: str) -> None:
    """Помилкові значення дають DataError з номером рядка і назвою поля."""
    with pytest.raises(DataError, match=message):
        parse_records(HEADER + line + "\n")


def test_error_keeps_cause() -> None:
    """raise ... from зберігає першопричину ValueError."""
    with pytest.raises(DataError) as info:
        parse_records(HEADER + "2026-09-01,Біг,-1,8.5,152\n")
    assert isinstance(info.value.__cause__, ValueError)


def test_row_number_counts_lines() -> None:
    """Номер рядка вказує на рядок файла (заголовок — 1)."""
    text = HEADER + "2026-09-01,Біг,45,8.5,152\n2026-09-02,Біг,45,8.5,-1\n"
    with pytest.raises(DataError, match="Рядок 3: поле pulse"):
        parse_records(text)


def test_zero_values_allowed() -> None:
    """Нулі — граничні допустимі значення."""
    assert parse_records(HEADER + "2026-09-08,Йога,0,0.0,0\n")[0].distance == 0.0


def test_broken_utf8(tmp_path: Path) -> None:
    """Файл не в UTF-8 — DataError із першопричиною."""
    path = tmp_path / "bad.csv"
    path.write_bytes(HEADER.encode() + "2026-09-01,Біг,45,8.5,152\n".encode("cp1251"))
    with pytest.raises(DataError) as info:
        load_records(path)
    assert isinstance(info.value.__cause__, UnicodeDecodeError)
