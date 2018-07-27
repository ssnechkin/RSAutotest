package modules.testExecutor.enums;

public enum SuiteStatus {
    EXPECTS,    // Ожидает выполнения
    RUN,        // Выполняется
    SUCCESSFUL, // Все тесты выполнены успешно
    FAILURE,    // Один или несколько или все тесты выполнены с отрицательным результатом
    LOCKED      // Не выполнять все тесты из набора
};
