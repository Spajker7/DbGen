package pro.spajk.dbgen.db;

import java.util.List;

public class Table {
    private final String name;
    private final List<Column> columns;

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }
}
