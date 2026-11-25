package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Тести підтипів, поліморфного навантаження та рівності. */
class PolymorphismTest {

    @Test
    void factoryChoosesSubtypeByKind() {
        assertInstanceOf(OutdoorTraining.class, Training.fromCsv("2026-09-01;Біг;45;8.5;520"));
        assertInstanceOf(IndoorTraining.class, Training.fromCsv("2026-09-04;Плавання;60;2.4;610"));
        assertInstanceOf(IndoorTraining.class, Training.of("2026-09-03", "Йога", 60, 0.0, 200));
        assertInstanceOf(OutdoorTraining.class, Training.of("2026-09-03", "Скелелазіння", 60, 0.0, 200));
    }

    @Test
    void subtypesCalculateLoadDifferently() {
        // Однакові час і дистанція, але різні формули підтипів.
        Training indoor = new IndoorTraining("2026-09-01", "Плавання", 60, 2.0, 500);
        Training outdoor = new OutdoorTraining("2026-09-01", "Біг", 60, 2.0, 500);
        assertEquals(84.0, indoor.load(), 1e-9);   // 60 × 1.4
        assertEquals(94.0, outdoor.load(), 1e-9);  // 60 × 1.5 + 2 × 2
        assertNotEquals(indoor.load(), outdoor.load());
    }

    @Test
    void zeroValuesGiveZeroLoad() {
        assertEquals(0.0, Training.of("2026-09-03", "Біг", 0, 0.0, 0).load());
        assertEquals(0.0, Training.of("2026-09-03", "Йога", 0, 0.0, 0).load());
    }

    @Test
    void polymorphicCollectionSumsLoads() {
        List<Training> trainings = List.of(
                Training.of("2026-09-01", "Біг", 45, 8.5, 520),
                Training.of("2026-09-02", "Велосипед", 90, 32.0, 780),
                Training.of("2026-09-04", "Плавання", 60, 2.4, 610));
        double total = 0.0;
        for (Training training : trainings) {
            total += training.load();
        }
        assertEquals(84.5 + 172.0 + 84.0, total, 1e-9);
    }

    @Test
    void outdoorPace() {
        assertEquals(5.0, new OutdoorTraining("2026-09-01", "Біг", 40, 8.0, 400).paceMinPerKm(), 1e-9);
        assertEquals(0.0, new OutdoorTraining("2026-09-01", "Біг", 40, 0.0, 400).paceMinPerKm());
    }

    @Test
    void subtypesRejectWrongKind() {
        assertThrows(IllegalArgumentException.class,
                () -> new IndoorTraining("2026-09-01", "Біг", 30, 5.0, 300));
        assertThrows(IllegalArgumentException.class,
                () -> new OutdoorTraining("2026-09-01", "Йога", 30, 0.0, 100));
    }

    @Test
    void subtypesRejectInvalidSharedState() {
        IllegalArgumentException indoor = assertThrows(IllegalArgumentException.class,
                () -> new IndoorTraining("2026-09-01", "Йога", -1, 0.0, 100));
        assertEquals("від'ємне числове значення", indoor.getMessage());
        IllegalArgumentException outdoor = assertThrows(IllegalArgumentException.class,
                () -> new OutdoorTraining("2026-09-01", "Біг", 10, Double.NaN, 100));
        assertEquals("некоректна дистанція", outdoor.getMessage());
    }

    @Test
    void equalObjectsHaveSameHashAndOneSetEntry() {
        Training a = Training.fromCsv("2026-09-01;Біг;45;8.5;520");
        Training b = Training.fromCsv(" 2026-09-01 ; Біг ; 45 ; 8.5 ; 520 ");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        Set<Training> set = new HashSet<>(List.of(a, b));
        assertEquals(1, set.size());
    }

    @Test
    void hashMapFindsLogicallyEqualKey() {
        Map<Training, String> notes = new HashMap<>();
        notes.put(Training.of("2026-09-04", "Плавання", 60, 2.4, 610), "басейн");
        assertEquals("басейн", notes.get(Training.of("2026-09-04", "Плавання", 60, 2.4, 610)));
    }

    @Test
    void differentFieldsAreNotEqual() {
        Training base = Training.of("2026-09-01", "Біг", 45, 8.5, 520);
        assertNotEquals(base, Training.of("2026-09-01", "Біг", 45, 8.5, 521));
        assertNotEquals(base, Training.of("2026-09-02", "Біг", 45, 8.5, 520));
        assertNotEquals(base, null);
        assertNotEquals(base, "2026-09-01;Біг;45;8.5;520");
    }

    @Test
    void differentSubtypesAreNeverEqual() {
        // Однакові поля, але різні підтипи: getClass() у equals робить їх нерівними.
        Training outdoor = Training.of("2026-09-01", "Біг", 30, 1.0, 100);
        Training indoor = Training.of("2026-09-01", "Йога", 30, 1.0, 100);
        assertNotEquals(outdoor.getClass(), indoor.getClass());
        assertNotEquals(outdoor, indoor);
    }
}
