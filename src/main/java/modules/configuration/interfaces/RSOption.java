package modules.configuration.interfaces;

/**
 * Интерфейс для опций параметра. Используется в ProgramSettings
 *
 * @author nechkin.sergei.sergeevich
 */
public interface RSOption {

    /**
     * Установить значение параметра и его описание
     *
     * @param value значение параметра
     * @param description описание параметра
     * @return текущий объект
     */
    RSOption set(String value, String description);

    /**
     * Возвращает значение параметра
     *
     * @return значение параметра
     */
    String getValue();

    /**
     * Установить значение параметра
     *
     * @param value значение параметра
     */
    void setValue(String value);

    /**
     * Возвращает описание параметра
     *
     * @return описание параметра
     */
    String getDescription();

    /**
     * Возвращает копию объекта
     *
     * @return текущий объект
     */
    RSOption clone();
}
