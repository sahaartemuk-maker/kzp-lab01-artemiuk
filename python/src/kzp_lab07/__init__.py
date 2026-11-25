"""ЛР7: об'єктно-орієнтована обробка CSV спортивних тренувань (варіант 2)."""

from .analysis import (
    MIN_DURATION,
    average_pulse,
    distance_by_kind,
    load_by_kind,
    long_trainings,
    longest_training,
    ordered_by_load,
    summarize,
    unique_kinds,
)
from .errors import DataError
from .loader import REQUIRED_FIELDS, build_training, load_records, parse_records, training_class
from .models import Cycling, GeneralTraining, KindLoad, Loadable, LoadSummary, Running, Training
from .reporting import ReportWriter, format_report, write_report

__version__ = "7.0.0"

__all__ = [
    "MIN_DURATION",
    "REQUIRED_FIELDS",
    "Cycling",
    "DataError",
    "GeneralTraining",
    "KindLoad",
    "LoadSummary",
    "Loadable",
    "ReportWriter",
    "Running",
    "Training",
    "average_pulse",
    "build_training",
    "distance_by_kind",
    "format_report",
    "load_by_kind",
    "load_records",
    "long_trainings",
    "longest_training",
    "ordered_by_load",
    "parse_records",
    "summarize",
    "training_class",
    "unique_kinds",
    "write_report",
]
