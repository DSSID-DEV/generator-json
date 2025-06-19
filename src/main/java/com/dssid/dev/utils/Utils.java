package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Structure;
import com.dssid.dev.enums.METHOD;
import com.dssid.dev.enums.TypeParameter;
import com.dssid.dev.enums.TypeReturn;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

import static com.dssid.dev.utils.Constants.*;
import static com.dssid.dev.utils.VerificationType.*;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static Path definePath(String strPath) {
        var path = hasContent(strPath) ? strPath : TARGET_PATH;
        return Paths.get(path);
    }

    public static void createDirectories(Path path) {
        try{
            //Verificar se o diretório não existe
            if(Files.notExists(path)) {
                //Verificar se o SO é Unix-like e dá permissão para manipular o arquivo
                if(isUnix()) allowWriting(path);
                //Criar diretório sem passar o attributes
                else createDirecoty(path);
                LOG.info("Directory " + path + " created with success");
            }
        } catch(Exception e) {
            LOG.error(ERROR_TRYING_TO_CREATE_DIRECTORY);
            throw new RuntimeException(ERROR_TRYING_TO_CREATE_DIRECTORY + path);
        }
    }

    private static void createDirecoty(Path path) throws IOException {
        Files.createDirectory(path);
    }

    private static void allowWriting(Path path) throws IOException {

        LOG.info("Checking permission");
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(PERMISSIO_RWXR);
        var attributes = PosixFilePermissions.asFileAttribute(permissions);

        //Criar diretório com permissão para manipulação de arquivo
        createDirectory(path, attributes);
    }

    private static void createDirectory(Path path, FileAttribute<Set<PosixFilePermission>> attributes) throws IOException {
        Files.createDirectories(path, attributes);
    }
    public static boolean hasContent(String str) {
        return StringUtils.isNotBlank(str);
    }

    public static void configurationFormatJson(ObjectMapper mapper) {
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void getValueEnum(String name, Class<?> type, ObjectNode node) {

        for (var map: valueEnum(type).entrySet()){
            if(map.getKey().equals(STRING)) node.put(name, ENUM_.concat(map.getValue().toString().toUpperCase()));
            if(map.getKey().equals(INTEGER)) node.put(name, Integer.parseInt(map.getValue().toString()));
            if(map.getKey().equals(DOUBLE)) node.put(name, Double.parseDouble(map.getValue().toString()));
            if(map.getKey().equals(BOOLEAN)) node.put(name, Boolean.parseBoolean(map.getValue().toString()));
        }
    }

    //Obter o valor quando for um enum
    public static Map<String, Object> valueEnum(Class<?> type) {
        Object[] constants = type.getEnumConstants();
        Map<String, Object> typeValues = new HashMap<>();
        if(constants.length > 0) {
            Object constant = constants[0];
            if(constant == String.class) typeValues.put(STRING, "enum_".concat(constant.toString()));
            if(valueIsBoolean(constant)) typeValues.put(BOOLEAN, true);
            if(constant == int.class) typeValues.put(INTEGER, 1);
            if(isNumberFloatDouble(constant)) typeValues.put(DOUBLE, 1.0);
        }
        return typeValues;
    }

    public static String fileName(String str) {
        if(stringNotHasContent(str)) return "";
        int lastIndexDot = lastIndexOf(str, ".");
        return lastIndexDot >= 0? str.substring(lastIndexDot +1) : str;
    }

    public static Class<?> getNameClassInCollection(String str) throws ClassNotFoundException {
        var clazzName = str.substring(str.indexOf("<") + 1, lastIndexOf(str, ">"));
        return Class.forName(clazzName);
    }

    public static String formatarCamelCaseParaEspacado(String str) {
        if(StringUtils.isBlank(str)) return str;
        return str
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .toLowerCase();
    }

    public static String getVerbHttp(NodeList<AnnotationExpr> annotationss) {
        var ann = annotationss.stream()
                .map(AnnotationExpr::getNameAsString)
                .filter(annotation -> ANNOTATIONS_MAPPING.contains(annotation))
                .findFirst().orElse(null);

        return ann.substring(0, ann.lastIndexOf("M")).toUpperCase();
    }

    public static String[] createSummaryAndDescription(METHOD method) {
        if(isPost(method)) return new String[]{"Register new ", "Endpoint to "};
        else if(isPutOrPatch(method)) return new String[]{"Update a ", "Endpoint to "};
        else if(isGet(method)) return new String[]{"Return to ", "Endpoint to "};
        else return new String[]{"Remove to ", "Endpoint to "};
    }
    public static int lastIndexOf(String str, String regex) {
        return str.lastIndexOf(regex);
    }

    public static Map<String, String> extractObject(MethodDeclaration methodDeclaration, boolean response) {
        Map<String, String> responseObject = new HashMap<>();
        System.out.println("METHODO -> " + methodDeclaration);

//        System.out.println("toDescriptor -> " + methodDeclaration.toDescriptor());
        System.out.println("getName -> " + methodDeclaration.getName());
        System.out.println("getParameters -> " + methodDeclaration.getParameters());
        System.out.println("getBody -> " + methodDeclaration.getBody());
        System.out.println("getNameAsString -> " + methodDeclaration.getNameAsString());
        System.out.println("getMetaModel -> " + methodDeclaration.getMetaModel());
        System.out.println("toString -> " + methodDeclaration.toString());

        System.out.println("String.valueOf(methodDeclaration) -> " + String.valueOf(methodDeclaration));
        System.out.println("getDeclarationAsString -> " + methodDeclaration.getDeclarationAsString());

        System.out.println("getType -> " + methodDeclaration.getType());
        System.out.println("getTypeAsString -> " + methodDeclaration.getTypeAsString());
        System.out.println("toTypeDeclaration -> " + methodDeclaration.toTypeDeclaration());
        responseObject.put("Object", "Object");
        return responseObject;
    }

    public static Structure loadMethodStructure(MethodDeclaration method) {
        return Structure.builder()
                .verb(getVerbHttp(method.getAnnotations()))
                .methodName(method.getNameAsString())
                .parameters(getRequestParameters(method.getParameters()))
                .response(getResponse(method.getTypeAsString()))
                .hasOperation(hasTagOperation(method.getAnnotations()))
                .hasApiResponses(hasTagApiResponses(method.getAnnotations()))
                .build();
    }

    private static Map<TypeReturn, Clazz> getResponse(String responseObject) {
        Map<TypeReturn, Clazz> responseEntity = new HashMap<>();
        var clazz = new Clazz();
        var object = extractObjectType(responseObject);
        if(isVoid(responseObject, object)) {
            responseEntity.put(TypeReturn.VOID, null);
            return responseEntity;
        }
        if(isCollectionOrArray(object)) {
            clazz.setCollection(true);
            clazz.setType(extractObjectType(object));
        }
        else if(isGenerics(object)) {
            clazz.setCollection(false);
            clazz.setType(OBJECT);
        }
        else {
            clazz.setCollection(false);
            clazz.setType(object);
        }
        responseEntity.put(TypeReturn.OBJECT, clazz);
        return responseEntity;
    }

    private static String extractObjectType(String object) {
        int startIndex = object.indexOf("<")+1;
        int lastIndex = object.lastIndexOf(">");
        return object.substring(startIndex, lastIndex);
    }

    private static Map<TypeParameter, List<Clazz>> getRequestParameters(NodeList<Parameter> paramters) {
        Map<TypeParameter, List<Clazz>> requestParameters = new HashMap<>();

        requestParameters.put(TypeParameter.BODY, new ArrayList<>());
        requestParameters.put(TypeParameter.PARAMETER, new ArrayList<>());


        int amountTypeParameter = amountTypeParameter(paramters);
        paramters.forEach(parameter -> {
            var clazz = new Clazz();
            clazz.setName(parameter.getNameAsString());
            clazz.setCollection(false);
            if (isBody(parameter.getAnnotations())) {
                clazz.setTypeParameter(TypeParameter.BODY.name());
                clazz.setType(parameter.getTypeAsString());
                if (parameter.getType().isArrayType()) {
                    clazz.setType(parameter.getTypeAsString());
                    clazz.setCollection(true);
                }
                requestParameters.get(TypeParameter.BODY).add(clazz);
            } else if (isPathVariable(parameter.getAnnotations())) {
                clazz.setTypeParameter(getTypeParameter(parameter.getAnnotations()));
                clazz.setType(parameter.getTypeAsString());
                clazz.setCollection(amountTypeParameter > 1);
                requestParameters
                        .get(TypeParameter.PARAMETER).add(clazz);
            } else {
                clazz.setCollection(amountTypeParameter < 2);
                clazz.setTypeParameter(getTypeParameter(parameter.getAnnotations()));
                clazz.setType(parameter.getTypeAsString());
                requestParameters
                        .get(TypeParameter.PARAMETER).add(clazz);
            }
        });
        return requestParameters;
    }

    private static String getTypeParameter(NodeList<AnnotationExpr> annotations) {
        return annotations.stream().filter(annotation -> ANNOTATIONS_PARAMETERS.contains(annotation.getName().asString()))
                .findFirst().get().getName().asString();
    }

    private static int amountTypeParameter(NodeList<Parameter> parameters) {
        return (int) parameters.stream().filter(parameter -> parameter.getNameAsString()
                        .equals("@".concat(ANNOTATIONS_PARAMETERS.get(2)))
                || parameter.getNameAsString()
                .equals("@".concat(ANNOTATIONS_PARAMETERS.get(0))))
                .count();
    }

    private static boolean isPathVariableOrRequestParam(NodeList<AnnotationExpr> annotations) {
        return annotations.stream()
                .filter(annotation -> annotation.getNameAsString()
                        .equals("@".concat(ANNOTATIONS_PARAMETERS.get(2)))
                        || annotation.getNameAsString()
                        .equals("@".concat(ANNOTATIONS_PARAMETERS.get(0))))
                .count() > 0;
    }
    private static boolean isPathVariable(NodeList<AnnotationExpr> annotations) {
        return false;
    }

    private static boolean isBody(NodeList<AnnotationExpr> annotations) {
        return annotations.stream().
                filter(annotation -> annotation.getName().asString().equals(ANNOTATIONS_PARAMETERS.get(3)))
                .count() > 0;
    }

}
