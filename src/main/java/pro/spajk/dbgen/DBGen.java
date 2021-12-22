package pro.spajk.dbgen;

import com.sun.codemodel.*;
import org.apache.commons.cli.*;
import pro.spajk.dbgen.db.Column;
import pro.spajk.dbgen.db.Table;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DBGen {
    private static Logger LOGGER = Logger.getLogger(DBGen.class.getName());

    public static void main(String[] args) {
        // Setup logger
        LOGGER.setLevel(Level.INFO);
        InputStream stream = DBGen.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            LOGGER = Logger.getLogger(DBGen.class.getName());

        } catch (IOException ignored) { }

        // Parsing args
        Options options = new Options();

        options.addOption(Option.builder()
                .longOpt("jdbi")
                .desc("Generate JDBI ColumnName annotation.")
                .build());

        options.addOption(Option.builder()
                .option("f")
                .longOpt("final")
                .desc("Should the classes fields be final.")
                .build());

        options.addOption(Option.builder()
                .option("o")
                .longOpt("output")
                .desc("Output path.")
                .required()
                .hasArg()
                .argName("path")
                .build());

        options.addOption(Option.builder()
                .option("c")
                .longOpt("jdbc")
                .desc("JDBC connection URL.")
                .required()
                .hasArg()
                .argName("url")
                .build());

        options.addOption(Option.builder()
                .option("u")
                .longOpt("username")
                .desc("Database username.")
                .hasArg()
                .argName("username")
                .build());

        options.addOption(Option.builder()
                .option("p")
                .longOpt("password")
                .desc("Database password.")
                .hasArg()
                .argName("password")
                .build());

        options.addOption(Option.builder()
                .longOpt("package")
                .desc("Package name for generated classes.")
                .hasArg()
                .argName("name")
                .build());

        options.addOption(Option.builder()
                .option("i")
                .longOpt("include")
                .desc("Include this table.")
                .hasArg()
                .argName("table")
                .build());

        options.addOption(Option.builder()
                .option("e")
                .longOpt("exclude")
                .desc("Exclude this table.")
                .hasArg()
                .argName("table")
                .build());

        options.addOption(Option.builder()
                .option("h")
                .longOpt("help")
                .desc("Print help.")
                .build());

        options.addOption(Option.builder()
                .option("v")
                .longOpt("verbose")
                .desc("More logging.")
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if(cmd.hasOption("verbose")) {
                LOGGER.setLevel(Level.ALL);
            }

            if(cmd.hasOption("help")) {
                printHelp(options);
            } else {
                GeneratorSettings generatorSettings = new GeneratorSettings(
                        cmd.hasOption("jdbi"),
                        cmd.hasOption("final"),
                        cmd.getOptionValue("output"),
                        cmd.getOptionValue("jdbc"),
                        cmd.getOptionValue("username"),
                        cmd.getOptionValue("password"),
                        cmd.getOptionValue("package"),
                        cmd.getOptionValues("include") != null ? Arrays.asList(cmd.getOptionValues("include")) : Collections.emptyList(),
                        cmd.getOptionValues("exclude") != null ? Arrays.asList(cmd.getOptionValues("exclude")) : Collections.emptyList());

                DBGen instance = new DBGen();
                instance.generate(generatorSettings);
            }
        } catch (ParseException e) {
            printHelp(options);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar dbgen.jar", options);
    }

    public void generate(GeneratorSettings settings) {
        List<Table> tables = new ArrayList<>();

        LOGGER.info("Obtaining database schema...");

        // Try to connect
        try(Connection connection = DriverManager.getConnection(settings.getJdbcUrl(), settings.getDbUsername(), settings.getDbPassword())) {
            DatabaseMetaData dbMetaData = connection.getMetaData();

            ResultSet resultSet = dbMetaData.getTables(connection.getCatalog(), null, null, new String[]{"TABLE"});
            while(resultSet.next())
            {
                // For every table
                String tableName = resultSet.getString("TABLE_NAME");

                if(settings.getIncludedTables().size() > 0 && !settings.getIncludedTables().contains(tableName)) {
                    continue;
                }

                if(settings.getExcludedTables().contains(tableName)) {
                    continue;
                }

                List<Column> columns = new ArrayList<>();

                // Get columns by selecting an empty set
                Statement selectEmpty = connection.createStatement();
                ResultSet rs = selectEmpty.executeQuery("SELECT * FROM " + tableName + " LIMIT 0");

                for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    columns.add(new Column(rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnClassName(i), rs.getMetaData().isNullable(i) == ResultSetMetaData.columnNullable));
                }

                tables.add(new Table(tableName, columns));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting database schema: " + e.getMessage());
            return;
        }

        LOGGER.info("Generating classes...");

        String packageName = settings.getPackageName() != null ? settings.getPackageName() : "";

        JCodeModel jCodeModel = new JCodeModel();
        jCodeModel._package(packageName);

        // for JDBI annotation
        JClass columnName = jCodeModel.ref("org.jdbi.v3.core.mapper.reflect.ColumnName");

        for (Table table : tables) {
            try {
                JDefinedClass jClass = jCodeModel._class(packageName + "." + table.getName());

                for (Column column : table.getColumns()) {

                    JType type;
                    try {
                        type = jCodeModel.parseType(column.getJavaClassName());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if(! column.isNullable())
                        type = type.unboxify();

                    String propertyName = NameUtils.getPropertyName(column.getName());
                    JFieldVar fieldVar = jClass.field(settings.isFinalClasses() ? JMod.PROTECTED | JMod.FINAL : JMod.PROTECTED, type, propertyName);

                    if(settings.isJdbi()) {
                        fieldVar.annotate(columnName).param("value", column.getName());
                    }

                    boolean isBool = false;

                    if((type.isPrimitive() && type instanceof JPrimitiveType && type.fullName().equals("boolean")) ||
                            (type.unboxify().isPrimitive() && type.unboxify() instanceof JPrimitiveType && type.unboxify().fullName().equals("boolean"))) {
                        isBool = true;
                    }

                    JMethod getterMethod = jClass.method(JMod.PUBLIC, type, NameUtils.getGetterName(propertyName, isBool));
                    getterMethod.body()._return(fieldVar);

                    if(!settings.isFinalClasses()) {
                        JMethod setterMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, NameUtils.getSetterName(propertyName));
                        setterMethod.param(type, propertyName);
                        setterMethod.body().assign(JExpr._this().ref(propertyName), JExpr.ref(propertyName));
                    }
                }

                LOGGER.info("Generated " + table.getName());
            } catch (JClassAlreadyExistsException ignored) {
            }
        }

        try {
            File destination = new File(settings.getOutputPath());

            if(! destination.isDirectory()) {
                destination.mkdir();
            } else {
                purgeDirectory(destination);
            }

            jCodeModel.build(destination, (PrintStream) null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void purgeDirectory(File dir) {
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }
}