# Спортивні тренування — лабораторна робота № 1 (варіант 2)

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

## Збірка та запуск
```text
./mvnw clean verify                         # тести + SpotBugs + JAR
java -jar target/lab01-1.0.0.jar --help
java -jar target/lab01-1.0.0.jar --version  # 1.0.0+local
java -jar target/lab01-1.0.0.jar
java -jar target/lab01-1.0.0.jar --input data/input.csv --output out/report.txt
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
