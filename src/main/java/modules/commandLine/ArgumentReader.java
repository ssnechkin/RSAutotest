package modules.commandLine;

import modules.commandLine.interfaces.RSArguments;
import modules.configuration.interfaces.RSOption;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс обработки аргументов командной строки
 *
 * @author nechkin.sergei.sergeevich
 */
public class ArgumentReader implements RSArguments {
    private CopyOnWriteArrayList<String> commadsList = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<String, String> settingsMap = new ConcurrentHashMap<>(); // имя аргумента, значение
    private CopyOnWriteArrayList<String> fileList = new CopyOnWriteArrayList<>(); // имя файла
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> fileSuitesMap = new ConcurrentHashMap<>(); // имя файла, лист наборов для выполнения
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> fileTestsMap = new ConcurrentHashMap<>(); // имя файла, лист тестов для выполнения
    private StringBuilder allArgumentsLine = new StringBuilder();

    public ArgumentReader(String[] arguments, String[] executableCommands, ConcurrentHashMap<String, RSOption> settings) {
        readArgumentMap(arguments, executableCommands, settings);
    }

    @Override
    public CopyOnWriteArrayList<String> getCommands() {
        return commadsList;
    }

    @Override
    public ConcurrentHashMap<String, String> getSettings() {
        return settingsMap;
    }

    @Override
    public CopyOnWriteArrayList<String> getFiles() {
        return fileList;
    }

    @Override
    public CopyOnWriteArrayList<String> getFileSuites(String fileName) {
        CopyOnWriteArrayList<String> result = fileSuitesMap.get(fileName);
        if(result != null) return result;
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public CopyOnWriteArrayList<String> getFileTests(String fileName) {
        CopyOnWriteArrayList<String> result = fileTestsMap.get(fileName);
        if(result != null) return result;
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public String getAllArguments() {
        return allArgumentsLine.toString();
    }

    /**
     * Читает аргументы и распределяет по массивам(картам, листам)
     *
     * @param arguments          аргументы командной строки переданные jar-файлу
     * @param executableCommands Список командных слов
     * @param settings           Карта настроек программы
     */
    private void readArgumentMap(String[] arguments, String[] executableCommands, ConcurrentHashMap<String, RSOption> settings) {
        String spliter = "#&",
                suite = "suite",
                test = "test",
                currentFileName = "",
                key, value;
        StringBuilder argumentsLine = new StringBuilder();
        CopyOnWriteArrayList<String> suiteTestList = new CopyOnWriteArrayList<>();

        // Объеденить все аргументы в строку с разделиетелем spliter
        for (String argument : arguments) {
            allArgumentsLine.append(argument);
            allArgumentsLine.append(" ");
        }

        // Разделяет аргументы разделиетелем spliter
        for (String argument : allArgumentsLine.toString().split(" ")) {
            if (settings.containsKey(argument.split("=")[0])
                    || argument.split("=")[0].equals(suite)
                    || argument.split("=")[0].equals(test)
                    || isArgumentByCommand(executableCommands, argument)
                    || isFile(argument)) {
                argumentsLine.append(spliter);
            } else {
                argumentsLine.append(" ");
            }
            argumentsLine.append(argument);
        }

        // Собирает карту-результат из собранной строки
        for (String line : argumentsLine.toString().split(spliter)) {
            key = line.split("=")[0];
            value = line.substring(key.length(), line.length());
            if(value.indexOf("=") == 0) value = value.substring(1, value.length());

            if (isFile(key)) {
                currentFileName = key;
                fileList.add(key);

            } else if (key.equals(suite)) {
                suiteTestList = fileSuitesMap.get(currentFileName);
                if(suiteTestList == null) suiteTestList = new CopyOnWriteArrayList<>();
                suiteTestList.add(value);
                fileSuitesMap.put(currentFileName, suiteTestList);

            } else if (key.equals(test)) {
                suiteTestList = fileTestsMap.get(currentFileName);
                if(suiteTestList == null) suiteTestList = new CopyOnWriteArrayList<>();
                suiteTestList.add(value);
                fileTestsMap.put(currentFileName, suiteTestList);

            } else if (settings.containsKey(key)) {
                settingsMap.put(key, value);

            } else if (isArgumentByCommand(executableCommands, key)) {
                commadsList.add(key);
            }
        }
    }

    /**
     * Проверяет аргумент на имя файла
     *
     * @param argument Cтрока аргумента
     * @return boolean. Если аргумент файл, то вернёт true, иначе false
     */
    private boolean isFile(String argument) {
        return new File(argument).isFile() || new File(argument.substring(argument.indexOf(" ") + 1, argument.length())).isFile();
    }

    /**
     * Проверяет наличие аргументоов в массиве командных слов
     *
     * @param executableCommands Список командных слов
     * @param argument           проверяемый аргумент
     * @return boolean. Если аргумент присутсвует в списке, то вернёт true, иначе false
     */
    private boolean isArgumentByCommand(String[] executableCommands, String argument) {
        for (String command : executableCommands) if (command.equals(argument)) return true;
        return false;
    }
}
