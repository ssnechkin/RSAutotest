package plugins.executers.sql;

import plugins.executers.sql.templates.CollectionsSql;
import plugins.executers.sql.templates.ColumnsSql;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nechkin.sergei.sergeevich
 * Класс для перебора результатов выполнения SQL-запроса.
 */
public class SQLIterator {
    private CollectionsSql collectionsSql;
    private Map<String, Integer> columNameMap = new HashMap<>();
    private Map<Integer, String> currentRowMap = new HashMap<>();
    private Integer row = -1;

    public SQLIterator(CollectionsSql collectionsSql) {
        this.collectionsSql = collectionsSql;
        int x = 0;
        if (collectionsSql != null && collectionsSql.getColums() != null) {
            for (String name : collectionsSql.getColums()) {
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

        if (collectionsSql != null && collectionsSql.getCollection() != null) {
            for (ColumnsSql rowList : collectionsSql.getCollection()) {
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
