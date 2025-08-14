package com.dssid.dev.repository;

import com.dssid.dev.config.DatabaseConfig;
import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Payload;
import com.dssid.dev.domain.model.VariableProperties;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dssid.dev.repository.queries.Query.*;
import static com.dssid.dev.verification.VerificationType.*;
public class CustomRepository {

    private static final String MY_SQL = "mysql";
    private static final String POSTGRE_SQL = "postgresql";
    private static final String ORACLE_SQL = "oracle";
    private static final String SQL_SERVER = "sqlserver";

    private String table;
    private Map<String, String> columns;

    private final DatabaseConfig databaseConfig;

    public CustomRepository() {
        this.databaseConfig = new DatabaseConfig();
    }

    public void getValueFromDataBase(Payload entity) {
        var extractedColumns = entity.getProperties().stream()
                .map(VariableProperties::getColumnName)
                .collect(Collectors.joining(", "));
        table = entity.getTableName();
        var query = findDataBaseQuery(extractedColumns);

        try (var connect = databaseConfig.getConnection();
             var statment = connect.createStatement();
             var resultSet = statment.executeQuery(query)){

            while(resultSet.next()){
                for(var property : entity.getProperties()) {
                    var column = property.getColumnName();
                    property.setValue(resultSet.getObject(column));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    public Map<String, Object> getPropertieValueOfInstance(Payload payload, String object) {
        this.table = object;
        var columns = payload.getProperties().stream().map(VariableProperties::getColumnName)
                .collect(Collectors.joining(", "));
        var query = findDataBaseQuery(columns);

        Map<String, Object> propertieValue = new HashMap<>();

        try (var connect = databaseConfig.getConnection();
             var statment = connect.createStatement();
             var resultSet = statment.executeQuery(query)){

            while(resultSet.next()){
                for(var property : payload.getProperties()) {
                    if(isBoolean(property.getType())) propertieValue.put(property.getName(), resultSet.getBoolean(property.getColumnName()));
                    else if(isNumberInteger(property.getType())) propertieValue.put(property.getName(), resultSet.getInt(property.getColumnName()));
                    else if(isNumberDouble(property.getType())) propertieValue.put(property.getName(), resultSet.getBigDecimal(property.getColumnName()));
                    else if(isString(property.getType())) propertieValue.put(property.getName(), resultSet.getString(property.getColumnName()));
                    else if(isDate(property.getType()) ||
                            isLocalDate(property.getType()) ||
                            isLocalDateTime(property.getType())) propertieValue.put(property.getName(), resultSet.getDate(property.getColumnName()));
                    else if(isByte(property.getType())) propertieValue.put(property.getName(), resultSet.getByte(property.getColumnName()));
                    else propertieValue.put(property.getName(), resultSet.getObject(property.getColumnName()));
                }
            }
            return propertieValue;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getPropertieValueOfInstance(Map<String, String> columns, List<Clazz> classProperties, String object) {
        this.table = object;
        this.columns = columns;
        var query = findDataBaseQuery();

        Map<String, Object> propertieValue = new HashMap<>();

        try (var connect = databaseConfig.getConnection();
             var statment = connect.createStatement();
             var resultSet = statment.executeQuery(query)){

            while(resultSet.next()){
                for(var property : classProperties) {
                    if(isBoolean(property.getType())) propertieValue.put(property.getName(), resultSet.getBoolean(columns.get(property.getName())));
                    else if(isNumberInteger(property.getType())) propertieValue.put(property.getName(), resultSet.getInt(columns.get(property.getName())));
                    else if(isNumberDouble(property.getType())) propertieValue.put(property.getName(), resultSet.getBigDecimal(columns.get(property.getName())));
                    else if(isString(property.getType())) propertieValue.put(property.getName(), resultSet.getString(columns.get(property.getName())));
                    else if(isDate(property.getType()) ||
                            isLocalDate(property.getType()) ||
                            isLocalDateTime(property.getType())) propertieValue.put(property.getName(), resultSet.getDate(columns.get(property.getName())));
                    else if(isByte(property.getType())) propertieValue.put(property.getName(), resultSet.getByte(columns.get(property.getName())));
                    else propertieValue.put(property.getName(), resultSet.getObject(columns.get(property.getName())));
                }
            }
            return propertieValue;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String findDataBaseQuery(String columns) {
        var typeDatabase = databaseConfig.getParameter().getDbType();
        switch (typeDatabase) {
            case ORACLE_SQL -> {
                return selectDataOracleSQL(table, columns);
            }
            case MY_SQL, POSTGRE_SQL -> {
                return selectDataBaseMySQL_PostgreSQL(table, columns);
            }
            case SQL_SERVER -> {
                return selectDataSQLServer(table, columns);
            }
        }
        return null;
    }

    private String findDataBaseQuery() {
        var typeDatabase = databaseConfig.getParameter().getDbType();
        switch (typeDatabase) {
            case ORACLE_SQL -> {
                return selectDataOracleSQL(table, columns);
            }
            case MY_SQL, POSTGRE_SQL -> {
                return selectDataBaseMySQL_PostgreSQL(table, columns);
            }
            case SQL_SERVER -> {
                return selectDataSQLServer(table, columns);
            }
        }
        return null;
    }


}
