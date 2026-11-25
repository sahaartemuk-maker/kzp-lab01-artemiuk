"""Тести звіту, запису файла та CLI."""

from pathlib import Path

from kzp_lab06 import format_report, load_records, write_report
from kzp_lab06.cli import main

EXPECTED = """\
Записів: 8
Види тренувань (6): Біг; Біг, інтервали; Велосипед; Йога; Плавання; Ходьба
Тренувань від 45 хв: 6
Дистанція за видом (від 45 хв):
  Біг: 17.70 км
  Велосипед: 60.50 км
  Йога: 0.00 км
  Плавання: 2.40 км
Середній пульс: 134.1 уд/хв
Найдовше тренування: Велосипед (2026-09-02), 90 хв
"""


def test_full_report(input_csv: Path) -> None:
    """Повний звіт контрольного файла збігається з еталоном."""
    assert format_report(load_records(input_csv)) == EXPECTED


def test_empty_report() -> None:
    """Звіт для порожнього набору."""
    assert format_report([]) == (
        "Записів: 0\nВиди тренувань (0): немає\nТренувань від 45 хв: 0\n"
        "Дистанція за видом (від 45 хв):\n  немає\nСередній пульс: —\n"
        "Найдовше тренування: —\n"
    )


def test_write_report(tmp_path: Path) -> None:
    """Запис UTF-8 у новий каталог."""
    output = tmp_path / "nested" / "report.txt"
    write_report(output, "Готово\n")
    assert output.read_bytes() == "Готово\n".encode()


def test_cli_success(input_csv: Path, tmp_path: Path, capsys) -> None:
    """Успішний запуск: код 0, файл збігається з еталоном, вхід не змінено."""
    before = input_csv.read_bytes()
    output = tmp_path / "summary.txt"
    assert main([str(input_csv), str(output)]) == 0
    assert output.read_text(encoding="utf-8") == EXPECTED
    assert input_csv.read_bytes() == before
    assert "Звіт записано" in capsys.readouterr().out


def test_cli_broken_csv(tmp_path: Path, capsys) -> None:
    """Пошкоджений CSV: код 1, повідомлення в stderr, звіт не створено."""
    source = tmp_path / "bad.csv"
    source.write_text("date,kind,duration,distance,pulse\n2026-09-01,Біг,x,1,1\n", "utf-8")
    output = tmp_path / "out.txt"
    assert main([str(source), str(output)]) == 1
    assert "Рядок 2: поле duration" in capsys.readouterr().err
    assert not output.exists()


def test_cli_missing_file(tmp_path: Path, capsys) -> None:
    """Відсутній вхідний файл: код 2."""
    assert main([str(tmp_path / "none.csv"), str(tmp_path / "out.txt")]) == 2
    assert "Помилка файлу" in capsys.readouterr().err


def test_cli_same_file(input_csv: Path) -> None:
    """Вхід і вихід не можуть збігатися."""
    assert main([str(input_csv), str(input_csv)]) == 1
