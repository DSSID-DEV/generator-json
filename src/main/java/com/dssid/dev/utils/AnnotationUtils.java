package com.dssid.dev.utils;

import com.dssid.dev.domain.model.VariableProperties;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.Optional;

import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.utils.FileUtils.loadObject;
import static com.dssid.dev.utils.JsonBuilder.getClassOrInterfaceDeclaration;
import static com.dssid.dev.utils.Utils.toSnakeCamelCase;
import static com.dssid.dev.verification.VerificationType.*;

public class AnnotationUtils {
    public static String extractColumnName(FieldDeclaration field, VariableDeclarator variable, VariableProperties property) {

        var annotationOptional = field.getAnnotationByName(JOIN_COLUMN);

        if(annotationOptional.isPresent() && !property.isCollection()) {
            if(isCustomClass(property.getType())) {
                var object = loadObject(property.getType().concat(DOT_JAVA));
                property.setValue(object);
                return resolveColumnName(field, variable);
            }
        }

        annotationOptional = field.getAnnotationByName(COLUMN);

        if(!annotationOptional.isPresent() && !property.isCollection()) return toSnakeCamelCase(variable.getNameAsString());

        return resolveColumnName(field, variable);
    }

    private static String resolveColumnName(FieldDeclaration field, VariableDeclarator variable) {
        //Verifica anotação @Column com name explícito
        Optional<AnnotationExpr> columnAnnotation = field.getAnnotationByName(COLUMN);
        if (columnAnnotation.isPresent() && columnAnnotation.get().isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotationExpr = columnAnnotation.get().asNormalAnnotationExpr();
            for (var pair : normalAnnotationExpr.getPairs()) {
                if (pair.getNameAsString().equals("name")) return extractAnnotationStringValue(pair.getValue());
            }
        }

        //Verifica relacionamentos (@ManyToOne, @OneToOne, etc.)
        for (String annName : RELATION_SHIP_ANNOTATIONS) {
            Optional<AnnotationExpr> relAnnotation = field.getAnnotationByName(annName);
            if (relAnnotation.isPresent()) {
                if (annName.equals(JOIN_COLUMN) || annName.equals(JOIN_TABLE)) {
                    // Extrai name diretamente da anotação de relacionamento
                    return extractNameFromRelationshipAnnotation(relAnnotation.get(), variable.getNameAsString());
                } else {
                    // Para outras anotações de relacionamento, verifica se tem @JoinColumn
                    Optional<AnnotationExpr> joinColumn = field.getAnnotationByName(JOIN_COLUMN);
                    if (joinColumn.isPresent()) {
                        return extractNameFromRelationshipAnnotation(joinColumn.get(), variable.getNameAsString());
                    }
                }
            }
        }
        return toSnakeCamelCase(variable.getNameAsString());
    }

    private static String extractNameFromRelationshipAnnotation(AnnotationExpr annotationExpr, String defaultName) {
        if (annotationExpr.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotationExpr = annotationExpr.asNormalAnnotationExpr();
            for (var pair : normalAnnotationExpr.getPairs()) {
                if (pair.getNameAsString().equals("name")) return extractAnnotationStringValue(pair.getValue());
            }
        }
        //Retornar padrão para relacionamento : nomeDaPropriedade + "_id";
        return toSnakeCamelCase(defaultName);
    }

    public static String extractTableFromEntity(CompilationUnit classJava) {

        ClassOrInterfaceDeclaration classDecl = getClassOrInterfaceDeclaration(classJava);


        //TODO: Tratar se caso não entidade
        if (!isEntity(classDecl)) return null;

        var tableAnnotation = classDecl.getAnnotationByName("Table");

        //TODO: Tratar se a anotação não for um @Table
        if (!tableAnnotation.isPresent()) return "";

        var tableAnnotationExpr = tableAnnotation.get();
        //TODO: Tratar se não for uma annotationExpr
        if (!tableAnnotationExpr.isNormalAnnotationExpr()) return "";

        var normalAnnotationExpr = tableAnnotationExpr.asNormalAnnotationExpr();

        for (var memberPair : normalAnnotationExpr.getPairs()) {
            if (memberPair.getNameAsString().equals("name")) {
                String tableName = memberPair.getValue().toString();
                if (memberPair.getValue() instanceof StringLiteralExpr) {
                    tableName = ((StringLiteralExpr) memberPair.getValue()).getValue();
                }
                return tableName;
            }
        }
        return classDecl.getNameAsString().toLowerCase();
    }

    private static String extractAnnotationStringValue(Expression value) {
        if (value instanceof StringLiteralExpr) return ((StringLiteralExpr) value).getValue();
        return value.toString().replace("\"", "");
    }

}
