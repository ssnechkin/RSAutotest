package controllers;

import modules.configuration.interfaces.ProgramSettings;
import modules.logger.interfaces.RSLogger;
import modules.testExecutor.AddStepsFromRelatedTests;
import modules.testExecutor.CreatorThreads;
import modules.testExecutor.TestSeparator;
import modules.testExecutor.ThreadStarter;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.templates.RSTests;
import modules.testExecutor.testReaders.TestsReaderFromFile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Класс контроллер. Выполняет переданные тесты.
 *
 * @author nechkin.sergei.sergeevich
 */
public class TestExecutorThreadCtrl {

    /**
     * Конструктор
     *
     * @param executorService       Cервис для выполнения потоков
     * @param reportExecutorService Cервис для выполнения потоков. Для отчетов. Один поток.
     * @param rsTests               Объект с деревом тестов
     * @param suites                Лист выполянемых наборов. Если пуст, то выполняются все наборы. (Краткое наименование набора)
     * @param tests                 Лист выполянемых тестов. Если пуст, то выполняются все тесты. (Краткое наименование теста)
     * @param programSettings       Настройки программы
     * @param exitProgram           Выключать программу после завершения всех тестов
     * @param rsLogger              Класс RSLogger - логирование
     * @throws Exception Ошибка чтения файла
     */
    public TestExecutorThreadCtrl(ExecutorService executorService, ExecutorService reportExecutorService,
                                  RSTests rsTests, CopyOnWriteArrayList<String> suites, CopyOnWriteArrayList<String> tests,
                                  ProgramSettings programSettings, Boolean exitProgram, RSLogger rsLogger) throws Throwable {

        // Дополнить шаги тестов шагами из связанных тестов
        AddStepsFromRelatedTests addStepsFromRelatedTests = new AddStepsFromRelatedTests(rsTests, new TestsReaderFromFile());
        rsTests = addStepsFromRelatedTests.getRsTests();

        // Распределение тестов по отдельными наборам
        TestSeparator testSeparator = new TestSeparator(rsTests, rsLogger.getLogger());

        // Карта для запуска потоков
        ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls = new ConcurrentHashMap<>();

        // Содержит флаг для завершения всех наборов с тестами. Флаг добавится после завершения наборов.
        ConcurrentHashMap<String, Boolean> allSuitesFinishedMap = new ConcurrentHashMap<>();

        // Создание списка потоков
        CreatorThreads creatorThreads = new CreatorThreads(mapOfTestCalls, testSeparator.getSuitesMap(), suites, tests, programSettings, rsLogger, reportExecutorService, allSuitesFinishedMap);

        // Добавитьобъект для вызова после завершения теста
        mapOfTestCalls.put("ThreadStarter", new ThreadStarter(executorService, reportExecutorService, creatorThreads.getThreads(), exitProgram));

        // Запустить выполнение потоков
        mapOfTestCalls.get("ThreadStarter").start();
    }
}
