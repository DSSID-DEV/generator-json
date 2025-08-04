package com.dssid.dev.verification;


import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.VariableProperties;
import com.dssid.dev.enums.TypeParameter;
import com.dssid.dev.enums.VerbHttp;
import com.dssid.dev.utils.JsonBuilder;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.utils.FileUtils.getCompilationUnitClass;
import static com.dssid.dev.utils.Utils.getVerbHttp;
import static com.dssid.dev.utils.Utils.normalize;
import static com.dssid.dev.view.components.LogMessage.logMessage;

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


    public static boolean isBytes(Class<?> type) {
        return type == byte.class ||
                type == Byte.class;
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

    public static boolean hasParameters(Map<TypeParameter, List<Clazz>> parameters) {
        return !parameters.get(TypeParameter.PARAMETER).isEmpty();
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

    public static boolean isPostOrPut(String verbHttp) {
        return isPost(verbHttp) || isPut(verbHttp);
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

    public static boolean hasProperties(List<VariableProperties> properties) {
        return properties != null && !properties.isEmpty();
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

    public static boolean isEntity(Path pathClazz) {
        CompilationUnit compalitionUnit = null;
        try {
            compalitionUnit = getCompilationUnitClass(pathClazz.toFile());
            ClassOrInterfaceDeclaration clazz = JsonBuilder.getClassOrInterfaceDeclaration(compalitionUnit);
            var annotation = clazz.getAnnotationByName("Entity");
            return annotation.isPresent();
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public static boolean containsAnnotation(Path file, JTextArea logArea) {
        try {
            String content = Files.readString(file);
            var isEntity = isEntity(content);
            logMessage(content, logArea);
            return isEntity;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isEntity(String content) {
        return content.contains(ENTITY) ||
                content.contains(JAVAX_PERSISTENCE_ENTITY) ||
                content.contains(JAKARTA_PERSISTENCE_ENTITY);
    }

    public static boolean isCustomClass(String type) {
        return !isNumberInteger(type) && !isNumberDouble(type) &&
                !isBoolean(type) && !isEnum(type) && !isString(type)
                && !isAnyDate(type);
    }

    public static boolean isNotPublic(MethodDeclaration method) {
        return !method.hasModifier(Modifier.Keyword.PUBLIC);
    }

    public static boolean notHasSwaggerAnnotations(MethodDeclaration method) {
        return !method.getAnnotations().stream()
                .anyMatch(annotation -> annotation
                        .getNameAsString()
                        .equals(OPERATION));
    }

    public static boolean containsUnwantedImports(String imprt) {
        for(var imp: IMPORTATIONS) {
            if(imprt.contains(imp)) return true;
        }
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

    public static boolean isSimilar(String value, String str) {
        if (value == null || str == null) return false;

        String s1 = normalize(value);
        String s2 = normalize(str);

        // Verificação direta
        if (s1.equals(s2) || s2.contains(s1) || s1.contains(s2)) {
            return true;
        }

        // Verificação de similaridade geral
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        double scoreGeral = similarity.apply(s1, s2);
        if (scoreGeral >= LIMIAR_SIMILARIDADE) {
            return true;
        }

        // Verificação por partes (sliding window)
        return verificarPorPartes(s1, s2, similarity);
    }

    private static boolean verificarPorPartes(String s1, String s2, JaroWinklerSimilarity similarity) {
        int windowSize = s1.length();
        for (int i = 0; i <= s2.length() - windowSize; i++) {
            String substring = s2.substring(i, i + windowSize);
            double score = similarity.apply(s1, substring);
            if (score >= LIMIAR_SIMILARIDADE) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEntity(ClassOrInterfaceDeclaration classDecl) {
        return classDecl.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals("Entity"));
    }
    public static boolean isEnum(String type) {
        return type.equals("enum");
    }

    public static boolean isLocalDateTime(String type) {
        return type.equals("LocalDateTime");
    }

    public static boolean isLocalDate(String type) {
        return type.equals("LocalDate");
    }

    public static boolean isDate(String type) {
        return type.equals("Date");
    }

    public static boolean isArrayOrCollection(String type) {
        return false;
    }

    public static boolean isByte(String type) {
        return TYPES_BYTES.contains(type);
    }

    public static boolean isBoolean(String type) {
        return TYPES_BOOLEANS.contains(type);
    }

    public static boolean isAnyDate(String type) {
        return TYPES_DATES.contains(type);
    }

    public static boolean isString(String type) {
        return TYPES_STRINGS.contains(type);
    }

    public static boolean isNumberDouble(String type) {
        return TYPES_DECIMAL_NUMBERS.contains(type);
    }

    public static boolean isNumberInteger(String type) {
        return TYPES_WHOLE_NUMBERS.contains(type);
    }
}