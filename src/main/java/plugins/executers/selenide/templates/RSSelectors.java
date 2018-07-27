package plugins.executers.selenide.templates;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс шаблон для создания файла RSSelectors.json
 *
 * @author nechkin.sergei.sergeevich
 */
public class RSSelectors {
    private CopyOnWriteArrayList<String> loadPagesSelectors;
    private ConcurrentHashMap<String, String> mapSelectors;
    private CopyOnWriteArrayList<String> commonSelectors;

    public CopyOnWriteArrayList<String> getLoadPagesSelectors() {
        return loadPagesSelectors;
    }

    public void setLoadPagesSelectors(CopyOnWriteArrayList<String> loadPagesSelectors) {
        this.loadPagesSelectors = loadPagesSelectors;
    }

    public ConcurrentHashMap<String, String> getMapSelectors() {
        return mapSelectors;
    }

    public void setMapSelectors(ConcurrentHashMap<String, String> mapSelectors) {
        this.mapSelectors = mapSelectors;
    }

    public CopyOnWriteArrayList<String> getCommonSelectors() {
        return commonSelectors;
    }

    public void setCommonSelectors(CopyOnWriteArrayList<String> commonSelectors) {
        this.commonSelectors = commonSelectors;
    }
}

