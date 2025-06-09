package com.dssid.dev.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.dssid.dev.utils.Constants.*;
import static com.dssid.dev.utils.VerificationType.*;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static Path definePath(String strPath) {
        var path = hasContent(strPath) ? strPath : TARGET_PATH;
        return Paths.get(path);
    }

    public static void createDirecoties(Path path) {
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
    private static boolean isUnix() {
        return FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains(POSIX);
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

    public static int lastIndexOf(String str, String regex) {
        return str.lastIndexOf(regex);
    }

}
