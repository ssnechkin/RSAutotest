package modules.testExecutor;

import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.enums.SuiteStatus;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Класс для для чтения шагов теста.
 *
 * @author nechkin.sergei.sergeevich
 */
public class StepsReader {
    private SuiteDatas suite;
    private TestDatas test;
    private Logger logger;
    private Report report;
    private StepExecutor stepExecutor;
    private TestStatus testStatus = TestStatus.SUCCESSFUL;
    private String testMessage = "";
    private String stepMessage = "";

    /**
     * Конструктор
     *
     * @param suite        Данные набора
     * @param test         Данные теста
     * @param report       Для формирования отчёта
     * @param stepExecutor Класс для выполнения шага
     */
    public StepsReader(SuiteDatas suite, TestDatas test, Report report, StepExecutor stepExecutor) {
        this.suite = suite;
        this.test = test;
        this.logger = test.getLogger();
        this.report = report;
        this.stepExecutor = stepExecutor;

        if (suite.getStatus().equals(SuiteStatus.EXPECTS)) suite.setStatus(SuiteStatus.RUN);

        report.suiteStarted(suite.getName());
        report.testStarted(test.getName());

        logger.info("=======================================");
        logger.info("======= З А П У Щ Е Н   Т Е С Т =======");
        logger.info("== Набор:  " + suite.getName());
        logger.info("== Тест:   " + test.getName());
        if (test.getValue() != null)
            logger.info("== Данные: " + test.getValue());
        logger.info("---------------------------------------");

        read(test.getListSteps(), "", false, false);

        logger.info("---------------------------------------");
        logger.info("====== Т Е С Т   З А В Е Р Ш Ё Н ======");
        logger.info("=======================================");

        // Если тест отменён, то записать в отчёт отмену теста
        if (test.getStatus().equals(TestStatus.CANCELLED)) {
            report.testCanceled(test.getErrorMessage());

        } else {

            // Если тест выполнен успешно, но был упавший шаг с флагом isSkipBagButTestFailed, то зафейлить тест и набор
            if ((test.getStatus().equals(TestStatus.RUN) || test.getStatus().equals(TestStatus.SUCCESSFUL)) && !testStatus.equals(TestStatus.SUCCESSFUL)) {
                test.setStatus(testStatus);
                suite.setStatus(SuiteStatus.FAILURE);
                report.suiteFailure();
                if (testStatus.equals(TestStatus.BROKEN)) report.testBroken(testMessage);
                if (testStatus.equals(TestStatus.FAILURE)) report.testFailure(testMessage);
            }

            // Если тест пройден успешно, то установить статус SUCCESSFUL
            if (test.getStatus().equals(TestStatus.RUN)) test.setStatus(TestStatus.SUCCESSFUL);


            // Проверить статус высех тестов в наборе. Если все выполнены то завершить набор
            if (suite.getStatus().equals(SuiteStatus.RUN)) {
                boolean isAllTestsWerePerformed = true;
                for (Map.Entry<Long, TestDatas> testEntry : suite.getTestsMap().entrySet()) {
                    if (testEntry.getValue().getStatus().equals(TestStatus.RUN)
                            || testEntry.getValue().getStatus().equals(TestStatus.EXPECTS))
                        isAllTestsWerePerformed = false;
                }
                if (isAllTestsWerePerformed) suite.setStatus(SuiteStatus.SUCCESSFUL);
            }
        }

        logger.info("= Статуc набора: " + suite.getStatus().toString());
        logger.info("= Статуc теста:  " + test.getStatus().toString());

        report.testFinished();
        report.suiteFinished();
    }

    /**
     * Рекурсивное чтение шагов и передача их на выполнение.
     *
     * @param listSteps  Лист с шагами
     * @param indents    Пробелы для дерева шагов в логе
     * @param isRunCycle Вызвано из цикла
     * @param isNotShow  Флаг не выводить в отчёт шаги
     * @return Статус выполнения шагов в листе listSteps
     */
    private StepStatus read(CopyOnWriteArrayList<RSTests> listSteps, String indents, boolean isRunCycle, boolean isNotShow) {
        StepStatus status = StepStatus.SUCCESSFUL;
        StepStatus stepStatus;
        boolean isNotShowBeginCycle = false;
        Integer numberIterationsCycle = 0;
        Integer runTimeCycleMilliseconds = 0;
        RSTests copyRSTests = new RSTests();
        RSTests cycle = null;

        copyRSTests.setList(new CopyOnWriteArrayList<>());

        for (RSTests step : listSteps) {
            if (!step.isNotRun()) {
                stepStatus = StepStatus.SUCCESSFUL;

                stepExecutor.processStep(step);

                // Если встретилось начало цикла, то создать новый объект cycle
                if (step.isBeginCycle()) {
                    isNotShowBeginCycle = step.isNotShow();
                    copyRSTests.getList().add(step);
                    cycle = new RSTests();
                    cycle.setList(new CopyOnWriteArrayList<>());
                    if (step.getNumberIterationsCycle() != null) // Количество итераций цикла
                        numberIterationsCycle = Integer.valueOf(step.getNumberIterationsCycle());
                    if (step.getRunTimeCycleMilliseconds() != null) // Время выполнения цикла
                        runTimeCycleMilliseconds = Integer.valueOf(step.getRunTimeCycleMilliseconds());
                }

                // Добавить шаги в объект cycle
                if (cycle != null && !step.isBeginCycle() && !step.isEndCycle()) cycle.getList().add(step);

                // Если встретилось окончание цикла, то рекурсивно передать на выполнение собранные шаги в объекте cycle
                if (step.isEndCycle() && cycle != null) {
                    copyRSTests.getList().add(step);
                    stepStatus = cycleExecute(cycle.getList(), runTimeCycleMilliseconds, numberIterationsCycle, indents, isNotShowBeginCycle, step.isNotShow(), isNotShow);
                    copyRSTests.getList().addAll(cycle.getList());
                    cycle = null;
                }

                // Если цикла не встретилось то выполнить шаг
                if (cycle == null) {

                    if (!isRunCycle && !step.isNotShow() && !isNotShow && (step.getStep() != null || step.getKey() != null))
                        report.stepStarted(getStepString(step));

                    // Если у шага есть вложаенные шаги, то рекурсивно передать их на выполнение, иначе выполнить
                    if (step.getList() != null) {
                        if (step.getStep() != null) logger.info(indents + ">> " + step.getStep());
                        if (step.isNotShow()) isNotShow = true;
                        stepStatus = read(step.getList(), indents + "        ", isRunCycle, isNotShow);
                        step.setStatus(stepStatus);

                    } else {
                        stepStatus = executeStep(step, indents);
                        if (!isRunCycle && !step.isNotRun() && !step.isBeginCycle() && !step.isEndCycle())
                            test.incrementNumberOfСompletedSteps();
                        if (!isRunCycle && !step.isSkipBagButTestFailed() && !step.isSkipBag()) {
                            setStatusSuiteAndTest(stepStatus);
                        }
                    }

                    if (!isRunCycle) {
                        addStatusAndAttachmentsToReportStep(step, isNotShow);
                        if (!step.isNotShow() && !isNotShow && (step.getStep() != null || step.getKey() != null)) report.stepFinished();
                    }

                    copyRSTests.getList().add(step);
                }

                // Если предыдущие шаги в листе выполнены успешно, то прировнять статус родительского шага к общему статусу листа шагов
                if (!status.equals(StepStatus.BROKEN) && !status.equals(StepStatus.CANCELLED) && !status.equals(StepStatus.FAILURE))
                    status = stepStatus;
            }
        }

        // Если не встретилось окончание цикла, то рекурсивно передать на выполнение собранные шаги в объекте cycle
        if (cycle != null) {
            RSTests rsTestsEndCycle = new RSTests();
            rsTestsEndCycle.setEndCycle(true);
            rsTestsEndCycle.setNotShow(isNotShowBeginCycle);

            // Добавить отсутсвующий шаг окончания цикла.
            copyRSTests.getList().add(rsTestsEndCycle);

            stepStatus = cycleExecute(cycle.getList(), runTimeCycleMilliseconds, numberIterationsCycle, indents, isNotShowBeginCycle, rsTestsEndCycle.isNotShow(), isNotShow);

            // Если предыдущие шаги в листе выполнены успешно, то прировнять статус текущего шага к общему статусу листа шагов
            if (!status.equals(StepStatus.BROKEN) && !status.equals(StepStatus.CANCELLED) && !status.equals(StepStatus.FAILURE))
                status = stepStatus;

            copyRSTests.getList().addAll(cycle.getList());
        }

        // Заменить шаги в основном дереве на выполненные шаги.
        listSteps.clear();
        listSteps.addAll(copyRSTests.getList());

        return status;
    }

    /**
     * Циклически выполняет шаги из listSteps.
     *
     * @param listSteps                Лист с шагами
     * @param runTimeCycleMilliseconds Время выполнения цикла
     * @param numberIterationsCycle    Количество итераций цикла
     * @param indents                  Пробелы для дерева шагов в логе
     * @param isNotShowBegin           Не выводить в отчёт строку начало цикла
     * @param isNotShowEnd             Не выводить в отчёт строку конец цикла
     * @param isNotShow                Не выводить в отчёт шаги, если родительски шаг с флагом isNotShow
     * @return Статус выполнения высех шагов цикла
     */
    private StepStatus cycleExecute(CopyOnWriteArrayList<RSTests> listSteps,
                                    Integer runTimeCycleMilliseconds, Integer numberIterationsCycle,
                                    String indents, boolean isNotShowBegin, boolean isNotShowEnd, boolean isNotShow) {
        StepStatus stepStatus = StepStatus.SUCCESSFUL;
        int numberOfIterationsPerformed = 0;

        logger.info(indents + "Начало цикла. Время: " + runTimeCycleMilliseconds + " Итераций: " + numberIterationsCycle);

        if (!isNotShowBegin && !isNotShow) {
            report.stepStarted("Начало цикла. Время: " + runTimeCycleMilliseconds + " Итераций: " + numberIterationsCycle);
            report.stepFinished();
        }

        Calendar timeoutCalendar = Calendar.getInstance(); // Получить текущее время
        timeoutCalendar.add(Calendar.MILLISECOND, runTimeCycleMilliseconds); // Прибавить к текущему времени время ожидания

        while (numberIterationsCycle > 0 || Calendar.getInstance().before(timeoutCalendar)/*Пока не достигли времени ожидания*/) {
            numberOfIterationsPerformed++;
            stepStatus = read(listSteps, indents, true, isNotShow);

            // Выйти из цикла если шаги выполнены успешно.
            if (test.getProgramSettings().getBoolean("CompleteCycleIfStepsAreSuccessful") && stepStatus.equals(StepStatus.SUCCESSFUL))
                break;

            // Если тест завершен (все шаги цикла отменяются), то выйти из цикла
            if (test.getStatus().equals(TestStatus.LOCKED)
                    || test.getStatus().equals(TestStatus.CANCELLED)
                    || test.getStatus().equals(TestStatus.BROKEN)
                    || test.getStatus().equals(TestStatus.FAILURE)
                    ) break;

            numberIterationsCycle--;
        }

        // Записать в отчёт выполненные шаги
        writeStepsInReport(listSteps, isNotShow, indents);

        logger.info(indents + "Конец цикла. Итераций: " + numberOfIterationsPerformed);

        if (!isNotShowEnd && !isNotShow) {
            report.stepStarted("Конец цикла. Итераций: " + numberOfIterationsPerformed);
            report.stepFinished();
        }

        return stepStatus;
    }

    /**
     * В отчёт для шага добавить статус и вложения.
     *
     * @param step      Выполненный шаг
     * @param isNotShow Не выводить в отчёт шаги, если родительски шаг с флагом isNotShow
     */
    private void addStatusAndAttachmentsToReportStep(RSTests step, boolean isNotShow) {

        if (!step.isNotShow() && !isNotShow) {
            if (step.getStatus().equals(StepStatus.BROKEN)) report.stepBroken(step.getErrorMessage());
            if (step.getStatus().equals(StepStatus.FAILURE)) report.stepFailure(step.getErrorMessage());
            if (step.getStatus().equals(StepStatus.CANCELLED)) report.stepCanceled();
        }

        // Добавление вложений в отчет из скрытого шага
        if (test.getProgramSettings().getBoolean("AddAttachmentsToAReportFromAHiddenStep")
                || (!step.isNotShow() && !isNotShow)) {
            for (Map.Entry<String, byte[]> attachment : step.getAttachments().entrySet()) {
                report.addAttachment(attachment.getKey(), attachment.getValue());
            }
        }

        if ((step.getStatus().equals(StepStatus.BROKEN) || step.getStatus().equals(StepStatus.FAILURE))
                && (step.getErrorMessage() == null || step.getErrorMessage().length() < 1)) {
            step.setErrorMessage("Текст ошибки отсутствует.");
        }

        // Добавление вложения с текстом ошибки в отчет
        if (test.getProgramSettings().getBoolean("AddAnErrorTextFileToTheReport")
                && step.getErrorMessage() != null && step.getErrorMessage().length() > 1
                && (step.getList() == null || step.getList().size() == 0)) {
            report.addAttachment(step.getStep() + "_error.txt", (getStepString(step)
                    + " Error: " + step.getErrorMessage()).getBytes(StandardCharsets.UTF_8));
        }

    }

    /**
     * Записать выполненные шаги в отчёт.
     *
     * @param listSteps Лист с выполненными шагами
     * @param isNotShow Не выводить в отчёт шаги, если родительски шаг с флагом isNotShow
     * @param indents   Пробелы для дерева шагов в логе
     */
    private void writeStepsInReport(CopyOnWriteArrayList<RSTests> listSteps, boolean isNotShow, String indents) {
        for (RSTests step : listSteps) {
            if (!step.isNotShow() && !isNotShow && step.getStep() != null) report.stepStarted(getStepString(step));

            addStatusAndAttachmentsToReportStep(step, isNotShow);

            if (!step.isSkipBagButTestFailed() && !step.isSkipBag()) {
                setStatusSuiteAndTest(step.getStatus());
            }

            if (step.getList() != null) {
                writeStepsInReport(step.getList(), isNotShow, indents + "       ");
            } else {
                if (!step.isNotRun() && !step.isBeginCycle() && !step.isEndCycle())
                    test.incrementNumberOfСompletedSteps();
            }
            if (!step.isNotShow() && !isNotShow && step.getStep() != null) report.stepFinished();
        }
    }

    /**
     * Возвращает собранную строку шага.
     *
     * @param step Шаг
     * @return Строка шага
     */
    private String getStepString(RSTests step) {
        return step.getStep()
                + (step.getKey() == null ? "" : " " + step.getKey())
                + (step.getValue() == null ? "" : " (" + step.getValue() + ")");
    }

    /**
     * Устанавливает статус набора и теста.
     *
     * @param stepStatus Статус шага
     */
    private void setStatusSuiteAndTest(StepStatus stepStatus) {
        if (stepStatus.equals(StepStatus.BROKEN) || stepStatus.equals(StepStatus.FAILURE)) {
            suite.setStatus(SuiteStatus.FAILURE);
            if (stepStatus.equals(StepStatus.BROKEN)) {
                test.setStatus(TestStatus.BROKEN);
                report.testBroken(stepMessage);
            }
            if (stepStatus.equals(StepStatus.FAILURE)) {
                test.setStatus(TestStatus.FAILURE);
                report.testFailure(stepMessage);
            }
            test.setErrorMessage(stepMessage);
        }
    }

    /**
     * Выполняет шаг и устанавливает статус шага.
     *
     * @param step    Выполняемый шаг
     * @param indents Пробелы для дерева шагов в логе
     * @return Статус шага
     */
    private StepStatus executeStep(RSTests step, String indents) {
        String message = "";
        StringBuilder line = new StringBuilder();

        if (!test.getStatus().equals(TestStatus.RUN) && !test.getStatus().equals(TestStatus.SUCCESSFUL)) {
            logger.info(indents + "| Отменён | " + getStepString(step));
            step.setStatus(StepStatus.CANCELLED);
            return StepStatus.CANCELLED;
        }

        if (step.getStep() != null) {

            logger.info(indents + getStepString(step));

            // Очистить результаты выполнения (шаг мог выполнятся в цикле)
            stepMessage = "";
            step.setErrorMessage("");
            step.setStatus(StepStatus.SUCCESSFUL);
            step.setAttachments(new ConcurrentHashMap<>());

            // Выполнить шаг
            stepExecutor.execute(step);

            //if(step.getErrorMessage().contains("Driver info: ") || step.getErrorMessage().contains("Element should be visibl")) step.setErrorMessage("");

            if (step.getStatus().equals(StepStatus.BROKEN))
                message = "| Шаг сломан." + step.getErrorMessage() + " |";

            if (step.getStatus().equals(StepStatus.FAILURE))
                message = "| Проверка не пройдена. " + step.getErrorMessage() + " |";

            if (message.length() > 0) {
                stepMessage = message;
                for (int i = 0; i < message.length(); i++) line.append("-");
                logger.info(indents + line);
                logger.info(indents + message);
                logger.info(indents + line);
            }

            return getChangedStatusOfStep(step);
        }
        return StepStatus.SUCCESSFUL;
    }

    /**
     * Возвращает изменённый статус шага.
     *
     * @param step Выполненный шаг
     * @return Изменённый статус шага
     */
    private StepStatus getChangedStatusOfStep(RSTests step) {
        stepMessage = "";

        if (step.getStatus().equals(StepStatus.SUCCESSFUL)) {
            return StepStatus.SUCCESSFUL;

        } else {
            stepMessage = step.getErrorMessage();

            if (step.isSkipBag()) return StepStatus.SUCCESSFUL;

            if (step.isSkipBagButTestFailed()) {
                testMessage = step.getErrorMessage();
                if (step.getStatus().equals(StepStatus.BROKEN)) testStatus = TestStatus.BROKEN;
                if (step.getStatus().equals(StepStatus.FAILURE)) testStatus = TestStatus.FAILURE;

                return step.getStatus();
            }
        }
        return step.getStatus();
    }
}
