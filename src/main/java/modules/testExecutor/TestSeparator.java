package modules.testExecutor;

import modules.testExecutor.enums.SuiteStatus;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.DependingOnTheTests;
import modules.testExecutor.templates.RSTests;
import modules.testExecutor.templates.Suite;
import modules.testExecutor.templates.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;


/**
 * Класс для распределения тестов по наборам.
 *
 * @author nechkin.sergei.sergeevich
 */
public class TestSeparator {
    private ConcurrentHashMap<Long, SuiteDatas> suitesMap = new ConcurrentHashMap<>(); //<id набора, набор>
    private ConcurrentHashMap<Long, Long> testIdsMap = new ConcurrentHashMap<>(); // <id набора, id теста>
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> testDatas = new ConcurrentHashMap<>(); // Данные для теста. <краткое наименование, <ключ, значение>>
    private RSTests rsTestsForSearch = new RSTests();
    private long suiteId = 1;
    private Long firstTestId = new Long(1);
    private Logger logger;

    /**
     * Конструктор
     *
     * @param rsTests Объект с деревом тестов
     * @param logger
     */
    public TestSeparator(RSTests rsTests, Logger logger) {
        Long dependingTestId;
        this.logger = logger;
        if (rsTests != null) rsTestsForSearch = rsTests;

        CopyOnWriteArrayList<RSTests> list = new CopyOnWriteArrayList();
        list.add(rsTestsForSearch);

        readTree(list, null, null, null);

        // Добавить тестовые данные в наборы и тесты. Добавить зависимости на другие тесты
        for (Map.Entry<Long, SuiteDatas> suiteEntry : suitesMap.entrySet()) {
            if (suiteEntry.getValue() != null && suiteEntry.getValue().getValue() != null && testDatas.get(suiteEntry.getValue().getValue()) != null)
                suiteEntry.getValue().getStringDataMap().putAll(testDatas.get(suiteEntry.getValue().getValue()));
            for (Map.Entry<Long, TestDatas> testEntry : suiteEntry.getValue().getTestsMap().entrySet()) {
                if (testEntry.getValue() != null && testEntry.getValue().getValue() != null && testDatas.get(testEntry.getValue().getValue()) != null)
                    testEntry.getValue().getStringDataMap().putAll(testDatas.get(testEntry.getValue().getValue()));

                // Добавить зависимости от других тестов
                for (DependingOnTheTests dependingOnTheTests : testEntry.getValue().getDependingOnTheTestsList()) {
                    dependingTestId = getTestId(dependingOnTheTests);
                    if (dependingTestId != null && dependingOnTheTests.getStatus() != null)
                        testEntry.getValue().getStartupDependency().put(dependingTestId, TestStatus.valueOf(dependingOnTheTests.getStatus()));
                }
            }
        }
    }

    /**
     * Возвращает идентификатор теста
     *
     * @param dependingOnTheTests Объект содержит наименования теста по которым будет определён id
     * @return идентификатор
     */
    private Long getTestId(DependingOnTheTests dependingOnTheTests) {
        // Цикл по наборам
        for (Map.Entry<Long, SuiteDatas> suiteEntry : suitesMap.entrySet()) {
            // Цикл по тестам
            for (Map.Entry<Long, TestDatas> testEntry : suiteEntry.getValue().getTestsMap().entrySet()) {
                if (((dependingOnTheTests.getValue() == null && testEntry.getValue().getValue() == null)) || (testEntry.getValue().getValue() != null && dependingOnTheTests.getValue() != null && testEntry.getValue().getValue().equals(dependingOnTheTests.getValue()))
                        && (testEntry.getValue().getName().equals(dependingOnTheTests.getName())
                        || testEntry.getValue().getShortName().equals(dependingOnTheTests.getShortName()))) {
                    return testEntry.getValue().getId();
                }
            }
        }
        return null;
    }

    /**
     * Возвращает лист с наборами тестов
     *
     * @return Лист с наборами
     */
    public ConcurrentHashMap<Long, SuiteDatas> getSuitesMap() {
        return suitesMap;
    }

    /**
     * Выполняет чтение сценариев и распределяет наборы тесты и тестовые данные но массивам.
     *
     * @param rsTestsList Список с деревом тестов.
     * @param suite       Объект для хранения наборов.
     * @param test        Объект для хранения тестов.
     * @param listSteps   дерево шагов.
     */
    private void readTree(CopyOnWriteArrayList<RSTests> rsTestsList, Suite suite, Test test, CopyOnWriteArrayList<RSTests> listSteps) {
        for (RSTests rsTtest : rsTestsList) {
            Suite addSuite = null;
            Test addTest = null;
            CopyOnWriteArrayList<RSTests> addListSteps = null;

            if (rsTtest.getSuite() != null) {
                addSuite = new Suite(logger);
                addSuite.setId(suiteId);
                testIdsMap.put(suiteId, firstTestId);
                suiteId++;
                addSuite.setName(rsTtest.getSuite());
                addSuite.setShortName(rsTtest.getShortName());
                addSuite.setValue(rsTtest.getValue());
                if (rsTtest.isNotRun()) addSuite.setStatus(SuiteStatus.LOCKED);
            }

            if (rsTtest.getTest() != null) {
                addTest = new Test();
                addTest.setId(testIdsMap.get(suite.getId()));
                testIdsMap.put(suite.getId(), testIdsMap.get(suite.getId()) + 1);
                addTest.setName(rsTtest.getTest());
                addTest.setShortName(rsTtest.getShortName());
                addTest.setValue(rsTtest.getValue());
                addTest.setDependingOnTheTestsList(rsTtest.getDependingOnTheTestsList());
                if (rsTtest.isNotRun()) addTest.setStatus(TestStatus.LOCKED);

                // Для добавления новому тесту чистого листа с шагами
                addListSteps = new CopyOnWriteArrayList<>();

                // Добавить зависимость выполнения теста (После упешного выполнения родительского теста запустить выполнение этого или зависит от переданного статуса)
                if (test != null) {
                    if (rsTtest.getStartupDependencyOnParent() == null) {
                        addTest.getStartupDependency().put(test.getId(), TestStatus.COMPLETED);
                    } else {
                        addTest.getStartupDependency().put(test.getId(), TestStatus.valueOf(rsTtest.getStartupDependencyOnParent()));
                    }
                }
            }

            // Если это шаг
            if (rsTtest.getSuite() == null && rsTtest.getTest() == null) {
                if (rsTtest.getList() != null) {
                    addListSteps = new CopyOnWriteArrayList<>();
                } else {
                    // Подсчёт количества выполняемых шагов теста
                    if (!rsTtest.isNotRun() && !rsTtest.isBeginCycle() && !rsTtest.isEndCycle())
                        test.setNumberOfSteps(test.getNumberOfSteps() + 1);

                    // Создать карту с тестовыми данныеми
                    // Если у теста или набора есть краткое наименование и шаг - это тестовые данные, то добавить их в карту testDatas
                    if (((test != null && test.getShortName() != null) || (suite != null && suite.getShortName() != null))
                            && (rsTtest.getKey() != null && rsTtest.getValue() != null && rsTtest.getStep() == null)) {
                        if (test.getShortName() != null && testDatas.get(test.getShortName()) == null) {
                            testDatas.put(test.getShortName(), new ConcurrentHashMap<>());
                        } else if (suite.getShortName() != null && testDatas.get(suite.getShortName()) == null) {
                            testDatas.put(suite.getShortName(), new ConcurrentHashMap<>());
                        }
                        testDatas.get(test.getShortName()).put(rsTtest.getKey(), rsTtest.getValue());
                    }
                }
            }

            if (rsTtest.getList() != null) {
                if (addSuite != null) {
                    readTree(rsTtest.getList(), addSuite, test, listSteps);
                } else if (addTest != null) {
                    readTree(rsTtest.getList(), suite, addTest, addListSteps);
                } else if (addListSteps != null) {
                    readTree(rsTtest.getList(), suite, test, addListSteps);
                } else {
                    readTree(rsTtest.getList(), suite, test, listSteps);
                }
            }

            if (rsTtest.getSuite() == null && rsTtest.getTest() == null && listSteps != null) {

                // Копировать шаг без вложенных шагов
                RSTests step = new RSTests();
                step.setNotRun(rsTtest.isNotRun());
                step.setNotShow(rsTtest.isNotShow());
                step.setSkipBagButTestFailed(rsTtest.isSkipBagButTestFailed());
                step.setSkipBag(rsTtest.isSkipBag());
                step.setBeginCycle(rsTtest.isBeginCycle());
                step.setEndCycle(rsTtest.isEndCycle());
                step.setNumberIterationsCycle(rsTtest.getNumberIterationsCycle());
                step.setTimeoutMilliseconds(rsTtest.getTimeoutMilliseconds());
                step.setRunTimeCycleMilliseconds(rsTtest.getRunTimeCycleMilliseconds());
                step.setStep(rsTtest.getStep());
                step.setKey(rsTtest.getKey());
                step.setValue(rsTtest.getValue());

                if (addListSteps != null) step.setList(addListSteps);
                listSteps.add(step);

            } else if (addSuite != null) {
                suitesMap.put(addSuite.getId(), addSuite);

            } else if (addTest != null && addListSteps != null) {
                addTest.setListSteps(addListSteps);
                suite.getTestsMap().put(addTest.getId(), addTest);
            }

        }

    }

}
