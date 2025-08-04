package com.dssid.dev.config;

import com.dssid.dev.domain.DatabaseParameter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

public class PropertieApplication {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS));


    public static void addProperty(Map<String, String> propertie) throws URISyntaxException, IOException {

        var resources = Paths.get(getResources());
        var ymlPath = resources.resolve("application.yml");

        var ymlFile = ymlPath.toFile();

        ObjectNode rootNode = yamlMapper.createObjectNode();

        if(ymlFile.exists()) {
            rootNode = (ObjectNode) yamlMapper.readTree(ymlFile);
        } else {
            rootNode = yamlMapper.createObjectNode();
        }

        ObjectNode springNode = rootNode.has("spring") ?
                (ObjectNode) rootNode.get("spring") :
                rootNode.putObject("spring");

        ObjectNode datasourceNode = springNode.has("datasource") ?
                (ObjectNode) springNode.get("datasource") :
                springNode.putObject("datasource");

        //Atualiza as propriedades
        for(var entry : propertie.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            // Converte 'url' para 'jdbc-url' se necessário (para alguns formatos)
            if ("url".equals(key) && value.startsWith("jdbc:")) {
                datasourceNode.put("jdbc-url", value);
            } else {
                datasourceNode.put(key, value);
            }
        }
        //Salva o arquivo atualizado
        yamlMapper.writeValue(ymlFile, rootNode);
    }

    private static URI getResources() throws URISyntaxException {
        return ResourceUtils.class.getClassLoader().getResource(".").toURI();
    }


    public static DatabaseParameter getPropertiesDatabase() {
        try {
            var configPath = Paths.get(getResources());
            var ymlFile = configPath.resolve("application.yml").toFile();
            ymlFile.setReadable(true);
            ymlFile.setWritable(true);
            ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
            JsonNode rootNode = ymlMapper.readTree(ymlFile);

            if(rootNode.has("spring")&& rootNode.get("spring").has("datasource")) {
                JsonNode datasourceNode = rootNode.get("spring").get("datasource");

                // Obtém a URL e determina o tipo de banco
                var url = datasourceNode.has("jdbc-url") ?
                        datasourceNode.get("jdbc-url").asText() :
                        datasourceNode.get("url").asText();

                var dbType = determineDbType(url);
                var username = datasourceNode.get("username").asText();
                var password = datasourceNode.get("password").asText();

                //return database properties
                var properties = new DatabaseParameter();
                properties.setUrl(url);
                properties.setPassword(password);
                properties.setUsername(username);
                properties.setDbType(dbType);
                return properties;
            } else {
                throw new RuntimeException("Configurações de banco de dados não encontradas no application.yml");
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo application.yml", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String determineDbType(String url) {
        if (url.contains("mysql")) return "mysql";
        if (url.contains("postgresql")) return "postgresql";
        if (url.contains("oracle")) return "oracle";
        if (url.contains("sqlserver")) return "sqlserver";
        throw new IllegalArgumentException("Tipo de banco de dados não suportado ou não reconhecido na URL: " + url);
    }

}

