package plugins.executers;

import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import plugins.interfaces.TestExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class WaitTestExecuter implements TestExecutor {
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<String, String> stepsMap = new ConcurrentHashMap<>();// <Наименование шага, Описание шага>
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private Logger logger;
    private Boolean threadSuspended = false;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private String programFilesDirectory;

    @Override
    public String getPluginName() {
        return "WaitTest";
    }

    @Override
    public String getGroupName() {
        return "Приостановка теста";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        return null;
    }

    @Override
    public void set(SuiteDatas suite, TestDatas test, Boolean threadSuspended, ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls, String programFilesDirectory, ConcurrentHashMap<String, String> settings) {
        this.suite = suite;
        this.test = test;
        this.stringDataMap = test.getStringDataMap();
        this.byteDataMap = test.getByteDataMap();
        this.threadSuspended = threadSuspended;
        this.mapOfTestCalls = mapOfTestCalls;
        this.programFilesDirectory = programFilesDirectory;
        this.logger = test.getLogger();
    }

    @Override
    public ConcurrentHashMap<String, String> getAllStepsMap() {
        if (stepsMap.size() == 0) execute(null);
        return stepsMap;
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;

        if (step != null) stepName = step.getStep();

        name = "Ожидание в миллисекундах";
        if (mapEmpty) {
            description = "Приостанавливает тест на указанное время. Пример: Шаг = Ожидание в миллисекундах; Значение = 1000";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            waitMilliseconds(step);
            return;
        }

        name = "Ожидать завершения выполнения теста с данными";
        if (mapEmpty) {
            description = "Приостанавливает тест до завершения указанного теста с данными. Ключ можно не указывать. Пример: 1. Шаг = Ожидать завершения выполнения теста с данными; Ключ = Наименование теста где хранятся данные; Значение = Наименование теста в наборе после завершения которого продолжить текущий тест";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            waitComplete(step);
            return;
        }

        name = "Ожидать положительного завершения выполнения теста с данными";
        if (mapEmpty) {
            description = "Приостанавливает тест до успешного выполнения указанного теста с данными. Ключ можно не указывать. Пример: 1. Шаг = Ожидать положительного завершения выполнения теста с данными; Ключ = Наименование теста где хранятся данные; Значение = Наименование теста в наборе после успешного выполнения которого продолжить текущий тест";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            waitPositive(step);
            return;
        }

        name = "Ожидать отрицательного завершения выполнения теста с данными";
        if (mapEmpty) {
            description = "Приостанавливает тест до падения указанного теста с данными. Ключ можно не указывать. Пример: 1. Шаг = Ожидать отрицательного завершения выполнения теста с данными; Ключ = Наименование теста где хранятся данные; Значение = Наименование теста в наборе после падения которого продолжить текущий тест";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            waitNegative(step);
            return;
        }

        name = "Приостановить тест и ожидать сигнала на запуск от других тестов";
        if (mapEmpty) {
            description = "Приостанавливает текущий тест до получения сигнала на запуск из других тестов. В случае отсутсвия сигнала после завершения всех тестов, текущий тест будет продолжен автоматически. Пример: 1. Шаг = Приостановить тест и ожидать сигнала на запуск от других тестов;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            suspenTest(step);
            return;
        }

        name = "Разбудить тест с данными";
        if (mapEmpty) {
            description = "Продолжает выполнение приостановленного теста указанного в значении. Пример: 1. Шаг = Разбудить тест с данными; Ключ = Наименование теста где хранятся данные; Значение = Наименование теста в наборе который должен быть разбужен";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            toWakeUpTest(step);
            return;
        }

    }

    private void toWakeUpTest(RSTests step) {
        TestDatas testDatas = getTest(step);
        if (testDatas != null) {
            if (testDatas.getDependingOnTheTestsList().size() == 0) {
                testDatas.setStatus(TestStatus.RUN);
                testDatas.setThreadSuspended(false);

                synchronized (threadSuspended) {
                    threadSuspended.notify();
                }
                mapOfTestCalls.get("ThreadStarter").start();
            } else {
                step.setErrorMessage("Тест не разбужен, поскольку он ожидает статуса других тестов.");
                step.setStatus(StepStatus.FAILURE);
            }
        } else {
            step.setErrorMessage("Тест не найден");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void suspenTest(RSTests step) {
        test.getStartupDependency().clear();
        suspend(step);
    }

    @Override
    public void close() {
    }

    private void waitMilliseconds(RSTests step) {
        try {
            sleep(Integer.valueOf(step.getValue()));
        } catch (InterruptedException e) {
        }
    }

    private void waitComplete(RSTests step) {
        test.getStartupDependency().clear();
        Long id = getTestId(step);
        if (id != null) {
            test.getStartupDependency().put(id, TestStatus.COMPLETED);
            suspend(step);
        } else {
            step.setErrorMessage("Тест не найден");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void waitPositive(RSTests step) {
        test.getStartupDependency().clear();
        Long id = getTestId(step);
        if (id != null) {
            test.getStartupDependency().put(id, TestStatus.SUCCESSFUL);
            suspend(step);
        } else {
            step.setErrorMessage("Тест не найден");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void waitNegative(RSTests step) {
        test.getStartupDependency().clear();
        Long id = getTestId(step);
        if (id != null) {
            test.getStartupDependency().put(id, TestStatus.FAILURE);
            suspend(step);
        } else {
            step.setErrorMessage("Тест не найден");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    /**
     * Возвращает id теста указанного в шаге
     *
     * @return Идентификатор теста
     */
    private Long getTestId(RSTests step) {
        // Поиск id теста по его наименованию
        for (Map.Entry<Long, TestDatas> testDatasEntry : suite.getTestsMap().entrySet()) {

            // Если тест найден
            if (testDatasEntry.getValue().getName().equals(step.getValue()) || (testDatasEntry.getValue().getShortName() != null && testDatasEntry.getValue().getShortName().equals(step.getValue()))) {

                // Если указаны данные для теста, то искать тест с данными
                if (step.getKey() != null && step.getKey().length() > 0) {
                    if (testDatasEntry.getValue().getValue() != null && testDatasEntry.getValue().getValue().equals(step.getKey())) {
                        return testDatasEntry.getKey();
                    }

                } else {
                    return testDatasEntry.getKey();
                }
            }
        }

        return null;
    }

    /**
     * Возвращает ссылку на тест указанного в шаге
     *
     * @return Ссылка на теста
     */
    private TestDatas getTest(RSTests step) {
        // Поиск id теста по его наименованию
        for (Map.Entry<Long, TestDatas> testDatasEntry : suite.getTestsMap().entrySet()) {

            // Если тест найден
            if (testDatasEntry.getValue().getName().equals(step.getValue()) || (testDatasEntry.getValue().getShortName() != null && testDatasEntry.getValue().getShortName().equals(step.getValue()))) {

                // Если указаны данные для теста, то искать тест с данными
                if (step.getKey() != null && step.getKey().length() > 0) {
                    if (testDatasEntry.getValue().getValue() != null && testDatasEntry.getValue().getValue().equals(step.getKey())) {
                        return testDatasEntry.getValue();
                    }

                } else {
                    return testDatasEntry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Приостанавливает поток
     */
    private void suspend(RSTests step) {
        test.setThreadSuspended(true);
        test.setStatus(TestStatus.EXPECTS);

        mapOfTestCalls.get("ThreadStarter").start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (threadSuspended) {
            while (test.isThreadSuspended()) {
                try {
                    threadSuspended.wait();
                } catch (InterruptedException e) {
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (test.getStatus().equals(TestStatus.CANCELLED)) {
            step.setStatus(StepStatus.FAILURE);
            step.setErrorMessage(test.getErrorMessage());
            test.setStatus(TestStatus.RUN);
        }

        if (test.getStatus().equals(TestStatus.EXPECTS)) {
            step.setStatus(StepStatus.BROKEN);
            step.setErrorMessage("Тест не разбужен другими тестами");
            test.setStatus(TestStatus.RUN);
        }
        mapOfTestCalls.get("ThreadStarter").start();
    }
}
