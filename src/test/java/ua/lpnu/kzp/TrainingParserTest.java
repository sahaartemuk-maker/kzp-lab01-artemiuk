package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TrainingParserTest {

    @Test
    void validLineHasNoError() {
        assertNull(TrainingParser.validate("2026-09-01;Біг;45;8.5;520"));
    }

    @Test
    void emptyLineIsRejected() {
        assertEquals("порожній рядок", TrainingParser.validate("   "));
    }

    @Test
    void wrongFieldCountIsRejected() {
        assertEquals("очікується 5 полів, отримано 4", TrainingParser.validate("2026-09-09;Йога;60;0.0"));
    }

    @Test
    void nonNumericFieldIsRejected() {
        assertEquals("числове поле має помилковий формат",
                TrainingParser.validate("2026-09-06;Біг;сорок;7.0;480"));
    }

    @Test
    void negativeValueIsRejected() {
        assertEquals("від'ємне числове значення", TrainingParser.validate("2026-09-07;Біг;-20;10.0;200"));
    }

    @Test
    void emptyKindIsRejected() {
        assertEquals("порожній вид тренування", TrainingParser.validate("2026-09-08;;40;5.0;300"));
    }

    @Test
    void ukrainianTextIsPreserved() {
        assertArrayEquals(new String[] {"2026-09-02", "Велосипед", "90", "32.0", "780"},
                TrainingParser.fields("2026-09-02; Велосипед ;90;32.0;780"));
    }
}
