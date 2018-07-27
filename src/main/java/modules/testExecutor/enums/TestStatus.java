package modules.testExecutor.enums;

public enum TestStatus {
        EXPECTS,        // Ожидает запуска
        RUN,            // Выполняется
        SUCCESSFUL,     // Успешное выполнение
        CANCELLED,      // Отменён
        BROKEN,         // Сломан
        FAILURE,        // Тест выполнен, но не пройден
        LOCKED,         // Заблокирован
        COMPLETED       // Зависимость на завершение теста. Включает статусы SUCCESSFUL или BROKEN или FAILURE
}
