package ua.lpnu.kzp;

/**
 * Тренування в приміщенні (плавання в басейні, йога).
 *
 * <p>Навантаження залежить лише від часу та інтенсивності виду: дистанція в
 * басейні чи на килимку не відображає зусилля.
 */
public final class IndoorTraining extends Training {

    /**
     * Створює тренування в приміщенні.
     *
     * @param date дата тренування
     * @param kind вид тренування, що належить до категорії «у приміщенні»
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @throws IllegalArgumentException якщо порушено інваріант або вид не є «у приміщенні»
     */
    public IndoorTraining(String date, String kind, int minutes, double distanceKm, int calories) {
        // Спочатку базовий клас перевіряє спільні поля, потім підтип — власне правило.
        super(date, kind, minutes, distanceKm, calories);
        if (!getType().isIndoor()) {
            throw new IllegalArgumentException("вид не є тренуванням у приміщенні");
        }
    }

    /** Навантаження = хвилини × інтенсивність виду. */
    @Override
    public double load() {
        return getMinutes() * getType().intensity();
    }
}
