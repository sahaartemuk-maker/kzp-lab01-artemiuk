"""Тести звіту, сумісності з ЛР6, запису файла та CLI."""

from pathlib import Path

import pytest

from kzp_lab07 import ReportWriter, format_report, load_records, write_report
from kzp_lab07.cli import main

LR6_BASELINE = Path(__file__).resolve().parents[1] / "reports" / "summary_lr6.txt"

LOAD_BLOCK = """\
Навантаження за видом (бал):
  Біг: 145.90 (2)
  Біг, інтервали: 67.20 (1)
  Велосипед: 211.00 (2)
  Йога: 36.80 (1)
  Плавання: 51.20 (1)
  Ходьба: 21.00 (1)
Сумарне навантаження: 533.10 бал
"""


@pytest.fixture
def expected() -> str:
    """Еталон: звіт ЛР6 без змін + новий блок навантаження."""
    return LR6_BASELINE.read_text(encoding="utf-8") + LOAD_BLOCK


def test_lr6_part_unchanged(input_csv: Path) -> None:
    """Регресія: перші рядки звіту байт-у-байт збігаються зі звітом ЛР6."""
    report = format_report(load_records(input_csv))
    assert report.startswith(LR6_BASELINE.read_text(encoding="utf-8"))


def test_full_report(input_csv: Path, expected: str) -> None:
    """Повний звіт контрольного файла."""
    assert format_report(load_records(input_csv)) == expected


def test_empty_report() -> None:
    """Звіт для порожнього набору."""
    assert format_report([]) == (
        "Записів: 0\nВиди тренувань (0): немає\nТренувань від 45 хв: 0\n"
        "Дистанція за видом (від 45 хв):\n  немає\nСередній пульс: —\n"
        "Найдовше тренування: —\nНавантаження за видом (бал):\n  немає\n"
        "Сумарне навантаження: 0.00 бал\n"
    )


def test_write_report(tmp_path: Path) -> None:
    """Запис UTF-8 у новий каталог, тимчасовий файл прибрано."""
    output = tmp_path / "nested" / "report.txt"
    write_report(output, "Готово\n")
    assert output.read_bytes() == "Готово\n".encode()
    assert list(output.parent.iterdir()) == [output]


def test_report_writer_closes_on_exception(tmp_path: Path) -> None:
    """Контекстний менеджер: при винятку файл закрито, ціль не змінено, виняток не загублено."""
    output = tmp_path / "report.txt"
    output.write_text("старий\n", encoding="utf-8")
    writer = ReportWriter(output)
    with pytest.raises(RuntimeError, match="збій"):
        with writer as target:
            target.write("новий")
            raise RuntimeError("збій")
    assert writer.closed
    assert output.read_text(encoding="utf-8") == "старий\n"
    assert list(tmp_path.iterdir()) == [output]


def test_cli_success(input_csv: Path, tmp_path: Path, expected: str, capsys) -> None:
    """Успішний запуск: код 0, файл збігається з еталоном, вхід не змінено."""
    before = input_csv.read_bytes()
    output = tmp_path / "summary.txt"
    assert main([str(input_csv), str(output)]) == 0
    assert output.read_text(encoding="utf-8") == expected
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
