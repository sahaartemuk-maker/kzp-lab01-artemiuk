package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Незмінні підсумкові показники варіанта 2.
 *
 * @param count кількість коректних записів
 * @param totalMinutes сумарний час у хвилинах
 * @param totalDistanceKm сумарна дистанція в кілометрах
 * @param maxCalories найбільша витрата калорій або 0, якщо записів немає
 * @param maxCaloriesLabel «вид (дата)» тренування з найбільшою витратою або порожній рядок
 */
public record TrainingSummary(int count,
                              long totalMinutes,
                              double totalDistanceKm,
                              int maxCalories,
                              String maxCaloriesLabel) {

    /** Порожні підсумки для набору без коректних записів. */
    public static final TrainingSummary EMPTY = new TrainingSummary(0, 0, 0.0, 0, "");

    /** Перевіряє допустимість підсумкових показників. */
    public TrainingSummary {
        Objects.requireNonNull(maxCaloriesLabel, "мітка не може бути null");
        if (count < 0 || totalMinutes < 0 || maxCalories < 0
                || totalDistanceKm < 0 || !Double.isFinite(totalDistanceKm)) {
            throw new IllegalArgumentException("Показники не можуть бути від'ємними");
        }
    }

    /**
     * Обчислює підсумки за списком коректних тренувань потоковими запитами (ЛР № 4).
     *
     * @param trainings тренування у порядку файла
     * @return підсумкові показники
     */
    public static TrainingSummary of(List<Training> trainings) {
        if (trainings.isEmpty()) {
            return EMPTY;
        }
        long minutes = trainings.stream().mapToLong(Training::getMinutes).sum();
        double distance = trainings.stream().mapToDouble(Training::getDistanceKm).sum();
        // BinaryOperator.maxBy залишає перший елемент серед рівних, як строге «>» у ЛР № 1.
        Training top = trainings.stream()
                .max(Comparator.comparingInt(Training::getCalories))
                .orElseThrow();
        return new TrainingSummary(trainings.size(), minutes, distance, top.getCalories(), top.label());
    }
}
