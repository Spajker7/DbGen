package pro.spajk.dbgen;

import java.util.List;

public class GeneratorSettings {
    private final boolean jdbi;
    private final boolean finalClasses;
    private final String outputPath;
    private final String jdbcUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String packageName;
    private final List<String> includedTables;
    private final List<String> excludedTables;

    public GeneratorSettings(boolean jdbi, boolean finalClasses, String outputPath, String jdbcUrl, String dbUsername, String dbPassword, String packageName, List<String> includedTables, List<String> excludedTables) {
        this.jdbi = jdbi;
        this.finalClasses = finalClasses;
        this.outputPath = outputPath;
        this.jdbcUrl = jdbcUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.packageName = packageName;
        this.includedTables = includedTables;
        this.excludedTables = excludedTables;
    }

    public boolean isJdbi() {
        return jdbi;
    }

    public boolean isFinalClasses() {
        return finalClasses;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<String> getIncludedTables() {
        return includedTables;
    }

    public List<String> getExcludedTables() {
        return excludedTables;
    }
}
