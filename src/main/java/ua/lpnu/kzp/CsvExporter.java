package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Універсальний рефлексивний CSV-експортер.
 *
 * <p>Експортер не знає жодного предметного класу: стовпці — це поля з
 * {@link CsvColumn}, знайдені рефлексією в класі та всіх його суперкласах до
 * {@link Object}. Порядок стовпців детермінований — за Java-іменем поля.
 */
public final class CsvExporter {

    /** Роздільник рядків у створеному CSV (однаковий на всіх ОС). */
    public static final String LINE_SEPARATOR = "\n";

    /* Забороняє створення екземплярів статичного експортера. */
    private CsvExporter() {
    }

    /**
     * Повертає анотовані поля класу та його суперкласів, відсортовані за іменем.
     *
     * @param type клас записів
     * @return незмінний список доступних полів
     * @throws DataStorageException якщо анотованих полів немає або доступ заборонено
     */
    public static List<Field> annotatedFields(Class<?> type) throws DataStorageException {
        Objects.requireNonNull(type, "клас не може бути null");
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)) {
                    fields.add(field);
                }
            }
        }
        if (fields.isEmpty()) {
            throw new DataStorageException("Клас " + type.getName() + " не має полів з @CsvColumn");
        }
        fields.sort(Comparator.comparing(Field::getName));
        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                throw new DataStorageException("Немає доступу до поля " + field.getName());
            }
        }
        return List.copyOf(fields);
    }

    /**
     * Будує CSV-текст: заголовок і по рядку на кожен запис.
     *
     * @param <T> тип записів
     * @param type клас записів (потрібен для заголовка навіть без записів)
     * @param records записи
     * @return CSV-текст
     * @throws DataStorageException якщо поля неможливо знайти або прочитати
     */
    public static <T> String toCsv(Class<T> type, List<? extends T> records) throws DataStorageException {
        Objects.requireNonNull(records, "список записів не може бути null");
        List<Field> fields = annotatedFields(type);
        StringBuilder csv = new StringBuilder(fields.stream()
                .map(field -> escape(field.getAnnotation(CsvColumn.class).value()))
                .collect(Collectors.joining(",")))
                .append(LINE_SEPARATOR);
        for (T record : records) {
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    values.add(escape(field.get(record)));
                } catch (IllegalAccessException exception) {
                    throw new DataStorageException("Не вдалося прочитати поле " + field.getName(), exception);
                }
            }
            csv.append(String.join(",", values)).append(LINE_SEPARATOR);
        }
        return csv.toString();
    }

    /**
     * Записує анотовані поля записів у CSV-файл в UTF-8. Старий вміст
     * повністю замінюється; батьківські теки створюються за потреби.
     *
     * @param <T> тип записів
     * @param path шлях до CSV-файла
     * @param type клас записів
     * @param records записи для експорту
     * @throws DataStorageException якщо поля неможливо прочитати або файл
     *         записати (причина — {@link IOException})
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        String csv = toCsv(type, records);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося записати CSV: " + path, exception);
        }
    }

    /**
     * Екранує значення за правилами CSV: поле з комою, лапкою або
     * перенесенням рядка береться в лапки, лапки всередині подвоюються.
     *
     * @param value значення (null — порожнє поле)
     * @return екранований текст
     */
    public static String escape(Object value) {
        String text = Objects.toString(value, "");
        boolean quoted = text.contains(",") || text.contains("\"")
                || text.contains("\r") || text.contains("\n");
        return quoted ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
    }
}
