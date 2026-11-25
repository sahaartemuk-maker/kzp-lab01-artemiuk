package ua.lpnu.kzp;

import java.time.LocalDate;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Текстове подання результатів потокових запитів {@link TrainingQueries} (опція --queries). */
public final class QueryReportFormatter {

    /* Забороняє створення екземплярів службового класу. */
    private QueryReportFormatter() {
    }

    /**
     * Формує звіт запитів. Числа форматуються з {@link Locale#ROOT}.
     *
     * @param trainings коректні тренування у порядку файла
     * @param searchDate дата для Optional-пошуку
     * @return багаторядковий текст
     */
    public static String format(List<Training> trainings, LocalDate searchDate) {
        StringBuilder text = new StringBuilder();
        line(text, "Запити Stream API (ЛР № 4)");
        line(text, "1. Тривалість >= %d хв: %s", TrainingQueries.MIN_LONG_MINUTES,
                labels(TrainingQueries.longTrainings(trainings)));
        line(text, "2. Дистанції, км: %s", TrainingQueries.distances(trainings).stream()
                .map(km -> String.format(Locale.ROOT, "%.2f", km))
                .collect(Collectors.joining(", ", "[", "]")));
        line(text, "3. Дистанція за видом, км: %s", TrainingQueries.distanceByKind(trainings)
                .entrySet().stream()
                .map(entry -> String.format(Locale.ROOT, "%s=%.2f",
                        entry.getKey().label(), entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}")));
        line(text, "4. Калорії: %s", statistics(TrainingQueries.caloriesStatistics(trainings)));
        line(text, "5. Top-%d за дистанцією: %s", TrainingQueries.TOP_N,
                TrainingQueries.topByDistance(trainings, TrainingQueries.TOP_N).stream()
                        .map(t -> String.format(Locale.ROOT, "%s %.2f км", t.label(), t.getDistanceKm()))
                        .collect(Collectors.joining(", ", "[", "]")));
        line(text, "Пошук за датою %s: %s", searchDate, TrainingQueries.findByDate(trainings, searchDate)
                .map(Training::label)
                .orElse("не знайдено"));
        return text.toString();
    }

    /* Порожній набір описується явно, без службових MIN/MAX_VALUE. */
    private static String statistics(IntSummaryStatistics stats) {
        if (stats.getCount() == 0) {
            return "немає даних";
        }
        return String.format(Locale.ROOT, "count=%d, sum=%d, min=%d, max=%d, avg=%.2f",
                stats.getCount(), stats.getSum(), stats.getMin(), stats.getMax(), stats.getAverage());
    }

    /* Мітки «вид (дата)» через кому. */
    private static String labels(List<Training> trainings) {
        return trainings.stream().map(Training::label).collect(Collectors.joining(", ", "[", "]"));
    }

    /* Додає рядок із локаллю ROOT. */
    private static void line(StringBuilder text, String pattern, Object... args) {
        text.append(String.format(Locale.ROOT, pattern, args)).append(System.lineSeparator());
    }
}
