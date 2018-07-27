package modules.testExecutor.interfaces;

import modules.testExecutor.templates.RSTests;

/**
 * Интерфейс для чтения объекта с тестами из файла
 *
 * @author nechkin.sergei.sergeevich
 */
public interface TestsFileReader {

    /**
     * Читает файл и возвращает объект с тестами из файла
     *
     * @param filePath путь и имя файла
     * @return RSTests. Дерево тестов
     * @throws Throwable Ошибка чтения файла
     */
    RSTests read(String filePath) throws Throwable;
}
