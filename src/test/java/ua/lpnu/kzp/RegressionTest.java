package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Доводить збереження зовнішньої поведінки: звіт після перебудови моделі
 * збігається зі звітом, який сформувала версія ЛР № 1 (v1.0.0) на тих самих даних.
 */
class RegressionTest {

    @ParameterizedTest
    @CsvSource({
        "lr1-input.csv, lr1-expected-report.txt",
        "lr1-edge-input.csv, lr1-edge-expected-report.txt"
    })
    void reportMatchesLab1Output(String input, String expected) throws IOException {
        List<String> lines = resource(input).lines().toList();
        String actual = ReportFormatter.format(TrainingStats.compute(lines));
        // %n залежить від ОС, тому порівнюються рядки без роздільників.
        // ЛР № 3 додала лише рядок навантаження; решта звіту — байт-у-байт як у v1.0.0.
        List<String> previous = actual.lines()
                .filter(line -> !line.startsWith(ReportFormatter.LOAD_LABEL))
                .toList();
        assertEquals(resource(expected).lines().toList(), previous);
    }

    @ParameterizedTest
    @CsvSource({
        "lr1-input.csv, 370.70",
        "lr1-edge-input.csv, 178.00"
    })
    void reportContainsPolymorphicLoad(String input, String load) throws IOException {
        String actual = ReportFormatter.format(TrainingStats.compute(resource(input).lines().toList()));
        String line = actual.lines().filter(l -> l.startsWith(ReportFormatter.LOAD_LABEL)).findFirst().orElseThrow();
        assertEquals(load, line.substring(ReportFormatter.LOAD_LABEL.length()).trim());
    }

    private static String resource(String name) throws IOException {
        try (InputStream stream = RegressionTest.class.getResourceAsStream("/" + name)) {
            assertNotNull(stream, name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
