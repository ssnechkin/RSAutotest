package plugins.executers.pdf.statusModel;

import plugins.executers.pdf.statusModel.templates.CollectionPDFSql;
import plugins.executers.pdf.statusModel.templates.ColumnPDFSql;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nechkin.sergei.sergeevich
 * Класс для перебора результатов выполнения SQL-запроса.
 */
public class SQLIterator {
    private CollectionPDFSql collectionPDFSql;
    private Map<String, Integer> columNameMap = new HashMap<>();
    private Map<Integer, String> currentRowMap = new HashMap<>();
    private Integer row = -1;

    public SQLIterator(CollectionPDFSql collectionPDFSql) {
        this.collectionPDFSql = collectionPDFSql;
        int x = 0;
        if (collectionPDFSql != null && collectionPDFSql.getColums() != null) {
            for (String name : collectionPDFSql.getColums()) {
                x++;
                columNameMap.put(name, x);
            }
            //next();
        }
    }

    public boolean next() {
        row++;
        int rowPosition = -1;
        int columnPosition;

        if (collectionPDFSql != null && collectionPDFSql.getCollection() != null) {
            for (ColumnPDFSql rowList : collectionPDFSql.getCollection()) {
                rowPosition++;
                if (rowPosition == row) {
                    if (rowList.getCol() != null) {
                        columnPosition = 0;
                        currentRowMap.clear();
                        for (String columnValue : rowList.getCol()) {
                            columnPosition++;
                            currentRowMap.put(columnPosition, columnValue);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getString(String columName) {
        if (columNameMap.get(columName) != null) {
            return currentRowMap.get(columNameMap.get(columName));
        }
        return null;
    }

    public String getString(int columIndex) {
        return currentRowMap.get(columIndex);
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer index) {
        row = index - 1;
        next();
    }
}
