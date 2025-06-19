package com.dssid.dev.domain.model;


import com.dssid.dev.enums.TypeParameter;
import com.dssid.dev.enums.TypeReturn;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class Structure {
    private String methodName;
    private String verb;
    private Map<TypeReturn, Clazz> response = new HashMap<>();
    private Map<TypeParameter, List<Clazz>> parameters = new HashMap<>();
    private boolean hasOperation = false;
    private boolean hasApiResponses = false;

}
