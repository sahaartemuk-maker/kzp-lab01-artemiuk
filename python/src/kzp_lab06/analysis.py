"""Обчислення над записами тренувань (без роботи з файлами)."""

from .loader import Record

MIN_DURATION = 45


def long_trainings(records: list[Record], min_duration: int = MIN_DURATION) -> list[Record]:
    """Відбір: тренування з duration >= min_duration (порядок вхідних даних)."""
    return [record for record in records if int(record["duration"]) >= min_duration]


def unique_kinds(records: list[Record]) -> set[str]:
    """Множина унікальних видів тренувань."""
    return {str(record["kind"]) for record in records}


def distance_by_kind(records: list[Record]) -> dict[str, float]:
    """Словник: сумарна дистанція за видом, ключі відсортовано."""
    return {
        kind: sum(float(r["distance"]) for r in records if r["kind"] == kind)
        for kind in sorted(unique_kinds(records))
    }


def average_pulse(records: list[Record]) -> float | None:
    """Середній пульс; None для порожнього набору."""
    if not records:
        return None
    return sum(int(record["pulse"]) for record in records) / len(records)


def longest_training(records: list[Record]) -> Record | None:
    """Найдовше тренування; серед рівних — перше у вхідних даних."""
    best: Record | None = None
    for record in records:
        if best is None or int(record["duration"]) > int(best["duration"]):
            best = record
    return best


def summarize(
    records: list[Record],
    *metrics: str,
    min_duration: int = MIN_DURATION,
    **labels: str,
) -> dict[str, object]:
    """Сумує вибрані числові метрики для тренувань від min_duration хв.

    metrics — назви полів (кортеж), labels — підписи ключів результату (словник).
    Невідома метрика -> ValueError; порожній після відбору набір дає 0.0.
    """
    selected = long_trainings(records, min_duration)
    result: dict[str, object] = {"count": len(selected)}
    for metric in metrics:
        if metric not in ("duration", "distance", "pulse"):
            raise ValueError(f"Невідома метрика: {metric}")
        result[labels.get(metric, metric)] = sum(float(r[metric]) for r in selected)
    return result
