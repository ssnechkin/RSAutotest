package controllers;

import modules.testExecutor.Report;
import modules.testExecutor.StepExecutor;
import modules.testExecutor.StepsReader;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.TestThread;
import plugins.PluginReader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Класс контроллер для чтения и запуска шагов из потока.
 * Контролируется запуск приостановленного потока.
 *
 * @author nechkin.sergei.sergeevich
 */
public class TestThreadCtrl extends Thread {
    private long suiteId;
    private long testId;
    private ConcurrentHashMap<Long, SuiteDatas> suitesMap;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private ConcurrentHashMap<String, Boolean> allSuitesFinishedMap;
    private ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads;
    private ExecutorService executorService;

    /**
     * Конструктор
     *
     * @param suiteId         Идентификатор набора
     * @param testId          Идентификатор теста
     * @param suitesMap       Карта с данными набора и теста
     * @param mapOfTestCalls  Карта для вызова объектов из потока
     * @param testThreads     Список потоков с тестами. Содержит все наборы
     * @param executorService Общий выполнитель для отчётов. Один поток
     */
    public TestThreadCtrl(Long suiteId, Long testId, ConcurrentHashMap<Long, SuiteDatas> suitesMap, ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls, ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads, ExecutorService executorService, ConcurrentHashMap<String, Boolean> allSuitesFinishedMap) {
        this.suiteId = suiteId;
        this.testId = testId;
        this.suitesMap = suitesMap;
        this.mapOfTestCalls = mapOfTestCalls;
        this.testThreads = testThreads;
        this.executorService = executorService;
        this.allSuitesFinishedMap = allSuitesFinishedMap;
    }

    @Override
    public void run() {
        SuiteDatas suite = suitesMap.get(suiteId);
        TestDatas test = suite.getTestsMap().get(testId);
        Boolean threadSuspended = false;
        Logger logger = test.getLogger();

        test.setThreadRun(true);

        synchronized (mapOfTestCalls) {
            mapOfTestCalls.get("ThreadStarter").start();
        }

        PluginReader pluginReader = new PluginReader();

        // Создать писателей отчётов
        Report report = new Report(pluginReader.getReportWriters(), suite, test, testThreads, executorService);

        // Создать исполнителя теста
        StepExecutor stepExecutor = new StepExecutor(pluginReader.getTestExecutors(), pluginReader.getDataHandlers(), suite, test, logger, threadSuspended, mapOfTestCalls);

        // Прочитать шаги и передать их отчётам и исполнителям
        new StepsReader(suite, test, report, stepExecutor);

        //Закрыть исполнителя теста и лог
        stepExecutor.close();

        test.setReportGenerated(true);
        test.setThreadRun(false);

        // Завершены все наборы
        synchronized (allSuitesFinishedMap) {
            if (allSuitesFinishedMap.size() == 0) {
                Boolean allSuitesFinished = true;

                // Цикл по наборам <id набора, список тестов в наборе>
                for (Map.Entry<Long, CopyOnWriteArrayList<TestThread>> threads : testThreads.entrySet()) {
                    // Цикл по тестам в наборе
                    for (TestThread testThread : threads.getValue()) {
                        TestDatas testDatas = testThread.getSuitesMap().get(testThread.getSuiteId()).getTestsMap().get(testThread.getTestId());
                        if (testDatas.isThreadRun() || testDatas.getStatus().equals(TestStatus.EXPECTS)) {
                            allSuitesFinished = false;
                            break;
                        }
                    }
                    if (!allSuitesFinished) break;
                }

                if (allSuitesFinished) {
                    allSuitesFinishedMap.put("allSuitesFinished", true);
                    suite.getProgramLogger().info("");
                    suite.getProgramLogger().info("Завершены все тесты.");
                    report.allSuiteFinished();

                    /*// Закрыть логер программы
                    Handler[] handler = suite.getProgramLogger().getHandlers();
                    for (Handler h : handler) {
                        h.close();
                    }*/
                }
            }

            test.setReportGenerated(false);

            synchronized (threadSuspended) {
                threadSuspended.notify();
                // Запустить выполнение потоков. Сигнал о завершении теста
                mapOfTestCalls.get("ThreadStarter").start(report);
            }
        }
        //mapOfTestCalls.get("ThreadStarter").start(report);
    }
}
