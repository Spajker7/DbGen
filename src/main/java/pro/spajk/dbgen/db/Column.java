package pro.spajk.dbgen.db;

public class Column {

    private final String name;
    private final String javaClassName;
    private final boolean nullable;

    public Column(String name, String javaClassName, boolean nullable) {
        this.name = name;
        this.javaClassName = javaClassName;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public boolean isNullable() {
        return nullable;
    }
}
