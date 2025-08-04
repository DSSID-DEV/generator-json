package com.dssid.dev.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Payload {

    private String className;
    private String tableName;

    List<VariableProperties> properties;

    private List<Payload> collection;
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<VariableProperties> getProperties() {
        return this.properties;
    }

    public void addPropertie(VariableProperties propertie) {
        if(this.properties == null) this.properties = new ArrayList<>();
        this.properties.add(propertie);
    }

    public void addPayload(Payload extractedPayload) {
        if(this.collection == null) this.collection = new ArrayList<>();
        this.collection.add(extractedPayload);
    }
    public List<Payload> allPayloadClasses() {
        return this.collection;
    }
}
