package plugins.executers.pdf.statusModel.templates;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author nechkin.sergei.sergeevich
 * Класс для данных из базы данных
 */
public class CollectionPDFSql {
    private CopyOnWriteArrayList<String> colums;
    private CopyOnWriteArrayList<ColumnPDFSql> collection;

    public CollectionPDFSql(CopyOnWriteArrayList<ColumnPDFSql> collection) {
        this.collection = collection;
    }

    public CopyOnWriteArrayList<ColumnPDFSql> getCollection() {
        return collection;
    }

    public void setCollection(CopyOnWriteArrayList<ColumnPDFSql> collection) {
        this.collection = collection;
    }

    public CopyOnWriteArrayList<String> getColums() {
        return colums;
    }

    public void setColums(CopyOnWriteArrayList<String> colums) {
        this.colums = colums;
    }
}
