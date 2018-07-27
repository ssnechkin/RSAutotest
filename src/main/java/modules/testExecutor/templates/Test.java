package modules.testExecutor.templates;

import modules.configuration.Option;
import modules.configuration.Settings;
import modules.configuration.interfaces.ProgramSettings;
import modules.testExecutor.enums.TestStatus;
import modules.testExecutor.interfaces.TestDatas;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Класс шаблон для теста с шагами
 *
 * @author nechkin.sergei.sergeevich
 */
public class Test implements TestDatas {
    private long id = 0;
    private String name = "Тест";
    private String shortName = "";
    private String value;
    private String errorMessage = "";
    private TestStatus testStatus = TestStatus.EXPECTS;
    private Integer numberOfSteps = 0;
    private Integer numberOfСompletedSteps = 0;
    private Logger logger = Logger.getLogger("Тест");
    private ConcurrentHashMap<String, String> stringDataMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, byte[]> byteDataMap = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<RSTests> listSteps = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<DependingOnTheTests> dependingOnTheTestsList = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<Long, TestStatus> startupDependency = new ConcurrentHashMap<>();
    private ProgramSettings programSettings = new Settings(new Option());
    private Boolean threadSuspended = false;
    private Boolean threadRun = false;
    private boolean reportGenerated = false;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Boolean isThreadRun() {
        return threadRun;
    }

    @Override
    public void setThreadRun(boolean runStatus) {
        threadRun = runStatus;
    }

    @Override
    public Boolean isThreadSuspended() {
        return threadSuspended;
    }

    @Override
    public void setThreadSuspended(Boolean threadSuspended) {
        this.threadSuspended = threadSuspended;
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
    public TestStatus getStatus() {
        return testStatus;
    }

    @Override
    public void setStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    @Override
    public boolean isReportGenerated() {
        return reportGenerated;
    }

    @Override
    public void setReportGenerated(boolean b) {
        this.reportGenerated = b;
    }

    @Override
    public Integer getNumberOfSteps() {
        return numberOfSteps;
    }

    public void setNumberOfSteps(Integer numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }

    @Override
    public Integer getNumberOfСompletedSteps() {
        return numberOfСompletedSteps;
    }

    @Override
    public void incrementNumberOfСompletedSteps() {
        ++numberOfСompletedSteps;
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
    public CopyOnWriteArrayList<RSTests> getListSteps() {
        return listSteps;
    }

    public void setListSteps(CopyOnWriteArrayList<RSTests> listSteps) {
        this.listSteps = listSteps;
    }

    @Override
    public ConcurrentHashMap<Long, TestStatus> getStartupDependency() {
        return startupDependency;
    }

    @Override
    public ProgramSettings getProgramSettings() {
        return programSettings;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
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

    @Override
    public CopyOnWriteArrayList<DependingOnTheTests> getDependingOnTheTestsList() {
        return dependingOnTheTestsList;
    }

    public void setDependingOnTheTestsList(CopyOnWriteArrayList<DependingOnTheTests> dependingOnTheTestsList) {
        this.dependingOnTheTestsList.clear();
        this.dependingOnTheTestsList.addAll(dependingOnTheTestsList);
    }
}
