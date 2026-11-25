package ua.lpnu.kzp;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Потокові запити до колекції тренувань (ЛР № 4, варіант 2).
 *
 * <p>Кожен запит — окремий метод з одним результатом. Методи працюють зі спільним
 * типом {@link Training} без перевірок підтипу, не змінюють вхідну колекцію й не
 * форматують текст: форматування виконує {@link QueryReportFormatter}.
 */
public final class TrainingQueries {

    /** Поріг відбору: тривалість не менша 45 хвилин (межа включна). */
    public static final int MIN_LONG_MINUTES = 45;

    /** Кількість записів у запиті top-N. */
    public static final int TOP_N = 5;

    /**
     * Порядок top-N: дистанція за спаданням, за рівної дистанції — дата за зростанням.
     * {@code reversed()} застосовано лише до ключа дистанції, до {@code thenComparing}.
     * Дата зберігається у форматі ISO (рррр-мм-дд), тому текстовий порядок збігається
     * з календарним.
     */
    public static final Comparator<Training> BY_DISTANCE_DESC_THEN_DATE =
            Comparator.comparingDouble(Training::getDistanceKm)
                    .reversed()
                    .thenComparing(Training::getDate);

    /* Забороняє створення екземплярів службового класу. */
    private TrainingQueries() {
    }

    /**
     * Запит 1 (filter): тренування тривалістю не менше {@value #MIN_LONG_MINUTES} хв.
     *
     * @param trainings вхідна колекція
     * @return незмінний список у початковому порядку
     */
    public static List<Training> longTrainings(List<Training> trainings) {
        return trainings.stream()
                .filter(training -> training.getMinutes() >= MIN_LONG_MINUTES)
                .toList();
    }

    /**
     * Запит 2 (map): дистанції всіх тренувань.
     *
     * @param trainings вхідна колекція
     * @return незмінний список дистанцій у кілометрах у початковому порядку
     */
    public static List<Double> distances(List<Training> trainings) {
        return trainings.stream()
                .map(Training::getDistanceKm)
                .toList();
    }

    /**
     * Запит 3 (groupingBy): сумарна дистанція за видом тренування.
     *
     * @param trainings вхідна колекція
     * @return незмінна карта «вид → сума км» у порядку оголошення {@link TrainingKind}
     */
    public static Map<TrainingKind, Double> distanceByKind(List<Training> trainings) {
        return Collections.unmodifiableMap(trainings.stream()
                .collect(Collectors.groupingBy(
                        Training::getType,
                        () -> new EnumMap<>(TrainingKind.class),
                        Collectors.summingDouble(Training::getDistanceKm))));
    }

    /**
     * Запит 4 (summarizingInt): статистика витрати калорій. За погодженням поле
     * «пульс» варіанта замінено на наявне поле «калорії», щоб не змінювати формат CSV.
     * Для порожнього входу count = 0, sum = 0, min = {@link Integer#MAX_VALUE},
     * max = {@link Integer#MIN_VALUE}.
     *
     * @param trainings вхідна колекція
     * @return статистика калорій
     */
    public static IntSummaryStatistics caloriesStatistics(List<Training> trainings) {
        return trainings.stream()
                .collect(Collectors.summarizingInt(Training::getCalories));
    }

    /**
     * Запит 5 (sorted + limit): перші {@code n} тренувань за
     * {@link #BY_DISTANCE_DESC_THEN_DATE}.
     *
     * @param trainings вхідна колекція
     * @param n кількість записів; 0 дає порожній список, завеликий n — усі записи
     * @return незмінний відсортований список
     * @throws IllegalArgumentException якщо {@code n} від'ємне
     */
    public static List<Training> topByDistance(List<Training> trainings, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("N не може бути від'ємним: " + n);
        }
        return trainings.stream()
                .sorted(BY_DISTANCE_DESC_THEN_DATE)
                .limit(n)
                .toList();
    }

    /**
     * Пошук (Optional): перше в початковому порядку тренування за датою.
     *
     * @param trainings вхідна колекція
     * @param date шукана дата
     * @return знайдене тренування або {@link Optional#empty()}
     */
    public static Optional<Training> findByDate(List<Training> trainings, LocalDate date) {
        String key = Objects.requireNonNull(date, "дата не може бути null").toString();
        return trainings.stream()
                .filter(training -> training.getDate().equals(key))
                .findFirst();
    }
}
