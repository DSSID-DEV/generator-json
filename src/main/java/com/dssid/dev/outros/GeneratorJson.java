package com.dssid.dev.outros;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.constants.MessageLog.*;
import static com.dssid.dev.verification.VerificationType.*;
import static com.dssid.dev.view.components.LogMessage.logMessage;

public class GeneratorJson {
    private static final Logger LOG = LoggerFactory.getLogger(GeneratorJson.class);
    static JTextArea logArea;

    //Criar um método que irá iniciar o processo de geração do arquivo json
    public static void now(Path pathRoot, Class<?> clazz,  JTextArea textArea) {
        logArea = textArea;
        //Definir o path onde o arquivo vai ser criado

        Path target = Paths.get(pathRoot + File.separator + TARGET_PATH);

        //Criar o diretório definido
        createDirectories(target, logArea);

        //Criar um ObjectMapper
        var mapper = new ObjectMapper();

        //Configurar o formato do arquivo json
        configurationFormatJson(mapper);

        try {
            //Criar o json a partir do método
            var json = generateJson(mapper, clazz);

            //Obter o nome do arquivo a ser gerado
            var name = fileName(clazz.getName()).concat(JSON);

            //Cria o arquivo em branco
            var file = target.resolve(name);

            //Escrever os dados do json gerado no arquivo criado, passando o conteúdo.
//                Files.writeString(file, json.toString());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), json);
            LOG.info(messageCuston(FILE_JSON_CREATED_WITH_SUCCESS, name));
            addMessageLog(messageCuston(FILE_JSON_CREATED_WITH_SUCCESS, name));
        }catch(IOException e) {
            LOG.error(ERROR_TRYING_TO_GET_CLASS_NAME);
            throw new RuntimeException(ERROR_TRYING_TO_GET_CLASS_NAME + ": " + e);
        } catch (ClassNotFoundException e) {
            LOG.error(ERROR_TRYING_TO_GET_CLASS_NAME);
            throw new RuntimeException(ERROR_TRYING_TO_GET_CLASS_NAME + ": " + e);
        }

    }

    private static ObjectNode generateJson(ObjectMapper mapper, Class<?> clazz) throws ClassNotFoundException {
        //var clazz = Class.forName(name);
        return generate(clazz, mapper);
    }

    private static ObjectNode generate(Class<?> clazz, ObjectMapper mapper) {
        //Fabricar um objeto do tipo ObjectNode
        var node = mapper.createObjectNode();

        //Percorrer todas as propriedades da classe
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            //Dá acesso a propriedade privada
            field.setAccessible(true);

            //Pegar o tipo da propriedade
            Class<?> type = field.getType();

            //Pegar o nome da propriedade definindo a estratégia da nomenclatura para lowerCamelCase
            String name =  mapper.getPropertyNamingStrategy()
                    .nameForField(mapper.getSerializationConfig(), null, field.getName());

            //Temos que ignorar o atributo serialVersionUUID
            if(name.equals(SERIAL_VERSION_UID)) return;

            //Iniciar as verificações de tipo de dados das propriedade e adicionar os valores
            if(type == String.class) node.put(name, TEXT_.concat(name));
            else if(isNumberTypeInteger(type)) node.put(name, 1);
            else if(isNumberTypeFloat(type)) node.put(name, 1.0);
            else if(isTypeDateOrLocalDate(type)) node.put(name, LocalDate.now().toString());
            else if(isTypeLocalDateTime(type)) node.put(name, LocalDateTime.now().toString());
            else if(isTypeBoolean(type)) node.put(name, true);
            else if(isBytes(type)) node.put(name, getByteValues());
            else if(isTypeEnum(type)) {
                //TODO: resolver retorno do enum (não está pegando o valor quando é paramentrizado)
                getValueEnum(name, type, node);
            }
            //Verificar se a propriedade é uma coleção do tipo lista ou array
            else if(isCollectionOrArray(type)) {
                //Criar uma instancia do tipo ArrayNode para tratar arrays ou coleções
                var array = mapper.createArrayNode();

                //Obter o tipo da propriedade do array ou da coleção
                var elementType = extractGenercType(field);

                //Verifica se é do tipo String e atribui valores se acondição for verdadeira
                if(elementType == String.class) {
                    //Adiciona strings no array do tipo ArrayNode
                    array.add(TEXT_.concat("_1"));
                    array.add(TEXT_.concat("_2"));
                }
                else if(isNumberTypeInteger(elementType)) {
                    array.add(1);
                    array.add(2);
                }
                else if(isNumberTypeFloat(elementType)) {
                    array.add(1.0);
                    array.add(2.0);
                }
                else if(isTypeDateOrLocalDate(elementType)) {
                    array.add(LocalDate.now().toString());
                    array.add(LocalDate.now().plusDays(30).toString());
                }
                else if(isTypeLocalDateTime(elementType)) {
                    array.add(LocalDateTime.now().toString());
                    array.add(LocalDateTime.now().plusDays(30).toString());
                }
                else if(isTypeEnum(elementType)) {
                    array.add(ENUM_.concat("1"));
                    array.add(ENUM_.concat("2"));
                }
                else if(isTypeBoolean(elementType)) {
                    array.add(true);
                    array.add(false);
                }
                else {
                    //De forma recursiva ele entra para gerar estrutura do json do objeto encontrado
                    var object = generate(elementType, mapper);

                    //Adiciona o json gerado no array do tipo ArrayNode
                    array.add(object);
                }
                //Adiciona o array no formato json no node (json pai)
                node.set(name, array);
            }
            else {
                //Se a propriedade não cair em nenhuma condição tratar como objeto
                var object = generate(type, mapper);

                //Seta o json gerado a partir do objeto no json pai
                node.set(name, object);
            }
        });
        return node;
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

    private static void addMessageLog(String message) {
        logMessage(message, logArea);
    }
}
