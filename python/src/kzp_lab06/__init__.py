"""ЛР6: обробка CSV спортивних тренувань засобами Python (варіант 2)."""

from .analysis import (
    MIN_DURATION,
    average_pulse,
    distance_by_kind,
    long_trainings,
    longest_training,
    summarize,
    unique_kinds,
)
from .errors import DataError
from .loader import REQUIRED_FIELDS, load_records, parse_records
from .reporting import format_report, write_report

__version__ = "6.0.0"

__all__ = [
    "MIN_DURATION",
    "REQUIRED_FIELDS",
    "DataError",
    "average_pulse",
    "distance_by_kind",
    "format_report",
    "load_records",
    "long_trainings",
    "longest_training",
    "parse_records",
    "summarize",
    "unique_kinds",
    "write_report",
]
