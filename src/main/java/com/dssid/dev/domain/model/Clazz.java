package com.dssid.dev.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Clazz {
    private String name;
    private String type;
    private String typeParameter;
    private boolean isCollection = false;
}
