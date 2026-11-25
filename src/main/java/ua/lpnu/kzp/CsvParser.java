package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Документний CSV-парсер: розбирає весь текст машиною станів і повертає
 * список записів, а не список фізичних рядків файла.
 *
 * <p>Усередині подвійних лапок кома та перенесення рядка є даними; подвоєна
 * лапка означає одну лапку. Підтримуються роздільники рядків {@code \n} і
 * {@code \r\n}.
 */
public final class CsvParser {

    /* Забороняє створення екземплярів статичного парсера. */
    private CsvParser() {
    }

    /**
     * Розбирає CSV-документ.
     *
     * @param text увесь текст документа
     * @return незмінний список записів; порожній текст дає порожній список
     * @throws DataStorageException якщо текст null, лапки не закриті або
     *         після закривальної лапки стоїть інший символ
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        if (text == null) {
            throw new DataStorageException("CSV-документ не може бути null");
        }
        List<List<String>> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;
        for (int index = 0; index < text.length(); index++) {
            char symbol = text.charAt(index);
            if (quoted) {
                if (symbol == '"' && index + 1 < text.length() && text.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else if (symbol == '"') {
                    quoted = false;
                    quoteClosed = true;
                } else {
                    current.append(symbol);
                }
            } else if (symbol == '"' && current.length() == 0 && !quoteClosed) {
                quoted = true;
            } else if (symbol == ',') {
                fields.add(current.toString());
                current.setLength(0);
                quoteClosed = false;
            } else if (symbol == '\r' || symbol == '\n') {
                fields.add(current.toString());
                records.add(List.copyOf(fields));
                fields = new ArrayList<>();
                current.setLength(0);
                quoteClosed = false;
                if (symbol == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
            } else if (quoteClosed) {
                throw new DataStorageException("Символ після закритих CSV-лапок у позиції " + index);
            } else {
                current.append(symbol);
            }
        }
        if (quoted) {
            throw new DataStorageException("Незакриті CSV-лапки");
        }
        if (!fields.isEmpty() || current.length() > 0 || quoteClosed) {
            fields.add(current.toString());
            records.add(List.copyOf(fields));
        }
        return List.copyOf(records);
    }

    /**
     * Читає CSV-файл у UTF-8 і повертає записи без рядка заголовка.
     *
     * @param path шлях до файла
     * @return записи даних; файл лише із заголовком дає порожній список
     * @throws DataStorageException якщо файл неможливо прочитати (причина —
     *         {@link IOException}), він порожній або має хибний формат
     */
    public static List<List<String>> readRecords(Path path) throws DataStorageException {
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося прочитати CSV: " + path, exception);
        }
        List<List<String>> rows = parseDocument(text);
        if (rows.isEmpty()) {
            throw new DataStorageException("CSV-файл порожній, немає заголовка: " + path);
        }
        return rows.subList(1, rows.size());
    }
}
