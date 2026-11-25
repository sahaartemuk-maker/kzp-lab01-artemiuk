package ua.lpnu.kzp;

/**
 * Сумісний з ЛР № 1 фасад перевірки рядка. Уся логіка розбору тепер живе в
 * {@link Training#fromCsv(String)}; клас збережено, щоб попередні тести й виклики
 * працювали без змін.
 */
public final class TrainingParser {

    /** Кількість полів у коректному записі. */
    public static final int FIELD_COUNT = Training.FIELD_COUNT;

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
        try {
            Training.fromCsv(line);
            return null;
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
    }

    /**
     * Розділяє рядок на поля без пробілів по краях (допоміжний метод ЛР № 1).
     *
     * @param line рядок
     * @return масив полів
     */
    public static String[] fields(String line) {
        String[] fields = line.split(";", -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }
}
