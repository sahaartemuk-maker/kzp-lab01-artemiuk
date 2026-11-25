package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне сховище об'єктів у пам'яті.
 *
 * <p>Тип записів задається параметром {@code T} під час створення сховища,
 * тому приведення типів не потрібні, а сирий тип не використовується.
 *
 * @param <T> тип записів
 */
public final class Repository<T> {

    private final List<T> items = new ArrayList<>();

    /** Створює порожнє сховище. */
    public Repository() {
        // Записи додаються методом add.
    }

    /**
     * Додає непорожній запис.
     *
     * @param item запис
     * @throws NullPointerException якщо запис дорівнює {@code null}
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "запис не може бути null"));
    }

    /**
     * Додає всі записи зі списку.
     *
     * @param records записи
     * @throws NullPointerException якщо список або один із записів дорівнює {@code null}
     */
    public void addAll(List<? extends T> records) {
        Objects.requireNonNull(records, "список не може бути null").forEach(this::add);
    }

    /**
     * Повертає незмінний знімок усіх записів у порядку додавання.
     *
     * @return незмінний список
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає записи, що задовольняють умову.
     *
     * @param condition умова, яка приймає {@code T} або його супертип
     * @return незмінний список знайдених записів
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "умова не може бути null");
        return items.stream().filter(condition).toList();
    }

    /** @return кількість записів */
    public int size() {
        return items.size();
    }
}
