package com.dssid.dev.config;

import com.dssid.dev.domain.DatabaseParameter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.dssid.dev.config.PropertieApplication.*;
import static com.dssid.dev.config.constants.DrivesAndURLs.*;
public class DatabaseConfig {

    private DatabaseParameter parameter;

    public DatabaseConfig() {
        this.parameter = new DatabaseParameter();
        loadConfigFromApplication();
    }

    private void loadConfigFromApplication() {
        this.parameter = getPropertiesDatabase();
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASSES.get(parameter.getDbType()));

            return DriverManager.getConnection(parameter.getUrl(),
                    parameter.getUsername(), parameter.getPassword());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC não encontrado para " + parameter.getDbType(), e);
        }
    }

    public boolean isConnected() {
        try(var connect = getConnection()) {
            return connect.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    public static DatabaseConfig createConfig(DatabaseParameter parameter) {
        var config = new DatabaseConfig();
        config.parameter = parameter;
        return config;
    }

    public DatabaseParameter getParameter() {
        return this.parameter;
    }
}
