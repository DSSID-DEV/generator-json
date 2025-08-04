package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Payload;
import com.dssid.dev.domain.model.VariableProperties;
import com.dssid.dev.utils.interfaces.ClazzUtilInterface;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Map;

import static com.dssid.dev.utils.JsonBuilder.extractType;
import static com.dssid.dev.verification.VerificationType.*;
import static com.dssid.dev.utils.JsonBuilder.isSerialVersionUID;

public class ClazzUtil {

    private  VariableProperties variable;
    private Clazz clazz;
    private Payload request;

//    public Object createNewInstance() {
//        variable = new VariableProperties();
//        return variable;
//    }

//    public Clazz createNewInsaceOfClazz() {
//        clazz = new Clazz();
//        return clazz;
//    }

//    @Override
    public Map<String, VariableProperties> GetPayloadProperties(ClassOrInterfaceDeclaration classDecl) {
        Map<String, VariableProperties> properties = new HashMap<>();
        classDecl.getFields().forEach(field -> {
            var classPropertie = new VariableProperties();
            field.getVariables().forEach(variable -> {
                Type typeField = field.getElementType();
                boolean isCollection = typeField.isArrayType();
                boolean isPrivate = typeField.isPrimitiveType();
                var propertyName = variable.getNameAsString();
                var type = extractType(typeField,  isCollection);

                classPropertie.setCollection(isCollection);
                classPropertie.setPrivate(isPrivate);
                classPropertie.setType(extractType(typeField, isCollection));

                if(isSerialVersionUID(propertyName)) return;
                if(!isCustomClass(type) && !isCollection) properties.put(propertyName, classPropertie);
                if(isCollection) properties.put(propertyName, null);
            });
        });
        return null;
    }


}
