package com.dssid.dev.utils;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Pattern;

public class Constants {

    public static final int PUT = 1;
    public static final int GET = 2;
    public static final int POST = 0;
    public static final int PATCH = 4;
    public static final int DELETE = 3;
    public static final int SUMMARY = 0;
    public static final String DOT = ".";
    public static final String TAG = "Tag";
    public static final String BARRA = "/";
    public static final String REF = "ref";
    public static final int DESCRIPTION = 1;
    public static final String PATH = "PATH";
    public static final String NAME = "name";
    public static final String JSON = ".json";
    public static final String TEXT_ = "text_";
    public static final String INDENT = "    ";
    public static final String ENUM_ = "enum_";
    public static final String QUERY = "QUERY";
    public static final String POSIX = "posix";
    public static final String OBJECT = "Object";
    public static final String STRING = "String";
    public static final String DOUBLE = "Double";
    public static final String PUBLIC = "public";
    public static final String DOT_JAVA = ".java";
    public static final String DOT_JSON = ".json";
    public static final String BOOLEAN = "Boolean";
    public static final String INTEGER = "Integer";
    public static final String DOT_CLASS = ".class";
    public static final String BARRA_INVERTIDA = "\\";
    public static final String OPERATION = "Operation";
    public static final String PARAMETER = "Parameter";
    public static final String INTERFACE = "Interface";
    public static final String PARAMETERS = "Parameters";
    public static final String SRC_JAVA = "src/main/java";
    public static final String INTERFACES = "/interfaces";
    public static final String PARAMETER_IN= "ParameterIn";
    public static final String PERMISSIO_RWXR = "rwxr-x---";
    public static final String API_RESPONSE = "ApiResponse";
    public static final String DOT_INTERFACES = ".interfaces";
    public static final String API_RESPONSES = "ApiResponses";
    public static final String IMPLEMENTATIONS = "implementation";
    public static final String SERIAL_VERSION_UID = "serialVersionUID";
    public static final String APPLICATION_JSON = "APPLICATION_JSON_VALUE";
    public static final String PATH_EXAMPLE_OBJECT = "@components/examples/";
    public static final String TARGET_PATH = "src/main/resources/swagger/examples";
    public static final String IMPORT_SWAGGER_TAG = "io.swagger.v3.oas.annotations.tags.Tag";
    public static final String ERROR_TYRING_TO_GENERATE_FILE = "Error trying to generate file";
    public static final String ERROR_TRYING_TO_GET_CLASS_NAME = "Error trying to get class name";
    public static final String ERROR_TRYING_TO_CREATE_DIRECTORY = "Error trying to create directory";
    public static final String TAG_OPERATION = """
            @Operation(summary="descrição do método")
            """;

    //constants do tipo array ou list

    public static final String[] GENERICS = {"?", "T"};
    public static final String[] VOID = {"void", "Void"};
    public static final String[] SCHEMA = {"schema", "Schema"};
    public static final String[] CONTENT = {"content", "Content"};
    public static final String[] MEDIA_TYPE = {"mediaType", "MediaType"};
    public static final String[] REQUEST_BODY = {"requestBody", "RequestBody"};
    public static final String[] EXAMPLE_OBJECT = {"examples", "ExampleObject"};
    public static final String[] METHODS = {"POST", "PUT", "PATCH", "GET", "DELETE"};


    public static final List<String> ANNOTATIONS_PARAMETERS =
            List.of("RequestParam", "Param", "PathVariable", "RequestBody");

    public static final List<String> ANNOTATIONS =
            List.of("PostMapping", "PutMapping", "GetMapping", "DeleteMapping",
                    "PatchMapping", "ResponseStatus", "RequiredArgsConstructor");

    public static final List<String> ANNOTATIONS_MAPPING =
            List.of("PostMapping", "PutMapping", "GetMapping", "DeleteMapping",
                    "PatchMapping");

    public static final List<String> IMPORTATIONS =
            List.of("PostMapping", "GetMapping", "PutMapping", "DeleteMapping", "PatchMapping", "RequiredArgsConstructor",
                    "ResponseStatus", "lombok", "Service", "HttpStatus", "org.springframework.web.bind.annotation");

    public static final Pattern PATTERN_PARAMETERS = Pattern.compile("(@RequestBody|@PathVariable|@RequestParam)\\s+([^\\s,)]+)\\s+([^\\s,)]+)");

    public static final List<String> IMPORTS_OF_SWAGGER = List.of(
            "io.swagger.v3.oas.annotations.Parameter", "io.swagger.v3.oas.annotations.enums.ParameterIn",
            "io.swagger.v3.oas.annotations.Operation", "io.swagger.v3.oas.annotations.media.ArraySchema",
            "io.swagger.v3.oas.annotations.media.Content", "io.swagger.v3.oas.annotations.media.ExampleObject",
            "io.swagger.v3.oas.annotations.media.Schema", "io.swagger.v3.oas.annotations.responses.ApiResponse",
            "io.swagger.v3.oas.annotations.responses.ApiResponses", "io.swagger.v3.oas.annotations.tags.Tag",
            "io.swagger.v3.oas.annotations.parameters.RequestBody");

    public static final Map<String, String> TYPE_MAP = Map.ofEntries(
            Map.entry("int", "1"),
            Map.entry("Integer", "1"),
            Map.entry("long", "1"),
            Map.entry("Long", "1"),
            Map.entry("float", "1.0"),
            Map.entry("Float", "1.0"),
            Map.entry("double", "1.0"),
            Map.entry("Double","1.0"),
            Map.entry("BigDecimal", "1.0"),
            Map.entry("String", TEXT_.concat("String")),
            Map.entry("boolean", "true"),
            Map.entry("Boolean", "true"),
            Map.entry("Date", LocalDate.now().toString()),
            Map.entry("LocalDate", LocalDate.now().toString()),
            Map.entry("LocalDateTime", LocalDateTime.now().toString())
    );
}
