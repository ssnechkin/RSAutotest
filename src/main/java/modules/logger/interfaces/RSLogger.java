package modules.logger.interfaces;

import java.util.logging.Logger;

/**
 * Интерфейс для записи событий
 *
 * @author nechkin.sergei.sergeevich
 */
public interface RSLogger {

    Logger getLogger();

    Logger getLogger(String logerName);

    void setThreadID(Number threadID);

    void setSuiteID(Number id);

    void setTestID(Number id);

    void setSuiteName(String name);

    void setReportResultsDirectory(String directoryPath);

    void printStackTrace(Throwable throwable);

    void closeLogger(Logger logger);
}
