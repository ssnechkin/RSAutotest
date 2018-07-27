package plugins.interfaces;

import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Интерфейс для выполнения шагов теста
 *
 * @author nechkin.sergei.sergeevich
 */
public interface TestExecutor {
    /**
     * Возвращает наименование плагина
     *
     * @return Наименование плагина
     */
    String getPluginName();

    /**
     * Возвращает наименование группы шагов
     */
    String getGroupName();

    /**
     * Возвращает карту с настройками по умолчанию
     *
     * @return Карта с настройками <Описание, <Имя параметра, Значение>>
     */
    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings();

    /**
     * Возвращает карту всех шагов
     * <Наименование шага, Описание>
     */
    ConcurrentHashMap<String, String> getAllStepsMap();

    /**
     * Установить данные для набора
     *
     * @param suite                 Данные набора
     * @param test                  Данные теста
     * @param threadSuspended       Переменная для приостановки потока. Получает оповещения при возобновлении потока
     *                              // Приостановить поток
     *                              synchronized (threadSuspended) {
     *                              test.setStatus(TestStatus.EXPECTS);
     *                              while (test.isThreadSuspended()) {
     *                              try {
     *                              threadSuspended.wait();
     *                              } catch (InterruptedException e) {
     *                              e.printStackTrace();
     *                              }
     *                              }
     *                              }
     * @param mapOfTestCalls        Ссылка на карту с классами для запуска.
     * @param programFilesDirectory Имя директории с файлами программы
     * @param settings              Настройки для плагина прочитанные из проперти файла с именем плагина
     */
    void set(SuiteDatas suite, TestDatas test,
             Boolean threadSuspended, ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls,
             String programFilesDirectory,
             ConcurrentHashMap<String, String> settings);

    /**
     * Завершает тест. Закрывает все соединения открытые для теста
     */
    void close();

    /**
     * Выполняет шаг и сохраняет результаты в шаг (Status, Attachments, ErrorMessage)
     *
     * @param step Шаг для выполнения
     */
    void execute(RSTests step);
}
