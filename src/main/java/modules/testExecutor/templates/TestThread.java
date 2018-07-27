package modules.testExecutor.templates;

import controllers.TestThreadCtrl;
import modules.testExecutor.interfaces.SuiteDatas;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс шаблон для хранения ссылок на потоки со сценариями, для дальнейшего запуска
 *
 * @author nechkin.sergei.sergeevich
 */
public class TestThread {
    private long suiteId;
    private long testId;
    private Thread testScriptThread;
    private ConcurrentHashMap<Long, SuiteDatas> suitesMap;

    public TestThread(Long suiteId, Long testId, TestThreadCtrl testThreadCtrl, ConcurrentHashMap<Long, SuiteDatas> suitesMap) {
        this.suiteId = suiteId;
        this.testId = testId;
        this.testScriptThread = testThreadCtrl;
        this.suitesMap = suitesMap;

    }

    public Long getTestId() {
        return testId;
    }

    public long getSuiteId() {
        return suiteId;
    }

    public ConcurrentHashMap<Long, SuiteDatas> getSuitesMap() {
        return suitesMap;
    }

    public Thread getTestScriptThread() {
        return testScriptThread;
    }
}
