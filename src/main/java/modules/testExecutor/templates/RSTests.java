package modules.testExecutor.templates;

import modules.testExecutor.enums.StepStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс шаблон для дерева тестов
 *
 * @author nechkin.sergei.sergeevich
 */
public class RSTests {
    boolean notRun;
    boolean notShow;
    boolean skipBagButTestFailed;
    boolean skipBag;
    boolean beginCycle;
    boolean endCycle;
    String suite;
    String test;
    String numberIterationsCycle;
    String timeoutMilliseconds;
    String runTimeCycleMilliseconds;
    String shortName;
    String step;
    String key;
    String value;
    CopyOnWriteArrayList<RSTests> list;
    CopyOnWriteArrayList<DependingOnTheTests> dependingOnTheTestsList = new CopyOnWriteArrayList<>();
    String startupDependencyOnParent;

    StepStatus status = StepStatus.EXPECTS;
    String errorMessage = "";
    ConcurrentHashMap<String, byte[]> attachments = new ConcurrentHashMap<>(); // <Имя файла, содержимое файла>

    public void setRSTest(RSTests newRSTests) {
        this.notShow = newRSTests.isNotShow();
        this.skipBagButTestFailed = newRSTests.isSkipBagButTestFailed();
        this.skipBag = newRSTests.isSkipBag();
        this.beginCycle = newRSTests.isBeginCycle();
        this.endCycle = newRSTests.isEndCycle();
        this.suite = newRSTests.getSuite();
        this.test = newRSTests.getTest();
        this.numberIterationsCycle = newRSTests.getNumberIterationsCycle();
        this.timeoutMilliseconds = newRSTests.getTimeoutMilliseconds();
        this.runTimeCycleMilliseconds = newRSTests.getRunTimeCycleMilliseconds();
        this.shortName = newRSTests.getShortName();
        this.step = newRSTests.getStep();
        this.key = newRSTests.getKey();
        this.value = newRSTests.getValue();
        this.startupDependencyOnParent = newRSTests.getStartupDependencyOnParent();
        this.list = newRSTests.getList();

        this.dependingOnTheTestsList.clear();
        this.dependingOnTheTestsList.addAll(newRSTests.getDependingOnTheTestsList());

        this.status = newRSTests.getStatus();
        this.errorMessage = newRSTests.getErrorMessage();
        this.attachments.clear();
        this.attachments.putAll(newRSTests.getAttachments());
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ConcurrentHashMap<String, byte[]> getAttachments() {
        return attachments;
    }

    public void setAttachments(ConcurrentHashMap<String, byte[]> attachments) {
        this.attachments.clear();
        this.attachments.putAll(attachments);
    }

    public boolean isNotRun() {
        return notRun;
    }

    public void setNotRun(boolean notRun) {
        this.notRun = notRun;
    }

    public boolean isNotShow() {
        return notShow;
    }

    public void setNotShow(boolean notShow) {
        this.notShow = notShow;
    }

    public boolean isSkipBagButTestFailed() {
        return skipBagButTestFailed;
    }

    public void setSkipBagButTestFailed(boolean skipBagButTestFailed) {
        this.skipBagButTestFailed = skipBagButTestFailed;
    }

    public boolean isSkipBag() {
        return skipBag;
    }

    public void setSkipBag(boolean skipBag) {
        this.skipBag = skipBag;
    }

    public boolean isBeginCycle() {
        return beginCycle;
    }

    public void setBeginCycle(boolean beginCycle) {
        this.beginCycle = beginCycle;
    }

    public boolean isEndCycle() {
        return endCycle;
    }

    public void setEndCycle(boolean endCycle) {
        this.endCycle = endCycle;
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getNumberIterationsCycle() {
        return numberIterationsCycle;
    }

    public void setNumberIterationsCycle(String numberIterationsCycle) {
        this.numberIterationsCycle = numberIterationsCycle;
    }

    public String getTimeoutMilliseconds() {
        return timeoutMilliseconds;
    }

    public void setTimeoutMilliseconds(String timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    public String getRunTimeCycleMilliseconds() {
        return runTimeCycleMilliseconds;
    }

    public void setRunTimeCycleMilliseconds(String runTimeCycleMilliseconds) {
        this.runTimeCycleMilliseconds = runTimeCycleMilliseconds;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CopyOnWriteArrayList<RSTests> getList() {
        return list;
    }

    public void setList(CopyOnWriteArrayList<RSTests> list) {
        this.list = list;
    }

    public CopyOnWriteArrayList<DependingOnTheTests> getDependingOnTheTestsList() {
        return dependingOnTheTestsList;
    }

    public void setDependingOnTheTestsList(CopyOnWriteArrayList<DependingOnTheTests> dependingOnTheTestsList) {
        this.dependingOnTheTestsList.clear();
        this.dependingOnTheTestsList.addAll(dependingOnTheTestsList);
    }

    public String getStartupDependencyOnParent() {
        return startupDependencyOnParent;
    }

    public void setStartupDependencyOnParent(String startupDependencyOnParent) {
        this.startupDependencyOnParent = startupDependencyOnParent;
    }
}