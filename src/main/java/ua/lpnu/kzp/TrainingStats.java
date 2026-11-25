package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Накопичує показники варіанта 2 за коректними записами та список помилок. */
public final class TrainingStats {

    private int validCount;
    private long totalMinutes;
    private double totalDistanceKm;
    private int maxCalories;
    private String maxCaloriesKind = "";
    private final List<String> errors = new ArrayList<>();

    /**
     * Обробляє всі рядки: коректні включає в показники, хибні — у список помилок.
     *
     * @param lines рядки вхідного файла
     * @return накопичена статистика
     */
    public static TrainingStats compute(List<String> lines) {
        TrainingStats stats = new TrainingStats();
        for (int index = 0; index < lines.size(); index++) {
            stats.accept(index + 1, lines.get(index));
        }
        return stats;
    }

    /**
     * Обробляє один рядок.
     *
     * @param lineNumber номер рядка у файлі, починаючи з 1
     * @param line вміст рядка
     */
    public void accept(int lineNumber, String line) {
        String error = TrainingParser.validate(line);
        if (error != null) {
            errors.add(String.format("Рядок %d: %s", lineNumber, error));
            return;
        }
        String[] fields = TrainingParser.fields(line);
        int minutes = Integer.parseInt(fields[2]);
        double distance = Double.parseDouble(fields[3]);
        int calories = Integer.parseInt(fields[4]);
        validCount++;
        totalMinutes += minutes;
        totalDistanceKm += distance;
        // Строге «більше» залишає перше тренування серед рівних.
        if (validCount == 1 || calories > maxCalories) {
            maxCalories = calories;
            maxCaloriesKind = fields[1] + " (" + fields[0] + ")";
        }
    }

    /** @return кількість коректних записів */
    public int validCount() {
        return validCount;
    }

    /** @return сумарний час у хвилинах */
    public long totalMinutes() {
        return totalMinutes;
    }

    /** @return сумарна дистанція в кілометрах */
    public double totalDistanceKm() {
        return totalDistanceKm;
    }

    /** @return найбільша витрата калорій або 0, якщо записів немає */
    public int maxCalories() {
        return maxCalories;
    }

    /** @return вид і дата тренування з найбільшою витратою калорій */
    public String maxCaloriesKind() {
        return maxCaloriesKind;
    }

    /** @return незмінний список повідомлень про пропущені рядки */
    public List<String> errors() {
        return Collections.unmodifiableList(errors);
    }
}
