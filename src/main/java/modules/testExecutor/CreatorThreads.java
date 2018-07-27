package modules.testExecutor;

import controllers.TestThreadCtrl;
import modules.configuration.interfaces.ProgramSettings;
import modules.logger.interfaces.RSLogger;
import modules.testExecutor.enums.SuiteStatus;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.TestThread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Класс для формирования запускаемых потоков.
 *
 * @author nechkin.sergei.sergeevich
 */
public class CreatorThreads {
    private ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads = new ConcurrentHashMap<>(); // <id набора, потоки>

    /**
     * Конструктор
     *
     * @param mapOfTestCalls        Карта с экземплером объекта для вызова из теста
     * @param suitesMap             Карта с наборами тестов
     * @param listOfSuitesToExecute Лист выполянемых наборов. Если пуст, то выполняются все наборы. (Краткое наименование наборов)
     * @param listOfTestsToExecute  Лист выполянемых тестов. Если пуст, то выполняются все тесты. (Краткое наименование тестов)
     * @param programSettings       настройки программы
     * @param rsLogger              Класс RSLogger - логирование
     * @param executorService       Запускатель для отчётов. Один поток.
     * @param allSuitesFinishedMap  Содержит флаг для завершения всех наборов с тестами.
     */
    public CreatorThreads(ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls, ConcurrentHashMap<Long, SuiteDatas> suitesMap,
                          CopyOnWriteArrayList<String> listOfSuitesToExecute, CopyOnWriteArrayList<String> listOfTestsToExecute,
                          ProgramSettings programSettings, RSLogger rsLogger, ExecutorService executorService, ConcurrentHashMap<String, Boolean> allSuitesFinishedMap) {

        for (Map.Entry<Long, SuiteDatas> suiteEntry : suitesMap.entrySet()) {
            CopyOnWriteArrayList<TestThread> testThreadList = new CopyOnWriteArrayList<>();

            for (Map.Entry<Long, TestDatas> testEntry : suiteEntry.getValue().getTestsMap().entrySet()) {
                if (isMustRead(suiteEntry.getValue(), null, listOfSuitesToExecute, listOfTestsToExecute)
                        && isMustRead(null, testEntry.getValue(), listOfSuitesToExecute, listOfTestsToExecute)) {

                    suitesMap.get(suiteEntry.getKey()).getTestsMap().get(testEntry.getKey()).getProgramSettings().updateAll(programSettings.getAllSettings());

                    rsLogger.setSuiteID(suiteEntry.getKey());
                    rsLogger.setTestID(testEntry.getKey());
                    rsLogger.setSuiteName(suiteEntry.getValue().getShortName() == null ? suiteEntry.getValue().getName() : suiteEntry.getValue().getShortName());
                    Logger logger = rsLogger.getLogger(testEntry.getValue().getShortName() == null ? testEntry.getValue().getName() : testEntry.getValue().getShortName());
                    suitesMap.get(suiteEntry.getKey()).getTestsMap().get(testEntry.getKey()).setLogger(logger);

                    TestThreadCtrl testThreadCtrl = new TestThreadCtrl(suiteEntry.getKey(), testEntry.getKey(), suitesMap, mapOfTestCalls, testThreads, executorService, allSuitesFinishedMap);
                    TestThread testThread = new TestThread(suiteEntry.getKey(), testEntry.getKey(), testThreadCtrl, suitesMap);
                    testThreadList.add(testThread);
                }
            }
            if (testThreadList.size() > 0)
                testThreads.put(suiteEntry.getKey(), testThreadList);
        }
    }

    /**
     * Возвращает список с потоками выполнения тестов
     *
     * @return Карта с потоками <id набора, потоки>
     */
    public ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> getThreads() {
        return testThreads;
    }

    /**
     * Возвращает true если набор с тестами или тест должны быть выполнены
     *
     * @param rsSuite               Набор
     * @param rsTest                Тест
     * @param listOfSuitesToExecute Лист выполянемых наборов. Если пуст, то выполняются все наборы. (Краткое наименование наборов)
     * @param listOfTestsToExecute  Лист выполянемых тестов. Если пуст, то выполняются все тесты. (Краткое наименование тестов)
     * @return результат проверки
     */
    private boolean isMustRead(SuiteDatas rsSuite, TestDatas rsTest,
                               CopyOnWriteArrayList<String> listOfSuitesToExecute, CopyOnWriteArrayList<String> listOfTestsToExecute) {

        // Если нет флага "не выполнять" и пусты спсики с выполняеымми наборами, то true
        if (rsSuite != null && rsSuite.getStatus().equals(SuiteStatus.EXPECTS) && listOfSuitesToExecute.size() == 0)
            return true;

        // Если нет флага "не выполнять" и пусты спсики с выполняеымми тестами, то true
        if (rsTest != null && rsTest.getStatus().equals(TestStatus.EXPECTS) && listOfTestsToExecute.size() == 0)
            return true;

        // Если краткое наименование набора есть в списке выполняемых наборов, то true
        for (String suiteShortName : listOfSuitesToExecute)
            if (rsSuite != null && rsSuite.getShortName() != null && suiteShortName.equals(rsSuite.getShortName()))
                return true;

        // Если краткое наименование теста есть в списке выполняемых наборов, то true
        for (String testShortName : listOfTestsToExecute)
            if (rsTest != null && rsTest.getShortName() != null && testShortName.equals(rsTest.getShortName()))
                return true;

        return false;
    }

}
