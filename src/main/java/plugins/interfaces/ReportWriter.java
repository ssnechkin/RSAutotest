package plugins.interfaces;

import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.TestThread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Интерфейс для формирования отчёта
 *
 * @author nechkin.sergei.sergeevich
 */
public interface ReportWriter {

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
     * Установить данные для набора
     *
     * @param suite                 Данные набора
     * @param test                  Данные теста
     * @param testThreads           Карта с потоками наборов
     * @param programFilesDirectory Имя директории с файлами программы
     * @param reportDirectory       Директория для отчётов
     * @param settings              Настройки для плагина прочитанные из проперти файла с именем плагина
     * @param executorService       Для всех отчётов. Один поток выполнения.
     */
    void set(SuiteDatas suite, TestDatas test, ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads,
             String programFilesDirectory, String reportDirectory,
             ConcurrentHashMap<String, String> settings, ExecutorService executorService);

    /**
     * Добавить файл в шаг отчёта
     *
     * @param name       Имя файла
     * @param attachment Содержимое файла
     */
    void addAttachment(String name, byte[] attachment);

    /**
     * Добавить набор
     *
     * @param name Имя набора
     */
    void suiteStarted(String name);

    /**
     * Негативное выполнение набора.
     * Неудачное выполнение одного из тестов в наборе
     */
    void suiteFailure();

    /**
     * Завершить набор
     */
    void suiteFinished();

    /**
     * Добавить тест
     *
     * @param name Имя теста
     */
    void testStarted(String name);

    /**
     * Отменить тест
     *
     * @param message Обоснование для отмены
     */
    void testCanceled(String message);

    /**
     * Тест сломан
     * Один из шагов теса не удалось выполнить
     *
     * @param message Текст ошибки
     */
    void testBroken(String message);

    /**
     * Тест не пройден
     * Один из шагов теса не прошёл проверку
     *
     * @param message Текст ошибки
     */
    void testFailure(String message);

    /**
     * Завершить тест
     */
    void testFinished();

    /**
     * Добавить шаг
     *
     * @param name Наименование шага
     */
    void stepStarted(String name);

    /**
     * Шаг сломан
     *
     * @param message Текст ошибки
     */
    void stepBroken(String message);

    /**
     * Шаг выполнен, но проверка не пройдена
     *
     * @param message Текст ошибки
     */
    void stepFailure(String message);

    /**
     * Отменить шаг
     */
    void stepCanceled();

    /**
     * Закрыть шаг
     */
    void stepFinished();

    /**
     * Вызывается после завершения всех наборов с тестами
     */
    void allSuiteFinished();
}
