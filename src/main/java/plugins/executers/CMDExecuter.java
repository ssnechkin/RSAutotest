package plugins.executers;

import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import plugins.interfaces.TestExecutor;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CMDExecuter implements TestExecutor {
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<String, String> stepsMap = new ConcurrentHashMap<>();// <Наименование шага, Описание шага>
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private Logger logger;
    private Boolean threadSuspended = false;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private String programFilesDirectory;

    private String directory;

    @Override
    public String getPluginName() {
        return "CMD";
    }

    @Override
    public String getGroupName() {
        return "Командная строка";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        return null;
    }

    @Override
    public ConcurrentHashMap<String, String> getAllStepsMap() {
        if (stepsMap.size() == 0) execute(null);
        return stepsMap;
    }

    @Override
    public void set(SuiteDatas suite, TestDatas test, Boolean threadSuspended, ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls, String programFilesDirectory, ConcurrentHashMap<String, String> settings) {
        this.suite = suite;
        this.test = test;
        this.stringDataMap = test.getStringDataMap();
        this.byteDataMap = test.getByteDataMap();
        this.threadSuspended = threadSuspended;
        this.mapOfTestCalls = mapOfTestCalls;
        this.programFilesDirectory = programFilesDirectory;
        this.logger = test.getLogger();
    }

    @Override
    public void close() {
    }

    private boolean deleteDirectory(File dir) {
        boolean result = true;
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    File f = new File(dir, children[i]);
                    deleteDirectory(f);
                }
                if (!dir.delete()) result = false;
            } else {
                if (!dir.delete()) result = false;
            }
        }
        return result;
    }

    private File[] listFilesMatchingFindeRegexp(File root, String regex) {
        if (!root.isDirectory()) {
            throw new IllegalArgumentException(root + " is no directory.");
        }
        final Pattern p = Pattern.compile(regex); // careful: could also throw an exception!
        return root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return p.matcher(file.getName()).matches();
            }
        });
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;

        if (step != null) stepName = step.getStep();

        name = "CMD Выполнить в командной строке";
        if (mapEmpty) {
            description = "В командной строке выполняет указанное выражение. Пример: Шаг = CMD Выполнить в командной строке; Значение = Запрос для выполнения (c:\\command.bat)";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            excCommand(step);
            return;
        }

        name = "Считать файл в переменную";
        if (mapEmpty) {
            description = "Читает файл из указанного пути и сохраняет его в указанную переменную. Пример: Шаг = Считать файл в переменную; Ключ = Имя переменной; Значение = пеуть к файлу (c:\\command.bat)";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            readFileFromPath(step);
            return;
        }

        name = "Удалить директорию";
        if (mapEmpty) {
            description = "Удаляет указанную директорию. Пример: Шаг = Удалить директорию; Значение = путь к директории;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            deleteDirectory(step);
            return;
        }

        name = "Задать директорию для поиска файла";
        if (mapEmpty) {
            description = "Сохранить в памяти путь к директории. Пример: Шаг = Задать директорию для поиска файла; Значение = путь к директории;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setDirectory(step);
            return;
        }

        name = "В заданной директории найти имя файла по регулярным выражениям и сохранить в переменную";
        if (mapEmpty) {
            description = "В ранее установленной директории выполнить поиск файла по заданному регулярному выражению и сохранить в заданную переменную. Пример: Шаг = В заданной директории найти имя файла по регулярным выражениям и сохранить в переменную; Ключ = Имя переменной; Значение = строка с регулярными выражениями (XT-20100505-\\\\d{4}\\\\.trx);";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            searchFileFormDirectoryByRegExp(step);
            return;
        }

        name = "Сохранить в переменную значение переменной среды";
        if (mapEmpty) {
            description = "Получает значение переменнной окружения и сохраняет в указанную переменную. Пример: Шаг = Сохранить в переменную значение переменной среды; Ключ = Имя переменной; Значение = Имя переменной среды;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            saveInAvariableEnvironmentVariableValue(step);
            return;
        }
    }

    private void saveInAvariableEnvironmentVariableValue(RSTests step) {
        String value = System.getenv(step.getValue());
        if(value != null) {
            stringDataMap.put(step.getKey(), value);
        } else {
            step.setErrorMessage("Переменная окружения не найдена.");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void searchFileFormDirectoryByRegExp(RSTests step) {
        try {
            File[] files = listFilesMatchingFindeRegexp(new File(directory), step.getValue());
            if (files.length > 0) {
                for (File file : files) {
                    stringDataMap.put(step.getKey(), file.getName());
                    break;
                }
            } else {
                step.setErrorMessage("Файл не найден");
                step.setStatus(StepStatus.FAILURE);
            }
        } catch (Exception e) {
            step.setErrorMessage(e.getMessage());
            step.setStatus(StepStatus.BROKEN);
        }
    }

    private void deleteDirectory(RSTests step) {
        if (!deleteDirectory(new File(step.getValue()))) {
            step.setErrorMessage("Не удалось удалить директорию");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void readFileFromPath(RSTests step) {
        try {
            byteDataMap.put(step.getKey(), Files.readAllBytes(new File(step.getValue()).toPath()));
        } catch (IOException e) {
            step.setErrorMessage("Неудалось прочитать файл. " + e.getMessage());
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void excCommand(RSTests step) {
        String command = step.getValue();
        if (stringDataMap.get(command) != null) command = stringDataMap.get(command);

        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
        builder.redirectErrorStream(true);

        try {
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null
                        || line.contains("<Ctrl+C>")
                        || line.contains("[Y(да)/N(нет)]")
                        || line.contains("Ctrl + C")
                        ) {
                    break;
                }
                logger.info(line);
            }
        } catch (IOException e) {
            step.setErrorMessage(e.getMessage());
            step.setStatus(StepStatus.BROKEN);
        }
    }

    public void setDirectory(RSTests step) {
        this.directory = step.getValue();
    }
}
