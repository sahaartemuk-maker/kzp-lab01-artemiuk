package ua.lpnu.kzp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Предметний імпорт тренувань з експортованого CSV.
 *
 * <p>Розбір тексту виконує універсальний {@link CsvParser}, а перетворення
 * полів на об'єкт — {@link Training#fromCsvFields(List)}.
 */
public final class Trainings {

    /* Забороняє створення екземплярів. */
    private Trainings() {
    }

    /**
     * Читає тренування з CSV-файла, створеного {@link CsvExporter}.
     *
     * @param path шлях до файла
     * @return незмінний список у порядку файла
     * @throws DataStorageException якщо файл неможливо прочитати або запис
     *         має хибний формат; повідомлення містить номер запису
     */
    public static List<Training> readCsv(Path path) throws DataStorageException {
        List<List<String>> records = CsvParser.readRecords(path);
        List<Training> result = new ArrayList<>();
        for (int index = 0; index < records.size(); index++) {
            try {
                result.add(Training.fromCsvFields(records.get(index)));
            } catch (DataStorageException exception) {
                throw new DataStorageException("Запис " + (index + 1) + ": " + exception.getMessage(),
                        exception);
            }
        }
        return List.copyOf(result);
    }
}
