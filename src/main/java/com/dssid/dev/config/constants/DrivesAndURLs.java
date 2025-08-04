package com.dssid.dev.config.constants;

import java.util.HashMap;
import java.util.Map;

public class DrivesAndURLs {
    public static final Map<String, String> DRIVER_CLASSES = new HashMap<>();
    public static final Map<String, String> URL_PREFIXES = new HashMap<>();

    static {
        // Inicializa os mapeamentos de drivers
        DRIVER_CLASSES.put("mysql", "com.mysql.cj.jdbc.Driver");
        DRIVER_CLASSES.put("postgresql", "org.postgresql.Driver");
        DRIVER_CLASSES.put("oracle", "oracle.jdbc.driver.OracleDriver");
        DRIVER_CLASSES.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
    };
    static {
        // Inicializa os mapeamentos de urls
        URL_PREFIXES.put("mysql", "jdbc:mysql://");
        URL_PREFIXES.put("postgresql", "jdbc:postgresql://");
        URL_PREFIXES.put("oracle", "jdbc:oracle:thin:@");
        URL_PREFIXES.put("sqlserver", "jdbc:sqlserver://");
    }

}
