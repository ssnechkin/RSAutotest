package plugins.executers.sql.templates;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author nechkin.sergei.sergeevich
 * Класс для колонок из результата выполнения SQL-запроса
 */
public class ColumnsSql {
    private CopyOnWriteArrayList<String> col = new CopyOnWriteArrayList<>();
    public ColumnsSql(CopyOnWriteArrayList<String> col) {
        this.col.addAll(col);
    }

    public CopyOnWriteArrayList<String> getCol() {
        return col;
    }

    public void setCol(CopyOnWriteArrayList<String> col) {
        this.col = col;
    }
}
