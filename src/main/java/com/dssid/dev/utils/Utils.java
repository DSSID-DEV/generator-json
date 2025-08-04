package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Resources;
import com.dssid.dev.domain.model.Structure;
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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.constants.MessageLog.ERROR_TRYING_TO_CREATE_DIRECTORY;
import static com.dssid.dev.verification.VerificationType.*;
import static com.dssid.dev.view.components.LogMessage.logMessage;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    static JTextArea logArea;

    public static Path definePath(String strPath) {
        var path = hasContent(strPath) ? strPath : TARGET_PATH;
        return Paths.get(path);
    }

    public static byte[] getByteValues() {
        return String.valueOf(new Random().nextInt(0, 100)).getBytes(StandardCharsets.UTF_8);
    }

    public static List<String> generateCanditates(String value) {
        var tokens = splitCamelCase(value);
        int size = tokens.size();

        Map<Integer, List<String>> bySize = new HashMap<>();
        for(int mask = 0; mask < (1 << size); mask++) {
            int sizeT = Integer.bitCount(mask);
            int finalMask = mask;
            String candidate = IntStream.range(0, size)
                    .filter(i -> (finalMask & (1 << i)) != 0)
                    .mapToObj(tokens::get)
                    .collect(Collectors.joining());
            bySize.computeIfAbsent(sizeT, k -> new ArrayList<>()).add(candidate);
        }

        return bySize.entrySet().stream()
                .sorted(Map.Entry.<Integer, List<String>> comparingByKey().reversed())
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());
    }

    public static List<String> splitCamelCase(String value) {
        return Arrays.stream(value.split("(?<=.)(?=\\p{Lu})"))
                .collect(Collectors.toList());
    }

//    public static String nameProperty(String str, Payload payload) {
//        Class<?> clazz = Payload.class;
//        final String value;
//        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
//            var get = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
//            try {
//                Method getter = clazz.getMethod(get);
//             //   value = getter.invoke(get);
//                var valueNormalized = normalize(value);
//                if(isSimilar(valueNormalized, str)) {
//                    return value;
//                }
//            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        return str;
//    }

    public static String normalize(String value) {
        var string = toSnakeCamelCase(value).replace("_", "");
        return string.replaceAll("[^a-z]", "");
    }

    public static void createDirectories(Path path, JTextArea jTextArea) {
        logArea = jTextArea;
        try{
            //Verificar se o diretório não existe
            if(Files.notExists(path)) {
                //Verificar se o SO é Unix-like e dá permissão para manipular o arquivo
                if(isUnix()) allowWriting(path);
                //Criar diretório sem passar o attributes
                else createDirecoty(path);
                LOG.info("Directory " + path + " created with success");
                LOG.error(ERROR_TRYING_TO_CREATE_DIRECTORY);
                addMessageLog(ERROR_TRYING_TO_CREATE_DIRECTORY);
            }
        } catch(Exception e) {
            LOG.error(ERROR_TRYING_TO_CREATE_DIRECTORY);
            LOG.error(ERROR_TRYING_TO_CREATE_DIRECTORY);
            addMessageLog(ERROR_TRYING_TO_CREATE_DIRECTORY);
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

    public static Resources extractPaths(String absoluteProjectPath) {

        var resources = new Resources();
        String pathProject;
        if(absoluteProjectPath.contains(getPathMainJava())) {
            pathProject = absoluteProjectPath.substring(0, absoluteProjectPath.indexOf(getPathMainJava()));
            var project = pathProject.substring(lastIndexOf(
                    pathProject.substring(0, lastIndexOf(
                            pathProject, getDefaultSystemBar())),
                    getDefaultSystemBar())
            ).replace(getDefaultSystemBar(), "");
            resources.setProject(project);
            resources.setPathProject(pathProject);
            resources.setMainDirJava(getPathMainJava());
            resources.setMainDirResources(getPathMainResources());
            resources.setPackageController(absoluteProjectPath
                    .substring(absoluteProjectPath.indexOf(getPathMainJava()) + getPathMainJava().length()));
            resources.setAbsolutPathPackege(absoluteProjectPath);
        }
        return resources;
    }

    private static String getPathMainResources() {
        return getPathMain() + "resources" + getSeparator();
    }

    private static String getPathMain() {
        return "src" + getSeparator() + "main" + getSeparator();
    }

    private static String getPathMainJava() {
        return getPathMain() + "java" + getSeparator();
    }

    private static String getSeparator() {
        return File.separator;
    }

    public static String getDefaultSystemBar() {
        return isWindows() ? "\\" : "/";
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
        return lastIndexDot >= 0 ? str.substring(lastIndexDot +1) : str;
    }

    public static String getNameClassOfCollection(String str) {
        if(!containsDiamoent(str)) return str;
        return str.substring(str.indexOf("<") + 1, lastIndexOf(str, ">"));
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
        System.out.println("Error -> " + ann);
        return ann != null ? ann.substring(0, ann.lastIndexOf("M")).toUpperCase() : null;
    }

    public static String[] createSummaryAndDescription(Structure method) {
        if(isPost(method.getVerbHttp())) return new String[]{"Register new ", "Endpoint to "};
        else if(isPutOrPatch(method.getVerbHttp())) return new String[]{"Update a ", "Endpoint to "};
        else if(isGet(method.getVerbHttp())) return new String[]{"Return to ", "Endpoint to "};
        else return new String[]{"Remove to ", "Endpoint to "};
    }

    public static String extractControllerName(String controllerName) {
        return controllerName.toLowerCase().replace("controller", "");
    }
    public static int lastIndexOf(String str, String regex) {
        return str.lastIndexOf(regex);
    }

    public static Structure loadMethodStructure(MethodDeclaration method, String controlName) {
        System.out.println(controlName);
        System.out.println(method.getNameAsString());
        return Structure.builder()
                .controllerName(controlName)
                .verbHttp(getVerbHttp(method.getAnnotations()))
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

    public static String extractObjectType(String object) {
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

    private static boolean isPathVariable(NodeList<AnnotationExpr> annotations) {
        return false;
    }

    private static boolean isBody(NodeList<AnnotationExpr> annotations) {
        return annotations.stream().
                filter(annotation -> annotation.getName().asString().equals(ANNOTATIONS_PARAMETERS.get(3)))
                .count() > 0;
    }

    public static String toSnakeCamelCase(String value) {
        if(!isNotBlank(value)) return value;

        return value.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    private static void addMessageLog(String message) {
        logMessage(message, logArea);
    }

}
