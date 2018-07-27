package modules.commandLine.interfaces;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Интерфейс для чтения и распределения аргументов (переданные jar-файлу) по картам и листам
 *
 * @author nechkin.sergei.sergeevich
 */
public interface RSArguments {

    /**
     * Возвращает лист выполянемых комманд
     *
     * @return список выполняемых команд
     */
    CopyOnWriteArrayList<String> getCommands();

    /**
     * Возвращает карту настроек программы для класса ProgramSettings
     *
     * @return карта настроек (имя настройки, значение)
     */
    ConcurrentHashMap<String, String> getSettings();

    /**
     * Возвращает список выполянемых файлов
     *
     * @return лист файлов (имя файла)
     */
    CopyOnWriteArrayList<String> getFiles();

    /**
     * Возвращает лист выполянемых наборов с тестами. В аргументе suite=краткое имя набора
     *
     * @return Лист наборов (краткие наименования наборов тестов)
     */
    CopyOnWriteArrayList<String> getFileSuites(String fileName);

    /**
     * Возвращает лист выполянемых тестов. В аргументе test=краткое имя теста
     *
     * @return Лист тестов (краткие наименования тестов)
     */
    CopyOnWriteArrayList<String> getFileTests(String fileName);

    /**
     * Возвращает строку с аргументами
     *
     * @return строка с аргументами
     */
    String getAllArguments();
}
