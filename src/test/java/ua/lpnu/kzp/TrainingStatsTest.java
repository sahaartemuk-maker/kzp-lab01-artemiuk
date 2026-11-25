package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingStatsTest {

    @Test
    void computesAllIndicatorsAndSkipsInvalidLines() {
        TrainingStats stats = TrainingStats.compute(List.of(
                "2026-09-01;Біг;45;8.5;520",
                "2026-09-02;Велосипед;90;32.0;780",
                "",
                "2026-09-06;Біг;сорок;7.0;480",
                "2026-09-04;Плавання;60;2.4;610"));
        assertEquals(3, stats.validCount());
        assertEquals(195, stats.totalMinutes());
        assertEquals(42.9, stats.totalDistanceKm(), 0.0001);
        assertEquals(780, stats.maxCalories());
        assertEquals("Велосипед (2026-09-02)", stats.maxCaloriesKind());
        assertEquals(List.of("Рядок 3: порожній рядок", "Рядок 4: числове поле має помилковий формат"),
                stats.errors());
    }

    @Test
    void fileWithoutValidRecordsGivesZeroes() {
        TrainingStats stats = TrainingStats.compute(List.of("", "a;b;c"));
        assertEquals(0, stats.validCount());
        assertEquals(0.0, stats.totalDistanceKm(), 0.0001);
        assertEquals(2, stats.errors().size());
        assertTrue(ReportFormatter.format(stats).contains("немає жодного коректного запису"));
    }

    @Test
    void reportUsesDotAsDecimalSeparator() {
        TrainingStats stats = TrainingStats.compute(List.of("2026-09-01;Біг;45;8.5;520"));
        assertTrue(ReportFormatter.format(stats).contains("8.50"));
    }

    @Test
    void validLinesBecomeTrainingObjects() {
        TrainingStats stats = TrainingStats.compute(List.of("2026-09-01;Біг;45;8.5;520", "bad"));
        assertEquals(List.of(Training.of("2026-09-01", "Біг", 45, 8.5, 520)), stats.trainings());
        assertEquals(new TrainingSummary(1, 45, 8.5, 520, "Біг (2026-09-01)"), stats.summary());
    }
}
