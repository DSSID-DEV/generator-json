package com.dssid.dev.domain;

import com.dssid.dev.config.PropertieApplication;
import com.dssid.dev.domain.model.PropertieValue;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Getter
@NoArgsConstructor
public class DataExtractedDefault {
    private HashMap<String, PropertieValue> columnValue;
    public void addColumnValue(String columnName, PropertieValue value) {
        if(columnValue == null || columnValue.isEmpty()) {
            columnValue = new HashMap<>();
        }
        if(columnValue.containsKey(columnName)) {
            columnValue.put(columnName, value);
        }
    }



}
