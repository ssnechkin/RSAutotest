package plugins.executers.pdf.statusModel.json;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Класс, предназначенный для хранения информации об объединенной группе.
 *
 */
public class JsonMergedGroup {
    public String id;
    public String type;
    public String receivedTypeName;
    public String expectedTypeName;

    /*
    В вектор будет записываться последовательность:
     - recStatus        - номер статуса полученного
     - recStatusName    - название статуса полученного
     - recStatusDate    - дата и время полученного статуса
     - expStatus        - номер ожидаемого статуса
     - expStatusName    - название ожидаемого статуса
     - isFalseLine      - верен ли статус
     */
    private List<Vector<Object>> resultStatus;

    // если блок полностью неверный, то устаналивает true
    public boolean isFalseBlock = false;

    public JsonMergedGroup() {
        this(null, null, null, null);
    }

    public JsonMergedGroup(String id, String type, String receivedTypeName, String expectedTypeName) {
        this.id = id;
        this.type = type;
        this.receivedTypeName = receivedTypeName;
        this.expectedTypeName = expectedTypeName;
        this.isFalseBlock = false;
        this.resultStatus = new LinkedList<>();
    }

    /**
     * Возвращает список векторов, в которых записано:
     *   - recStatus        - номер статуса полученного
     *   - recStatusName    - название статуса полученного
     *   - recStatusDate    - дата и время полученного статуса
     *   - expStatus        - номер ожидаемого статуса
     *   - expStatusName    - название ожидаемого статуса
     *   - isFalseLine      - верен ли статус
     *
     * @return - список векторов
     */
    public List<Vector<Object>> getResultStatus() {
        return this.resultStatus;
    }

    /**
     * Метод объединяем статусы в порядке их следования
     *
     * @param statusesExpected - список ожидаемых статусов
     * @param statusesReceived - список полученных статусов
     */
    public boolean generateMergedStatuses(List<JsonStatus> statusesExpected, List<JsonStatus> statusesReceived) {
        JsonStatus temp;
        int number = 0, errors = 0;
        boolean isFail = false;

        // Проходимся по ОЖИДАЕМЫМ
        for (JsonStatus status: statusesExpected) {
            temp = number < statusesReceived.size() ? statusesReceived.get(number) : null;

            // если по списку идет нужный нам статус - то все ок
            if (temp != null && status.getStatus().equals(temp.getStatus()) && status.getStatusName().trim().equals(temp.getStatusName().trim())) {
                addNewLine(temp.getStatus(), temp.getStatusName(), temp.getStatusDate(), status.getStatus(), status.getStatusName(), false);
                statusesReceived.remove(number);
            }
            else {
                errors++;
                addNewLine("", "", "", status.getStatus(), status.getStatusName(), true);
            }
        }
        // Если остались присланные, то выводим их списком ниже
        if (statusesReceived.size() > 0) {
            errors++;
            for (JsonStatus status: statusesReceived) {
                addNewLine(status.getStatus(), status.getStatusName(), status.getStatusDate(), "", "", true);
            }
        }

        return errors == 0;
    }

    /**
     * Метод добавялет новое значение в список объединенных статусов
     *
     * @param recStatus - номер полученного статуса
     * @param recStatusName - название полученного статуса
     * @param recStatusDate - дата полученного статуса
     * @param expStatus - номер ожидаемого статуса
     * @param expStatusName - название ожидаемого статуса
     * @param isFalseLine - ошибочное ли сравнение статусов
     */
    public void addNewLine(String recStatus, String recStatusName, String recStatusDate, String expStatus, String expStatusName, Boolean isFalseLine) {
        Vector addNew = new Vector();
        addNew.add(!recStatus.isEmpty()     ? recStatus     : "");
        addNew.add(!recStatusName.isEmpty() ? recStatusName : "");
        addNew.add(!recStatusDate.isEmpty() ? recStatusDate : "");
        addNew.add(!expStatus.isEmpty()     ? expStatus     : "");
        addNew.add(!expStatusName.isEmpty() ? expStatusName : "");
        addNew.add(isFalseLine);
        this.resultStatus.add(addNew);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("[");
        out.append("\n\t id=[").append(this.id).append("]");
        out.append("\n\t type=[").append(this.type).append("]");
        out.append("\n\t receivedTypeName=[").append(this.receivedTypeName).append("]");
        out.append("\n\t expectedTypeName=[").append(this.expectedTypeName).append("]");
        out.append("\n\t statuses=[");

        for (Object status: this.resultStatus) {
            out.append("\n\t ").append(status.toString());
        }
        out.append("\n\t ]");

        return out.toString();
    }
}
