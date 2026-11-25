package ua.lpnu.kzp;

import java.util.Locale;

/** Формує текст звіту, однаковий для консолі та файла. */
public final class ReportFormatter {

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
        StringBuilder report = new StringBuilder();
        // Locale.ROOT гарантує крапку як десятковий роздільник на всіх ОС.
        report.append(String.format(Locale.ROOT, "Звіт: спортивні тренування%n"));
        report.append(String.format(Locale.ROOT, "%-30s %15s%n", "Показник", "Значення"));
        report.append(String.format(Locale.ROOT, "%-30s %15d%n", "Коректних записів", stats.validCount()));
        report.append(String.format(Locale.ROOT, "%-30s %15d%n", "Сумарний час, хв", stats.totalMinutes()));
        report.append(String.format(Locale.ROOT, "%-30s %15.2f%n", "Сумарна дистанція, км", stats.totalDistanceKm()));
        if (stats.validCount() > 0) {
            report.append(String.format(Locale.ROOT, "%-30s %15d  %s%n", "Найбільша витрата калорій",
                    stats.maxCalories(), stats.maxCaloriesKind()));
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
