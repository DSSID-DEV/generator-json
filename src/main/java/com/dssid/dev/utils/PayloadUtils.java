package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Payload;
import com.dssid.dev.domain.model.VariableProperties;
import com.dssid.dev.utils.interfaces.ClazzUtilInterface;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import lombok.NoArgsConstructor;

import java.io.FileNotFoundException;
import java.util.Optional;

import static com.dssid.dev.constants.Constants.COLUMN;
import static com.dssid.dev.utils.AnnotationUtils.extractColumnName;
import static com.dssid.dev.utils.FileUtils.getRequestProperties;
import static com.dssid.dev.utils.JsonBuilder.extractType;
import static com.dssid.dev.utils.JsonBuilder.isSerialVersionUID;
import static com.dssid.dev.utils.Utils.toSnakeCamelCase;
import static com.dssid.dev.verification.VerificationType.isCustomClass;
import static com.dssid.dev.verification.VerificationType.isSimilar;

@NoArgsConstructor
public class PayloadUtils implements ClazzUtilInterface {
    private Payload payload;

   public PayloadUtils(Payload payload) {
        this.payload = payload;
    }

    @Override
    public Payload getPayloadProperties(ClassOrInterfaceDeclaration classDecl, ClassOrInterfaceDeclaration entity) {

       payload.addPayload(payload);
        entity.getFields().forEach(fieldEntity -> {
            fieldEntity.getVariables().forEach(variableEntity -> {
                if(isSerialVersionUID(variableEntity.getNameAsString())) return;
                var fieldNameEntity = variableEntity.getNameAsString();

                classDecl.getFields().forEach(fieldClassDecl -> {

                    fieldClassDecl.getVariables().forEach(variableClassDecl -> {
                        if(isSerialVersionUID(variableClassDecl.getNameAsString())) return;

                        var fieldNameClassDecl = variableClassDecl.getNameAsString();

                        if(isSimilar(fieldNameEntity, fieldNameClassDecl)) {
                            var annotation = fieldEntity.getAnnotationByName(COLUMN);
                            try {
                                getPropertiesToEntity(fieldClassDecl, annotation, variableEntity, fieldNameClassDecl);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    return;
                    });
                    return;
                });
                return;
            });
        });
        return payload;
    }

    @Override
    public Payload getInstanceProperties(ClassOrInterfaceDeclaration entity, String table, String className) {
        var instanceEntity = new Payload();
        instanceEntity.setTableName(table);
        instanceEntity.setClassName(className);

        entity.getFields().forEach(fieldEntity -> {
            fieldEntity.getVariables().forEach(variableEntity -> {
                if(isSerialVersionUID(variableEntity.getNameAsString())) return;
                if(fieldEntity.getElementType().isArrayType()) return;
                var variable = extractVariableProperties(fieldEntity);
                var columnName = extractColumnName(fieldEntity, variableEntity, variable);
                variable.setColumnName(columnName);
                variable.setName(variableEntity.getNameAsString());

                instanceEntity.addPropertie(variable);

                });
            });

       return instanceEntity;
    }

    private VariableProperties extractVariableProperties(FieldDeclaration field) {
        var variableProperties = new VariableProperties();
        var isPrivate = field.isPrivate();
        var fildType = field.getElementType();
        var isCollection = fildType.isArrayType();
        var type = extractType(fildType, isCollection);
        variableProperties.setType(type);
        variableProperties.setCollection(isCollection);
        variableProperties.setPrivate(isPrivate);
        return variableProperties;
    }

    private void getPropertiesToEntity(FieldDeclaration field, Optional<AnnotationExpr> annotation, VariableDeclarator variable, String fieldName) throws FileNotFoundException {
        var property = new VariableProperties();
        Type typeField = field.getElementType();
        boolean isCollection = typeField.isArrayType();
        boolean isPrivate = typeField.isPrimitiveType();
        var type = extractType(typeField,  isCollection);

        property.setCollection(isCollection);
        property.setName(fieldName);
        property.setType(type);
        property.setPrivate(isPrivate);

        if(isCustomClass(type)) {
            getRequestProperties(type);
//            property.getColumnName()
            return;
        }
        String columnName;
        if(annotation.isPresent()) {
            columnName = extractColumnName(field, variable, property);
        }else {
            columnName = toSnakeCamelCase(fieldName);
        }
        property.setColumnName(columnName);

        payload.addPropertie(property);

    }
}
