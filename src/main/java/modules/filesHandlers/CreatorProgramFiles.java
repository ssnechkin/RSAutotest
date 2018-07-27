package modules.filesHandlers;

import modules.configuration.interfaces.RSOption;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для создания файлов программы
 *
 * @author nechkin.sergei.sergeevich
 */
public class CreatorProgramFiles {
    public void createPropertiesFile(String pathFileName, ConcurrentHashMap<String, RSOption> settings) throws IOException {
        String description;
        File file = new File(pathFileName);
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

        for (Map.Entry<String, RSOption> setting : settings.entrySet()) {
            description = setting.getValue().getDescription() == null ? "" : "\n#" + setting.getValue().getDescription() + "\n";
            writer.write(description + setting.getKey() + "=" + setting.getValue().getValue() + "\n");
        }
        writer.close();
    }
}
