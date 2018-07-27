package plugins.executers.selenide.templates;

/**
 * @author nechkin.sergei.sergeevich
 * Класс данных о Web-драйвере
 */
public class WebDriverName {
    public String driverName;
    public String fileName;
    public String fileFullName;

    public WebDriverName(String driverName, String fileName, String fileFullName) {
        this.driverName = driverName;
        this.fileName = fileName;
        this.fileFullName = fileFullName;
    }
}
