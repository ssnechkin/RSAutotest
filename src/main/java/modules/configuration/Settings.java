package modules.configuration;

import modules.configuration.interfaces.ProgramSettings;
import modules.configuration.interfaces.RSOption;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс хранения изменения и получения настроек программы
 *
 * @author nechkin.sergei.sergeevich
 */
public class Settings implements ProgramSettings {
    private ConcurrentHashMap<String, RSOption> settings = new ConcurrentHashMap<>(); //<имя настройки, класс с параметрами настройки>
    private String thisJarFileName;
    private RSOption option;

    public Settings(RSOption option) {
        this.option = option;
        setDefaultSettings(option);
    }

    @Override
    public void setDefaultSettings(RSOption option) {
        settings.clear();
        settings.put("ReportResultsDirectory", option.set("report", "Имя директория для записи отчётов и логов.").clone());
        settings.put("ServerPort", option.set("80", "Порт сервера для работы программы в режиме сервера.").clone());
        settings.put("Server", option.set("false", "Запуск программы в режиме сервера." + "\n#Команды для выполнения тестов ожидаются через GET-запрос.").clone());
        settings.put("MaximumThreads", option.set("100", "Максимальное количество одновременно выполняемых тестов.").clone());
        settings.put("CompleteCycleIfStepsAreSuccessful", option.set("true", "Завершать цикл, если все шаги в цикле выполнены успешно.").clone());
        settings.put("AddAnErrorTextFileToTheReport", option.set("true", "Добавлять в шаг отчёта файл с текстом ошибки," + "\n# в том числе для скрытого шага").clone());
        settings.put("AddAttachmentsToAReportFromAHiddenStep", option.set("true", "Добавлять вложения в отчет из скрытого шага.").clone());
        settings.put("ToCaptureTheScreenAfterTheFailureStep", option.set("false", "Делать снимок экрана после поломки или непройденного шага.").clone());
    }

    @Override
    public ConcurrentHashMap<String, RSOption> getAllSettings() {
        return settings;
    }

    @Override
    public String get(String settingName) {
        if (settings.get(settingName) != null) return settings.get(settingName).getValue();
        return null;
    }

    @Override
    public Integer getInteger(String settingName) {
        if (settings.get(settingName) != null) return Integer.valueOf(settings.get(settingName).getValue());
        return null;
    }

    @Override
    public boolean getBoolean(String settingName) {
        if (settings.get(settingName) != null) return Boolean.valueOf(settings.get(settingName).getValue());
        return false;
    }

    @Override
    public boolean update(String settingName, String newValue) {
        if (settings.get(settingName) != null) {
            settings.get(settingName).setValue(newValue);
            return true;
        } else {
            settings.put(settingName, option.set(newValue, "Добавленный параметр.").clone());
        }
        return false;
    }

    /**
     * Обновляет все настройки значениями из строки c разделителем "&"
     *
     * @param stringWithSettings Cтрока с перечислением новых настроек через разделитель "&". ServerPort=8080&Server=true
     * @return boolean результат обновления. Если хотябы одна настройка обновлена то вернёт true
     */
    @Override
    public boolean updateAll(String stringWithSettings) {
        boolean result = false;
        String urlString = stringWithSettings;
        String settingName;

        for (String argument : urlString.split("&")) {
            settingName = argument.split("=")[0];
            if (settings.containsKey(settingName) && argument.split("=").length > 1) {
                update(settingName, argument.split("=")[1]);
                result = true;
            }
        }
        return result;
    }

    /**
     * Обновляет все настройки значениями из карты
     *
     * @param newSettings карта с новыми настройками
     */
    @Override
    public void updateAll(ConcurrentHashMap<String, RSOption> newSettings) {
        settings.clear();
        settings.putAll(newSettings);
    }

    /**
     * Обновляет все настройки значениями из Propertie-файла
     *
     * @param propertieFile Propertie-файл
     * @return boolean результат обновления. Если обновлено то вернёт true
     * @throws Throwable Ошибка чтения файла
     */
    @Override
    public void updateAll(File propertieFile) throws Exception {
        String settingName;
        PropertiesEx properties = new PropertiesEx(propertieFile, StandardCharsets.UTF_8);

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            settingName = entry.getKey().toString();

            if (settings.containsKey(settingName)) {
                update(settingName, entry.getValue().toString());
            }
        }
    }

    @Override
    public String getThisJarFileName() {
        return thisJarFileName;
    }

    @Override
    public void setThisJarFileName(String thisJarFileName) {
        this.thisJarFileName = thisJarFileName;
    }

    /**
     * Прочитать файл с настройками. Если его нет то создать новый из переданых данных в settings
     * Возвращает прочитанную карту с параметрами
     *
     * @param pathFileName Путь к файлу с настройками
     * @param settings     Карта с параметрами <Описание, <Имя параметра, Значение>>
     * @param reWrite      Флаг - перезаписать файл данными из settings
     * @return карта с параметрами
     */
    public ConcurrentHashMap<String, String> readPropertiesFile(String pathFileName, ConcurrentHashMap<String, ConcurrentHashMap<String, String>> settings, boolean reWrite) throws IOException {
        ConcurrentHashMap<String, String> result = new ConcurrentHashMap<>();

        if (settings != null) {
            // Создать файл если не существует
            if (!new File(pathFileName).exists() || reWrite) {

                File file = new File(pathFileName);
                Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

                for (Map.Entry<String, ConcurrentHashMap<String, String>> setting : settings.entrySet()) {
                    writer.write(setting.getKey() == null ? "" : "\n#" + setting.getKey() + "\n");
                    for (Map.Entry<String, String> settingDatas : setting.getValue().entrySet()) {
                        writer.write(settingDatas.getKey() + "=" + settingDatas.getValue() + "\n");
                    }
                }
                writer.close();
            }
        }

        // Прочитать файл
        PropertiesEx properties = new PropertiesEx(new File(pathFileName), StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return result;
    }
}

