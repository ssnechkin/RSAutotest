package modules.testExecutor;

import modules.testExecutor.interfaces.TestsFileReader;
import modules.testExecutor.templates.RSTests;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс для дополнения шагов теста шагими связанных тестов
 *
 * @author nechkin.sergei.sergeevich
 */
public class AddStepsFromRelatedTests {
    private RSTests rsTestsResult;
    private boolean isStepWithFile = true;

    /**
     * Конструктор
     *
     * @param rsTests         Объект с деревом тестов
     * @param testsFileReader Класс для чтения файла с деревом тестов
     * @throws Throwable Ошибка чтения файла
     */
    public AddStepsFromRelatedTests(RSTests rsTests, TestsFileReader testsFileReader) throws Throwable {
        CopyOnWriteArrayList<RSTests> rsTestsList = new CopyOnWriteArrayList<>();

        rsTestsList.add(rsTests);
        rsTestsResult = rsTests;

        // Повторять чтение тестов пока не будут загружены все файлы из добавленных шагов
        while (isStepWithFile) {
            isStepWithFile = false;
            rsTestsList = toComplementRSTests(rsTestsList, testsFileReader);

            // Обновить результирующий лист. Выполнится 1 раз
            for (RSTests rsTest : rsTestsList) {
                rsTestsResult = rsTest;
            }
        }

    }

    public RSTests getRsTests() {
        return rsTestsResult;
    }

    /**
     * Возвращает лист с деревом шагов дополненных шагами из общего дерева шагов или из файлов (добавляет шаги связанных тестов)
     *
     * @param rsTests         Дерево шагов
     * @param testsFileReader Класс для чтения файла с деревом тестов
     * @return Лист с деревом тестов
     * @throws Throwable Ошибка чтения файла
     */
    private CopyOnWriteArrayList<RSTests> toComplementRSTests(CopyOnWriteArrayList<RSTests> rsTests, TestsFileReader testsFileReader) throws Throwable {
        String data;
        RSTests codeRSTest;

        for (RSTests rsTest : rsTests) {
            if (rsTest.getStep() != null && !rsTest.getStep().isEmpty()) {
                if (new File(rsTest.getStep()).exists()) {
                    codeRSTest = testsFileReader.read(rsTest.getStep());
                    isStepWithFile = true;
                } else {
                    codeRSTest = getRSTestsByShortName(rsTest.getStep(), rsTestsResult);
                }
                if (codeRSTest != null) {
                    data = rsTest.getValue();
                    rsTest.setRSTest(codeRSTest);
                    rsTest.setValue(data);
                    if (rsTest.getStep() == null) {
                        rsTest.setStep(codeRSTest.getTest());
                        rsTest.setTest(null);
                        rsTest.setSuite(null);
                    }
                }
            }
            if (rsTest.getList() != null) rsTest.setList(toComplementRSTests(rsTest.getList(), testsFileReader));
        }

        return rsTests;
    }

    /**
     * Возвращает дерево шагов связанного теста по краткому наименованию.
     * Выполняется поиск краткого наименования по дереву тестов в переданном объекте
     *
     * @param shortNameTest Краткое наименование теста
     * @param rsTests       Дерево шагов
     * @return Объект с деревом шагов связанного теста
     */
    private RSTests getRSTestsByShortName(String shortNameTest, RSTests rsTests) {
        if (rsTests != null) {
            if (rsTests.getShortName() != null && rsTests.getShortName().equals(shortNameTest)) {
                return rsTests;
            }
            if (rsTests.getList() != null) {
                for (RSTests subRSTest : rsTests.getList()) {
                    RSTests codeRSTest = getRSTestsByShortName(shortNameTest, subRSTest);
                    if (codeRSTest != null && codeRSTest.getShortName().equals(shortNameTest)) {
                        return codeRSTest;
                    }
                }
            }
        }
        return null;
    }

}
