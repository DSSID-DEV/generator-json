package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Payload;
import com.dssid.dev.repository.CustomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.constants.MessageLog.*;
import static com.dssid.dev.utils.FileUtils.getCompilationUnitClass;
import static com.dssid.dev.utils.FileUtils.getPermission;
import static com.dssid.dev.utils.SearchFileJava.findClassFiles;
import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.verification.VerificationType.*;
import static com.dssid.dev.view.components.LogMessage.logMessage;

public class JsonBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(JsonBuilder.class);

    static JTextArea logArea;
    static String pathRootProject;
    static Path pathMainResources;
    static ObjectMapper objectMapper;

    static String table;

    static Map<String, String> attributeOfInstance;
    static Map<String, String> propertieColumn;

    static Payload payload;


    public static void buildJsonFile(String pathRoot, Payload payloadExtracted, String tableName, boolean isCollection, JTextArea jTextArea) throws IOException {
        logArea = jTextArea;
        table = tableName;
        payload = payloadExtracted;
        pathRootProject = pathRoot;
        var classNameFormatt =  payload.getClassName().replace(DOT_JAVA, "");
                //+ "s" + DOT_JSON : clazz.replace(DOT_JAVA, "") + DOT_JSON;
        pathMainResources = Paths.get(pathRoot.concat(TARGET_PATH));

        LOG.info(CHECKING_IF_EXISTS_FILE_DOT_JAVA);
        logMessage(CHECKING_IF_EXISTS_FILE_DOT_JAVA, logArea);

        createDirectories(pathMainResources, logArea);


        objectMapper = new ObjectMapper();

        var json = buildJson(payload.getClassName().replace(DOT_JAVA, ""), table, isCollection);

        var file = pathMainResources.resolve(classNameFormatt);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), json);
    }

    public static void buildJsonFile(Path pathClassJava, String pathRoot, String clazz, boolean isCollection, Payload payloadExtracted, JTextArea jTextArea) throws IOException {
        logArea = jTextArea;
        payload = payloadExtracted;
        pathRootProject = pathRoot;
        var classNameFormatt = isCollection ? clazz.replace(DOT_JAVA, "") + "s" + DOT_JSON : clazz.replace(DOT_JAVA, "") + DOT_JSON;
        pathMainResources = Paths.get(pathRoot.concat(TARGET_PATH));

        LOG.info(CHECKING_IF_EXISTS_FILE_DOT_JAVA);
        logMessage(CHECKING_IF_EXISTS_FILE_DOT_JAVA, logArea);

        createDirectories(pathMainResources, logArea);

        List<Clazz> classProperties = null;
        try {
            classProperties = extractClassJavaProperties(pathClassJava, clazz);
        } catch (FileNotFoundException e) {
            LOG.error(messageCuston(GETTING_COMPILATION_UNID_CLASS, clazz), e);
            logMessage(messageWithException(ERROR_TRYING_TO_GET_CLASS_FILE, e), logArea);
        }

        if (classProperties.isEmpty()) return;

        objectMapper = new ObjectMapper();

        var json = buildJson(classProperties, clazz.replace(DOT_JAVA, ""), table, isCollection);

        var file = pathMainResources.resolve(classNameFormatt);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), json);
    }

    private static List<Clazz> extractClassJavaProperties(Path pathClassJava, String clazz) throws FileNotFoundException {

        LOG.info(messageCuston(GETTING_COMPILATION_UNID_CLASS, clazz));
        logMessage(messageCuston(GETTING_COMPILATION_UNID_CLASS, clazz), logArea);

        File fileJava = getPermission(pathClassJava);
        var classJava = getCompilationUnitClass(fileJava);

        table = extractTableFromEntity(classJava);

        propertieColumn = extractColumnNameOrPropertie(classJava);

        return extractProperties(classJava);
    }

    private static Map<String, String> extractColumnNameOrPropertie(CompilationUnit classJava) {
        ClassOrInterfaceDeclaration classDecl = getClassOrInterfaceDeclaration(classJava);

        //TODO: Tratar se caso não entidade
        if (!isEntity(classDecl)) return null;

        Map<String, String> columnMap = new HashMap<>();

        //TODO: Tratar se a anotação não for um @Table
        classDecl.getFields().forEach(field -> {
            field.getVariables().forEach(variable -> {
                var propertyName = variable.getNameAsString();
                if (isSerialVersionUID(propertyName)) return;

                if (hasAnnotationOneToMany(field.getAnnotations())) return;

                var columnName = resolveColumnName(field, variable);
                columnMap.put(propertyName, columnName);
            });
        });
        return columnMap;
    }

    public static boolean isSerialVersionUID(String propertyName) {
        return propertyName.equals(SERIAL_VERSION_UID);
    }

    public static ClassOrInterfaceDeclaration getClassOrInterfaceDeclaration(CompilationUnit classJava) {
        ClassOrInterfaceDeclaration classDecl = classJava.findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new IllegalArgumentException("O arquivo não contém uma classe"));
        return classDecl;
    }

    private static boolean hasAnnotationOneToMany(NodeList<AnnotationExpr> annotations) {
        return annotations.stream().map(AnnotationExpr::getNameAsString)
                .anyMatch(annotation -> annotation.contains("OneToMany"));
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
    private static String extractAnnotationStringValue(Expression value) {
        if (value instanceof StringLiteralExpr) return ((StringLiteralExpr) value).getValue();
        return value.toString().replace("\"", "");
    }

    private static String extractTableFromEntity(CompilationUnit classJava) {

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

    private static ObjectNode buildJson(String className, String table, boolean isCollection) {

        configurationFormatJson(objectMapper);

        var objectNode = objectMapper.createObjectNode();

        var repository = new CustomRepository();

//        var propertiesOfRequest = getPropertiesClassOfRequest(propertieColumn, classProperties);


        var propertieValues = repository.getPropertieValueOfInstance(payload, table);

        payload.getProperties().forEach(property -> {
            var name = property.getName();
            if (isSerialVersionUID(name)) return;
            var propertyNode = objectMapper.createObjectNode();
            if (isString(property.getType())) objectNode.put(name, getString(propertieValues.get(name), name));
            else if (isNumberInteger(property.getType())) objectNode.put(name, getInteger(propertieValues.get(name)));
            else if (isNumberDouble(property.getType())) objectNode.put(name, getDouble(propertieValues.get(name)));
            else if (isBoolean(property.getType())) objectNode.put(name, true);
            else if (isByte(property.getType())) objectNode.put(name, getByte(propertieValues.get(name)));
            else if (isDate(property.getType())) objectNode.put(name, getDate(propertieValues.get(name)));
            else if (isLocalDate(property.getType())) objectNode.put(name, getLocalDate(propertieValues.get(name)));
            else if (isLocalDateTime(property.getType()))
                objectNode.put(name, getLocalDateTime(propertieValues.get(name)));
            else if (isEnum(property.getType())) objectNode.put(name, propertieValues.get(name).toString());
//            else if(isArrayOrCollection(property.getType())) propertyNode.put(name, getArrayOrColletion(property.getType()));
            else objectNode.put(property.getName(), getObject(property.getType())); //getObject(property.getType()));
        });

        return objectNode;
    }

    private static ObjectNode buildJson(List<Clazz> classProperties, String className, String table, boolean isCollection) {

        configurationFormatJson(objectMapper);

        var objectNode = objectMapper.createObjectNode();

        var repository = new CustomRepository();

        var propertiesOfRequest = getPropertiesClassOfRequest(propertieColumn, classProperties);


        var propertieValue = repository.getPropertieValueOfInstance(propertieColumn, classProperties, table);

        classProperties.forEach(property -> {
            var name = property.getName();
            if (isSerialVersionUID(name)) return;
            var propertyNode = objectMapper.createObjectNode();
            if (isString(property.getType())) objectNode.put(name, getString(propertieValue.get(name), name));
            else if (isNumberInteger(property.getType())) objectNode.put(name, getInteger(propertieValue.get(name)));
            else if (isNumberDouble(property.getType())) objectNode.put(name, getDouble(propertieValue.get(name)));
            else if (isBoolean(property.getType())) objectNode.put(name, true);
            else if (isByte(property.getType())) objectNode.put(name, getByte(propertieValue.get(name)));
            else if (isDate(property.getType())) objectNode.put(name, getDate(propertieValue.get(name)));
            else if (isLocalDate(property.getType())) objectNode.put(name, getLocalDate(propertieValue.get(name)));
            else if (isLocalDateTime(property.getType()))
                objectNode.put(name, getLocalDateTime(propertieValue.get(name)));
            else if (isEnum(property.getType())) objectNode.put(name, propertieValue.get(name).toString());
//            else if(isArrayOrCollection(property.getType())) propertyNode.put(name, getArrayOrColletion(property.getType()));
            else objectNode.put(property.getName(), getObject(property.getType())); //getObject(property.getType()));
        });

        return objectNode;
    }

    private static HashMap<String, String> getPropertiesClassOfRequest(Map<String, String> propertieColumn, List<Clazz> classProperties) {

        HashMap<String, String> columns = new HashMap<>();
        propertieColumn.entrySet().forEach(propertie -> {

            propertie.getKey().contains(nameProperty(propertie.getKey(),payload));
        });
        return null;
    }

    private static String nameProperty(String str, Payload payload) {
        return "true";
    }

    private static String getLocalDateTime(Object value) {
        return value != null && isNotBlank(String.valueOf(value)) ?
                LocalDateTime.parse(value.toString()).toString() :
                LocalDateTime.now().toString();
    }

    private static String getLocalDate(Object value) {
        return value != null && isNotBlank(String.valueOf(value)) ? LocalDate.parse(value.toString()).toString() :
        LocalDate.now().toString();
    }

    private static String getDate(Object value) {
        return value != null && isNotBlank(String.valueOf(value)) ?
                new Date(value.toString()).toString() :
                new Date().toString();
    }

    private static ObjectNode getObject(String type) {
        var object = getNameClassOfCollection(type);
        if(object.equals("?") || object.equals("T")) return null;
        var findClass = findClassFiles(Paths.get(pathRootProject), object.concat(DOT_JAVA), logArea);
        List<Clazz> properties = null;
        try {
            properties = extractClassJavaProperties(findClass, object);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return buildJson(properties, object, table, false);
    }

    private static ArrayNode getArrayOrColletion(String type) {
        if(type.contains("ResponseEntity")) type = extractObjectType(type);
        var arrayNode = objectMapper.createArrayNode();
        var node = getObject(getNameClassOfCollection(type));
        arrayNode.add(node);
        return arrayNode;
    }

    public static void configurationFormatJson(ObjectMapper mapper) {
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private static byte[] getByte(Object value) {
        return value != null && isNotBlank(value.toString()) ?
                value.toString().getBytes() :
                new byte[0];
    }

    private static BigDecimal getDouble(Object value) {
        return value != null ?
                BigDecimal.valueOf(Double.parseDouble(value.toString())) :
                BigDecimal.TEN.setScale(4, RoundingMode.DOWN);
    }

    private static Integer getInteger(Object value) {
        return value != null ? Integer.parseInt(value.toString()) : 1;
    }

    private static String getString(Object value, String name) {
        return value != null && isNotBlank(String.valueOf(value)) ? String.valueOf(value) : toSnakeCamelCase(name);
    }

    private static List<Clazz> extractProperties(CompilationUnit classJava) {
        List<Clazz> properties = new ArrayList<>();
        attributeOfInstance = new HashMap<>();
        classJava.findAll(FieldDeclaration.class).forEach(field -> {
            if(hasAnnotationOneToMany(field.getAnnotations())) return;
            Type fieldType = field.getElementType();
            var isPrivate = field.isPrivate();
            var isCollection = fieldType.isArrayType();
            var type = extractType(fieldType, isCollection);
            for(var variable : field.getVariables()) {
                var property = new Clazz();
               // attributeOfInstance.put(variable.getNameAsString(), "text_value".concat(variable.getNameAsString()));
                property.setName(variable.getNameAsString());
                property.setPrivate(isPrivate);
                property.setCollection(isCollection);
                property.setType(type);
                properties.add(property);
            }
        });
        return properties;
    }

    public static String extractType(Type fieldType, boolean isCollection) {
        return isCollection ? getNameClassOfCollection(fieldType.toString()) : fieldType.asString();
    }

    private static Class<?> extractGenercType(Field field) {
        //Pega o nome do tipo
        String typeName = field.getGenericType().getTypeName();

        //Verifica se no typeName contem '<' e '>'
        if(containsDiamoent(typeName)) {
            //Pega o objeto dentro do diamante
            Class<?> clazz = null;
            try {
                clazz = getNameClassInCollection(typeName);
                LOG.info("Class " + clazz.getName() + "extraído da collection");
                return clazz;
            } catch (ClassNotFoundException e) {
                LOG.warn("A Class " + clazz.getName() + "não foi extraído da collection");
                return Object.class;
            }
        }
        return Object.class;
    }

    private static void addMessageLog(String message, JTextArea logArea) {
        logMessage(message, JsonBuilder.logArea);
    }

}
