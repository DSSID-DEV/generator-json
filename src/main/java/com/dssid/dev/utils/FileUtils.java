package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Structure;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.printer.configuration.Indentation;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.utils.Constants.*;
import static com.dssid.dev.utils.SwaggerBuilder.*;
import static com.dssid.dev.utils.Utils.lastIndexOf;
import static com.dssid.dev.utils.VerificationType.*;

public class FileUtils {

    public static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static Set<Class<?>> findAllControllers(String packageController) {
        var path = packageController.replace(".", "/");

        Set<Class<?>> controllers = new HashSet<>();
        System.out.println("****************************************************************************");
        LOG.info("Initializing controller listing process");
        try(var inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path)){
            if(inputStream == null) return controllers;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            var line = "";

            LOG.info("Listing package classes " + packageController);
            while((line = reader.readLine()) != null) {
                if(line.endsWith(DOT_CLASS)) {
                    var className = packageController.concat(".").concat(line.substring(0, line.length() -6));
                    try{
                        Class<?> controller = Class.forName(className);
                        controllers.add(controller);
                        System.out.println("-> " + controller.getName());
                    } catch (ClassNotFoundException e) {
                        LOG.error(ERROR_TRYING_TO_GET_CLASS_NAME + ": " + e);
                        throw new RuntimeException(ERROR_TRYING_TO_GET_CLASS_NAME);
                    }
                }
            }
            return controllers;
        } catch (IOException e) {
            LOG.error(ERROR_TRYING_TO_GET_CLASS_NAME + ": " + e);
            throw new RuntimeException(ERROR_TRYING_TO_GET_CLASS_NAME);
        }
    }

    public static void execute(Path pathInterfaces, Set<Class<?>> controllers) {

        LOG.info("Initialize the process of building the interfaces documented with swagger");

        //Obter o pathClass
        var packageController = getPackageController(pathInterfaces);
        //Percorrer array controladores para gerar arquivo
        LOG.info("Iterating the list of controllers");
        controllers.forEach(controller -> {

            //Pegar o arquivo da class
            LOG.info("Getting the class file");
            File fileClazz = new File(SRC_JAVA.concat(BARRA + controller.getName().replace(DOT, BARRA)).concat(DOT_JAVA));

            //Verificar se existe
            LOG.info("Checking if exists the file .class");
            if(!fileClazz.exists()) return;

            try {
                //Parsear o arquivo Java com o JavaParse
                LOG.info("Getting the compilation unit class");
                var compilationUnitClass = StaticJavaParser.parse(fileClazz);

                LOG.info("Getting the imports of class");
                var imports = getImports(compilationUnitClass.getImports());

                //Obter o tipo declarado
                LOG.info("Getting the type declaration");
                Optional<TypeDeclaration<?>> type = compilationUnitClass.getTypes().stream().findFirst();

                //Verificar se o type está fazio
                LOG.info("Checking if type declaration is empty");
                if(type.isEmpty()) return;

                //Obter copia da classe original
                LOG.info("Getting the copy of original class");
                var originalClass = (ClassOrInterfaceDeclaration) type.get();

                //Criar nova  interface e adicionar 'Interface' na nomenclatura da classe
                LOG.info("Create new file interface and  the concatenate whith 'Interface'");
                var compilationUnitIterface = new CompilationUnit(getPackageFromInterfaces(packageController.concat(DOT_INTERFACES)));

                LOG.info("Checking exists imports");
                if(!imports.isEmpty()) {

                    LOG.info("Adding imports in interface");
                    compilationUnitIterface.setImports(imports);
                }

                String nomeFile = originalClass.getNameAsString().concat(INTERFACE);
                var newInterface = compilationUnitIterface.addInterface(nomeFile);

                LOG.info("Adding TAG annotation in interface");
                newInterface.addAnnotation(buildTagAnnotation(controller));

                //Copiar apenas os métodos públicos
                LOG.info("Iterating the list of method of original class");
                originalClass.getMethods().forEach(method -> {

                    var methodStruct = loadMethodStructure(method);

                    //Verificar se o método não é público
                    LOG.info("checking if method modifier is public");
                    if(isNotPublic(method)) return;

                    //Remover conteúdo do método
                    LOG.info("Removing method body");
                    method.removeBody();

                    //Verificar se há anotações do swagger no controller
                    LOG.info("Checking if method has swagger annotations");
                    if(notHasSwaggerAnnotations(method)) {
                        //Adicionar documentação do swagger senão existir
                        LOG.info("Add swagger documentation in the method");
                        addDocumentationOfSwaagerIfNotExists(method, methodStruct);
                    }

                    //Remover anotações
                    LOG.info("Removing other annotations from the method");
                    removeAnnotations(method);

                    //Limpar anotações de parametros
                    LOG.info("Removing parameter annotations");
                    method.getParameters().forEach(parameter -> parameter.getAnnotations().clear());

                    //Adicionar ponto e vírgula no final do método
                    LOG.info("Adding semicolon in method signature");
                    method.setBody(null);

                    LOG.info("Adding method signature in interface");
                    newInterface.addMember(method);
                });
                //Escrever interface java
                LOG.info("Preparing to write file");
                var interfaceFilePath = Paths.get(pathInterfaces.toString(), nomeFile.concat(DOT_JAVA));
                getPermission(interfaceFilePath);
                String finalCode = compilationUnitIterface.toString(new PrettyPrinterConfiguration()
                        .setIndentation(new Indentation(Indentation.IndentType.SPACES)));
                LOG.info("Writing interface file");
                Files.write(interfaceFilePath, finalCode.getBytes(StandardCharsets.UTF_8));

            } catch (IOException e) {
                LOG.error("Error trying to write interface file");
                throw new RuntimeException("Error trying to write interface file", e);
            }
        });
    }


    private static void addDocumentationOfSwaagerIfNotExists(MethodDeclaration method, Structure methodStruct) {
        if(methodStruct.isHasOperation() || !method.hasModifier(Modifier.Keyword.PUBLIC)) return;
            addDocumentationOfSwaggerForMethod(method, methodStruct);
    }

    private static String getPackageFromInterfaces(String str) {
        LOG.info("Getting package from interfaces");
        int initIndex = SRC_JAVA.length() + 1;
        return str.substring(initIndex)
                .replace(BARRA_INVERTIDA, DOT);
    }

    private static File getPermission(Path arquivo) {
        LOG.info("Getting permission to write file");
        var file = arquivo.toFile();
        file.setWritable(true, false);
        return file;
    }

    private static String getPackageController(Path pathInterfaces) {
        LOG.info("Package das interfaces: " + pathInterfaces);
        var barra = isWindows() ? BARRA_INVERTIDA : BARRA;
        var lastIndexOf = lastIndexOf(pathInterfaces.toString(), barra);
        return pathInterfaces
                .toString()
                .substring(0, lastIndexOf)
                .replace(".", barra);
    }

}
