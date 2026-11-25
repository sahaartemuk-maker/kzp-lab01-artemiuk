"""Обчислення над об'єктами; жодних перевірок конкретного підтипу."""

from collections.abc import Iterable, Sequence

from .models import KindLoad, Loadable, LoadSummary, Training

MIN_DURATION = 45
METRICS = ("duration", "distance", "pulse")


def long_trainings(records: Iterable[Training], min_duration: int = MIN_DURATION) -> list[Training]:
    """Відбір: тренування з duration >= min_duration (порядок вхідних даних)."""
    return [record for record in records if record.duration >= min_duration]


def unique_kinds(records: Iterable[Loadable]) -> set[str]:
    """Множина унікальних видів тренувань."""
    return {record.kind for record in records}


def distance_by_kind(records: Sequence[Training]) -> dict[str, float]:
    """Сумарна дистанція за видом, ключі відсортовано."""
    return {
        kind: sum(r.distance for r in records if r.kind == kind)
        for kind in sorted(unique_kinds(records))
    }


def average_pulse(records: Sequence[Training]) -> float | None:
    """Середній пульс; None для порожнього набору."""
    if not records:
        return None
    return sum(record.pulse for record in records) / len(records)


def longest_training(records: Iterable[Training]) -> Training | None:
    """Найдовше тренування; серед рівних — перше (max стабільний)."""
    return max(records, key=lambda record: record.duration, default=None)


def ordered_by_load(records: Iterable[Loadable]) -> list[Loadable]:
    """Сортування через key: за спаданням навантаження, рівні — у вхідному порядку."""
    return sorted(records, key=lambda record: record.load(), reverse=True)


def load_by_kind(records: Sequence[Loadable]) -> LoadSummary:
    """Підсумок навантаження за видом через спільний метод load()."""
    items = tuple(
        KindLoad(
            kind=kind,
            count=sum(1 for r in records if r.kind == kind),
            load=sum(r.load() for r in records if r.kind == kind),
        )
        for kind in sorted(unique_kinds(records))
    )
    return LoadSummary(
        count=len(records), total_load=sum(item.load for item in items), by_kind=items
    )


def summarize(
    records: Sequence[Training],
    *metrics: str,
    min_duration: int = MIN_DURATION,
    **labels: str,
) -> dict[str, object]:
    """Сумує вибрані метрики тренувань від min_duration хв (поведінка ЛР6)."""
    selected = long_trainings(records, min_duration)
    result: dict[str, object] = {"count": len(selected)}
    for metric in metrics:
        if metric not in METRICS:
            raise ValueError(f"Невідома метрика: {metric}")
        result[labels.get(metric, metric)] = float(sum(getattr(r, metric) for r in selected))
    return result
