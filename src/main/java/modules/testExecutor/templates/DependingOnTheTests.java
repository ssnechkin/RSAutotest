package modules.testExecutor.templates;

public class DependingOnTheTests {
    private String name // Наименование теста
            , shortName // Краткое наименование теста
            , value     // Данные для теста (Краткое наименование теста содержащий данные для этого теста)
            , status   // Статсу в после которого должен запустится завсимый тест
            , statusDescription;   // Описание статуса

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
