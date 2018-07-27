package plugins.executers.pdf.statusModel.json;

import com.google.gson.annotations.SerializedName;

/**
 * Класс, предназначенный для хранения информации о статусе.
 * Класс сериализуется и десериализуется из формата JSON.
 *
 */
public class JsonStatus {

    @SerializedName("status")
    private String status;

    @SerializedName("statusName")
    private String statusName;

    @SerializedName("statusDate")
    private String statusDate = null;

    public JsonStatus(String status, String statusName) {
        this(status, statusName, "");
    }

    public JsonStatus(String status, String statusName, String statusDate) {
        this.status = status;
        this.statusName = statusName;
        this.statusDate = statusDate;
    }

    //region Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(String statusDate) {
        this.statusDate = statusDate;
    }
    //endregion

    @Override
    public String toString() {
        return String.format("[status=[%s], statusName=[%s], statusDate=[%s]",
                this.status,
                this.statusName,
                this.statusDate
        );
    }
}
