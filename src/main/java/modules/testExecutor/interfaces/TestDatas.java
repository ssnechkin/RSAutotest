package modules.testExecutor.interfaces;

import modules.configuration.interfaces.ProgramSettings;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.templates.DependingOnTheTests;
import modules.testExecutor.templates.RSTests;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Интерфейс получения данных теста
 *
 * @author nechkin.sergei.sergeevich
 */
public interface TestDatas {
    /**
     * Возвращает краткое наименование набора или теста с тестовыми данными
     *
     * @return Кратоке наименование
     */
    String getValue();

    /**
     * Возвращает идентификатор теста
     *
     * @return Идентификатор теста
     */
    Long getId();

    /**
     * Возвращает состояние выполнения потока
     *
     * @return Если выполняется то true иначе false
     */
    Boolean isThreadRun();

    /**
     * Устанавливает состояние выполнения потока
     *
     * @return Если выполняется то true иначе false
     */
    void setThreadRun(boolean runStatus);

    /**
     * Возвращает состояние приостановки потока
     *
     * @return Если приостановлен то true иначе false
     */
    Boolean isThreadSuspended();

    /**
     * Устанавливает состояние приостановки потока
     *
     * @param threadSuspended Новое состояние приостановки потока
     */
    void setThreadSuspended(Boolean threadSuspended);

    /**
     * Возвращает полное наименование теста
     *
     * @return Полное наименование теста
     */
    String getName();

    /**
     * Возвращает краткое наименование теста
     *
     * @return Краткое наименование теста
     */
    String getShortName();

    /**
     * Возвращает статус выполнения теста
     *
     * @return Статус выполнения
     */
    TestStatus getStatus();

    /**
     * Устанавливает новый статус теста
     *
     * @param status статус
     */
    void setStatus(TestStatus status);

    /**
     * Устанавливает флаг начала формирования отчёта
     *
     * @param b статус
     */
    void setReportGenerated(boolean b);

    /**
     * Возвращает флаг начала формирования отчёта
     *
     * @return  флаг
     */
    boolean isReportGenerated();

    /**
     * Возвращает общее количество шагов теста
     *
     * @return Количество шагов
     */
    Integer getNumberOfSteps();

    /**
     * Возвращает количество выполненных шагов теста
     *
     * @return Количество шагов
     */
    Integer getNumberOfСompletedSteps();

    /**
     * Устанавливает количество выполненных шагов теста на 1
     */
    void incrementNumberOfСompletedSteps();

    /**
     * Возвращает карту со строковыми данными для теста
     *
     * @return Карта с данными теста <ключ, значение>
     */
    ConcurrentHashMap<String, String> getStringDataMap();

    /**
     * Возвращает карту с байтами данных для теста
     *
     * @return Карта с данными теста <ключ, содержимое в byte>
     */
    ConcurrentHashMap<String, byte[]> getByteDataMap();

    /**
     * Возвращает лист с шагами теста
     *
     * @return Лист с шагами теста
     */
    CopyOnWriteArrayList<RSTests> getListSteps();

    /**
     * Возвращает карту с зависимостями запуска теста
     *
     * @return Карта с зависимостями <id теста, статус его выполнения после которого будет выполнен запуск>
     */
    ConcurrentHashMap<Long, TestStatus> getStartupDependency();

    /**
     * Возвращает независимую копию настроек программы
     *
     * @return Настройки программы
     */
    ProgramSettings getProgramSettings();

    /**
     * Возвращает логгер
     *
     * @return логгер
     */
    Logger getLogger();

    /**
     * Печать трассировки исключения
     *
     * @param t      Исключение
     */
    void printStackTrace(Throwable t);

    /**
     * Устанавливает новый логгер для теста
     *
     * @param logger Логгер
     */
    void setLogger(Logger logger);

    /**
     * Возвращает сообщение об ошибке
     *
     * @return Сообщение об ошибке
     */
    String getErrorMessage();

    /**
     * Устанавливает сообщение об ошибке
     *
     * @param message сообщение
     */
    void setErrorMessage(String message);

    /**
     * Возвращает список тестов от которых зависит запуск текущего теста
     *
     * @return Список тестов (наименование, краткое наименование, статус)
     */
    CopyOnWriteArrayList<DependingOnTheTests> getDependingOnTheTestsList();
}
