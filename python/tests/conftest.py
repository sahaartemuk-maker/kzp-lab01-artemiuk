"""Спільні фікстури тестів."""

from pathlib import Path

import pytest

DATA_DIR = Path(__file__).resolve().parents[1] / "data"


@pytest.fixture
def input_csv() -> Path:
    """Шлях до контрольного CSV варіанта."""
    return DATA_DIR / "input.csv"
