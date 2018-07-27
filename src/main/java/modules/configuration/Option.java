package modules.configuration;

import modules.configuration.interfaces.RSOption;

/**
 * Класс получения настройки программы
 *
 * @author nechkin.sergei.sergeevich
 */
public class Option implements RSOption {
    private String value, description;

    @Override
    public Option set(String value, String description) {
        this.value = value;
        this.description = description;
        return this;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public RSOption clone() {
        Option option = new Option();
        option.set(value, description);
        return option;
    }
}
