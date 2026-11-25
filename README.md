# Спортивні тренування — лабораторні роботи № 1–7 (варіант 2)
# Спортивні тренування — лабораторні роботи № 1–5 (варіант 2)
=======
# Спортивні тренування — лабораторні роботи № 1–7(варіант 2)
>>>>>>> origin/main

## Призначення
Консольна програма читає записи тренувань із `data/input.csv`, перевіряє їх,
обчислює показники та виводить звіт у консоль і файл `out/report.txt`.

## Формат входу
UTF-8, один запис у рядку, поля розділено `;`:

```text
date;kind;minutes;distanceKm;calories
2026-09-01;Біг;45;8.5;520
```

| Поле | Тип | Перевірка |
|---|---|---|
| date | String (YYYY-MM-DD) | не порожнє |
| kind | String | не порожнє |
| minutes | int | ціле, ≥ 0 |
| distanceKm | double (з крапкою) | число, ≥ 0 |
| calories | int | ціле, ≥ 0 |

Хибні рядки пропускаються з повідомленням «Рядок N: причина».

## Показники
1. Кількість коректних записів.
2. Сумарний час, хв.
3. Сумарна дистанція, км.
4. Найбільша витрата калорій (з видом і датою тренування).
5. Сумарне навантаження, бал (з ЛР № 3, поліморфний метод `Training.load()`).

## Збірка та запуск
```text
./mvnw clean verify                         # тести + SpotBugs + JAR
java -jar target/lab01-5.0.0.jar --help
java -jar target/lab01-5.0.0.jar --version  # 4.0.0+local
java -jar target/lab01-5.0.0.jar
java -jar target/lab01-5.0.0.jar --input data/input.csv --output out/report.txt
```
У Windows замість `./mvnw` використовуйте `mvnw.cmd`.

## Структура
```text
src/main/java/ua/lpnu/kzp/
  Main.java             аргументи командного рядка, запуск
  TrainingParser.java   перевірка й розбір рядка
  TrainingStats.java    обчислення показників
  ReportFormatter.java  формування тексту звіту
  FileReport.java       читання/запис файлів (UTF-8)
  BuildInfo.java        версія + номер збірки
```

## ЛР № 2 — класи, record і інкапсуляція
Рядки та `String[]` замінено моделлю `Training` (клас-сутність з інваріантами
в конструкторі й фабрикою `fromCsv`) і record `TrainingSummary`. Зовнішня
поведінка не змінилась — це перевіряє `RegressionTest` проти звіту версії v1.0.0.
Деталі: [REPORT_LR2.md](REPORT_LR2.md).

## ЛР № 3 — спадкування, інтерфейси й поліморфізм
`Training` став абстрактним базовим класом зі спільними полями й інваріантами
та абстрактним методом `load()`. Підтипи:

| Клас | Види | `load()` |
|---|---|---|
| `IndoorTraining` | Плавання, Йога | хвилини × інтенсивність |
| `OutdoorTraining` | Біг, Велосипед, Ходьба, інші | хвилини × інтенсивність + км × 2 |

`TrainingKind` (enum) містить підпис, ознаку «у приміщенні» й коефіцієнт
інтенсивності. Підтип обирається один раз у фабриці `Training.of`; звіт
підсумовує `load()` через спільний тип без `if`/`switch` за підтипом.
У звіт додано рядок «Сумарне навантаження, бал»; решта звіту не змінилась
(`RegressionTest`). Деталі: [REPORT_LR3.md](REPORT_LR3.md).

## ЛР № 4 — потокова обробка даних (Stream API)

Запити винесено в клас `TrainingQueries` (без форматування), текст формує `QueryReportFormatter`.
Основний звіт не змінився; результати запитів виводяться лише з опцією `--queries <дата>`:

```bash
java -jar target/lab01-5.0.0.jar --input data/queries.csv --queries 2026-09-02
```

| Запит | Метод | Конвеєр | Результат |
|---|---|---|---|
| Відбір: тривалість ≥ 45 хв | `longTrainings` | `filter` → `toList` | `List<Training>` |
| Перетворення: дистанції | `distances` | `map(getDistanceKm)` → `toList` | `List<Double>` |
| Групування: сума км за видом | `distanceByKind` | `groupingBy(getType, EnumMap, summingDouble)` | `Map<TrainingKind, Double>` |
| Статистика калорій* | `caloriesStatistics` | `summarizingInt(getCalories)` | `IntSummaryStatistics` |
| Top-5: дистанція ↓, дата ↑ | `topByDistance` | `sorted(comparingDouble.reversed().thenComparing)` → `limit(n)` | `List<Training>` |
| Пошук за датою | `findByDate` | `filter` → `findFirst` | `Optional<Training>` |

\* Поле «пульс» варіанта замінено на наявне «калорії», щоб не змінювати формат CSV.
Контрольний набір — `data/queries.csv` (7 записів: межа 45 хв, дубль дати 2026-09-02, рівні 8.5 км).

## ЛР № 5 — параметризоване сховище, винятки й CSV-експорт

Коректні записи кладуться у `Repository<Training>`, експортуються універсальним рефлексивним
`CsvExporter` у UTF-8 CSV, читаються назад документним `CsvParser` і порівнюються через `equals`:

```bash
java -jar target/lab01-5.0.0.jar --input data/queries.csv --export out/trainings.csv
```

| Компонент | Призначення |
|---|---|
| `Repository<T>` | `add`, `addAll`, `all()` (незмінний знімок), `find(Predicate<? super T>)` |
| `@CsvColumn` | `RUNTIME`, `FIELD`, `String value()` — назва стовпця |
| `CsvExporter` | поля з `@CsvColumn` по ланцюжку класів, сортування за іменем, екранування, `Files.writeString` UTF-8 |
| `CsvParser` | машина станів: коми, лапки й перенесення рядків усередині лапок, `\n` / `\r\n` |
| `Training.fromCsvFields`, `Trainings.readCsv` | предметне відновлення записів (ключ round-trip — дата + вид) |
| `DataStorageException` | перевірюваний виняток із `cause` (`IOException`, `NumberFormatException`, `IllegalArgumentException`) |

Заголовок CSV: `калорії,дата,дистанція,вид,тривалість` (порядок — Java-імена `calories, date, distanceKm, kind, minutes`).
Помилка експорту/імпорту → код завершення 1 і рядок «Причина: …». Основний звіт не змінився.
