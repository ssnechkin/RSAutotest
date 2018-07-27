package main;

import controllers.StartProgramCtrl;
import controllers.СommandLineCtrl;
import modules.configuration.interfaces.ProgramSettings;
import modules.logger.RSLog;
import modules.logger.interfaces.RSLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public class Main {

    public static void main(String[] args) throws Exception {
        String thisJarFileName;

        URL propertiesUrl = Main.class.getResource("/log4j.properties");
        if (propertiesUrl == null) {
            //Hide no appender warning
            Logger.getRootLogger().setLevel(Level.OFF);
        }

        RSLogger rsLogger = new RSLog();
        ExecutorService executorService;

        /* Получить наименование текщего jar-файла */
        File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        thisJarFileName = file.getName();

        if (thisJarFileName.lastIndexOf(".") >= 0)
            thisJarFileName = thisJarFileName.substring(0, thisJarFileName.lastIndexOf("."));

        if (thisJarFileName.lastIndexOf("-") >= 0)
            thisJarFileName = thisJarFileName.substring(0, thisJarFileName.lastIndexOf("-"));
        /******************************************/

        // создать и прочитать программные файлы
        StartProgramCtrl startProgramCtrl = new StartProgramCtrl(thisJarFileName, rsLogger);
        ProgramSettings programSettings = startProgramCtrl.getSettings();

        new СommandLineCtrl(args, programSettings, rsLogger);
    }
}
