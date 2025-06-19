package com.dssid.dev.utils;


import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.enums.VerbHttp;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.dssid.dev.utils.Constants.*;
import static com.dssid.dev.utils.Utils.getVerbHttp;
public class VerificationType {

    public static boolean isNumberTypeInteger(Class<?> type) {
        return type == int.class ||
                type == Integer.class ||
                type == long.class ||
                type == Long.class;
    }

    public static boolean isNumberTypeFloat(Class<?> type) {
        return type == float.class ||
                type == Float.class ||
                type == double.class ||
                type == Double.class ||
                type == BigDecimal.class;
    }

    public static boolean isTypeBoolean(Class<?> type) {
        return type == boolean.class ||
                type == Boolean.class;
    }

    public static boolean isTypeDateOrLocalDate(Class<?> type) {
        return type == Date.class ||
                type == LocalDate.class;
    }

    public static boolean isTypeLocalDateTime(Class<?> type) {
        return type == LocalDateTime.class;
    }

    public static boolean isMappingType(NodeList<AnnotationExpr> annotations, String verbHttp) {
        return getVerbHttp(annotations).equals(verbHttp);
    }

    public static boolean hasSingleParameter(List<Clazz> parameters) {
        return parameters.size() == 1;
    }

    public static boolean hasManyParameters(List<Clazz> parameters) {
        return parameters.size()  > 1;
    }

    public static boolean isTypeEnum(Class<?> type) {
        return type.isEnum();
    }

    public static boolean isCollectionOrArray(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || type.isArray();
    }

    public static boolean isCollectionOrArray(String returnType) {
        return returnType.contains("<") && returnType.contains(">");
    }

    public static boolean hasTagApiResponses(NodeList<AnnotationExpr> annotations) {
        for(var annotation: annotations) {
            if (annotation.getNameAsString().equals("@".concat(API_RESPONSES))
            || annotation.getNameAsString().equals("@".concat(API_RESPONSE))) return true;
        }
        return false;
    }

    public static boolean hasTagOperation(NodeList<AnnotationExpr> annotations) {
        for(var annotation: annotations) {
            if (annotation.getNameAsString().equals("@".concat(OPERATION))) return true;
        }
        return false;
    }

    public static boolean isVoid(String responseEntity, String object) {
        return responseEntity.equals(VOID[0]) || responseEntity.equals(VOID[1])
                || object.equals(VOID[0]) || object.equals(VOID[1]);
    }

    public static boolean isGenerics(String object) {
        return object.equals(GENERICS[0]) || object.equals(GENERICS[1]);
    }

    public static boolean valueIsBoolean(Object value) {
        var clazz = value.getClass();
        return isTypeBoolean(clazz);
    }

    public static boolean isNumberFloatDouble(Object value) {
        var clazz = value.getClass();
        return isNumberTypeFloat(clazz);
    }

    public static boolean isPost(String verbHttp) {
        return verbHttp.equals(VerbHttp.POST.name());
    }

    public static boolean isPut(String verbHttp) {
        return verbHttp.equals(VerbHttp.PUT.name());
    }

    public static boolean isPatch(String verbHttp) {
        return  verbHttp.equals(VerbHttp.PATCH.name());
    }

    public static boolean isPutOrPatch(String verbHttp) {
        return isPut(verbHttp) || isPatch(verbHttp);
    }

    public static boolean isGet(String method) {
        return !isPost(method) && method.equals(VerbHttp.GET.name());
    }


    public static boolean hasBody(List<Clazz> parameters) {
        return !parameters.isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }
    public static boolean stringNotHasContent(String str) {
        return !isNotBlank(str);
    }
    public static boolean containsDiamoent(String str) {
        return str.contains("<") && str.contains(">");
    }

    public static boolean isNotPublic(MethodDeclaration method) {
        return !method.hasModifier(Modifier.Keyword.PUBLIC);
    }

    public static boolean notHasSwaggerAnnotations(MethodDeclaration method) {
        return !method.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getNameAsString().equals(OPERATION));
    }

    public static boolean containsUnwantedImports(String imprt) {
        for(var imp: IMPORTATIONS) {
            if(imprt.contains(imp)) return true;
        };
        return false;
    }

    public static boolean isWindows() {
        return !isUnix();
    }

    public static boolean isUnix() {
        return FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains(POSIX);
    }
}