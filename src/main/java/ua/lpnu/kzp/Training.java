package ua.lpnu.kzp;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Спільний абстрактний тип тренування (варіант 2).
 *
 * <p>Тут зосереджено спільні поля та інваріанти: дата й вид — непорожній текст;
 * хвилини, дистанція та калорії не від'ємні; дистанція — скінченне число. Нуль
 * дозволено, як і в ЛР № 1. Відмінна поведінка — розрахунок навантаження
 * {@link #load()} — реалізується у підтипах {@link IndoorTraining} і
 * {@link OutdoorTraining}.
 */
public abstract class Training {

    /** Кількість полів у CSV-рядку {@code date;kind;minutes;distanceKm;calories}. */
    public static final int FIELD_COUNT = 5;

    @CsvColumn("дата")
    private final String date;
    @CsvColumn("вид")
    private final String kind;
    private final TrainingKind type;
    @CsvColumn("тривалість")
    private final int minutes;
    @CsvColumn("дистанція")
    private final double distanceKm;
    @CsvColumn("калорії")
    private final int calories;

    /**
     * Створює спільну частину тренування після перевірки інваріантів.
     *
     * @param date дата тренування
     * @param kind вид тренування (текст із CSV)
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @throws NullPointerException якщо дата або вид дорівнюють {@code null}
     * @throws IllegalArgumentException якщо порушено інваріант
     */
    protected Training(String date, String kind, int minutes, double distanceKm, int calories) {
        // Перевірка виконується до виклику приватного конструктора, тож частково
        // створений об'єкт не з'являється (і SpotBugs не бачить CT_CONSTRUCTOR_THROW).
        this(validate(date, kind, minutes, distanceKm, calories), date, kind, minutes, distanceKm, calories);
    }

    /* Присвоює вже перевірені значення. */
    private Training(boolean validated, String date, String kind, int minutes, double distanceKm, int calories) {
        this.date = date.trim();
        this.kind = kind.trim();
        this.type = TrainingKind.fromLabel(kind);
        this.minutes = minutes;
        this.distanceKm = distanceKm;
        this.calories = calories;
    }

    /* Перевіряє інваріанти в тому самому порядку, що й ЛР № 2. */
    private static boolean validate(String date, String kind, int minutes, double distanceKm, int calories) {
        requireText(Objects.requireNonNull(date, "дата не може бути null"), "порожня дата");
        requireText(Objects.requireNonNull(kind, "вид не може бути null"), "порожній вид тренування");
        if (minutes < 0 || distanceKm < 0 || calories < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        if (!Double.isFinite(distanceKm)) {
            throw new IllegalArgumentException("некоректна дистанція");
        }
        return true;
    }

    /**
     * Створює тренування потрібного підтипу за видом: у приміщенні —
     * {@link IndoorTraining}, інакше — {@link OutdoorTraining}.
     *
     * @param date дата тренування
     * @param kind вид тренування
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @return створене тренування
     * @throws NullPointerException якщо дата або вид дорівнюють {@code null}
     * @throws IllegalArgumentException якщо порушено інваріант
     */
    public static Training of(String date, String kind, int minutes, double distanceKm, int calories) {
        Objects.requireNonNull(date, "дата не може бути null");
        Objects.requireNonNull(kind, "вид не може бути null");
        // Єдине місце вибору підтипу; далі код працює лише зі спільним типом.
        return TrainingKind.fromLabel(kind).isIndoor()
                ? new IndoorTraining(date, kind, minutes, distanceKm, calories)
                : new OutdoorTraining(date, kind, minutes, distanceKm, calories);
    }

    /**
     * Створює запис із CSV-рядка. Фабрика перевіряє структуру й типи полів,
     * конструктор — інваріанти.
     *
     * @param line рядок у форматі {@code date;kind;minutes;distanceKm;calories}
     * @return створений запис потрібного підтипу
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
            return of(fields[0], fields[1], minutes, distanceKm, calories);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("числове поле має помилковий формат", exception);
        }
    }

    /**
     * Відновлює запис із полів експортованого CSV. Порядок полів збігається з
     * порядком стовпців {@link CsvExporter}: калорії, дата, дистанція, вид,
     * тривалість (сортування Java-імен полів).
     *
     * @param fields розібрані CSV-поля одного запису
     * @return відновлене тренування потрібного підтипу
     * @throws DataStorageException якщо кількість полів інша, число має хибний
     *         формат або порушено інваріант моделі (причина зберігається)
     */
    public static Training fromCsvFields(List<String> fields) throws DataStorageException {
        if (fields == null || fields.size() != FIELD_COUNT) {
            throw new DataStorageException(String.format(Locale.ROOT,
                    "Очікується %d CSV-полів, отримано %d",
                    FIELD_COUNT, fields == null ? 0 : fields.size()));
        }
        try {
            int calories = Integer.parseInt(fields.get(0).trim());
            double distanceKm = Double.parseDouble(fields.get(2).trim());
            int minutes = Integer.parseInt(fields.get(4).trim());
            return of(fields.get(1), fields.get(3), minutes, distanceKm, calories);
        } catch (IllegalArgumentException exception) {
            throw new DataStorageException("Некоректний CSV-запис тренування: " + fields, exception);
        }
    }

    /* Відхиляє порожній або пробільний текст із заданою причиною. */
    private static void requireText(String value, String message) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Обчислює тренувальне навантаження в умовних балах.
     * Кожен підтип має власну формулу.
     *
     * @return навантаження, не від'ємне
     */
    public abstract double load();

    /** @return дата тренування */
    public final String getDate() {
        return date;
    }

    /** @return вид тренування (текст із CSV) */
    public final String getKind() {
        return kind;
    }

    /** @return категорія тренування */
    public final TrainingKind getType() {
        return type;
    }

    /** @return тривалість у хвилинах */
    public final int getMinutes() {
        return minutes;
    }

    /** @return дистанція в кілометрах */
    public final double getDistanceKm() {
        return distanceKm;
    }

    /** @return витрата калорій */
    public final int getCalories() {
        return calories;
    }

    /** @return коротка мітка для звіту у форматі «вид (дата)» */
    public final String label() {
        return String.format(Locale.ROOT, "%s (%s)", kind, date);
    }

    /**
     * Порівнює тренування за класом і всіма полями запису. Перевірка
     * {@code getClass()} не дозволяє вважати рівними різні підтипи.
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Training that = (Training) other;
        return minutes == that.minutes
                && Double.compare(distanceKm, that.distanceKm) == 0
                && calories == that.calories
                && date.equals(that.date)
                && kind.equals(that.kind);
    }

    /** Повертає хеш-код, узгоджений із {@link #equals(Object)}. */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), date, kind, minutes, distanceKm, calories);
    }

    /** Повертає текстове подання запису (не CSV). */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s %s: %d хв, %.2f км, %d ккал",
                date, kind, minutes, distanceKm, calories);
    }
}
