package modules.testExecutor.templates;

import modules.testExecutor.enums.SuiteStatus;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Класс шаблон для набора тестов
 *
 * @author nechkin.sergei.sergeevich
 */
public class Suite implements SuiteDatas {
    private long id = 0;
    private String name = "Набор тестов";
    private String shortName = "";
    private String value;
    private SuiteStatus suiteStatus = SuiteStatus.EXPECTS;
    private ConcurrentHashMap<String, String> stringDataMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, byte[]> byteDataMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, TestDatas> testsMap = new ConcurrentHashMap<>(); // <id теста, тест>
    private Logger logger;

    public Suite(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public SuiteStatus getStatus() {
        return suiteStatus;
    }

    @Override
    public void setStatus(SuiteStatus suiteStatus) {
        this.suiteStatus = suiteStatus;
    }

    @Override
    public ConcurrentHashMap<String, String> getStringDataMap() {
        return stringDataMap;
    }

    @Override
    public ConcurrentHashMap<String, byte[]> getByteDataMap() {
        return byteDataMap;
    }

    @Override
    public ConcurrentHashMap<Long, TestDatas> getTestsMap() {
        return testsMap;
    }

    @Override
    public Logger getProgramLogger() {
        return logger;
    }

    @Override
    public void printStackTrace(Throwable t) {
        if (logger != null)
            logger.info(t.getMessage());
        StackTraceElement[] stackTraceElements = t.getStackTrace().clone();
        for (StackTraceElement stackTrace : stackTraceElements) {
            System.out.println(stackTrace.toString());
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
