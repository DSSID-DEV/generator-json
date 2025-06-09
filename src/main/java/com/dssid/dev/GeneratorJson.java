package com.dssid.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.utils.Constants.*;
import static com.dssid.dev.utils.VerificationType.*;

public class GeneratorJson {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorJson.class);
    private static final Scanner scanner = new Scanner(System.in);
    private List<Class<?>> classes;

    //Criar um vericador das classes
    public boolean checkValidityOfClass(List<Class<?>> classes) {
        System.out.println("Check that the class match what is expected.");
        this.classes = classes;

        //Listar as classes para validar se são as esperadas
        this.classes.forEach(clazz -> System.out.println("-> " +clazz.getName()));

        System.out.println("Type 'y' to generate file or 'n' to exit: ");
        return scanner.nextLine().equals("y");
    }

    //Criar um método que irá iniciar o processo de geração do arquivo json
    public void now(String path) {
        //Definir o path onde o arquivo vai ser criado
        var target = definePath(path);

        //Criar o diretório definido
        createDirecoties(target);

        //Criar um ObjectMapper
        var mapper = new ObjectMapper();

        //Configurar o formato do arquivo json
        configurationFormatJson(mapper);

        //percorrer a lista de classes e gerar arquivos json
        this.classes.forEach(clazz -> {
            try {
                //Criar o json a partir do método
                var json = generateJson(mapper, clazz.getName());

                //Obter o nome do arquivo a ser gerado
                var name = fileName(clazz.getName()).concat(JSON);

                //Cria o arquivo em branco
                var file = target.resolve(name);

                //Escrever os dados do json gerado no arquivo criado, passando o conteúdo.
//                Files.writeString(file, json.toString());
                mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), json);
                LOG.info("File " + name + " created with success!");
            }catch(IOException e) {
                LOG.error(ERROR_TRYING_TO_GET_CLASS_NAME);
                throw new RuntimeException(ERROR_TRYING_TO_GET_CLASS_NAME + ": " + e);
            } catch (ClassNotFoundException e) {
                LOG.error(ERROR_TRYING_TO_GET_CLASS_NAME);
                throw new RuntimeException(ERROR_TRYING_TO_GET_CLASS_NAME + ": " + e);
            }
        });
    }

    private ObjectNode generateJson(ObjectMapper mapper, String name) throws ClassNotFoundException {
        var clazz = Class.forName(name);
        return generate(clazz, mapper);
    }

    private ObjectNode generate(Class<?> clazz, ObjectMapper mapper) {
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
            if(name.equals(SERIAL_VERSION_UUID)) return;

            //Iniciar as verificações de tipo de dados das propriedade e adicionar os valores
            if(type == String.class) node.put(name, TEXT_.concat(name));
            else if(isNumberTypeInteger(type)) node.put(name, 1);
            else if(isNumberTypeFloat(type)) node.put(name, 1.0);
            else if(isTypeDateOrLocalDate(type)) node.put(name, LocalDate.now().toString());
            else if(isTypeLocalDateTime(type)) node.put(name, LocalDateTime.now().toString());
            else if(isTypeBoolean(type)) node.put(name, true);
            else if(isTypeEnum(type)) {
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
                    array.add(elementType.getName().concat("_1"));
                    array.add(elementType.getName().concat("_2"));
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

    private Class<?> extractGenercType(Field field) {
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
}
