package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingSummaryTest {

    @Test
    void recordsWithSameValuesAreEqual() {
        TrainingSummary a = new TrainingSummary(2, 135, 40.5, 780, "Велосипед (2026-09-02)");
        TrainingSummary b = new TrainingSummary(2, 135, 40.5, 780, "Велосипед (2026-09-02)");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new TrainingSummary(3, 135, 40.5, 780, "Велосипед (2026-09-02)"));
    }

    @Test
    void negativeOrNonFiniteValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new TrainingSummary(-1, 0, 0.0, 0, ""));
        assertThrows(IllegalArgumentException.class, () -> new TrainingSummary(0, 0, Double.NaN, 0, ""));
        assertThrows(NullPointerException.class, () -> new TrainingSummary(0, 0, 0.0, 0, null));
    }

    @Test
    void emptyListGivesEmptySummary() {
        assertEquals(TrainingSummary.EMPTY, TrainingSummary.of(List.of()));
    }

    @Test
    void ofComputesIndicatorsAndKeepsFirstAmongEqualMaximums() {
        TrainingSummary summary = TrainingSummary.of(List.of(
                new Training("2026-09-01", "Біг", 45, 8.5, 780),
                new Training("2026-09-02", "Велосипед", 90, 32.0, 780),
                new Training("2026-09-04", "Плавання", 60, 2.4, 610)));
        assertEquals(new TrainingSummary(3, 195, 42.9, 780, "Біг (2026-09-01)"),
                new TrainingSummary(summary.count(), summary.totalMinutes(),
                        Math.round(summary.totalDistanceKm() * 1000) / 1000.0,
                        summary.maxCalories(), summary.maxCaloriesLabel()));
    }
}
