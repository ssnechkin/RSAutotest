package modules.commandLine.printers;

import modules.commandLine.interfaces.CommandLlineExecuted;
import modules.configuration.interfaces.ProgramSettings;
import modules.logger.interfaces.RSLogger;

import java.util.logging.Logger;

public class PrintHelp implements CommandLlineExecuted {
    @Override
    public void execute(ProgramSettings programSettings, RSLogger rsLogger) {
        Logger log = rsLogger.getLogger();
        log.info("==========================================================");
        log.info(programSettings.getThisJarFileName());
        log.info("Программа для создания и выполнения автоматических тестов.");
        log.info("Версия:   2.0");
        log.info("Москва 2018г.");
        log.info("----------------------------------------------------------");
        log.info("java -jar " + programSettings.getThisJarFileName() + ".jar SERVER");
        log.info("    Запуск программы в режиме сервера.");
        log.info("    В этом режиме через браузер доступен редактор тестов.");
        log.info("    Возможен дополнительный параметр для изменения порта ServerPort=8080");
        log.info("    Пример вызова:");
        log.info("        java -jar " + programSettings.getThisJarFileName() + ".jar SERVER ServerPort=8080");
        log.info("----------------------------------------------------------");
        log.info("java -jar " + programSettings.getThisJarFileName() + ".jar <<имя файла>>");
        log.info("    Запуск чтения файла (JSON, YAML) с тестами и выполнение прочитанных тестов.");
        log.info("    Возможны дополнительные параметры для запуска определенных наборов или тестов:");
        log.info("        suite=<<краткое наименование набора>>");
        log.info("        test=<<краткое наименование теста>>");
        log.info("    Пример вызова:");
        log.info("        java -jar " + programSettings.getThisJarFileName() + ".jar jest.json suite=Набор2 test=Тест1 suite=Набор2  test=Тест1 test=Тест2 test=Тест3");
        log.info("----------------------------------------------------------");
        log.info("Замена значений в шаге:");
        log.info("Заменяет стороку \"Datetime: -1d\" на текущую дату минус 1 день");
        log.info("Пример: Шаг = Заполнить поле; Ключ = Имя_поля; Значение = Datetime: +2h");
        log.info("При выполнении будет сгенерирована строка: Значение = текущая дата увеличенная на 2 часа");
        log.info("");
        log.info("Заменяет CurrentDateTime на текущую дату. Формат может указыватся в скобках CurrentDateTime(dd/MM/yyyy H:mm:ss)");
        log.info("Пример: Значение = CurrentDateTime(dd.MM.yyyy)");
        log.info("==========================================================");
    }
}
