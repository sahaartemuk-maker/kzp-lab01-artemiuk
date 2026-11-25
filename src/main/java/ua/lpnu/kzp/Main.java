package ua.lpnu.kzp;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Консольна програма обліку спортивних тренувань (варіант 2). */
public final class Main {

    private static final String USAGE =
            "Використання: java -jar lab01-5.0.0.jar [--help] [--version] [--input <файл>] [--output <файл>] [--queries <дата>] [--export <файл>]%n"
            + "  --input   вхідний CSV (за замовчуванням data/input.csv)%n"
            + "  --output  файл звіту (за замовчуванням out/report.txt)%n"
            + "  --queries додатково вивести запити Stream API; <дата> (рррр-мм-дд) — для пошуку%n"
            + "  --export  експортувати коректні записи в CSV (UTF-8) і перевірити round-trip%n";

    /* Забороняє створення екземплярів службового класу. */
    private Main() {
    }

    /**
     * Точка входу до програми.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        System.exit(run(args, out));
    }

    /**
     * Виконує програму й повертає код завершення; виділено для тестування.
     *
     * @param args аргументи командного рядка
     * @param out потік для виводу
     * @return 0 — успіх, 1 — помилка вводу/виводу, 2 — неправильні аргументи
     */
    public static int run(String[] args, PrintStream out) {
        if (args.length == 1 && "--version".equals(args[0])) {
            out.printf("%s%n", BuildInfo.version());
            return 0;
        }
        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");
        LocalDate searchDate = null;
        Path export = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg)) {
                out.printf(USAGE);
                return 0;
            } else if ("--input".equals(arg) && i + 1 < args.length) {
                input = Path.of(args[++i]);
            } else if ("--output".equals(arg) && i + 1 < args.length) {
                output = Path.of(args[++i]);
            } else if ("--queries".equals(arg) && i + 1 < args.length) {
                try {
                    searchDate = LocalDate.parse(args[++i]);
                } catch (DateTimeParseException exception) {
                    out.printf("Некоректна дата: %s%n", args[i]);
                    return 2;
                }
            } else if ("--export".equals(arg) && i + 1 < args.length) {
                export = Path.of(args[++i]);
            } else {
                out.printf("Невідомий або неповний аргумент: %s%n", arg);
                out.printf(USAGE);
                return 2;
            }
        }
        try {
            List<String> lines = FileReport.readLines(input);
            TrainingStats stats = TrainingStats.compute(lines);
            String report = ReportFormatter.format(stats);
            out.print(report);
            if (searchDate != null) {
                out.printf("%n");
                out.print(QueryReportFormatter.format(stats.trainings(), searchDate));
            }
            FileReport.writeReport(output, report);
            out.printf("%nЗвіт записано у %s%n", output);
            if (export != null) {
                exportAndVerify(export, stats.trainings(), out);
            }
            return 0;
        } catch (IOException exception) {
            out.printf("Помилка роботи з файлом: %s%n", exception.getMessage());
            return 1;
        } catch (DataStorageException exception) {
            out.printf("Помилка CSV-експорту: %s%n", exception.getMessage());
            if (exception.getCause() != null) {
                out.printf("Причина: %s%n", exception.getCause());
            }
            return 1;
        }
    }

    /* Кладе записи у сховище, експортує їх у CSV, читає назад і порівнює. */
    private static void exportAndVerify(Path path, List<Training> trainings, PrintStream out)
            throws DataStorageException {
        Repository<Training> repository = new Repository<>();
        repository.addAll(trainings);
        CsvExporter.write(path, Training.class, repository.all());
        List<Training> restored = Trainings.readCsv(path);
        boolean equal = repository.all().equals(restored);
        out.printf("CSV-експорт: %s (%d записів)%n", path, repository.size());
        out.printf("Round-trip: відновлено %d записів, рівність: %s%n",
                restored.size(), equal ? "так" : "ні");
    }
}
