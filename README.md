# DbGen
Small project to generate Java class files from database schema.

Currently, only MySQL is supported. Other databases can be used by including their database drivers in `pom.xml`.

# Usage
```
usage: java -jar dbgen.jar
 -c,--jdbc <url>            JDBC connection URL. (required)
 -e,--exclude <table>       Exclude this table.
 -f,--final                 Should the classes be final.
 -h,--help                  Print help.
 -i,--include <table>       Include this table.
    --jdbi                  Generate JDBI ColumnName annotation.
 -o,--output <path>         Output path. (required)
 -p,--password <password>   Database password.
    --package <name>        Package name for generated classes.
 -u,--username <username>   Database username.
 -v,--verbose               More logging.
```

#### Required options
```
 -c,--jdbc <url>            JDBC connection URL. (required)
 -o,--output <path>         Output path. (required)
```

#### Optional options
###### Database auth
You can use the following options to specify database username and password.
```
 -u,--username <username>   Database username.
 -p,--password <password>   Database password.
```
###### Table inclusion and exclusion
By default, all tables in the database will be generated. You can use multiple `--include` and `--exclude` options to control this.

You can use `--include` options to specify a whitelist of tables.
```
 -i,--include <table>       Include this table.
 -e,--exclude <table>       Exclude this table.
```

###### Generation options
You can use the following options to control generation,
```
 -f,--final                 Should the classes fields be final.
    --jdbi                  Generate JDBI ColumnName annotation.
    --package <name>        Package name for generated classes.
```