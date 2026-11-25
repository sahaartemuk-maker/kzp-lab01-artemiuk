package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TrainingTest {

    @Test
    void constructorStoresAllFields() {
        // Arrange і Act
        Training training = new Training("2026-09-01", "Біг", 45, 8.5, 520);

        // Assert
        assertAll(
                () -> assertEquals("2026-09-01", training.getDate()),
                () -> assertEquals("Біг", training.getKind()),
                () -> assertEquals(45, training.getMinutes()),
                () -> assertEquals(8.5, training.getDistanceKm(), 1e-9),
                () -> assertEquals(520, training.getCalories()));
    }

    @Test
    void zeroValuesAreAllowedAsInLab1() {
        Training training = new Training("2026-09-03", "Йога", 0, 0.0, 0);
        assertEquals(0, training.getMinutes());
    }

    @Test
    void nullDateOrKindIsRejected() {
        assertThrows(NullPointerException.class, () -> new Training(null, "Біг", 1, 1.0, 1));
        assertThrows(NullPointerException.class, () -> new Training("2026-09-01", null, 1, 1.0, 1));
    }

    @Test
    void blankDateIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Training("  ", "Біг", 1, 1.0, 1));
        assertEquals("порожня дата", e.getMessage());
    }

    @Test
    void blankKindIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "", 1, 1.0, 1));
        assertEquals("порожній вид тренування", e.getMessage());
    }

    @Test
    void negativeMinutesIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Training("2026-09-01", "Біг", -1, 1.0, 1));
    }

    @Test
    void negativeDistanceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Training("2026-09-01", "Біг", 1, -0.1, 1));
    }

    @Test
    void negativeCaloriesIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Training("2026-09-01", "Біг", 1, 1.0, -5));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY})
    void nonFiniteDistanceIsRejected(double distance) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "Біг", 1, distance, 1));
        assertEquals("некоректна дистанція", e.getMessage());
    }

    @Test
    void fromCsvAcceptsValidLineAndTrimsFields() {
        Training training = Training.fromCsv(" 2026-09-02 ; Велосипед ;90; 32.0 ;780");
        assertEquals(new Training("2026-09-02", "Велосипед", 90, 32.0, 780), training);
    }

    @Test
    void fromCsvRejectsWrongFieldCount() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Training.fromCsv("2026-09-09;Йога;60;0.0"));
        assertEquals("очікується 5 полів, отримано 4", e.getMessage());
    }

    @Test
    void fromCsvRejectsBadNumber() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Training.fromCsv("2026-09-06;Біг;сорок;7.0;480"));
        assertEquals("числове поле має помилковий формат", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void fromCsvRejectsBlankLine(String line) {
        assertEquals("порожній рядок",
                assertThrows(IllegalArgumentException.class, () -> Training.fromCsv(line)).getMessage());
    }

    @Test
    void fromCsvRejectsNullLine() {
        assertThrows(IllegalArgumentException.class, () -> Training.fromCsv(null));
    }

    @Test
    void fromCsvChecksTextBeforeNumbers() {
        // Порядок причин має збігатися з ЛР № 1: порожня дата важливіша за хибне число.
        assertEquals("порожня дата",
                assertThrows(IllegalArgumentException.class, () -> Training.fromCsv(";;x;1;1")).getMessage());
    }

    @Test
    void equalsAndHashCodeDependOnValues() {
        Training a = new Training("2026-09-01", "Біг", 45, 8.5, 520);
        Training b = new Training("2026-09-01", "Біг", 45, 8.5, 520);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Training("2026-09-01", "Біг", 46, 8.5, 520));
    }

    @Test
    void toStringUsesDotAndReadableFormat() {
        Training training = new Training("2026-09-01", "Біг", 45, 8.5, 520);
        assertEquals("2026-09-01 Біг: 45 хв, 8.50 км, 520 ккал", training.toString());
        assertEquals("Біг (2026-09-01)", training.label());
    }
}
