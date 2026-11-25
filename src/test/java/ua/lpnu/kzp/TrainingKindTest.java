package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Тести enum {@link TrainingKind}. */
class TrainingKindTest {

    @ParameterizedTest
    @CsvSource({"Біг, RUNNING", "  велосипед , CYCLING", "ХОДЬБА, WALKING", "Плавання, SWIMMING", "Йога, YOGA"})
    void recognisesKnownLabels(String text, TrainingKind expected) {
        assertSame(expected, TrainingKind.fromLabel(text));
    }

    @Test
    void unknownLabelIsOther() {
        assertSame(TrainingKind.OTHER, TrainingKind.fromLabel("Скелелазіння"));
        assertSame(TrainingKind.OTHER, TrainingKind.fromLabel("Інше"));
    }

    @Test
    void indoorFlagAndLabel() {
        assertTrue(TrainingKind.SWIMMING.isIndoor());
        assertTrue(TrainingKind.YOGA.isIndoor());
        assertFalse(TrainingKind.RUNNING.isIndoor());
        assertFalse(TrainingKind.OTHER.isIndoor());
        assertEquals("Біг", TrainingKind.RUNNING.label());
    }

    @Test
    void everyKindHasPositiveIntensity() {
        for (TrainingKind kind : TrainingKind.values()) {
            assertTrue(kind.intensity() > 0, kind.name());
        }
    }
}
