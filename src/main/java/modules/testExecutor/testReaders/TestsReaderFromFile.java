package modules.testExecutor.testReaders;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import modules.testExecutor.interfaces.TestsFileReader;
import modules.testExecutor.templates.RSTests;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Класс для чтения тестов из файла
 *
 * @author nechkin.sergei.sergeevich
 */
public class TestsReaderFromFile implements TestsFileReader {

    @Override
    public RSTests read(String filePath) throws Exception {
        RSTests result = null;
        String pathFile = new File(filePath).getName().toLowerCase();

        if (pathFile.endsWith(".json")) {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
            result = new Gson().fromJson(reader, RSTests.class);

        } else if (pathFile.endsWith(".yaml") || pathFile.endsWith(".iml")) {
            result = (RSTests) new Yaml().load(new String(Files.readAllBytes(new File(filePath).toPath()), StandardCharsets.UTF_8));
        }
        return result;
    }
}
