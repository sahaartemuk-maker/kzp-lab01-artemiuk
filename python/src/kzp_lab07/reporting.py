"""Формування детермінованого текстового звіту та безпечний запис у файл."""

import os
from pathlib import Path
from types import TracebackType
from typing import TextIO

from .analysis import (
    MIN_DURATION,
    average_pulse,
    distance_by_kind,
    load_by_kind,
    long_trainings,
    longest_training,
    unique_kinds,
)
from .models import Training


def format_report(records: list[Training], min_duration: int = MIN_DURATION) -> str:
    """Звіт ЛР6 без змін + блок навантаження за видом (ЛР7)."""
    selected = long_trainings(records, min_duration)
    kinds = sorted(unique_kinds(records))
    pulse = average_pulse(records)
    longest = longest_training(records)
    lines = [
        f"Записів: {len(records)}",
        f"Види тренувань ({len(kinds)}): {'; '.join(kinds) if kinds else 'немає'}",
        f"Тренувань від {min_duration} хв: {len(selected)}",
        f"Дистанція за видом (від {min_duration} хв):",
    ]
    totals = distance_by_kind(selected)
    lines += [f"  {kind}: {km:.2f} км" for kind, km in totals.items()] or ["  немає"]
    lines.append(f"Середній пульс: {pulse:.1f} уд/хв" if pulse is not None else "Середній пульс: —")
    if longest is None:
        lines.append("Найдовше тренування: —")
    else:
        lines.append(f"Найдовше тренування: {longest}")
    summary = load_by_kind(records)
    lines.append("Навантаження за видом (бал):")
    lines += [f"  {item.kind}: {item.load:.2f} ({item.count})" for item in summary.by_kind] or [
        "  немає"
    ]
    lines.append(f"Сумарне навантаження: {summary.total_load:.2f} бал")
    return "\n".join(lines) + "\n"


class ReportWriter:
    """Контекстний менеджер: пише у тимчасовий файл і замінює ціль лише при успіху."""

    def __init__(self, path: Path) -> None:
        """Запам'ятовує цільовий шлях."""
        self.path = path
        self._tmp = path.with_name(path.name + ".tmp")
        self._stream: TextIO | None = None

    def __enter__(self) -> TextIO:
        """Створює каталог і відкриває тимчасовий файл (UTF-8, "\\n")."""
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self._stream = self._tmp.open("w", encoding="utf-8", newline="\n")
        return self._stream

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc: BaseException | None,
        tb: TracebackType | None,
    ) -> bool:
        """Закриває файл завжди; при винятку видаляє тимчасовий файл і не глушить виняток."""
        assert self._stream is not None
        self._stream.close()
        if exc_type is None:
            os.replace(self._tmp, self.path)
        else:
            self._tmp.unlink(missing_ok=True)
        return False

    @property
    def closed(self) -> bool:
        """Чи закрито потік (для тестів)."""
        return self._stream is None or self._stream.closed


def write_report(path: Path, text: str) -> None:
    """Записує звіт через ReportWriter."""
    with ReportWriter(path) as target:
        target.write(text)
