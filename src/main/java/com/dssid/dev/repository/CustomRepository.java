package com.dssid.dev.repository;

import com.dssid.dev.config.DatabaseConfig;
import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Payload;
import com.dssid.dev.domain.model.VariableProperties;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dssid.dev.constants.Constants.DOT;
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
        var extractedColumns = buildColumn(entity);
        var tables = getTables(entity);
//        table = entity.getTableName();
        var query = findDataBaseQuery(extractedColumns, tables);
        System.out.println("QUERY: " + query);
        try (var connect = databaseConfig.getConnection();
             var statment = connect.createStatement();
             var resultSet = statment.executeQuery(query)){

            while(resultSet.next()){
                for(var property : entity.getProperties()) {

                    if(property.getValue() instanceof Payload) {
                       var object = (Payload) property.getValue();
                        for(var property2 : object.getProperties()) {
                            var columnName = property2.getColumnName();
                            property2.setValue(resultSet.getObject(columnName));
                        }
                        property.setValue(object);
                    } else {
                        var column = property.getColumnName();
                        property.setValue(resultSet.getObject(column));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(entity.toString());
    }

    private List<String> getTables(Payload entity) {
        List<String> tables = new ArrayList<>();
        var tableName = entity.getTableName()+ " " + createAliasFromTableName(entity.getTableName());
        entity.setTableName(tableName);
        tables.add(tableName);
        entity.getProperties().forEach(variable -> {
            if (variable.getValue() instanceof Payload) {
                var tableName2 = ((Payload) variable.getValue()).getTableName();
                ((Payload) variable.getValue()).setTableName(tableName2);
                tables.add(tableName2 + " " + createAliasFromTableName(tableName2));
            }
        });
        return tables;
    }

    private String buildColumn(Payload entity) {
        var alias = createAliasFromTableName(entity.getTableName());
        String columns = entity.getProperties()
                .stream()
                .map(variable -> {
                    if(variable.getValue() instanceof Payload payload2) {
                        return getColumnNameFromAliasTable(alias, variable.getColumnName()).concat(", ") + buildColumn(payload2);
                    }
                    else return getColumnNameFromAliasTable(alias, variable.getColumnName());
                })
                .collect(Collectors.joining(", "));
        return columns;
    }

    private String getColumnNameFromAliasTable(String alias, String columnName) {
        return alias + DOT + columnName;
    }

    private String createAliasFromTableName(String tableName) {
        var values = tableName.split("_");
        int length = values.length;
        if(length > 1) return values[0].substring(0, 2) + "_" + values[length - 1].substring(0, 2);
        return values[0].substring(0, 3);
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

    private String findDataBaseQuery(String columns, List<String> tables) {
        var typeDatabase = databaseConfig.getParameter().getDbType();
        var joinsTables = tables.size() > 1;
        switch (typeDatabase) {
            case ORACLE_SQL -> {
                return joinsTables ? selectDataOracleWithJoinTableSQL(tables, columns) : selectDataOracleSQL(tables.get(0), columns);
            }
            case MY_SQL, POSTGRE_SQL -> {
                return selectDataBaseWithJoinTableMySQL_PostgreSQL(tables, columns);
            }
            case SQL_SERVER -> {
                return selectDataWithJoinTableSQLServer(tables, columns);
            }
        }
        return null;
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
