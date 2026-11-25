"""Тести моделі: властивості, dataclass, спеціальні методи, підтипи, протокол."""

import dataclasses
from datetime import date

import pytest

from kzp_lab07 import (
    Cycling,
    GeneralTraining,
    KindLoad,
    LoadSummary,
    Running,
    Training,
    load_by_kind,
    ordered_by_load,
)

DAY = date(2026, 9, 1)


def run(duration: int = 45, pulse: int = 152) -> Running:
    """Типовий біг для тестів."""
    return Running(DAY, "Біг", duration, 8.5, pulse)


@pytest.mark.parametrize("value", [60, 0])
def test_setter_accepts_correct_and_zero(value: int) -> None:
    """Коректне і нульове (граничне) значення приймаються."""
    training = run()
    training.duration = value
    assert training.duration == value


@pytest.mark.parametrize(
    ("field", "value", "message"),
    [
        ("duration", -1, "duration не може бути від'ємним"),
        ("duration", 4.5, "duration має бути цілим"),
        ("pulse", -1, "pulse не може бути від'ємним"),
        ("distance", float("inf"), "distance має бути скінченним"),
        ("distance", -0.1, "distance не може бути від'ємним"),
        ("kind", "   ", "kind порожнє"),
        ("day", "2026-09-01", "date має бути типу date"),
    ],
)
def test_setter_rejects_bad_value(field: str, value: object, message: str) -> None:
    """Setter відхиляє помилкове значення і не змінює стан."""
    training = run()
    before = repr(training)
    with pytest.raises(ValueError, match=message):
        setattr(training, field, value)
    assert repr(training) == before


def test_constructor_validates() -> None:
    """__init__ проходить через ті самі setter-и."""
    with pytest.raises(ValueError, match="pulse"):
        Running(DAY, "Біг", 45, 8.5, -5)


def test_kind_is_normalized() -> None:
    """Зайві пробіли у виді стискаються."""
    assert Running(DAY, "  Біг,   інтервали ", 1, 1.0, 1).kind == "Біг, інтервали"


def test_str_and_repr() -> None:
    """__str__ для читача, __repr__ з назвою підтипу."""
    training = run()
    assert str(training) == "Біг (2026-09-01), 45 хв"
    assert repr(training) == (
        "Running(day=datetime.date(2026, 9, 1), kind='Біг', duration=45, distance=8.5, pulse=152)"
    )


def test_equality_duplicates_and_types() -> None:
    """Дублікат рівний; інший підтип з тим самим станом — ні; чужий тип — NotImplemented."""
    assert run() == run()
    assert run() != run(duration=46)
    assert run() != GeneralTraining(DAY, "Біг", 45, 8.5, 152)
    assert run().__eq__("Біг") is NotImplemented
    with pytest.raises(TypeError):
        hash(run())


def test_subtypes_have_different_loads() -> None:
    """Однаковий стан — різні формули load()."""
    args = (DAY, "X", 60, 10.0, 150)
    assert Running(*args).load() == pytest.approx(90.0)
    assert Cycling(*args).load() == pytest.approx(50.0)
    assert GeneralTraining(*args).load() == pytest.approx(60.0)


def test_base_load_not_implemented() -> None:
    """Базовий клас лише оголошує операцію."""
    with pytest.raises(NotImplementedError):
        Training(DAY, "X", 1, 1.0, 1).load()


class Stub:
    """Не нащадок Training: лише kind і load() — достатньо для протоколу."""

    kind = "Тест"

    def load(self) -> float:
        """Фіксоване навантаження."""
        return 5.0


def test_protocol_duck_typing() -> None:
    """Спільна функція працює з обома підтипами та стороннім об'єктом."""
    items = [run(), Cycling(DAY, "Велосипед", 90, 32.0, 138), Stub()]
    summary = load_by_kind(items)
    assert summary.count == 3
    assert summary.total_load == pytest.approx(68.4 + 109.0 + 5.0)
    assert [item.kind for item in summary.by_kind] == ["Біг", "Велосипед", "Тест"]


def test_ordered_by_load_key() -> None:
    """Сортування через key: спадання, рівні — у вхідному порядку."""
    a, b, c = run(10, 100), run(50, 100), run(10, 100)
    assert [id(x) for x in ordered_by_load([a, b, c])] == [id(b), id(a), id(c)]


def test_summary_dataclass_equal_and_frozen() -> None:
    """dataclass: автоматична рівність і незмінність."""
    first = LoadSummary(1, 5.0, (KindLoad("Біг", 1, 5.0),))
    assert first == LoadSummary(1, 5.0, (KindLoad("Біг", 1, 5.0),))
    with pytest.raises(dataclasses.FrozenInstanceError):
        first.count = 3  # type: ignore[misc]


def test_empty_summary() -> None:
    """Порожній вхід — нульовий підсумок."""
    assert load_by_kind([]) == LoadSummary(0, 0, ())
