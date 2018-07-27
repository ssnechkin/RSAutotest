package modules.logger;

import modules.logger.interfaces.RSLogger;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.*;

/**
 * Класс для записи результатов выполнения тестов в консоль и в файл
 *
 * @author nechkin.sergei.sergeevich
 */
public class RSLog implements RSLogger {
    private String thisJarFileName;
    private final String FILE_FORMAT = "YYYY_MM_dd_HH_mm_ss";
    private final String DIR_LOGS = "log";
    private final String FILE_EXT = "log";
    private final String ERROR_SECURE = "Не удалось создать файл лога из-за политики безопасности.";
    private final String ERROR_IO = "Не удалось создать файл лога из-за ошибки ввода-вывода.";
    private String reportResultsDirectory = "report";
    private Logger rsLogger;
    private CopyOnWriteArrayList<String> testLogHistory = new CopyOnWriteArrayList<>();
    private long threadID = 0;
    private long suiteID = 0;
    private long testID = 0;
    private String suiteName = "";

    public RSLog() {
        /* Получить наименование текщего jar-файла */
        try {
            thisJarFileName = FilenameUtils.getBaseName(new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName());
        } catch (Exception e) {
            thisJarFileName = "System";
        }
    }

    @Override
    public Logger getLogger() {
        if (rsLogger != null) return rsLogger;
        rsLogger = getLogger(thisJarFileName);
        return rsLogger;
    }

    @Override
    public void setReportResultsDirectory(String reportResultsDirectory) {
        this.reportResultsDirectory = reportResultsDirectory;
    }

    @Override
    public void setThreadID(Number threadID) {
        this.threadID = (long) threadID;
    }

    @Override
    public void setSuiteID(Number id) {
        suiteID = (long) id;
    }

    @Override
    public void setTestID(Number id) {
        testID = (long) id;
    }

    @Override
    public void setSuiteName(String name) {
        this.suiteName = name;
    }

    @Override
    public void printStackTrace(Throwable throwable) {
        if (rsLogger == null) rsLogger = getLogger();
        if (throwable.getMessage() != null) rsLogger.info(throwable.getMessage());
        StackTraceElement[] stackTraceElements = throwable.getStackTrace().clone();
        for (StackTraceElement stackTrace : stackTraceElements) {
            rsLogger.info("   " + stackTrace.toString());
        }
    }

    @Override
    public void closeLogger(Logger logger) {
        Handler[] handler = logger.getHandlers();
        for (Handler h : handler) {
            h.close();
        }
    }

    @Override
    public Logger getLogger(String loggerName) {
        String logPath;
        String logFileName;

        Logger logger = Logger.getLogger(threadID + thisJarFileName);
        FileHandler fileHandler;
        ConsoleHandler consoleHandler;
        LoggerFormatterEmpty loggerFormatterEmpty = new LoggerFormatterEmpty();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FILE_FORMAT);
        LoggerFormatter formatterConsole = new LoggerFormatter();
        LoggerFormatter formatterFile = new LoggerFormatter();

        try {
            formatterConsole.setThreadID(threadID);
            formatterConsole.setLogHistory(testLogHistory);
            formatterFile.setThreadID(threadID);

            if (!thisJarFileName.equals(loggerName)) {
                formatterConsole.setSuiteID(suiteID);
                formatterConsole.setTestID(testID);
                formatterFile.setSuiteID(suiteID);
                formatterFile.setTestID(testID);
            }

            logger = Logger.getLogger(threadID + loggerName + suiteID + testID + Thread.currentThread().getId());

            if (logger.getHandlers().length == 0) {
                for (Handler h : logger.getParent().getHandlers()) {
                    h.setFormatter(loggerFormatterEmpty);

                    if (File.separator.equals("/")) {
                       h.setEncoding("UTF-8");
                    } else {
                       h.setEncoding("866");
                    }
                }

                if (suiteName.length() > 0) {
                    logPath = reportResultsDirectory + File.separator + DIR_LOGS + File.separator + replaceFileName(suiteName) + File.separator + replaceFileName(loggerName) + File.separator;
                } else {
                    logPath = reportResultsDirectory + File.separator + DIR_LOGS + File.separator + replaceFileName(loggerName) + File.separator;
                }

                logFileName = simpleDateFormat.format(new Date()) + "_suite" + suiteID + "_test" + testID + "_thread" + Thread.currentThread().getId();

                File file = new File(logPath);
                file.mkdirs();
                fileHandler = new FileHandler(file.getPath() + File.separator + logFileName + "." + FILE_EXT);
                fileHandler.setEncoding("UTF-8");
                fileHandler.setFormatter(formatterFile.getFormatter());
                logger.addHandler(fileHandler);

                consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(formatterConsole.getFormatter());

                if (File.separator.equals("/")) {
                    consoleHandler.setEncoding("UTF-8");
                } else {
                    consoleHandler.setEncoding("866");
                }
                logger.addHandler(consoleHandler);
            }
            return logger;

        } catch (SecurityException e) {
            logger.log(Level.SEVERE, ERROR_SECURE, e);
            e.printStackTrace();
            System.out.println("LOGGER ERROR");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, ERROR_IO, e);
            e.printStackTrace();
            System.out.println("LOGGER ERROR");
            System.out.println(e.getMessage());
        }
        if (thisJarFileName.equals(loggerName)) {
            rsLogger = logger;
        }
        return logger;
    }

    private String replaceFileName(String fileName) {
        if (fileName.length() > 210) fileName = fileName.substring(0, 210);

        fileName = fileName.replaceAll("\\\\", "");
        fileName = fileName.replaceAll("/", "");
        fileName = fileName.replace("/", "");
        fileName = fileName.replace("\\\\", "");
        fileName = fileName.replace("\\", "");
        fileName = fileName.replace(File.separator, "");

        fileName = fileName.replace(":", "");
        fileName = fileName.replace("-", "");
        fileName = fileName.replace(",", "");
        fileName = fileName.replaceAll("\"", "");
        return fileName;
    }
}
