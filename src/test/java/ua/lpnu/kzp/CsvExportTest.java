package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Тести рефлексивного експорту, CSV-парсера та round-trip. */
class CsvExportTest {

    static final String HEADER = "калорії,дата,дистанція,вид,тривалість";

    @TempDir
    Path dir;

    static List<Training> sample() {
        return List.of(
                Training.of("2026-09-01", "Біг", 45, 8.5, 520),
                Training.of("2026-09-02", "Плавання", 50, 1.5, 450),
                Training.of("2026-09-02", "Велосипед, інтервали", 90, 30.0, 700),
                Training.of("2026-09-03", "Йога \"ранкова\"", 40, 0.0, 120),
                Training.of("2026-09-04", "Ходьба\nз підйомом", 60, 5.25, 0));
    }

    /** Простий клас іншої області: експортер не залежить від Training. */
    static final class Note {
        @CsvColumn("Текст")
        private final String text;
        @CsvColumn("Номер")
        private final int id;
        private final String hidden = "x";

        Note(int id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    static final class Plain {
        private final int value = 1;
    }

    @Test
    void headerFollowsSortedFieldNames() throws DataStorageException {
        String csv = CsvExporter.toCsv(Training.class, List.of());
        assertEquals(HEADER + "\n", csv);
    }

    @Test
    void escapesCommaQuoteAndNewline() {
        assertEquals("Біг", CsvExporter.escape("Біг"));
        assertEquals("\"a,b\"", CsvExporter.escape("a,b"));
        assertEquals("\"Йога \"\"ранкова\"\"\"", CsvExporter.escape("Йога \"ранкова\""));
        assertEquals("\"a\nb\"", CsvExporter.escape("a\nb"));
        assertEquals("", CsvExporter.escape(null));
    }

    @Test
    void escapedRowForCommaInKind() throws DataStorageException {
        String csv = CsvExporter.toCsv(Training.class, List.of(sample().get(2)));
        assertTrue(csv.contains("700,2026-09-02,30.0,\"Велосипед, інтервали\",90\n"), csv);
    }

    @Test
    void exporterWorksWithAnyAnnotatedClass() throws DataStorageException {
        String csv = CsvExporter.toCsv(Note.class, List.of(new Note(7, "так, ні")));
        assertEquals("Номер,Текст\n7,\"так, ні\"\n", csv);
    }

    @Test
    void classWithoutAnnotationsFails() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.toCsv(Plain.class, List.of(new Plain())));
        assertTrue(exception.getMessage().contains("@CsvColumn"));
    }

    @Test
    void roundTripRestoresEqualObjects() throws DataStorageException {
        Repository<Training> repository = new Repository<>();
        repository.addAll(sample());
        Path file = dir.resolve("out").resolve("trainings.csv");
        CsvExporter.write(file, Training.class, repository.all());
        List<Training> restored = Trainings.readCsv(file);
        assertEquals(repository.all(), restored);
        assertEquals(IndoorTraining.class, restored.get(1).getClass());
        assertEquals(OutdoorTraining.class, restored.get(0).getClass());
    }

    @Test
    void writeTruncatesOldContent() throws Exception {
        Path file = dir.resolve("t.csv");
        CsvExporter.write(file, Training.class, sample());
        CsvExporter.write(file, Training.class, sample().subList(0, 1));
        assertEquals(2, Files.readAllLines(file, StandardCharsets.UTF_8).size());
    }

    @Test
    void emptySetGivesHeaderOnlyAndEmptyList() throws DataStorageException {
        Path file = dir.resolve("empty.csv");
        CsvExporter.write(file, Training.class, List.of());
        assertEquals(List.of(), Trainings.readCsv(file));
    }

    @Test
    void emptyFileIsError() throws IOException {
        Path file = Files.writeString(dir.resolve("zero.csv"), "", StandardCharsets.UTF_8);
        assertThrows(DataStorageException.class, () -> Trainings.readCsv(file));
    }

    @Test
    void parserKeepsCommaAndNewlineInsideQuotes() throws DataStorageException {
        List<List<String>> rows = CsvParser.parseDocument("a,\"b,c\"\r\n\"d\ne\",\"f\"\"g\"\n");
        assertEquals(List.of(List.of("a", "b,c"), List.of("d\ne", "f\"g")), rows);
    }

    @Test
    void parserRejectsUnclosedQuote() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("a,\"b\n"));
    }

    @Test
    void wrongFieldCountIsError() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> Training.fromCsvFields(List.of("1", "2026-09-01", "Біг")));
        assertTrue(exception.getMessage().contains("отримано 3"));
    }

    @Test
    void badNumberKeepsCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> Training.fromCsvFields(List.of("abc", "2026-09-01", "5.0", "Біг", "30")));
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    void modelInvariantKeepsCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> Training.fromCsvFields(List.of("100", "2026-09-01", "5.0", " ", "30")));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void missingFileKeepsIoCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> Trainings.readCsv(dir.resolve("немає.csv")));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void writeIntoFileAsParentKeepsIoCause() throws IOException {
        Path blocker = Files.writeString(dir.resolve("file"), "x", StandardCharsets.UTF_8);
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(blocker.resolve("t.csv"), Training.class, sample()));
        assertInstanceOf(IOException.class, exception.getCause());
    }
}
