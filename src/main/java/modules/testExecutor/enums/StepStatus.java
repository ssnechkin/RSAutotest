package modules.testExecutor.enums;

public enum StepStatus {
    EXPECTS,    // Ожидает выполнения
    SUCCESSFUL, // Успешное выполнение
    CANCELLED,  // Отменён
    BROKEN,     // Сломан
    FAILURE     // Проверка не пройдена
}