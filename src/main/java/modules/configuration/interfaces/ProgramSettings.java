package modules.configuration.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Интерфейс для хранения изменения и получения настроек программы
 *
 * @author nechkin.sergei.sergeevich
 */
public interface ProgramSettings {

    void setDefaultSettings(RSOption option);

    //void setDefaultSelectors();

    /*CopyOnWriteArrayList<String> getLoadPagesSelectors();

    void setLoadPagesSelectors(CopyOnWriteArrayList<String> loadPagesSelectors);

    ConcurrentHashMap<String, String> getMapSelectors();

    void setMapSelectors(ConcurrentHashMap<String, String> mapSelectors);

    CopyOnWriteArrayList<String> getCommonSelectors();

    void setCommonSelectors(CopyOnWriteArrayList<String> commonSelectors);

    void readAllSelectors(String filename) throws Throwable;
*/
    ConcurrentHashMap<String, RSOption> getAllSettings();

    String get(String settingName);

    Integer getInteger(String settingName);

    boolean getBoolean(String settingName);

    boolean update(String settingName, String newValue);

    /**
     * Обновляет все настройки значениями из строки c разделителем "&"
     *
     * @param stringWithSettings Cтрока с перечислением новых настроек через разделитель "&". ServerPort=8080&Server=true
     * @return boolean результат обновления. Если хотябы одна настройка обновлена то вернёт true
     */
    boolean updateAll(String stringWithSettings);

    /**
     * Обновляет все настройки значениями из карты
     *
     * @param newSettings карта с новыми настройками
     */
    void updateAll(ConcurrentHashMap<String, RSOption> newSettings);

    /**
     * Обновляет все настройки значениями из Propertie-файла
     *
     * @param propertieFile Propertie-файл
     * @return boolean результат обновления. Если обновлено то вернёт true
     * @throws Throwable Ошибка чтения файла
     */
    void updateAll(File propertieFile) throws Throwable;

    void setThisJarFileName(String thisJarFileName);

    String getThisJarFileName();

    /**
     * Прочитать файл с настройками. Если его нет то создать новый из переданых данных в settings
     * Возвращает прочитанную карту с параметрами
     *
     * @param pathFileName Путь к файлу с настройками
     * @param settings     Карта с параметрами <Описание, <Имя параметра, Значение>>
     *                     @param reWrite      Флаг - перезаписать файл данными из settings
     * @return карта с параметрами
     */
    ConcurrentHashMap<String, String> readPropertiesFile(String pathFileName, ConcurrentHashMap<String, ConcurrentHashMap<String, String>> settings, boolean reWrite) throws IOException;
}