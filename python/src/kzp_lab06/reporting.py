"""Формування детермінованого текстового звіту та запис у файл."""

from pathlib import Path

from .analysis import (
    MIN_DURATION,
    average_pulse,
    distance_by_kind,
    long_trainings,
    longest_training,
    unique_kinds,
)
from .loader import Record


def format_report(records: list[Record], min_duration: int = MIN_DURATION) -> str:
    """Повертає текст звіту зі стабільним порядком рядків і форматом чисел."""
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
        lines.append(
            f"Найдовше тренування: {longest['kind']} ({longest['date']}), {longest['duration']} хв"
        )
    return "\n".join(lines) + "\n"


def write_report(path: Path, text: str) -> None:
    """Записує звіт у UTF-8 з "\\n"; каталог створюється за потреби."""
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as target:
        target.write(text)
