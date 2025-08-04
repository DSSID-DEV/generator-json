package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Structure;
import com.dssid.dev.enums.TypeParameter;
import com.dssid.dev.enums.TypeReturn;
import com.dssid.dev.enums.VerbHttp;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;

import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.constants.MessageLog.*;
import static com.dssid.dev.verification.VerificationType.*;
import static com.dssid.dev.view.components.LogMessage.logMessage;

public class SwaggerBuilder {

    static JTextArea logArea;
    private static final Logger LOG = LoggerFactory.getLogger(SwaggerBuilder.class);
    public static NodeList<ImportDeclaration> getImports(NodeList<ImportDeclaration> imports) {
        LOG.info(COPYING_IMPORTS);
        addMessageLog(COPYING_IMPORTS);
        NodeList<ImportDeclaration> listImports = new NodeList<>();
        for(var imprt: imports){
            if(!containsUnwantedImports(imprt.getName().toString())) {
                listImports.add(imprt);
            }
        }
        addImportsOfSwagger(listImports);
        return listImports;
    }

    private static void addImportsOfSwagger(NodeList<ImportDeclaration> imports) {
        //Converter em lista de String
        LOG.info(ADDING_SWAGGER_IMPORTS);
        addMessageLog(ADDING_SWAGGER_IMPORTS);
        var collectionImport = imports.stream()
                .map(ImportDeclaration::getNameAsString)
                .toList();

        //Adicionar cada importação inexistente
        IMPORTS_OF_SWAGGER.forEach(imp -> {
            if(!collectionImport.contains(imp)) {
                imports.add(new ImportDeclaration(imp, false, false));
            }
        });
    }

    public static NormalAnnotationExpr buildTagAnnotation(Class<?> controller, JTextArea logText) {
        logArea = logText;
        LOG.info(BUILDING_TAG_ANNOTATION);
        addMessageLog(BUILDING_TAG_ANNOTATION);
        //Adicionar tag do swagger
        NodeList<MemberValuePair> memberValuePairs = new NodeList<>();
        createAnnotation(memberValuePairs, controller);
        return new NormalAnnotationExpr(
                new Name(TAG),
                memberValuePairs
        );
    }

    private static void createAnnotation(NodeList<MemberValuePair> memberValuePairs, Class<?> controller) {
        LOG.info(ADDING_NAME_AND_DESCRIPTION_OF_TAG_ANNOTATION);
        addMessageLog(ADDING_NAME_AND_DESCRIPTION_OF_TAG_ANNOTATION);
        memberValuePairs
                .add(new MemberValuePair("name", new StringLiteralExpr(controller.getSimpleName())));
        memberValuePairs
                .add(new MemberValuePair("description", new StringLiteralExpr("Interface para " + controller.getSimpleName())));
    }

    public static void addDocumentationOfSwaggerForMethod(MethodDeclaration methodDeclaration, Structure methodStruct, JTextArea textArea) {
        logArea = textArea;
        LOG.info(CHECKING_IF_THE_MAPPING_INS_CONTAINED_IN_THE_METHOD);
        addMessageLog(CHECKING_IF_THE_MAPPING_INS_CONTAINED_IN_THE_METHOD);
        if(!isMappingType(methodDeclaration.getAnnotations(), methodStruct.getVerbHttp())) return;

        //Criar tag Operation
        LOG.info(messageCuston(CREATE_OPERATOIN_ANNOTATION_OF_METHOD, methodDeclaration.getNameAsString()));
        addMessageLog(messageCuston(CREATE_OPERATOIN_ANNOTATION_OF_METHOD, methodDeclaration.getNameAsString()));
        var operation = buildAnnotationOperation(methodStruct, VerbHttp.valueOf(methodStruct.getVerbHttp()));
        methodDeclaration.addAnnotation(operation);

        //Criar tag ApiApplication
        LOG.info(messageCuston(CREATE_APPICATIONS_ANNOTATION_OF_METHOD, methodDeclaration.getNameAsString()));
        addMessageLog(messageCuston(CREATE_APPICATIONS_ANNOTATION_OF_METHOD, methodDeclaration.getNameAsString()));
        var apiResponseAnnotation = buildApiResponseAnnotation(methodStruct);
        methodDeclaration.addAnnotation(apiResponseAnnotation);
    }

    private static NormalAnnotationExpr buildAnnotationOperation(Structure structure, VerbHttp verbHttp) {
        var methodName = formatarCamelCaseParaEspacado(structure.getMethodName());

        boolean hasBody = hasBody(structure.getParameters().get(TypeParameter.BODY));

        String object = !structure.getParameters().get(TypeParameter.BODY).isEmpty() ?
                structure.getParameters().get(TypeParameter.BODY).get(0).getType() : OBJECT;
        structure.addObjectName(object);

        LOG.info(messageCuston(BUILDING_ANNOTATION_OPERATION_OF_METHOD, structure.getMethodName()));
        addMessageLog(messageCuston(BUILDING_ANNOTATION_OPERATION_OF_METHOD, structure.getMethodName()));

        LOG.info(messageCuston(DEFINING_SUMMARY_OF_METHOD_OPERATION, structure.getMethodName()));
        addMessageLog(messageCuston(DEFINING_SUMMARY_OF_METHOD_OPERATION, structure.getMethodName()));
        var summary = createSummaryAndDescription(structure)[SUMMARY].concat(hasBody ? object :
                extractControllerName(structure.getControllerName()));


        LOG.info(messageCuston(DEFINING_DESCRIPTION_OF_METHOD_OPERATION, structure.getMethodName()));
        addMessageLog(messageCuston(DEFINING_DESCRIPTION_OF_METHOD_OPERATION, structure.getMethodName()));
        var description = createSummaryAndDescription(structure)[DESCRIPTION]
                .concat(methodName.concat(" ")
                        .concat(hasBody ?  object :
                                extractControllerName(structure.getControllerName())));

        var operation = buildOperation(summary, description, structure.getVerbHttp());


        LOG.info(messageCuston(CHECKING_IF_METHOD_HAS_PARAMETERS, structure.getMethodName()));
        addMessageLog(messageCuston(CHECKING_IF_METHOD_HAS_PARAMETERS, structure.getMethodName()));
        if(hasParameters(structure.getParameters())) {
            LOG.info(messageCuston(BUILDING_ANNOTATION_PARAMETERS, structure.getMethodName()));
            addMessageLog(messageCuston(BUILDING_ANNOTATION_PARAMETERS, structure.getMethodName()));
            var parameter = buildParametersDocumentation(structure);
            operation.getPairs().add(parameter);
        }
        LOG.info(CHECKING_IF_THE_METHOD_HAS_BODY);
        addMessageLog(CHECKING_IF_THE_METHOD_HAS_BODY);
        if(hasBody)
            operation.getPairs().add(buildModelRequestsAndResponse(object, true));
        return operation;
    }

    private static MemberValuePair buildParametersDocumentation(Structure method) {
        var parameters = method.getParameters();
        ArrayInitializerExpr annotataionParameter = null;
        //TODO: NESTE MÉTODO QUE ESTÁ DANDO ERRO
        LOG.info(messageCuston(CHECKING_ANNOTATION_PARAMETERS_OF_METHOD, method.getMethodName()));
        addMessageLog(messageCuston(CHECKING_ANNOTATION_PARAMETERS_OF_METHOD, method.getMethodName()));
        if(hasManyParameters(method.getParameters().get(TypeParameter.PARAMETER))) {
            LOG.info(messageCuston(INITIALIZE_CONSTRUCTION_OF_PARAMETERS_ANNOTATIONS_OF_METHOD, method.getMethodName()));
            addMessageLog(messageCuston(INITIALIZE_CONSTRUCTION_OF_PARAMETERS_ANNOTATIONS_OF_METHOD, method.getMethodName()));
            annotataionParameter = buildManyParemeters(method.getParameters().get(TypeParameter.PARAMETER));
        } else if(hasSingleParameter(method.getParameters().get(TypeParameter.PARAMETER))) {
            LOG.info(messageCuston(INITIALIZE_CONSTRUCTION_OF_PARAMETER_ANNOTATION_OF_METHOD, method.getMethodName()));
            addMessageLog(messageCuston(INITIALIZE_CONSTRUCTION_OF_PARAMETER_ANNOTATION_OF_METHOD, method.getMethodName()));
            annotataionParameter = buildSingleParameter(method.getParameters().get(TypeParameter.PARAMETER).get(0));
        }
        return new MemberValuePair("parameters",
                annotataionParameter);
    }

    private static ArrayInitializerExpr buildSingleParameter(Clazz parameter) {
        var memberValueParir = buildContentParameter(parameter);
        return new ArrayInitializerExpr(
                new NodeList<>(new NormalAnnotationExpr(buildName(PARAMETER), memberValueParir)));
    }

    private static NodeList<MemberValuePair> buildContentParameter(Clazz parameter) {
        NodeList<MemberValuePair> parameters = new NodeList<>();
        LOG.info(messageCuston(BUILD_PROPERTIES_OF_PARAMETER, parameter.getName()));
        addMessageLog(messageCuston(BUILD_PROPERTIES_OF_PARAMETER, parameter.getName()));
        var typeParameter = parameter.getTypeParameter().equals(PATH_VARIABLE_PROPERTIE) ? buildFieldAccess(PARAMETER_IN, PATH) :
                buildFieldAccess(PARAMETER_IN, QUERY);
        parameters.add(buildMemberValueStringLiteralExpr(NAME, parameter.getName()));
        parameters.add(buildMemberValueStringLiteralExpr(DESCRIPTION_PROPERTIE, parameter.getName()));
        if(isNotBlank(parameter.getTypeParameter())) {
            parameters.add(new MemberValuePair(EXAMPLE_PROPERTIE, valueOfType(parameter.getType())));
        }
        parameters.add(new MemberValuePair(REQUIRED_PROPERTIE, new BooleanLiteralExpr(true)));
        parameters.add(new MemberValuePair(IN_PROPERTIE, typeParameter));
        return parameters;
    }
    public static StringLiteralExpr valueOfType(String type) {
        return buildStringLiteralExpr(TYPE_MAP.get(type));
    }
    private static ArrayInitializerExpr buildManyParemeters(List<Clazz> parameters) {
        NodeList<AnnotationExpr> annotationsParameter = new NodeList<>();
        LOG.info(BUILDING_A_LIST_OF_METHOD_PARAMETERS);
        addMessageLog(BUILDING_A_LIST_OF_METHOD_PARAMETERS);
        parameters.forEach(parameter -> {
            var propertiesAndValue = buildContentParameter(parameter);
            annotationsParameter.add(new NormalAnnotationExpr(buildName(PARAMETER), propertiesAndValue));
        });
        var array = new ArrayInitializerExpr();
        array.getValues().addAll(annotationsParameter);
        return array;
    }

    public static NormalAnnotationExpr buildApiResponseAnnotation(Structure type) {
        LOG.info(INITIALIZING_CONSTRUCTION_OF_API_RESPONSES_ANNOTATIONS);
        addMessageLog(INITIALIZING_CONSTRUCTION_OF_API_RESPONSES_ANNOTATIONS);
        var success = buildSuccessAnnotation(type);
        var notFound = buildBadNotFoundAnnotation();
        var badRequest = buildBadRequestAnnotation();
        var noContent = buildNoContentAnnotation();
        var internalError = buildInternalErrorAnnotation();

        var arrayInitializerExpr = buildArrayInitializerExpr(success, notFound, badRequest, noContent, internalError);
        NodeList<MemberValuePair> responseStatus = new NodeList<>();
        var memberValuerPair = new MemberValuePair("value", arrayInitializerExpr);
        responseStatus.add(memberValuerPair);

        return new NormalAnnotationExpr(buildName(API_RESPONSES),
                responseStatus);
    }


    private static NormalAnnotationExpr buildSuccessAnnotation(Structure type) {
        LOG.info(INITIALIZING_CONSTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_200);
        addMessageLog(INITIALIZING_CONSTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_200);
        var apiResponse = buildApiResponse("200", SUCCESS);
        var returnType = type.getResponse().entrySet().stream().findFirst().get();

        if(returnType.getKey().equals(TypeReturn.VOID))
            return apiResponse;

        if(returnType.getValue().isCollection()) {
            //TODO: Implementar adição de coleção no apiResponse
            LOG.info("Adding method for Collection of objects");
            return apiResponse;
        }

        apiResponse.getPairs().add(buildModelRequestsAndResponse(returnType.getValue().getType(), false));

        return apiResponse;
    }

    private static MemberValuePair buildModelRequestsAndResponse(String object, boolean requestBody) {
        LOG.info(INITIALIZING_CONSTRUCTION_OF_MODEL_REQUEST_AND_RESPONSE);
        addMessageLog(INITIALIZING_CONSTRUCTION_OF_MODEL_REQUEST_AND_RESPONSE);
        //Construir exemples
        LOG.info(BUIDING_CONTENT_OF_ANNOTATION_EXAMPLE_OBJECT);
        addMessageLog(BUIDING_CONTENT_OF_ANNOTATION_EXAMPLE_OBJECT);
        var examplesPairs = newInstanceMemberValuePair();
        examplesPairs.add(new MemberValuePair(NAME, buildStringLiteralExpr(object)));
        examplesPairs.add(new MemberValuePair(REF, buildStringLiteralExpr(PATH_EXAMPLE_OBJECT
                .concat(object.concat(DOT_JSON)))));

        //Construir schema
        LOG.info(BUIDING_CONTENT_OF_ANNOTATION_SCHEMA);
        addMessageLog(BUIDING_CONTENT_OF_ANNOTATION_SCHEMA);
        var schemaPairs = newInstanceMemberValuePair();
        schemaPairs.add(new MemberValuePair(NAME, buildStringLiteralExpr(object)));
        schemaPairs.add(new MemberValuePair(IMPLEMENTATIONS, buildClassExpr(object)));

        //Construir o content
        LOG.info(BUIDING_CONTENT_OF_ANNOTATION_CONTENT);
        addMessageLog(BUIDING_CONTENT_OF_ANNOTATION_CONTENT);
        var contentPairs = newInstanceMemberValuePair();
        contentPairs.add(buildContentTypePair(MEDIA_TYPE));
        contentPairs.add(buildSchemaPair(SCHEMA, schemaPairs));
        contentPairs.add(buildExampleObjectPair(EXAMPLE_OBJECT, examplesPairs));


        //Criar ArrayContent
        LOG.info(ADDING_CONTENT_IN_ARRAY_OF_ANNOTATION_CONTENT);
        addMessageLog(ADDING_CONTENT_IN_ARRAY_OF_ANNOTATION_CONTENT);
        var contentArray = new ArrayInitializerExpr();
        contentArray.getValues().add(buildContentArrayExpr(CONTENT, contentPairs));

        //Retorna se for requestBody
        LOG.info(CHECKING_IF_HAS_REQUEST_BODY_IN_OPERATION_OF_METHOD);
        addMessageLog(CHECKING_IF_HAS_REQUEST_BODY_IN_OPERATION_OF_METHOD);
        if (!requestBody) return new MemberValuePair(CONTENT[0], contentArray);

        //Adicionar RequestBody
        LOG.info(BUILDING_PROPERTIE_REQUEST_BODY_OF_ANNOTATION_OPERATION);
        addMessageLog(BUILDING_PROPERTIE_REQUEST_BODY_OF_ANNOTATION_OPERATION);
        var requestBodyPairs = newInstanceMemberValuePair();
        requestBodyPairs.add(new MemberValuePair(CONTENT[0], contentArray));

        //Return requestBody ou Objeto de response
        return new MemberValuePair(REQUEST_BODY[0],
                new NormalAnnotationExpr(buildName(REQUEST_BODY[1]), requestBodyPairs));
    }


    public static NormalAnnotationExpr buildOperation(String summary, String description, String verbHttp) {
        LOG.info(BUILDING_OPERATION_ANNOTATION);
        addMessageLog(BUILDING_OPERATION_ANNOTATION);
        var nodeListMember = new NodeList<MemberValuePair>();
        var memberValueOperation =  new MemberValuePair("summary", buildStringLiteralExpr(summary));
        var memberValueDescription = new MemberValuePair("description", buildStringLiteralExpr(description));
        var memberValueMethod = new MemberValuePair("method", buildStringLiteralExpr(verbHttp));
        nodeListMember.add(memberValueOperation);
        nodeListMember.add(memberValueDescription);
        nodeListMember.add(memberValueMethod);

        return new NormalAnnotationExpr(
                buildName(OPERATION),
                nodeListMember
        );
    }

    private static NormalAnnotationExpr buildApiResponse(String statusCode, String description) {
        LOG.info(BUILDING_API_RESPONSE_ANNOTATION);
        addMessageLog(BUILDING_API_RESPONSE_ANNOTATION);
        var nodeListMember = new NodeList<MemberValuePair>();
        var memberValueResponseCode =  new MemberValuePair("responseCode", buildStringLiteralExpr(statusCode));
        var memberValueDescription = new MemberValuePair("description", buildStringLiteralExpr(description));

        nodeListMember.add(memberValueResponseCode);
        nodeListMember.add(memberValueDescription);

        return new NormalAnnotationExpr(
                buildName(API_RESPONSE),
                nodeListMember
        );
    }
    //TODO: IMPLEMENTAR CASO ESTEJA TRATANDO ARRAY
    private static NodeList<MemberValuePair> buildContentValue(String requestBodyClass) {
        NodeList<MemberValuePair> contentsValue = new NodeList<>();
        var schema = true ? buildSchema(requestBodyClass) : buildArraySchema(requestBodyClass);
        var examples = buildExample(requestBodyClass);
        contentsValue.add(schema);
        contentsValue.add(examples);
        return contentsValue;
    }

    private static MemberValuePair buildArraySchema(String requestBody) {
        //TODO: IMPLEMENTAR EXEMPLOS DE ARRAYS
        return null;
    }


    private static MemberValuePair buildExample(String requestBody) {
        LOG.info(BUILDING_EXAMPLE_OBJECT_ANNOTATION);
        addMessageLog(BUILDING_EXAMPLE_OBJECT_ANNOTATION);
        return new MemberValuePair(
                EXAMPLE_OBJECT[0],
                new NormalAnnotationExpr(buildName(EXAMPLE_OBJECT[1]),
                        buildSchemaAndExamples(requestBody, EXAMPLE_OBJECT[0])
                )
        );
    }

    private static MemberValuePair buildExampleObjectPair(String[] exampleObject, NodeList<MemberValuePair> examplesPairs) {
        LOG.info(BUILDING_PROPERTIE_EXAMPLE_ANNOTATION);
        addMessageLog(BUILDING_PROPERTIE_EXAMPLE_ANNOTATION);
        return new MemberValuePair(exampleObject[0], new NormalAnnotationExpr(buildName(exampleObject[1]), examplesPairs));
    }


    private static MemberValuePair buildSchema(String requestBody) {
        LOG.info(BUILDING_SCHEMA_PROPERTIE_AND_SCHEMA_ANNOTATION);
        addMessageLog(BUILDING_SCHEMA_PROPERTIE_AND_SCHEMA_ANNOTATION);
        return new MemberValuePair(
                SCHEMA[0],
                new NormalAnnotationExpr(buildName(SCHEMA[1]),
                        buildSchemaAndExamples(requestBody, SCHEMA[0])
                )
        );
    }

    private static FieldAccessExpr buildFieldAccess(String type, String enumeration) {
        LOG.info(BUILDING_FIELD_ACCESS);
        addMessageLog(BUILDING_FIELD_ACCESS);
        return new FieldAccessExpr(buildNameExpr(type), enumeration);
    }

    private static NodeList<MemberValuePair> buildSchemaAndExamples(String object, String typeExample) {
        LOG.info(INITALIZING_CONTRUCTION_OF_SCHEMA_AND_EXAMPLE_OBJECT_ANNOTATIONS);
        addMessageLog(INITALIZING_CONTRUCTION_OF_SCHEMA_AND_EXAMPLE_OBJECT_ANNOTATIONS);
        var memberList = new NodeList<MemberValuePair>();
        memberList.add(new MemberValuePair(NAME, buildStringLiteralExpr(object)));
        var memberExampleObject = typeExample.equals(SCHEMA[0]) ?
                new MemberValuePair(IMPLEMENTATIONS, new ClassExpr(parseClassType(object))) :
                new MemberValuePair(REF, buildStringLiteralExpr(PATH_EXAMPLE_OBJECT.concat(object.concat(DOT_JSON))));
        memberList.add(memberExampleObject);
         return memberList;
    }

    private static Name buildName(String name) {return new Name(name);}

    private static NameExpr buildNameExpr(String name) {return new NameExpr(name);}

    private static StringLiteralExpr buildStringLiteralExpr(String name) {return new StringLiteralExpr(name);}

    public static FieldAccessExpr buildAccessExpr(String scope, String field) {
        return new FieldAccessExpr(buildNameExpr(scope), field);
    }

    private static NormalAnnotationExpr buildBadNotFoundAnnotation() {
        LOG.info(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_204);
        addMessageLog(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_204);
        return buildApiResponse("204", NOT_FOUND);
    }

    private static NormalAnnotationExpr buildNoContentAnnotation() {
        LOG.info(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_404);
        addMessageLog(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_404);
        return buildApiResponse("404", NO_CONTENT);
    }
    private static NormalAnnotationExpr buildBadRequestAnnotation() {
        LOG.info(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_400);
        addMessageLog(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_400);
        return buildApiResponse("400", BAD_REQUEST);
    }

    private static NormalAnnotationExpr buildInternalErrorAnnotation() {
        LOG.info(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_500);
        addMessageLog(INITALIZING_CONTRUCTION_OF_API_RESPONSES_ANNOTATIONS_FOR_STATUS_CODE_500);
        return buildApiResponse("500", INTERNAL_SERVER_ERROR);
    }

    private static ClassExpr buildClassExpr(String className) {
        return new ClassExpr(parseClassType(className));
    }

    private static ClassOrInterfaceType parseClassType(String className) {
        return StaticJavaParser.parseClassOrInterfaceType(className);
    }

    private static NormalAnnotationExpr buildAnnotationExpr(String name, MemberValuePair... pairs) {
        var annotation = new NormalAnnotationExpr(buildName(name), new NodeList<>());
        for (MemberValuePair pair: pairs) {
            annotation.getPairs().add(pair);
        }
        return annotation;
    }

    private static MemberValuePair buildPair(String name, Expression value) {
        return new MemberValuePair(name, value);
    }
    private static ArrayInitializerExpr buildArrayInitializerExpr(Expression... values) {
        var arrayInitializerExpr = new ArrayInitializerExpr();
        for(var expression: values) {
            arrayInitializerExpr.getValues().add(expression);
        }
        return arrayInitializerExpr;
    }

    private static NodeList<MemberValuePair> newInstanceMemberValuePair() {
        return new NodeList<>();
    }

    private static MemberValuePair buildContentTypePair(String[] mediaType) {
        return new MemberValuePair(mediaType[0], buildFieldAccess(mediaType[1], APPLICATION_JSON));
    }

    private static MemberValuePair buildSchemaPair(String[] schema, NodeList<MemberValuePair> schemaPairs) {
        return new MemberValuePair(schema[0], new NormalAnnotationExpr(buildName(schema[1]), schemaPairs));
    }

    private static Expression buildContentArrayExpr(String[] content, NodeList<MemberValuePair> contentPairs) {
        return new NormalAnnotationExpr(buildName(CONTENT[1]), contentPairs);
    }
    private static MemberValuePair buildMemberValueStringLiteralExpr(String label, String value) {
        return new MemberValuePair(label, buildStringLiteralExpr(value));
    }
    public static void removeAnnotations(NodeWithAnnotations<?> node) {
        node.getAnnotations().removeIf(annotation -> ANNOTATIONS.contains(annotation.getNameAsString()));
    }

    private static void addMessageLog(String message) {
        logMessage(message, logArea);
    }
}
