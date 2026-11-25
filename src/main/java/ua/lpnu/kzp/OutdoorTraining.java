package ua.lpnu.kzp;

/**
 * Тренування на вулиці (біг, велосипед, ходьба та інші види).
 *
 * <p>До навантаження за часом додається внесок дистанції, бо на вулиці вона
 * відображає реальну пройдену роботу.
 */
public final class OutdoorTraining extends Training {

    /** Балів навантаження за один кілометр дистанції. */
    public static final double POINTS_PER_KM = 2.0;

    /**
     * Створює тренування на вулиці.
     *
     * @param date дата тренування
     * @param kind вид тренування, що не належить до категорії «у приміщенні»
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @throws IllegalArgumentException якщо порушено інваріант або вид є «у приміщенні»
     */
    public OutdoorTraining(String date, String kind, int minutes, double distanceKm, int calories) {
        super(date, kind, minutes, distanceKm, calories);
        if (getType().isIndoor()) {
            throw new IllegalArgumentException("вид не є тренуванням на вулиці");
        }
    }

    /** Навантаження = хвилини × інтенсивність + дистанція × {@value #POINTS_PER_KM}. */
    @Override
    public double load() {
        return getMinutes() * getType().intensity() + getDistanceKm() * POINTS_PER_KM;
    }

    /**
     * Обчислює середній темп — властивість, яка має сенс лише для тренувань на вулиці.
     *
     * @return хвилин на кілометр або 0, якщо дистанція нульова
     */
    public double paceMinPerKm() {
        return getDistanceKm() == 0.0 ? 0.0 : getMinutes() / getDistanceKm();
    }
}
