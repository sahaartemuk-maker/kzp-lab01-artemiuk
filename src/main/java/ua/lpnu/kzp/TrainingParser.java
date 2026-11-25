package ua.lpnu.kzp;

/**
 * Розбирає та перевіряє один рядок формату
 * {@code date;kind;minutes;distanceKm;calories}.
 */
public final class TrainingParser {

    /** Кількість полів у коректному записі. */
    public static final int FIELD_COUNT = 5;

    /* Забороняє створення екземплярів службового класу. */
    private TrainingParser() {
    }

    /**
     * Перевіряє рядок і повертає причину помилки.
     *
     * @param line рядок вхідного файла
     * @return опис помилки або {@code null}, якщо рядок коректний
     */
    public static String validate(String line) {
        if (line == null || line.isBlank()) {
            return "порожній рядок";
        }
        // -1 зберігає порожні поля в кінці рядка.
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            return String.format("очікується %d полів, отримано %d", FIELD_COUNT, fields.length);
        }
        if (fields[0].isBlank()) {
            return "порожня дата";
        }
        if (fields[1].isBlank()) {
            return "порожній вид тренування";
        }
        try {
            int minutes = Integer.parseInt(fields[2].trim());
            double distance = Double.parseDouble(fields[3].trim());
            int calories = Integer.parseInt(fields[4].trim());
            if (minutes < 0 || distance < 0 || calories < 0) {
                return "від'ємне числове значення";
            }
            if (Double.isNaN(distance) || Double.isInfinite(distance)) {
                return "некоректна дистанція";
            }
        } catch (NumberFormatException exception) {
            return "числове поле має помилковий формат";
        }
        return null;
    }

    /**
     * Розділяє коректний рядок на поля. Викликати лише після {@link #validate(String)}.
     *
     * @param line коректний рядок
     * @return масив із п'яти полів без пробілів по краях
     */
    public static String[] fields(String line) {
        String[] fields = line.split(";", -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }
}
