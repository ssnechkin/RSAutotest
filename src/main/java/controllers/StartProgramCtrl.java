package controllers;

import modules.configuration.Option;
import modules.configuration.Settings;
import modules.configuration.interfaces.ProgramSettings;
import modules.filesHandlers.CreatorProgramFiles;
import modules.filesHandlers.RSZip;
import modules.logger.interfaces.RSLogger;

import java.io.File;

/**
 * Класс контроллер. Выполняет создание и копирование файлов программы
 *
 * @author nechkin.sergei.sergeevich
 */
public class StartProgramCtrl {
    private Settings settings;

    /**
     * Конструктор
     *
     * @param thisJarFileName имя текущего jar-файла
     * @param rsLogger        Класс RSLogger - логирование
     */
    public StartProgramCtrl(String thisJarFileName, RSLogger rsLogger) {
        String propertiesFilePath = thisJarFileName + File.separator + thisJarFileName + ".properties";
        String webFolder = "web";
        //String pluginsFolder = "plugins";
        settings = new Settings(new Option());
        CreatorProgramFiles creatorProgramFiles = new CreatorProgramFiles();

        try {
            // Создать директорию с именем программы, если не существует.
            if (!new File(thisJarFileName).exists())
                new File(thisJarFileName).mkdir();

            /*// Создать директорию для плагинов, если не существует.
            if (!new File(thisJarFileName + File.separator + pluginsFolder).exists())
                new File(thisJarFileName + File.separator + pluginsFolder).mkdir();*/

            // Создать файл с настройками, если его нет. RSAutotest/RSAutotest.properties
            if (!new File(propertiesFilePath).exists())
                creatorProgramFiles.createPropertiesFile(propertiesFilePath, settings.getAllSettings());

            // Читать настройки из файла.
            settings.updateAll(new File(propertiesFilePath));

        } catch (Exception e) {
            rsLogger.getLogger().info(e.getMessage());
            rsLogger.printStackTrace(e);
        }

        rsLogger.setReportResultsDirectory(settings.get("ReportResultsDirectory"));
        settings.setThisJarFileName(thisJarFileName);

        try {
            // Копировать из ресурсов каталог web, если его нет.
            if (!new File(thisJarFileName + File.separator + webFolder).exists())
                new RSZip().unzipFolder(thisJarFileName, thisJarFileName + ".jar", "web");

        } catch (Exception e) {
            rsLogger.getLogger().info(e.getMessage());
            rsLogger.printStackTrace(e);
        }
    }

    /**
     * Возвращает настройки программы
     *
     * @return ProgramSettings. Настройки программы.
     */
    public ProgramSettings getSettings() {
        return (ProgramSettings) settings;
    }
}
