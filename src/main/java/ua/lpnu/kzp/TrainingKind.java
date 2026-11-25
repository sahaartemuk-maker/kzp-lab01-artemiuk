package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Фіксований набір видів тренувань варіанта 2.
 *
 * <p>Кожна константа знає свій текстовий підпис у CSV, чи проходить тренування
 * у приміщенні, та коефіцієнт інтенсивності для розрахунку навантаження.
 * Невідомий текст виду не є помилкою (як і в ЛР № 1–2) і отримує {@link #OTHER}.
 */
public enum TrainingKind {
    /** Біг (на вулиці). */
    RUNNING("Біг", false, 1.5),
    /** Велосипед (на вулиці). */
    CYCLING("Велосипед", false, 1.2),
    /** Ходьба (на вулиці). */
    WALKING("Ходьба", false, 0.8),
    /** Плавання (у басейні). */
    SWIMMING("Плавання", true, 1.4),
    /** Йога (у приміщенні). */
    YOGA("Йога", true, 0.6),
    /** Будь-який інший вид; вважається тренуванням на вулиці. */
    OTHER("Інше", false, 1.0);

    private final String label;
    private final boolean indoor;
    private final double intensity;

    /* Конструктор enum викликається JVM для створення фіксованих констант. */
    TrainingKind(String label, boolean indoor, double intensity) {
        this.label = label;
        this.indoor = indoor;
        this.intensity = intensity;
    }

    /**
     * Визначає вид за текстом із CSV (без урахування регістру й пробілів по краях).
     *
     * @param text текст виду тренування
     * @return відповідна константа або {@link #OTHER}
     */
    public static TrainingKind fromLabel(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        for (TrainingKind kind : values()) {
            if (kind != OTHER && kind.label.toLowerCase(Locale.ROOT).equals(normalized)) {
                return kind;
            }
        }
        return OTHER;
    }

    /** @return текстовий підпис виду */
    public String label() {
        return label;
    }

    /** @return {@code true}, якщо тренування проходить у приміщенні */
    public boolean isIndoor() {
        return indoor;
    }

    /** @return коефіцієнт інтенсивності, бал за хвилину */
    public double intensity() {
        return intensity;
    }
}
