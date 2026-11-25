"""Тести функцій аналізу."""

import pytest

from kzp_lab06 import (
    average_pulse,
    distance_by_kind,
    long_trainings,
    longest_training,
    summarize,
    unique_kinds,
)


def rec(kind: str, duration: int, distance: float, pulse: int, day: str = "2026-09-01") -> dict:
    """Створює запис для тесту."""
    return {"date": day, "kind": kind, "duration": duration, "distance": distance, "pulse": pulse}


RECORDS = [
    rec("Біг", 45, 8.5, 150, "2026-09-01"),
    rec("Біг", 44, 5.0, 160, "2026-09-02"),
    rec("Велосипед", 90, 30.0, 130, "2026-09-03"),
    rec("Велосипед", 90, 20.0, 140, "2026-09-04"),
]


def test_filter_boundary() -> None:
    """45 хв входить у відбір, 44 — ні."""
    assert [r["duration"] for r in long_trainings(RECORDS)] == [45, 90, 90]


def test_unique_kinds_with_duplicates() -> None:
    """Дублікати видів дають одне значення множини."""
    assert unique_kinds(RECORDS) == {"Біг", "Велосипед"}


def test_distance_by_kind_sorted() -> None:
    """Сума дистанцій за видом, ключі відсортовано."""
    totals = distance_by_kind(RECORDS)
    assert totals == {"Біг": 13.5, "Велосипед": 50.0}
    assert list(totals) == ["Біг", "Велосипед"]


def test_average_pulse() -> None:
    """Середній пульс за всіма записами."""
    assert average_pulse(RECORDS) == pytest.approx(145.0)


def test_longest_first_of_equal() -> None:
    """Серед рівних за тривалістю обирається перше."""
    assert longest_training(RECORDS)["date"] == "2026-09-03"


def test_empty_input() -> None:
    """Порожній вхід: порожні колекції і None замість метрик."""
    assert long_trainings([]) == []
    assert distance_by_kind([]) == {}
    assert average_pulse([]) is None
    assert longest_training([]) is None


def test_summarize_args_kwargs() -> None:
    """*metrics і **labels; типовий min_duration=45."""
    result = summarize(RECORDS, "distance", "duration", distance="Дистанція")
    assert result == {"count": 3, "Дистанція": 58.5, "duration": 225.0}


def test_summarize_custom_threshold_and_empty() -> None:
    """Після відбору без записів метрики дорівнюють 0.0."""
    assert summarize(RECORDS, "pulse", min_duration=120) == {"count": 0, "pulse": 0.0}


def test_summarize_unknown_metric() -> None:
    """Невідома метрика — явна ValueError."""
    with pytest.raises(ValueError, match="Невідома метрика"):
        summarize(RECORDS, "speed")
