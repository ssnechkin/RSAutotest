package modules.testExecutor.interfaces;

import modules.testExecutor.enums.SuiteStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Интерфейс получения данных набора
 *
 * @author nechkin.sergei.sergeevich
 */
public interface SuiteDatas {
    /**
     * Возвращает краткое наименование набора или теста с тестовыми данными
     *
     * @return Кратоке наименование
     */
    String getValue();

    /**
     * Возвращает идентификатор набора
     *
     * @return Идентификатор набора
     */
    Long getId();

    /**
     * Возвращает полное наименование набора
     *
     * @return Полное наименование набора
     */
    String getName();

    /**
     * Возвращает краткое наименование набора
     *
     * @return Краткое наименование набора
     */
    String getShortName();

    /**
     * Возвращает статус выполнения набора
     *
     * @return Статус выполнения
     */
    SuiteStatus getStatus();

    /**
     * Устанавливает статус выполнения набора
     *
     * @param status статус
     */
    void setStatus(SuiteStatus status);

    /**
     * Возвращает карту со строковыми данными для набора
     *
     * @return Карта с данными теста <ключ, значение>
     */
    ConcurrentHashMap<String, String> getStringDataMap();

    /**
     * Возвращает карту с байтами данных для набора
     *
     * @return Карта с данными теста <ключ, содержимое в byte>
     */
    ConcurrentHashMap<String, byte[]> getByteDataMap();

    /**
     * Возвращает карту с тестами
     *
     * @return Карта с тестами
     */
    ConcurrentHashMap<Long, TestDatas> getTestsMap();

    /**
     * Возвращает логгер программы (Общий для всех тестов. Файл RSAutotest.log)
     *
     * @return логгер
     */
    Logger getProgramLogger();

    /**
     * Печать трассировки исключения
     *
     * @param t Исключение
     */
    void printStackTrace(Throwable t);
}
