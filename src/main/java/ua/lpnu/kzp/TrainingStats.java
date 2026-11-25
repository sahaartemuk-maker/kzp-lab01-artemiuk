package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Результат обробки файла: список коректних тренувань, підсумки та помилки. */
public final class TrainingStats {

    private final List<Training> trainings;
    private final TrainingSummary summary;
    private final List<String> errors;

    private TrainingStats(List<Training> trainings, List<String> errors) {
        this.trainings = List.copyOf(trainings);
        this.errors = List.copyOf(errors);
        this.summary = TrainingSummary.of(this.trainings);
    }

    /**
     * Перетворює рядки на об'єкти {@link Training}; хибні рядки потрапляють у список помилок.
     *
     * @param lines рядки вхідного файла
     * @return результат обробки
     */
    public static TrainingStats compute(List<String> lines) {
        List<Training> trainings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            try {
                // Запис потрапляє до списку лише після повної перевірки.
                trainings.add(Training.fromCsv(line));
            } catch (IllegalArgumentException exception) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: %s", lineNumber, exception.getMessage()));
            }
        }
        return new TrainingStats(trainings, errors);
    }

    /** @return незмінний список коректних тренувань */
    public List<Training> trainings() {
        return trainings;
    }

    /** @return підсумкові показники */
    public TrainingSummary summary() {
        return summary;
    }

    /** @return кількість коректних записів */
    public int validCount() {
        return summary.count();
    }

    /** @return сумарний час у хвилинах */
    public long totalMinutes() {
        return summary.totalMinutes();
    }

    /** @return сумарна дистанція в кілометрах */
    public double totalDistanceKm() {
        return summary.totalDistanceKm();
    }

    /** @return найбільша витрата калорій або 0, якщо записів немає */
    public int maxCalories() {
        return summary.maxCalories();
    }

    /** @return вид і дата тренування з найбільшою витратою калорій */
    public String maxCaloriesKind() {
        return summary.maxCaloriesLabel();
    }

    /** @return незмінний список повідомлень про пропущені рядки */
    public List<String> errors() {
        return errors;
    }
}
