package modules.testExecutor;

import modules.commandLine.DiagnosticSignalHandler;
import modules.commandLine.SignalExecutorServiceHandlerRun;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.TestThread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс для запуска потоков со сценариями.
 *
 * @author nechkin.sergei.sergeevich
 */
public class ThreadStarter implements CalledFromTest {
    private ExecutorService executorService, oneExecutorService, reportExecutorService;
    private ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads;
    private Boolean notify = false;
    private ConcurrentHashMap<Thread, Long> runThreads = new ConcurrentHashMap<>();
    private Boolean exitProgram;
    private Boolean suiteFailure = false;
    private Report report;

    /**
     * Конструктор
     *
     * @param executorService       Cервис для выполнения потоков
     * @param reportExecutorService Cервис для выполнения потоков в отчётах. Тут нужен для его завершения.
     * @param testThreads           Список с потоками сценариев тестирования <id набора, потоки>
     * @param exitProgram           выключать программу после завершения всех тестов
     */
    public ThreadStarter(ExecutorService executorService, ExecutorService reportExecutorService, ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads, Boolean exitProgram) {
        this.executorService = executorService;
        this.reportExecutorService = reportExecutorService;
        this.testThreads = testThreads;
        this.exitProgram = exitProgram;

        // Остановить сервисы если выполнена остановка процесса
        SignalExecutorServiceHandlerRun signalHandler = new SignalExecutorServiceHandlerRun(executorService, oneExecutorService, reportExecutorService);
        DiagnosticSignalHandler.install("TERM", signalHandler);
        DiagnosticSignalHandler.install("INT", signalHandler);
        DiagnosticSignalHandler.install("ABRT", signalHandler);
    }

    @Override
    public void start(Report report) {
        this.report = report;
        start();
    }

    /**
     * Выполняет запуск потоков
     */
    @Override
    public synchronized void start() {
        if (oneExecutorService == null)
            oneExecutorService = Executors.newFixedThreadPool(1);

        execute(true);
        cleaSuspendFlag();

        synchronized (notify) {
            notify.notify();
        }

        if (!executorService.isShutdown()) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    execute(false);
                }
            });
            oneExecutorService.execute(thread);
        }
    }


    /**
     * Запускает потоки
     *
     * @param wakeTheSleeping если true, то проверить все тесты и возобновить потоки если они
     *                        приостановлены. Иначе будет выполнен запуск потока
     * @return true, если есть запущенные или разбуженные тесты
     */
    private boolean execute(boolean wakeTheSleeping) {
        String message;
        TestDatas testDatas, testDependencyDatas;
        CopyOnWriteArrayList<TestThread> testList;
        TestStatus expectedStatus, currentStatus;
        ConcurrentHashMap<Long, TestStatus> dependencyMap;
        boolean allTestsWerePerformed = true;

        suiteFailure = false;

        // Цикл по наборам <id набора, список тестов в наборе>
        for (Map.Entry<Long, CopyOnWriteArrayList<TestThread>> suites : testThreads.entrySet()) {
            testList = suites.getValue();

            // Цикл по тестам в наборе
            for (TestThread testThread : testList) {
                testDatas = testThread.getSuitesMap().get(testThread.getSuiteId()).getTestsMap().get(testThread.getTestId());
                dependencyMap = testDatas.getStartupDependency();

                // Если тест ожидает запуска
                if (testDatas.getStatus().equals(TestStatus.EXPECTS)) {

                    // Если у теста есть зависимости, то проверить статусы зависимости, иначе запустить тест
                    if (dependencyMap.size() > 0) {
                        boolean start = true;
                        // Цикл по зависимостям
                        for (Map.Entry<Long, TestStatus> dependency : dependencyMap.entrySet()) {
                            expectedStatus = dependency.getValue();

                            // В наборе поиск теста из зависимости
                            for (TestThread test : testList) {

                                // Если id теста из зависимости совпадает с id теста из набора
                                if (dependency.getKey().equals(test.getTestId())) {
                                    testDependencyDatas = test.getSuitesMap().get(test.getSuiteId()).getTestsMap().get(test.getTestId());
                                    currentStatus = testDependencyDatas.getStatus();

                                    if (!wakeTheSleeping) {
                                        // Получить сообщение для отмены теста
                                        message = getMessageForCanceledTest(expectedStatus, currentStatus);

                                        // Если есть сообщение для отмены теста, то отменить тест и запустить поток для записи в отчёт
                                        if (message != null) {
                                            testDatas.setErrorMessage(message + " (" + testDependencyDatas.getName() + ")");
                                            testDatas.setThreadSuspended(false);
                                            testDatas.setStatus(TestStatus.CANCELLED);
                                            testDatas.setThreadRun(true);
                                            startThread(testThread.getTestScriptThread(), testDatas);
                                            return true;
                                        }
                                    }

                                    // Если пока не нужно запускать тест, то установить флаг
                                    if (!isStartThread(expectedStatus, currentStatus)) start = false;
                                }
                            }
                        }

                        // Если нужно запустить тест, то запустить поток с тестом
                        if (start) {
                            if (wakeTheSleeping) {
                                if (testDatas.isThreadSuspended()) {
                                    testDatas.setThreadSuspended(false);
                                    testDatas.setStatus(TestStatus.RUN);
                                }
                            } else {
                                startThread(testThread.getTestScriptThread(), testDatas);
                                return true;
                            }
                        }

                    } else {
                        if (!wakeTheSleeping) {
                            // Запустить поток с тестом
                            startThread(testThread.getTestScriptThread(), testDatas);
                            return true;
                        }
                    }
                }

                // Если есть выполняющийся тест, то установить флаг
                if (testDatas.isThreadRun() && !wakeTheSleeping) allTestsWerePerformed = false;

                // Если тест генерирует отчёт, то установить флаг есть выполняющийся тест
                if (!testDatas.isThreadRun() && testDatas.isReportGenerated()) allTestsWerePerformed = false;

                // Еслс есть неудачные тесты то установить флаг неудачного набора для выхода из программы
                if (testDatas.getStatus().equals(TestStatus.BROKEN) || testDatas.getStatus().equals(TestStatus.FAILURE))
                    suiteFailure = true;
            }
        }

        // Eсли все тесты выполнены, то остановить ExecutorService
        if (allTestsWerePerformed && !executorService.isShutdown() && !wakeTheSleeping) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isAliveOneThread()) {
                //if (report != null) report.allSuiteFinished();
                executorService.shutdown();
                reportExecutorService.shutdown();
                oneExecutorService.shutdown();
                if (exitProgram) {
                    if (suiteFailure) {
                        System.exit(1);
                    } else {
                        System.exit(0);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Проверяет наличие выполняющегося теста/потока
     *
     * @return true, если есть выполняющиеся тесты
     */
    private synchronized boolean isAliveOneThread() {
        CopyOnWriteArrayList<TestThread> testList;
        TestDatas testDatas;

        // Цикл по наборам <id набора, список тестов в наборе>
        for (Map.Entry<Long, CopyOnWriteArrayList<TestThread>> suites : testThreads.entrySet()) {
            testList = suites.getValue();

            // Цикл по тестам в наборе
            for (TestThread testThread : testList) {
                testDatas = testThread.getSuitesMap().get(testThread.getSuiteId()).getTestsMap().get(testThread.getTestId());
                if (testDatas.isThreadRun()
                        || (testDatas.getStatus() != null && testDatas.getStatus().equals(TestStatus.RUN))
                        || (testDatas.getStatus() != null && testDatas.getStatus().equals(TestStatus.EXPECTS))
                        ) return true;
            }
        }
        return false;
    }

    /**
     * Снимает флаг приостановки теста, у теста без зависимости, если все тесты выполнены
     */
    private void cleaSuspendFlag() {
        CopyOnWriteArrayList<TestThread> testList;
        TestDatas testDatas;
        boolean pressSuspendedTest = false;
        boolean pressRunTest = false;

        // Цикл по наборам <id набора, список тестов в наборе>
        for (Map.Entry<Long, CopyOnWriteArrayList<TestThread>> suites : testThreads.entrySet()) {
            testList = suites.getValue();

            // Цикл по тестам в наборе
            for (TestThread testThread : testList) {
                testDatas = testThread.getSuitesMap().get(testThread.getSuiteId()).getTestsMap().get(testThread.getTestId());

                // Проверка на наличие приостановленного теста
                if (testDatas.isThreadRun() && testDatas.isThreadSuspended())
                    pressSuspendedTest = true;

                // Проверка на наличие выполняющихся или ожидающих тестов
                if (testDatas.isThreadRun() && !testDatas.isThreadSuspended()) {
                    pressRunTest = true;
                    break;
                }
            }
            if (pressRunTest) break;
        }

        // Если все тесты выполнены, но есть приостановленные без зависимости, то убрать флаг приостановки
        if (!pressRunTest && pressSuspendedTest) {
            // Цикл по наборам <id набора, список тестов в наборе>
            for (Map.Entry<Long, CopyOnWriteArrayList<TestThread>> suites : testThreads.entrySet()) {
                testList = suites.getValue();

                // Цикл по тестам в наборе
                for (TestThread testThread : testList) {
                    testDatas = testThread.getSuitesMap().get(testThread.getSuiteId()).getTestsMap().get(testThread.getTestId());

                    if (testDatas.isThreadRun() && testDatas.isThreadSuspended() && testDatas.getStartupDependency().size() == 0) {
                        testDatas.setThreadSuspended(false);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Возвращает true, если поток с тестом нужно запустить
     *
     * @param expectedStatus Ожидаемый статус
     * @param currentStatus  Текущий статус
     * @return boolean результат проверки
     */
    private boolean isStartThread(TestStatus expectedStatus, TestStatus currentStatus) {
        // если тест из зависимости (найденный) ещё не запускался
        // или ещё выполняется или отменён или заблокирован, то не выполнять
        if (currentStatus.equals(TestStatus.EXPECTS)
                || (currentStatus.equals(TestStatus.RUN) && !expectedStatus.equals(TestStatus.RUN))
                || currentStatus.equals(TestStatus.LOCKED)) return false;

        // если ожидается положительное завершение, но тест отменён или сломан или завершён отрицательно, то не выполнять
        if (expectedStatus.equals(TestStatus.SUCCESSFUL) && (
                currentStatus.equals(TestStatus.CANCELLED)
                        || currentStatus.equals(TestStatus.BROKEN)
                        || currentStatus.equals(TestStatus.FAILURE)
        )) return false;

        // если ожидается отмена теста, но тест завешён положительно или сломан или завершён отрицательно, то не выполнять
        if (expectedStatus.equals(TestStatus.CANCELLED) && (
                currentStatus.equals(TestStatus.SUCCESSFUL)
                        || currentStatus.equals(TestStatus.BROKEN)
                        || currentStatus.equals(TestStatus.FAILURE)
        )) return false;

        // если ожидается поломка теста, но тест завешён положительно или отменён или завершён отрицательно, то не выполнять
        if (expectedStatus.equals(TestStatus.BROKEN) && (
                currentStatus.equals(TestStatus.SUCCESSFUL)
                        || currentStatus.equals(TestStatus.CANCELLED)
                        || currentStatus.equals(TestStatus.FAILURE)
        )) return false;

        // если ожидается отрицательное завершение теста, но тест завешён положительно или отменён или сломан, то не выполнять
        if (expectedStatus.equals(TestStatus.FAILURE) && (
                currentStatus.equals(TestStatus.SUCCESSFUL)
                        || currentStatus.equals(TestStatus.CANCELLED)
                        || currentStatus.equals(TestStatus.BROKEN)
        )) return false;

        // если ожидается завершение теста, но тест отменён, то не выполнять
        if (expectedStatus.equals(TestStatus.COMPLETED) && currentStatus.equals(TestStatus.CANCELLED)) return false;

        return true;
    }

    /**
     * Возвращает текст сообщения для отмены теста
     *
     * @param expectedStatus Ожидаемый статус
     * @param currentStatus  Текущий статус
     * @return Текст сообщения для отмены теста, или null
     */
    private String getMessageForCanceledTest(TestStatus expectedStatus, TestStatus currentStatus) {

        // Если ожидается завершение теста, но тест отменён или блокирован, то отменить текущий тест
        if (expectedStatus.equals(TestStatus.COMPLETED)
                && (currentStatus.equals(TestStatus.CANCELLED) || currentStatus.equals(TestStatus.LOCKED))) {
            return "Ожидалось выполнение родительского теста, но его статус: " + currentStatus;
        }

        // Если ожидается выполнение теста, но тест выполнен, то отменить текущий тест
        if (expectedStatus.equals(TestStatus.RUN)
                && (currentStatus.equals(TestStatus.SUCCESSFUL)
                || currentStatus.equals(TestStatus.CANCELLED)
                || currentStatus.equals(TestStatus.BROKEN)
                || currentStatus.equals(TestStatus.FAILURE)
                || currentStatus.equals(TestStatus.LOCKED))) {
            return "Ожидался запуск теста, но его статус: " + currentStatus;
        }

        // Если ожидается неудачное выполнение теста, но тест выполнен успешно, то отменить текущий тест
        if (expectedStatus.equals(TestStatus.FAILURE)
                && (currentStatus.equals(TestStatus.SUCCESSFUL)
                || currentStatus.equals(TestStatus.CANCELLED)
                || currentStatus.equals(TestStatus.BROKEN)
                || currentStatus.equals(TestStatus.LOCKED))) {
            return "Ожидалось негативное выполнение родительского теста, но его статус: " + currentStatus;
        }

        // Если ожидается успешное выполнение теста, но тест выполнен неуспешно, то отменить текущий тест
        if (expectedStatus.equals(TestStatus.SUCCESSFUL)
                && (currentStatus.equals(TestStatus.CANCELLED)
                || currentStatus.equals(TestStatus.BROKEN)
                || currentStatus.equals(TestStatus.FAILURE)
                || currentStatus.equals(TestStatus.LOCKED))) {
            return "Ожидалось успешное выполнение родительского теста, но его статус: " + currentStatus;
        }

        // Если ожидается сломанный тест, но тест не сломан, то отменить текущий тест
        if (expectedStatus.equals(TestStatus.BROKEN)
                && (currentStatus.equals(TestStatus.SUCCESSFUL)
                || currentStatus.equals(TestStatus.CANCELLED)
                || currentStatus.equals(TestStatus.FAILURE)
                || currentStatus.equals(TestStatus.LOCKED))) {
            return "Ожидалась поломка родительского теста, но его статус: " + currentStatus;
        }

        // Если ожидается отменённый теста, но тест не отменён, то отменить текущий тест
        if (expectedStatus.equals(TestStatus.CANCELLED)
                && (currentStatus.equals(TestStatus.SUCCESSFUL)
                || currentStatus.equals(TestStatus.BROKEN)
                || currentStatus.equals(TestStatus.FAILURE)
                || currentStatus.equals(TestStatus.LOCKED))) {
            return "Ожидалась отмена родительского теста, но его статус: " + currentStatus;
        }

        return null;
    }

    /**
     * Добавляет поток в ExecutorService и ожидает начало выполнения теста или падение потока.
     *
     * @param thread    Поток с тестом.
     * @param testDatas Тестовые данные для получения статуса теста из потока.
     */
    private void startThread(Thread thread, TestDatas testDatas) {
        if (!thread.isAlive() && runThreads.get(thread) == null) {
            if (!testDatas.getStatus().equals(TestStatus.CANCELLED))
                testDatas.setStatus(TestStatus.RUN);
        }

        synchronized (runThreads) {
            if (!thread.isAlive() && runThreads.get(thread) == null) {
                /*
                if (!testDatas.getStatus().equals(TestStatus.CANCELLED))
                    testDatas.setStatus(TestStatus.RUN);
*/
                executorService.execute(thread);
                runThreads.put(thread, testDatas.getId());

                synchronized (notify) {
                    while (!testDatas.isThreadRun()) {
                        try {
                            notify.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }
}
