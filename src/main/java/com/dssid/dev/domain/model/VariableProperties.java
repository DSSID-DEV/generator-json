package com.dssid.dev.domain.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VariableProperties {
    private String name;
    private String columnName;
    private String type;
    private Object value;
    private boolean isPrivate;
    private boolean isCollection;
}
