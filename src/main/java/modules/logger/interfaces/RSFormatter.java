package modules.logger.interfaces;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Интерфейс для установки id в строке события
 *
 * @author nechkin.sergei.sergeevich
 */
public interface RSFormatter {
    java.util.logging.Formatter getFormatter();

    void setThreadID(Number threadId);

    void setSuiteID(Number suiteID);

    void setTestID(Number testId);

    void setLogHistory(CopyOnWriteArrayList<String> logHistory);
}
