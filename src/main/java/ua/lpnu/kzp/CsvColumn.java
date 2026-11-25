package ua.lpnu.kzp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Позначає поле, яке експортується в CSV, і задає заголовок його стовпця.
 *
 * <p>{@code RUNTIME} залишає анотацію доступною для рефлексії під час
 * виконання, {@code FIELD} дозволяє ставити її лише над полями. Порядок
 * стовпців анотація не задає: його визначає сортування Java-імен полів.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {

    /**
     * Повертає заголовок CSV-стовпця.
     *
     * @return назва стовпця
     */
    String value();
}
