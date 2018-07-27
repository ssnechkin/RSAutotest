package plugins.executers.sql.templates;

import java.util.List;

/**
 * @author nechkin.sergei.sergeevich
 * Класс для данных из базы данных
 */
public class CollectionsSql {
    private List<String> colums;
    private List<ColumnsSql> collection;

    public CollectionsSql(List<ColumnsSql> collection) {
        this.collection = collection;
    }

    public List<ColumnsSql> getCollection() {
        return collection;
    }

    public void setCollection(List<ColumnsSql> collection) {
        this.collection = collection;
    }

    public List<String> getColums() {
        return colums;
    }

    public void setColums(List<String> colums) {
        this.colums = colums;
    }
}
