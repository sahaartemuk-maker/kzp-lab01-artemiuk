package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Тести п'яти потокових запитів і Optional-пошуку на контрольному наборі ЛР № 4. */
class TrainingQueriesTest {

    private List<Training> data;

    @BeforeEach
    void load() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/lr4-queries.csv")) {
            List<String> lines = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
            TrainingStats stats = TrainingStats.compute(lines);
            assertTrue(stats.errors().isEmpty());
            data = stats.trainings();
        }
    }

    private static List<String> labels(List<Training> trainings) {
        return trainings.stream().map(Training::label).toList();
    }

    @Test
    void filterKeepsBoundaryAndDropsBelow() {
        assertEquals(List.of("Біг (2026-09-01)", "Велосипед (2026-09-02)", "Плавання (2026-09-04)",
                "Йога (2026-09-02)", "Біг (2026-09-06)"), labels(TrainingQueries.longTrainings(data)));
    }

    @Test
    void mapReturnsDistancesInSourceOrder() {
        assertEquals(List.of(8.5, 32.0, 8.5, 2.4, 3.1, 0.0, 12.0), TrainingQueries.distances(data));
    }

    @Test
    void groupingSumsDistanceByKindInEnumOrder() {
        Map<TrainingKind, Double> byKind = TrainingQueries.distanceByKind(data);
        assertEquals(List.of(TrainingKind.RUNNING, TrainingKind.CYCLING, TrainingKind.WALKING,
                TrainingKind.SWIMMING, TrainingKind.YOGA), new ArrayList<>(byKind.keySet()));
        assertEquals(29.0, byKind.get(TrainingKind.RUNNING), 1e-9);
        assertEquals(32.0, byKind.get(TrainingKind.CYCLING), 1e-9);
        assertEquals(0.0, byKind.get(TrainingKind.YOGA), 1e-9);
        assertThrows(UnsupportedOperationException.class, () -> byKind.put(TrainingKind.OTHER, 1.0));
    }

    @Test
    void statisticsOfCalories() {
        IntSummaryStatistics stats = TrainingQueries.caloriesStatistics(data);
        assertEquals(7, stats.getCount());
        assertEquals(3460, stats.getSum());
        assertEquals(150, stats.getMin());
        assertEquals(780, stats.getMax());
        assertEquals(494.2857, stats.getAverage(), 1e-4);
    }

    @Test
    void topFiveUsesDateAsSecondCriterionAndTruncates() {
        assertEquals(List.of("Велосипед (2026-09-02)", "Біг (2026-09-06)", "Біг (2026-09-01)",
                "Біг (2026-09-03)", "Ходьба (2026-09-05)"),
                labels(TrainingQueries.topByDistance(data, TrainingQueries.TOP_N)));
    }

    @Test
    void topNBoundaries() {
        assertTrue(TrainingQueries.topByDistance(data, 0).isEmpty());
        assertEquals(7, TrainingQueries.topByDistance(data, 100).size());
        assertThrows(IllegalArgumentException.class, () -> TrainingQueries.topByDistance(data, -1));
    }

    @Test
    void findByDateReturnsFirstOfDuplicatesOrEmpty() {
        assertEquals("Велосипед (2026-09-02)",
                TrainingQueries.findByDate(data, LocalDate.of(2026, 9, 2)).orElseThrow().label());
        assertFalse(TrainingQueries.findByDate(data, LocalDate.of(2026, 9, 30)).isPresent());
    }

    @Test
    void emptyInput() {
        List<Training> empty = List.of();
        assertTrue(TrainingQueries.longTrainings(empty).isEmpty());
        assertTrue(TrainingQueries.distances(empty).isEmpty());
        assertTrue(TrainingQueries.distanceByKind(empty).isEmpty());
        IntSummaryStatistics stats = TrainingQueries.caloriesStatistics(empty);
        assertEquals(0, stats.getCount());
        assertEquals(0, stats.getSum());
        assertEquals(Integer.MAX_VALUE, stats.getMin());
        assertEquals(Integer.MIN_VALUE, stats.getMax());
        assertTrue(TrainingQueries.topByDistance(empty, 5).isEmpty());
        assertTrue(TrainingQueries.findByDate(empty, LocalDate.of(2026, 9, 1)).isEmpty());
        assertTrue(QueryReportFormatter.format(empty, LocalDate.of(2026, 9, 1)).contains("немає даних"));
    }

    @Test
    void queriesDoNotMutateSource() {
        List<Training> source = new ArrayList<>(data);
        List<Training> before = List.copyOf(source);
        TrainingQueries.longTrainings(source);
        TrainingQueries.distances(source);
        TrainingQueries.distanceByKind(source);
        TrainingQueries.caloriesStatistics(source);
        TrainingQueries.topByDistance(source, 5);
        TrainingQueries.findByDate(source, LocalDate.of(2026, 9, 2));
        assertEquals(before, source);
    }

    @Test
    void streamResultsMatchLoopImplementation() {
        List<Training> loopFilter = new ArrayList<>();
        List<Double> loopMap = new ArrayList<>();
        Map<TrainingKind, Double> loopGroup = new EnumMap<>(TrainingKind.class);
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Training found = null;
        for (Training t : data) {
            if (t.getMinutes() >= TrainingQueries.MIN_LONG_MINUTES) {
                loopFilter.add(t);
            }
            loopMap.add(t.getDistanceKm());
            loopGroup.merge(t.getType(), t.getDistanceKm(), Double::sum);
            sum += t.getCalories();
            min = Math.min(min, t.getCalories());
            max = Math.max(max, t.getCalories());
            if (found == null && t.getDate().equals("2026-09-02")) {
                found = t;
            }
        }
        List<Training> loopTop = new ArrayList<>(data);
        loopTop.sort(TrainingQueries.BY_DISTANCE_DESC_THEN_DATE);
        loopTop = loopTop.subList(0, 5);

        IntSummaryStatistics stats = TrainingQueries.caloriesStatistics(data);
        assertEquals(loopFilter, TrainingQueries.longTrainings(data));
        assertEquals(loopMap, TrainingQueries.distances(data));
        assertEquals(loopGroup, TrainingQueries.distanceByKind(data));
        assertEquals(sum, stats.getSum());
        assertEquals(min, stats.getMin());
        assertEquals(max, stats.getMax());
        assertEquals(loopTop, TrainingQueries.topByDistance(data, 5));
        assertEquals(found, TrainingQueries.findByDate(data, LocalDate.of(2026, 9, 2)).orElseThrow());
    }
}
