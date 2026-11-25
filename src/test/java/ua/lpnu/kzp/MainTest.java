package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    @TempDir
    Path tempDir;

    @Test
    void endToEndWritesSameReportToFile() throws IOException {
        Path input = tempDir.resolve("in.csv");
        Path output = tempDir.resolve("sub").resolve("report.txt");
        Files.writeString(input, "2026-09-01;Біг;45;8.5;520\n2026-09-02;Плавання;-1;1.0;100\n",
                StandardCharsets.UTF_8);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int code = Main.run(new String[] {"--input", input.toString(), "--output", output.toString()},
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertEquals(0, code);
        String file = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(file.contains("Рядок 2: від'ємне числове значення"));
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains(file));
    }

    @Test
    void unknownArgumentReturnsUsageError() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        assertEquals(2, Main.run(new String[] {"--bad"}, new PrintStream(buffer, true, StandardCharsets.UTF_8)));
    }

    @Test
    void queriesOptionPrintsStreamResultsAfterUnchangedReport(@org.junit.jupiter.api.io.TempDir Path dir) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int code = Main.run(new String[] {"--input", "data/queries.csv",
                "--output", dir.resolve("r.txt").toString(), "--queries", "2026-09-30"},
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(0, code);
        assertTrue(text.contains("Запити Stream API"));
        assertTrue(text.contains("Пошук за датою 2026-09-30: не знайдено"));
    }

    @Test
    void queriesOptionRejectsBadDate() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        assertEquals(2, Main.run(new String[] {"--queries", "02.09.2026"},
                new PrintStream(buffer, true, StandardCharsets.UTF_8)));
    }

    @Test
    void exportWritesCsvAndReportsRoundTrip() throws IOException {
        Path csv = tempDir.resolve("export").resolve("trainings.csv");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int code = Main.run(new String[] {"--input", "data/queries.csv",
            "--output", tempDir.resolve("r.txt").toString(), "--export", csv.toString()},
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertEquals(0, code);
        assertTrue(Files.readString(csv, StandardCharsets.UTF_8)
                .startsWith("калорії,дата,дистанція,вид,тривалість\n"));
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("рівність: так"));
    }

    @Test
    void exportFailureReturnsOneAndShowsCause() throws IOException {
        Path blocker = Files.writeString(tempDir.resolve("file"), "x", StandardCharsets.UTF_8);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int code = Main.run(new String[] {"--input", "data/queries.csv",
            "--output", tempDir.resolve("r.txt").toString(),
            "--export", blocker.resolve("t.csv").toString()},
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertEquals(1, code);
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("Причина:"));
    }
}
