package modules.testExecutor.interfaces;

import modules.testExecutor.Report;

/**
 * Интерфейс для вызова из потка теста
 *
 * @author nechkin.sergei.sergeevich
 */
public interface CalledFromTest {
    /**
     * Вызывается из потока теста. Должен быть потокабезопасным (synchronized)
     */
    void start();
    void start(Report report);
}
