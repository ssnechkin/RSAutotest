package plugins.interfaces;

import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Интерфейс для формирования отчёта
 *
 * @author nechkin.sergei.sergeevich
 */
public interface DataHandler {
    /**
     * Возвращает наименование плагина
     *
     * @return Наименование плагина
     */
    String getPluginName();

    /**
     * Возвращает карту с настройками по умолчанию
     *
     * @return Карта с настройками <Описание, <Имя параметра, Значение>>
     */
    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings();

    /**
     * Установить ссылки на данные для обработки шага
     *
     * @param programFilesDirectory имя директории
     * @param suite                 Данные набора
     * @param test                  Данные теста
     * @param stringDataMap         ссылка на ихменяемые текстовые данные теста
     * @param byteDataMap           ссылка на ихменяемые файловые данные теста
     * @param settings              Карта с настройками
     */
    void set(String programFilesDirectory, SuiteDatas suite, TestDatas test, ConcurrentHashMap<String, String> stringDataMap, ConcurrentHashMap<String, byte[]> byteDataMap, ConcurrentHashMap<String, String> settings);

    /**
     * Обработать данные шага
     *
     * @param step шаг
     */
    void processing(RSTests step);

    /**
     * Завершает тест. Закрывает все соединения открытые для теста
     */
    void close();
}
