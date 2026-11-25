package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/** Тести сховища, анотації та власного винятку. */
class RepositoryTest {

    private static Repository<Training> sample() {
        Repository<Training> repository = new Repository<>();
        repository.add(Training.of("2026-09-01", "Біг", 30, 5.0, 300));
        repository.add(Training.of("2026-09-02", "Плавання", 50, 1.5, 450));
        repository.add(Training.of("2026-09-03", "Велосипед", 90, 30.0, 700));
        return repository;
    }

    @Test
    void allReturnsRecordsInInsertionOrder() {
        List<Training> all = sample().all();
        assertEquals(3, all.size());
        assertEquals("Біг", all.get(0).getKind());
        assertEquals("Велосипед", all.get(2).getKind());
    }

    @Test
    void snapshotIsImmutableAndDetached() {
        Repository<Training> repository = sample();
        List<Training> snapshot = repository.all();
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(Training.of("2026-09-04", "Йога", 40, 0, 120)));
        repository.add(Training.of("2026-09-04", "Йога", 40, 0, 120));
        assertEquals(3, snapshot.size());
        assertEquals(4, repository.size());
    }

    @Test
    void findAcceptsSupertypePredicate() {
        Predicate<Object> notNull = item -> item != null;
        assertEquals(3, sample().find(notNull).size());
        List<Training> long45 = sample().find(t -> t.getMinutes() >= 45);
        assertEquals(2, long45.size());
    }

    @Test
    void addRejectsNull() {
        assertThrows(NullPointerException.class, () -> new Repository<String>().add(null));
    }

    @Test
    void csvColumnIsRuntimeFieldAnnotation() {
        Retention retention = CsvColumn.class.getAnnotation(Retention.class);
        Target target = CsvColumn.class.getAnnotation(Target.class);
        assertNotNull(retention);
        assertNotNull(target);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
        assertTrue(List.of(target.value()).equals(List.of(ElementType.FIELD)));
    }

    @Test
    void exceptionKeepsMessageAndCause() {
        IOException cause = new IOException("disk");
        DataStorageException exception = new DataStorageException("Не вдалося записати CSV", cause);
        assertEquals("Не вдалося записати CSV", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
