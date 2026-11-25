package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Незмінний запис про одне спортивне тренування (варіант 2).
 *
 * <p>Інваріанти: дата й вид — непорожній текст; хвилини, дистанція та калорії
 * не від'ємні; дистанція — скінченне число. Нуль дозволено, як і в ЛР № 1.
 */
public final class Training {

    /** Кількість полів у CSV-рядку {@code date;kind;minutes;distanceKm;calories}. */
    public static final int FIELD_COUNT = 5;

    private final String date;
    private final String kind;
    private final int minutes;
    private final double distanceKm;
    private final int calories;

    /**
     * Створює коректний запис після перевірки інваріантів.
     *
     * @param date дата тренування
     * @param kind вид тренування
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @throws NullPointerException якщо дата або вид дорівнюють {@code null}
     * @throws IllegalArgumentException якщо порушено інваріант
     */
    public Training(String date, String kind, int minutes, double distanceKm, int calories) {
        // Перевірки виконуються до присвоєння, тому некоректний об'єкт не створюється.
        requireText(Objects.requireNonNull(date, "дата не може бути null"), "порожня дата");
        requireText(Objects.requireNonNull(kind, "вид не може бути null"), "порожній вид тренування");
        if (minutes < 0 || distanceKm < 0 || calories < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        if (!Double.isFinite(distanceKm)) {
            throw new IllegalArgumentException("некоректна дистанція");
        }
        this.date = date.trim();
        this.kind = kind.trim();
        this.minutes = minutes;
        this.distanceKm = distanceKm;
        this.calories = calories;
    }

    /**
     * Створює запис із CSV-рядка. Фабрика перевіряє структуру й типи полів,
     * конструктор — інваріанти.
     *
     * @param line рядок у форматі {@code date;kind;minutes;distanceKm;calories}
     * @return створений запис
     * @throws IllegalArgumentException якщо рядок порожній, має іншу кількість полів
     *         або числове поле має помилковий формат
     */
    public static Training fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // -1 зберігає порожні поля в кінці рядка.
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "очікується %d полів, отримано %d", FIELD_COUNT, fields.length));
        }
        // Текстові поля перевіряються раніше за числа, щоб причини збігалися з ЛР № 1.
        requireText(fields[0], "порожня дата");
        requireText(fields[1], "порожній вид тренування");
        try {
            int minutes = Integer.parseInt(fields[2].trim());
            double distanceKm = Double.parseDouble(fields[3].trim());
            int calories = Integer.parseInt(fields[4].trim());
            return new Training(fields[0], fields[1], minutes, distanceKm, calories);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("числове поле має помилковий формат", exception);
        }
    }

    /* Відхиляє порожній або пробільний текст із заданою причиною. */
    private static void requireText(String value, String message) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /** @return дата тренування */
    public String getDate() {
        return date;
    }

    /** @return вид тренування */
    public String getKind() {
        return kind;
    }

    /** @return тривалість у хвилинах */
    public int getMinutes() {
        return minutes;
    }

    /** @return дистанція в кілометрах */
    public double getDistanceKm() {
        return distanceKm;
    }

    /** @return витрата калорій */
    public int getCalories() {
        return calories;
    }

    /** @return коротка мітка для звіту у форматі «вид (дата)» */
    public String label() {
        return String.format(Locale.ROOT, "%s (%s)", kind, date);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Training that)) {
            return false;
        }
        return minutes == that.minutes
                && Double.compare(distanceKm, that.distanceKm) == 0
                && calories == that.calories
                && date.equals(that.date)
                && kind.equals(that.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, kind, minutes, distanceKm, calories);
    }

    /** Повертає текстове подання запису (не CSV). */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s %s: %d хв, %.2f км, %d ккал",
                date, kind, minutes, distanceKm, calories);
    }
}
