package plugins.executers.pdf.statusModel.json;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

/**
 * Класс, предназначенный для хранения информации о группе.
 * Класс сериализуется и десериализуется из формата JSON.
 *
 */
public class JsonGroup {
    @SerializedName("id")
    private String id;
    @SerializedName("typ")
    private String type;
    @SerializedName("typeName")
    private String typeName;
    @SerializedName("status")
    private List<JsonStatus> statuses;

    // флаг не используется.
    // планировалось, что флаг будет контролировать надобность проверки блока
    public boolean isExpected = true;

    // Главный конструктор
    public JsonGroup() {
        this(null, null, null);
    }

    // Дополнительный конструктор с параметрами
    public JsonGroup(String id, String type, String typeName) {
        this.id = id;
        this.type = type;
        this.typeName = typeName;
        this.isExpected = this.id != null;
        this.statuses = new LinkedList<>();
    }

    //region Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<JsonStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<JsonStatus> statuses) {
        this.statuses = statuses;
    }
    //endregion

    /**
     * Метод добавляет новый статус в список к текущей группе
     *
     * @param jsonStatus - объект json статуса
     */
    public void addStatus(JsonStatus jsonStatus) {
        this.statuses.add(jsonStatus);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("[");
        out.append("\n\t id=[").append(this.id).append("]");
        out.append("\n\t type=[").append(this.type).append("]");
        out.append("\n\t typeName=[").append(this.typeName).append("]");
        out.append("\n\t statuses=[");

        for (JsonStatus status: statuses) {
            out.append("\n\t ").append(status.toString());
        }
        out.append("\n\t ]");

        return out.toString();
    }
}
