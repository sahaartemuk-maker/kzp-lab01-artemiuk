package ua.lpnu.kzp;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/** Консольна програма обліку спортивних тренувань (варіант 2). */
public final class Main {

    private static final String USAGE =
            "Використання: java -jar lab01-3.0.0.jar [--help] [--version] [--input <файл>] [--output <файл>]%n"
            + "  --input   вхідний CSV (за замовчуванням data/input.csv)%n"
            + "  --output  файл звіту (за замовчуванням out/report.txt)%n";

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
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg)) {
                out.printf(USAGE);
                return 0;
            } else if ("--input".equals(arg) && i + 1 < args.length) {
                input = Path.of(args[++i]);
            } else if ("--output".equals(arg) && i + 1 < args.length) {
                output = Path.of(args[++i]);
            } else {
                out.printf("Невідомий або неповний аргумент: %s%n", arg);
                out.printf(USAGE);
                return 2;
            }
        }
        try {
            List<String> lines = FileReport.readLines(input);
            String report = ReportFormatter.format(TrainingStats.compute(lines));
            out.print(report);
            FileReport.writeReport(output, report);
            out.printf("%nЗвіт записано у %s%n", output);
            return 0;
        } catch (IOException exception) {
            out.printf("Помилка роботи з файлом: %s%n", exception.getMessage());
            return 1;
        }
    }
}
