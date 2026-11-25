package ua.lpnu.kzp;

import java.util.Locale;

/** Формує текст звіту, однаковий для консолі та файла. */
public final class ReportFormatter {

    /** Назва показника навантаження, доданого в ЛР № 3. */
    public static final String LOAD_LABEL = "Сумарне навантаження, бал";

    /* Забороняє створення екземплярів службового класу. */
    private ReportFormatter() {
    }

    /**
     * Будує звіт із показниками та повідомленнями про помилки.
     *
     * @param stats обчислена статистика
     * @return текст звіту з платформним роздільником рядків
     */
    public static String format(TrainingStats stats) {
        TrainingSummary summary = stats.summary();
        StringBuilder report = new StringBuilder();
        // Locale.ROOT гарантує крапку як десятковий роздільник на всіх ОС.
        report.append(String.format(Locale.ROOT, "Звіт: спортивні тренування%n"));
        report.append(String.format(Locale.ROOT, "%-30s %15s%n", "Показник", "Значення"));
        report.append(String.format(Locale.ROOT, "%-30s %15d%n", "Коректних записів", summary.count()));
        report.append(String.format(Locale.ROOT, "%-30s %15d%n", "Сумарний час, хв", summary.totalMinutes()));
        report.append(String.format(Locale.ROOT, "%-30s %15.2f%n", "Сумарна дистанція, км", summary.totalDistanceKm()));
        // Показник ЛР № 3: обчислюється поліморфним методом load() через спільний тип.
        report.append(String.format(Locale.ROOT, "%-30s %15.2f%n", LOAD_LABEL, stats.totalLoad()));
        if (summary.count() > 0) {
            report.append(String.format(Locale.ROOT, "%-30s %15d  %s%n", "Найбільша витрата калорій",
                    summary.maxCalories(), summary.maxCaloriesLabel()));
        } else {
            report.append(String.format(Locale.ROOT, "%-30s %15s%n", "Найбільша витрата калорій", "-"));
            report.append(String.format(Locale.ROOT, "Увага: у файлі немає жодного коректного запису%n"));
        }
        report.append(String.format(Locale.ROOT, "%nПропущено рядків: %d%n", stats.errors().size()));
        for (String error : stats.errors()) {
            report.append(String.format(Locale.ROOT, "%s%n", error));
        }
        return report.toString();
    }
}
