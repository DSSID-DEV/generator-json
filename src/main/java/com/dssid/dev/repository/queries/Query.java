package com.dssid.dev.repository.queries;

import java.util.Map;
import java.util.stream.Collectors;

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

    public static String selectDataBaseMySQL_PostgreSQL(String table, Map<String, String> properties) {
        var columns = getColumns(properties);
        return SELECT + columns + FROM + table + LIMIT + Q;
    }

    public static String selectDataOracleSQL(String table, String columns) {
        return SELECT + columns + FROM + table + FETCH_FIRST + Q + ROWS_ONLY;
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
