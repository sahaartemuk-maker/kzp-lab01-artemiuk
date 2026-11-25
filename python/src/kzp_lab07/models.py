"""Предметна модель: тренування, підтипи, спільний протокол і незмінні підсумки."""

import math
from dataclasses import dataclass
from datetime import date
from typing import Protocol


class Loadable(Protocol):
    """Протокол об'єктів із видом і числовим навантаженням (duck typing)."""

    @property
    def kind(self) -> str:
        """Назва виду тренування."""
        ...

    def load(self) -> float:
        """Повертає навантаження в балах."""
        ...


class Training:
    """Базове тренування з перевіреним станом; формула load() належить підтипам."""

    def __init__(self, day: date, kind: str, duration: int, distance: float, pulse: int) -> None:
        """Створює тренування; кожне поле проходить перевірку у властивості."""
        self.day = day
        self.kind = kind
        self.duration = duration
        self.distance = distance
        self.pulse = pulse

    @property
    def day(self) -> date:
        """Дата тренування."""
        return self._day

    @day.setter
    def day(self, value: date) -> None:
        """Приймає лише datetime.date."""
        if not isinstance(value, date):
            raise ValueError("поле date має бути типу date")
        self._day = value

    @property
    def kind(self) -> str:
        """Вид тренування (пробіли нормалізовано)."""
        return self._kind

    @kind.setter
    def kind(self, value: str) -> None:
        """Непорожній вид; зайві пробіли стискаються."""
        normalized = " ".join(str(value).split())
        if not normalized:
            raise ValueError("поле kind порожнє")
        self._kind = normalized

    @property
    def duration(self) -> int:
        """Тривалість, хв."""
        return self._duration

    @duration.setter
    def duration(self, value: int) -> None:
        """Ціле число >= 0 (нуль — допустима межа)."""
        if isinstance(value, bool) or not isinstance(value, int):
            raise ValueError("поле duration має бути цілим числом")
        if value < 0:
            raise ValueError("поле duration не може бути від'ємним")
        self._duration = value

    @property
    def distance(self) -> float:
        """Дистанція, км."""
        return self._distance

    @distance.setter
    def distance(self, value: float) -> None:
        """Скінченне число >= 0."""
        try:
            number = float(value)
        except (TypeError, ValueError) as error:
            raise ValueError("поле distance має бути числом") from error
        if not math.isfinite(number):
            raise ValueError("поле distance має бути скінченним числом")
        if number < 0:
            raise ValueError("поле distance не може бути від'ємним")
        self._distance = number

    @property
    def pulse(self) -> int:
        """Середній пульс, уд/хв."""
        return self._pulse

    @pulse.setter
    def pulse(self, value: int) -> None:
        """Ціле число >= 0."""
        if isinstance(value, bool) or not isinstance(value, int):
            raise ValueError("поле pulse має бути цілим числом")
        if value < 0:
            raise ValueError("поле pulse не може бути від'ємним")
        self._pulse = value

    def load(self) -> float:
        """Спільна операція; конкретна формула визначається підтипом."""
        raise NotImplementedError

    def _key(self) -> tuple[object, ...]:
        """Повний стан для порівняння."""
        return (self.day, self.kind, self.duration, self.distance, self.pulse)

    def __str__(self) -> str:
        """Коротке подання для читача."""
        return f"{self.kind} ({self.day.isoformat()}), {self.duration} хв"

    def __repr__(self) -> str:
        """Діагностичне подання з назвою підтипу."""
        return (
            f"{type(self).__name__}(day={self.day!r}, kind={self.kind!r}, "
            f"duration={self.duration!r}, distance={self.distance!r}, pulse={self.pulse!r})"
        )

    def __eq__(self, other: object) -> bool:
        """Рівні лише об'єкти одного підтипу з однаковим станом."""
        if not isinstance(other, Training):
            return NotImplemented
        return type(self) is type(other) and self._key() == other._key()

    # Об'єкт змінюваний, тому не хешується (не може бути ключем словника).
    __hash__ = None  # type: ignore[assignment]


class Running(Training):
    """Біг: навантаження за пульсом."""

    def load(self) -> float:
        """duration × pulse / 100."""
        return self.duration * self.pulse / 100


class Cycling(Training):
    """Велосипед: навантаження за дистанцією."""

    def load(self) -> float:
        """distance × 2 + duration × 0.5."""
        return self.distance * 2 + self.duration * 0.5


class GeneralTraining(Training):
    """Інші види (плавання, ходьба, йога): м'якша формула за пульсом."""

    def load(self) -> float:
        """duration × pulse / 150."""
        return self.duration * self.pulse / 150


@dataclass(frozen=True)
class KindLoad:
    """Незмінне навантаження одного виду."""

    kind: str
    count: int
    load: float


@dataclass(frozen=True)
class LoadSummary:
    """Незмінний підсумок «навантаження за видом»."""

    count: int
    total_load: float
    by_kind: tuple[KindLoad, ...]
