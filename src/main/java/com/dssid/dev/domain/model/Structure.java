package com.dssid.dev.domain.model;


import com.dssid.dev.enums.TypeParameter;
import com.dssid.dev.enums.TypeReturn;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

@Getter
@Builder
public class Structure {
    private String controllerName;
    private String methodName;
    private String verbHttp;
    private Map<TypeReturn, Clazz> response = new HashMap<>();
    private Map<TypeParameter, List<Clazz>> parameters = new HashMap<>();
    private boolean hasOperation = false;
    private boolean hasApiResponses = false;

    private Set<String> clazzes;

    public void addObjectName(String objectName) {
        if(clazzes == null) {
            clazzes = new HashSet<>();
        }
        clazzes.add(objectName);
    }
}
