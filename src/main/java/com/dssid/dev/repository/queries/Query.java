package com.dssid.dev.repository.queries;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dssid.dev.constants.Constants.DOT;

public class Query {
    public static final String SELECT = "select ";
    public static final String FROM = " from ";
    public static final String LIMIT = " limit ";
    public static final String FETCH_FIRST = " fetch first ";
    public static final String ROWS_ONLY = " rows only";
    public static final String TOP = " top ";
    public static final String WHERE_ROWNUM = " where rownum = ";
    public static final int Q = 1;
    
    public static String selectDataBaseMySQL_PostgreSQL(String table, String columns) {
        return SELECT + columns + FROM + table + LIMIT + Q;
    }

    public static String selectDataBaseWithJoinTableMySQL_PostgreSQL(List<String> tables, String columns) {
        var joinTables = builJoins(columns, tables);
        return SELECT + columns + FROM + joinTables + FETCH_FIRST + Q + ROWS_ONLY;
    }

    public static String selectDataBaseMySQL_PostgreSQL(String table, Map<String, String> properties) {
        var columns = getColumns(properties);
        return SELECT + columns + FROM + table + LIMIT + Q;
    }

    public static String selectDataOracleSQL(String table, String columns) {
        return SELECT + columns + FROM + table + FETCH_FIRST + Q + ROWS_ONLY;
    }

    public static String selectDataOracleWithJoinTableSQL(List<String> tables, String columns) {
        var joinTables = builJoins(columns, tables);
        return SELECT + columns + FROM + joinTables + FETCH_FIRST + Q + ROWS_ONLY;
    }

    private static String builJoins(String columns, List<String> tables) {
        Set<String> alias = tables.stream()
                .map(table -> table.split(" ")[0]).collect(Collectors.toSet());
        List<String> columnsId = getColumnsReferences(columns);

        String joins = "";
        var lastTable = "";
        for(var table : tables) {
            if(!joins.contains(table)) {
                if(joins.isBlank()) {
                    joins = table;
                }
                if(!joins.contains(table)) {
                    joins += " left join " + table + " on " + addCompareReference(joins, table, columnsId);
                }
            }
        }
        return joins;
    }

    private static String addCompareReference(String joins, String table, List<String> columnsId) {
        var alias = table.substring(table.indexOf(" ") +1);
        var column1 = columnsId.stream()
                .filter(column -> column.contains(alias)).findFirst().get();
        System.out.println(table + " -> foregn key: " + column1);
        var column2 = columnsId.stream()
                .filter(column -> column.contains(column1.substring(column1.indexOf(DOT) +1))
        && !column.contains(alias)).findFirst().orElse(null);
        System.out.println(table + " -> foregn key: " + column1 + " primary key: " + column2);
        return column1 + " = " + column2;
    }

    private static List<String> getColumnsReferences(String columns) {
        var listColumns = Arrays.stream(columns.split(","))
                .map(String::trim)
                .filter(column -> column.contains("id") || column.contains("Id"))
                .toList();

        Map<String, List<String>> groupColumns = listColumns.
                stream().collect(Collectors.groupingBy(column -> column.substring(column.indexOf(".") + 1)));

        return groupColumns.values()
                .stream().filter(list -> list.size() > 1)
                .flatMap(List::stream)
                .toList();
    }

    public static String selectDataOracleSQL(String table, Map<String, String> properties) {
        var columns = getColumns(properties);
        return SELECT + columns + FROM + table + FETCH_FIRST + Q + ROWS_ONLY;
    }

    public static String selectDataOldOracleSQL(String table, String columns) {
        return SELECT + columns + FROM + table + WHERE_ROWNUM + Q;
    }

    public static String selectDataOldOracleSQL(String table, Map<String, String> properties) {
        var columns = getColumns(properties);
        return SELECT + columns + FROM + table + WHERE_ROWNUM + Q;
    }

    public static String selectDataSQLServer(String table, String columns) {
        return SELECT + TOP + Q + columns + FROM + table;
    }

    public static String selectDataWithJoinTableSQLServer(List<String> tables, String columns) {
        var joinTables = builJoins(columns, tables);
        return SELECT + columns + FROM + joinTables;
    }

    public static String selectDataSQLServer(String table, Map<String, String> properties) {
        var columns = getColumns(properties);
        return SELECT + TOP + Q + columns + FROM + table;
    }


    private static String getColumns(Map<String, String> properties) {
        if(properties.isEmpty()) return "*";
        return properties.values().stream()
                .map(String::new)
                .collect(Collectors.joining(","));
    }

}
