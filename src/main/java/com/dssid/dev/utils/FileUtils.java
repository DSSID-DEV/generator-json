package com.dssid.dev.utils;

import com.dssid.dev.domain.DataExtractedDefault;
import com.dssid.dev.domain.model.Payload;
import com.dssid.dev.domain.model.Resources;
import com.dssid.dev.domain.model.Structure;
import com.dssid.dev.enums.TypeParameter;
import com.dssid.dev.repository.CustomRepository;
import com.dssid.dev.utils.interfaces.ClazzUtilInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.printer.configuration.Indentation;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dssid.dev.config.PropertieApplication.addProperty;
import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.constants.MessageLog.*;
import static com.dssid.dev.utils.AnnotationUtils.extractTableFromEntity;
import static com.dssid.dev.utils.JsonBuilder.*;
import static com.dssid.dev.utils.SearchFileJava.*;
import static com.dssid.dev.utils.SwaggerBuilder.*;
import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.verification.VerificationType.*;
import static com.dssid.dev.view.components.LogMessage.logMessage;
import static java.nio.file.Files.list;
import static java.nio.file.Files.walk;

public class FileUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    private static final String YML = ".yml";
    private static final String YAML = ".yaml";
    private static final String PROPERTIES = ".properties";
    private static final String APPLICATION = "application";
    static String pathRoot;
    static JTextArea logArea;
    static Resources projectPaths;
    static String dirMainResources;
    static Payload payload;

    static DataExtractedDefault dataExtracted;

    public static void readAndCopyReadApplication(Resources resources, JTextArea jTextArea) throws ConfigurationException, IOException {
        logArea = jTextArea;
        projectPaths = resources;
        var resoucesPath = Paths.get(resources.getPathProject(), "src", "main", "resources");

        var application = extractDataBaseProperties(resoucesPath);
        if(application.isEmpty()) return;

        try {
            //Cria propriedades do application do projeto desejado ao application deste projeto
            addProperty(application);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> extractDataBaseProperties(Path resouces) throws IOException, ConfigurationException {

        var ymlPath = resouces.resolve(APPLICATION.concat(YML));
        var yamlPath = resouces.resolve(APPLICATION.concat(YAML));
        if(Files.exists(ymlPath)) return readYmlFile(ymlPath);

        if(Files.exists(yamlPath)) return readYmlFile(yamlPath);

        var propertiesPath = resouces.resolve(APPLICATION.concat(PROPERTIES));
        if(Files.exists(propertiesPath)) return readPropertiesFile(propertiesPath);

        return new HashMap<>();
    }

    private static Map<String, String> readPropertiesFile(Path propertiesPath) throws ConfigurationException {
        PropertiesConfiguration config = new PropertiesConfiguration(propertiesPath.toFile());

        Map<String, String> dbProperties = new HashMap<>();

        // Verifica diferentes formatos de propriedades de banco de dados
        String url = config.getString("spring.datasource.url");
        if (url == null) {
            url = config.getString("spring.datasource.jdbc-url");
        }

        if (url != null) {
            dbProperties.put("url", url);
            dbProperties.put("username", config.getString("spring.datasource.username"));
            dbProperties.put("password", config.getString("spring.datasource.password"));
        }

        return dbProperties;
    }


    private static Map<String, String> readYmlFile(Path ymlPath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        JsonNode rootNode = yamlMapper.readTree(ymlPath.toFile());

        Map<String, String> dbProperties = new HashMap<>();

        // Verifica diferentes formatos de propriedades de banco de dados no YAML
        if (rootNode.has("spring")) {
            JsonNode springNode = rootNode.get("spring");
            if (springNode.has("datasource")) {
                JsonNode datasourceNode = springNode.get("datasource");

                // Extrai as propriedades padrão
                extractYamlProperty(datasourceNode, "url", dbProperties);
                extractYamlProperty(datasourceNode, "username", dbProperties);
                extractYamlProperty(datasourceNode, "password", dbProperties);

                // Verifica formato alternativo (com jdbc)
                if (dbProperties.isEmpty() && datasourceNode.has("jdbc-url")) {
                    extractYamlProperty(datasourceNode, "jdbc-url", dbProperties);
                    extractYamlProperty(datasourceNode, "username", dbProperties);
                    extractYamlProperty(datasourceNode, "password", dbProperties);
                }
            }
        }
        return dbProperties;
    }

    private static void extractYamlProperty(JsonNode node, String propertyName, Map<String, String> properties) {
        if (node.has(propertyName)) {
            properties.put(propertyName, node.get(propertyName).asText());
        }
    }

    public static Set<Path> findAllJavaFiles(Resources resources, boolean onlyFromDirectory, JTextArea jtextArea) {
        logArea = jtextArea;
        var path = Paths.get(resources.getAbsolutPathPackege());

        if(!Files.exists(path) || !Files.isDirectory(path))
            return Set.of();

        try {
            return onlyFromDirectory ? list(path)
                        .filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().endsWith(DOT_JAVA))
                        .collect(Collectors.toSet())
                    : walk(path)
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(DOT_JAVA))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            addMessageLog(ERROR_TRYING_TO_GENERATE_FILE);
            LOG.error(ERROR_TRYING_TO_GENERATE_FILE, e);
        }
        return Set.of();
    }
    private static void getPathInterface(Resources resources, JTextArea logArea) {
        //Cria o path para criar o subpacote interfaces se não existir
        LOG.info(CREATE_INTERFACE_PACKAGE);
        addMessageLog(CREATE_INTERFACE_PACKAGE);
        var strInterface = resources.getAbsolutPathPackege() + getSeparator() + INTERFACES;
        resources.setPackageIntefaceOfController(strInterface);
        //Verificar se existe subpacote interfaces, se não existir criar
        var pathInterfaces = Paths.get(strInterface);
        createDirectories(pathInterfaces, logArea);
    }

    private static void addMessageLog(String message) {
        logMessage(message, logArea);
    }



    public static void extractValueFromDataBase(Resources resources) {
        var dataExtracted = new DataExtractedDefault();
        var packageMainJava = resources.getPathProject().concat(resources.getMainDirJava());
        var paths = findEntityClasses(Path.of(packageMainJava), logArea);

        paths.forEach(path -> extractDataFromEntity(path));
    }

    private static void extractDataFromEntity(Path path)  {

        if(!isEntity(path)) return;

        Payload entity = null;
        try {
            entity = getEntity(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        var repository = new CustomRepository();
        repository.getValueFromDataBase(entity);
        System.out.println(entity.toString());

    }

    private static Payload getEntity(Path path) throws FileNotFoundException {
        var cuc = getCompilationUnitClass(path.toFile());
        var className = findClassName(getContent(path), PATTERN_CLASS_NAME);
        var table = extractTableFromEntity(cuc);
        var entityClass = getClassOrInterfaceDeclaration(cuc);
        var instanceProperties = new PayloadUtils();
        var entity = instanceProperties.getInstanceProperties(entityClass, table, className);
        entity.setClassName(className);
        entity.setTableName(table);
        return entity;
    }

    private static String getContent(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String findClassName(String content, Pattern pattern) {
        var macher = pattern.matcher(content);
        if(macher.find()) return macher.group(3);

        return null;
    }

    private static String findTableName(String content, Pattern pattern) {
        var matcher = pattern.matcher(content);
        return matcher.group(1);
    }

    public static void runInterfaceBuildingwithSwaggerDocumentation(Resources resources, Set<Path> controllers, JTextArea jTextArea) {
        logArea = jTextArea;
        dirMainResources = resources.getMainDirResources();
        pathRoot = resources.getPathProject();

        LOG.info(INITIALIZE_THE_PROCESS_OF_BUILDING_INTERFACE);
        addMessageLog(LINE);
        addMessageLog(INITIALIZE_THE_PROCESS_OF_BUILDING_INTERFACE);
        addMessageLog(LINE);

        LOG.info(GETTING_INTERFACES_PATH_DESTINATION);
        addMessageLog(GETTING_INTERFACES_PATH_DESTINATION);
        getPathInterface(resources, logArea);

        //Obter o pathClass
        //Percorrer array controladores para gerar arquivo
        LOG.info(INTERACTING_JAVA_CLASSES);
        addMessageLog(INTERACTING_JAVA_CLASSES);
        controllers.forEach(controller -> {

            //Pegar o arquivo da class
            LOG.info(GETTING_JAVA_FILE);
            addMessageLog(GETTING_JAVA_FILE + fileName(controller.getFileName().toString()));
            File fileClazz = new File(controller.toUri());

            //Verificar se existe
            LOG.info(CHECKING_IF_EXISTS_FILE_DOT_JAVA);
            addMessageLog(CHECKING_IF_EXISTS_FILE_DOT_JAVA);
            if(!fileClazz.exists()) return;

            try {
                //Parsear o arquivo Java com o JavaParse
                LOG.info(messageCuston(GETTING_COMPILATION_UNID_CLASS, fileClazz.getName()));
                addMessageLog(messageCuston(GETTING_COMPILATION_UNID_CLASS, fileClazz.getName()));
                var compilationUnitClass = getCompilationUnitClass(fileClazz);

                var classJava = getParseClassJava(compilationUnitClass);

                LOG.info(GETTING_IMPORTS_OF_CLASS);
                addMessageLog(GETTING_IMPORTS_OF_CLASS);
                var imports = getImports(compilationUnitClass.getImports());

                //Obter o tipo declarado
                LOG.info(GETTING_THE_TYPE_DECLARATION);
                addMessageLog(GETTING_THE_TYPE_DECLARATION);
                Optional<TypeDeclaration<?>> type = compilationUnitClass.getTypes().stream().findFirst();

                //Verificar se o type está fazio
                LOG.info(CHECKING_IF_TYPE_DECLARATION_IS_EMPTY);
                addMessageLog(CHECKING_IF_TYPE_DECLARATION_IS_EMPTY);
                if(type.isEmpty()) return;

                //Obter copia da classe original
                LOG.info(GETTING_THE_COPY_OF_ORIGINAL_CLASS);
                addMessageLog(GETTING_THE_COPY_OF_ORIGINAL_CLASS);
                var originalClass = (ClassOrInterfaceDeclaration) type.get();

                //Criar nova  interface e adicionar 'Interface' na nomenclatura da classe
                LOG.info(messageCuston(CREATE_NEW_FILE_INTERFACE, classJava.getName()));
                addMessageLog(messageCuston(CREATE_NEW_FILE_INTERFACE, classJava.getName()));
                var compilationUnitIterface = new CompilationUnit(getPackageFromInterfaces(resources.getPackageController()).concat(DOT_INTERFACES));

                LOG.info(CHECKING_EXISTS_IMPORTS);
                addMessageLog(CHECKING_EXISTS_IMPORTS);
                if(!imports.isEmpty()) {
                    LOG.info(ADDING_IMPORTS_IN_INTERFACE);
                    addMessageLog(ADDING_IMPORTS_IN_INTERFACE);
                    compilationUnitIterface.setImports(imports);
                }

                String nomeFile = originalClass.getNameAsString().concat(INTERFACE);
                var newInterface = compilationUnitIterface.addInterface(nomeFile);

                LOG.info(ADDING_TAG_ANNOTATION_IN_INTERFACE);
                addMessageLog(ADDING_TAG_ANNOTATION_IN_INTERFACE);
                newInterface.addAnnotation(buildTagAnnotation(classJava, logArea));

                //Copiar apenas os métodos públicos
                LOG.info(ITERATING_THE_LIST_OF_MEHTOD_OF_ORIGINAL_CLASS);
                addMessageLog(ITERATING_THE_LIST_OF_MEHTOD_OF_ORIGINAL_CLASS);
                originalClass.getMethods().forEach(method -> {

                    if(method.isPrivate()) return;

                    Structure methodStruct = loadMethodStructure(method, originalClass.getNameAsString());

                    //Verificar se o método não é público
                    LOG.info(CHECKING_IF_METHOD_MODIFIER_IS_PUBLIC);
                    addMessageLog(CHECKING_IF_METHOD_MODIFIER_IS_PUBLIC);
                    if(isNotPublic(method)) return;

                    //Remover conteúdo do método
                    LOG.info(REMOVING_METHOD_BODY);
                    addMessageLog(REMOVING_METHOD_BODY);
                    method.removeBody();

                    //Verificar se há anotações do swagger no controller
                    LOG.info(CHECKING_IF_METHOD_HAS_SWAGGER_ANNOTATIONS);
                    addMessageLog(CHECKING_IF_METHOD_HAS_SWAGGER_ANNOTATIONS);
                    if(notHasSwaggerAnnotations(method)) {
                        //Adicionar documentação do swagger senão existir
                        LOG.info(STARTING_THE_API_DOCUMENTATION_BUILDING_PROCESS);
                        addMessageLog(STARTING_THE_API_DOCUMENTATION_BUILDING_PROCESS);
                        addDocumentationOfSwaagerIfNotExists(method, methodStruct, logArea);
                    }

                    //Remover anotações
                    LOG.info(REMOVING_OTHER_ANNOTATIONS_FROM_THE_METHOD);
                    addMessageLog(REMOVING_OTHER_ANNOTATIONS_FROM_THE_METHOD);
                    removeAnnotations(method);

                    //Limpar anotações de parametros
                    LOG.info(REMOVING_PARAMETER_ANNOTATIONS);
                    addMessageLog(REMOVING_PARAMETER_ANNOTATIONS);
                    method.getParameters().forEach(parameter -> parameter.getAnnotations().clear());

                    //Adicionar ponto e vírgula no final do método
                    LOG.info(ADDING_SEMICOLON_IN_METHOD_SIGNATURE);
                    addMessageLog(ADDING_SEMICOLON_IN_METHOD_SIGNATURE);
                    method.setBody(null);

                    LOG.info(ADDING_METHOD_SIGNATURE_IN_INTERFACE);
                    addMessageLog(ADDING_METHOD_SIGNATURE_IN_INTERFACE);
                    newInterface.addMember(method);

                    try {
                        runCreatePayloadandResponseExamples(resources, methodStruct);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                //Escrever interface java
                LOG.info(PREPARING_TO_WRITE_FILE);
                addMessageLog(PREPARING_TO_WRITE_FILE);
                var interfaceFilePath = Paths.get(resources.getPackageIntefaceOfController(),
                        nomeFile.concat(DOT_JAVA));
                getPermission(interfaceFilePath);
                String finalCode = compilationUnitIterface.toString(new PrettyPrinterConfiguration()
                        .setIndentation(new Indentation(Indentation.IndentType.SPACES)));
                LOG.info(WRITING_INTERFACE_FILE);
                addMessageLog(WRITING_INTERFACE_FILE);
                Files.write(interfaceFilePath, finalCode.getBytes(StandardCharsets.UTF_8));
                addMessageLog(LINE);
                addMessageLog("");
            } catch (IOException e) {
                LOG.error(ERROR_TRYING_TO_WRITE_INTERFACE_FILE, e);
                addMessageLog(messageWithException(ERROR_TRYING_TO_WRITE_INTERFACE_FILE, e));
                throw new RuntimeException(ERROR_TRYING_TO_WRITE_INTERFACE_FILE, e);
            }
        });
    }

    public static Class<?> getParseClassJava(CompilationUnit compilationUnitClass) {
        return parseClassJava(compilationUnitClass);
    }

    public static CompilationUnit getCompilationUnitClass(File fileClazz) throws FileNotFoundException {
        return StaticJavaParser.parse(fileClazz);
    }

    public static void runCreatePayloadandResponseExamples(Resources resources, Structure methodStruct) throws IOException {
        var pathRoot = resources.getPathProject().concat(resources.getMainDirJava());
        var path = Paths.get(pathRoot);
        String clazz;
        if(isPostOrPut(methodStruct.getVerbHttp())) {

            if(!hasBody(methodStruct.getParameters().get(TypeParameter.BODY))) return;

            clazz = methodStruct.getParameters().get(TypeParameter.BODY).get(0).getType();

            payload = getRequestProperties(clazz.concat(DOT_JAVA));

            if(!hasProperties(payload.getProperties())) return;

            buildJsonFile(resources.getPathProject(), payload, table, false, logArea);
        }
    }

    public static Payload getRequestProperties(String clazz) throws FileNotFoundException {
        var pathMainJava = projectPaths.getPathProject().concat(projectPaths.getMainDirJava());
        var fileClass = findClassFiles(Paths.get(pathMainJava), clazz, logArea);

        if(fileClass == null) return null;

        var classJava = getCompilationUnitClass(fileClass.toFile());
        var payload = new Payload();
        payload.setClassName(clazz);
        return extractPropertyPayload(classJava, payload, pathMainJava);
    }

    public static Payload extractPropertyPayload(CompilationUnit classJava, Payload payload, String pathMainJava) throws FileNotFoundException {
        var classDecl = getClassOrInterfaceDeclaration(classJava);
        var classEntity = getClassEntity(payload.getClassName(), Path.of(pathMainJava));
        ClazzUtilInterface extractPayload = new PayloadUtils(payload);
        return extractPayload.getPayloadProperties(classDecl, classEntity);
    }

    private static ClassOrInterfaceDeclaration getClassEntity(String clazz, Path pathMainResources) throws FileNotFoundException {
        var canditates = generateCanditates(clazz);
        var foundPath = searchFile(pathMainResources, canditates, true, logArea);
        if(!foundPath.isPresent()) {
            foundPath = searchFile(pathMainResources, List.of(clazz.replace(DOT_JAVA, "")), false, logArea);
        }
        var compilationUnitClass = getCompilationUnitClass(foundPath.get().toFile());
        var tableExtracted = extractTableFromEntity(compilationUnitClass);
        if(isNotBlank(tableExtracted))
            table = tableExtracted;
        return getClassOrInterfaceDeclaration(compilationUnitClass);
    }

    private static void runCreateExamples(Path pathRoot, String pathProject, String clazz, boolean isCollection) throws IOException {
        clazz = isCollection ? getNameClassOfCollection(clazz) : clazz;
        String normalizedClassName = clazz.endsWith(DOT_JAVA) || clazz.endsWith(DOT_CLASS) ?
                clazz.replace("Request", "")
                        .replace("Response", "")
                        .replace("DTO", "")
                        .replace("Dto", "") :
                clazz.concat(DOT_JAVA).replace("Request", "")
                .replace("Response", "")
                .replace("DTO", "")
                .replace("Dto", "");

        var foundPath = findClassFiles(Path.of(pathProject), normalizedClassName, logArea);
        buildJsonFile(foundPath, pathProject, normalizedClassName, isCollection, payload, logArea);
    }

    private static Class<?> parseClassJava(CompilationUnit compilationUnitClass) {
        var classes = compilationUnitClass.findAll(ClassOrInterfaceDeclaration.class);
        return classes.get(0).getClass();
    }

    private static void addDocumentationOfSwaagerIfNotExists(MethodDeclaration method, Structure methodStruct, JTextArea logArea) {
        if(methodStruct.isHasOperation() || !method.hasModifier(Modifier.Keyword.PUBLIC)) return;
            addDocumentationOfSwaggerForMethod(method, methodStruct, logArea);
    }

    private static String getPackageFromInterfaces(String str) {
        LOG.info(GETTING_PACKAGE_FROM_INTERFACES);
        addMessageLog(GETTING_PACKAGE_FROM_INTERFACES);
        return str.replace(getSeparator(), DOT);
    }

    public static File getPermission(Path arquivo) {
         LOG.info(GETTING_PERMISSION_TO_WRITE_FILE);
        addMessageLog(GETTING_PERMISSION_TO_WRITE_FILE);
        var file = arquivo.toFile();
        file.setWritable(true, false);
        return file;
    }
    private static String getSeparator() {
        return File.separator;
    }


}