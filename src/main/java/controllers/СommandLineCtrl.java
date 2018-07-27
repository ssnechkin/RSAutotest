package controllers;

import modules.commandLine.ArgumentReader;
import modules.commandLine.interfaces.CommandLlineExecuted;
import modules.commandLine.printers.PrintHelp;
import modules.configuration.interfaces.ProgramSettings;
import modules.logger.interfaces.RSLogger;
import modules.server.ServerStarter;
import modules.testExecutor.testReaders.TestsReaderFromFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * Класс контроллер. Выполняет команды переданные jar-файлу
 *
 * @author nechkin.sergei.sergeevich
 */
public class СommandLineCtrl {

    /**
     * Конструктор
     *
     * @param args            Аргументы коммандной строки переданные jar-файлу
     * @param programSettings Настройки программы
     * @param rsLogger        Класс RSLogger - логирование
     */
    public СommandLineCtrl(String[] args,
                           ProgramSettings programSettings, RSLogger rsLogger) {

        ConcurrentHashMap<String, CommandLlineExecuted> runningCommandLineMap = getCommandLineMap();
        String[] executableCommands = new String[runningCommandLineMap.size()];
        TestsReaderFromFile testsReaderFromFile = new TestsReaderFromFile();
        CopyOnWriteArrayList<String> suites;
        CopyOnWriteArrayList<String> tests;
        int i = 0;

        // Получить массив коммандных слов
        for (Map.Entry<String, CommandLlineExecuted> command : runningCommandLineMap.entrySet()) {
            executableCommands[i] = command.getKey();
            i++;
        }
        // Прочитать аргументы
        ArgumentReader argumentReader = new ArgumentReader(args, executableCommands, programSettings.getAllSettings());

        // Обновить настройки программы настройками из аргументов
        for (Map.Entry<String, String> string : argumentReader.getSettings().entrySet())
            programSettings.update(string.getKey(), string.getValue());


        //if (argumentReader.getAllArguments().contains("NOT_READ_PLUGINS")) {

        // Если команды не переданы, но вывести в консоль текст из PrintHelp
        if (argumentReader.getCommands().size() == 0 && argumentReader.getFiles().size() == 0) {
            new PrintHelp().execute(programSettings, rsLogger);
        }

        // Выполнить команду из аргумента
        for (String command : argumentReader.getCommands())
            if (runningCommandLineMap.get(command) != null)
                runningCommandLineMap.get(command).execute(programSettings, rsLogger);

        // Выполнить файлы с тестами
        for (String fileName : argumentReader.getFiles()) {

            rsLogger.getLogger().info("Чтение тестов из файла:");
            rsLogger.getLogger().info("=======================");
            rsLogger.getLogger().info(fileName);

            suites = argumentReader.getFileSuites(fileName);
            tests = argumentReader.getFileTests(fileName);

            if (suites.size() > 0) {
                rsLogger.getLogger().info("");
                rsLogger.getLogger().info("Выполняемые наборы:");
                rsLogger.getLogger().info("------------------");
                for (String suiteShortName : suites) rsLogger.getLogger().info(suiteShortName);
            }

            if (tests.size() > 0) {
                rsLogger.getLogger().info("");
                rsLogger.getLogger().info("Выполняемые тесты:");
                rsLogger.getLogger().info("------------------");
                for (String testShortName : tests) rsLogger.getLogger().info(testShortName);
            }

            rsLogger.getLogger().info("");

            try {
                new TestExecutorThreadCtrl(Executors.newFixedThreadPool(programSettings.getInteger("MaximumThreads")), Executors.newFixedThreadPool(1), testsReaderFromFile.read(fileName), suites, tests, programSettings, true, rsLogger);

            } catch (Throwable throwable) {
                rsLogger.getLogger().info("Ошибка чтения файла: " + fileName);
                rsLogger.printStackTrace(throwable);
            }
        }

       /* } else {
            try {
                String line;
                final BufferedReader ir, bre;

                Process proc = Runtime.getRuntime().exec("java -jar RSAutotest.jar NOT_READ_PLUGINS " + argumentReader.getAllArguments());

                SignalHandler signalHandler = new SignalHandlerRun(proc);
                DiagnosticSignalHandler.install("TERM", signalHandler);
                DiagnosticSignalHandler.install("INT", signalHandler);
                DiagnosticSignalHandler.install("ABRT", signalHandler);

                *//*final PrintStream out = new PrintStream(new File("out.txt"));
                System.setOut(out);
                System.setErr(out);*//*

                ir = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                bre = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                while (null != (line = ir.readLine())) {
                    System.out.println(line);
                }
                ir.close();
                while (null != (line = bre.readLine())) {
                    System.out.println(line);
                }
                bre.close();
                proc.waitFor();

                *//*ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;

                while ((length = proc.getInputStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                    System.out.println(result.toString("UTF-8"));
                }

                result = new ByteArrayOutputStream();
                while ((length = proc.getErrorStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                    System.out.println(result.toString("UTF-8"));
                }*//*

            } catch (final Exception err) {
                System.err.println(err.getLocalizedMessage());
            }
        }*/
    }

    private ConcurrentHashMap<String, CommandLlineExecuted> getCommandLineMap() {
        ConcurrentHashMap<String, CommandLlineExecuted> resultMap = new ConcurrentHashMap<>();

        //resultMap.put("NOT_READ_PLUGINS", new PrintHelp());

        resultMap.put("HELP", new PrintHelp());
        resultMap.put("-HELP", new PrintHelp());
        resultMap.put("-help", new PrintHelp());
        resultMap.put("SERVER", new ServerStarter());

        return resultMap;
    }
}
