package com.dssid.dev.utils;

import com.dssid.dev.domain.model.Clazz;
import com.dssid.dev.domain.model.Structure;
import com.dssid.dev.enums.VerbHttp;
import com.dssid.dev.enums.TypeParameter;
import com.dssid.dev.enums.TypeReturn;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.dssid.dev.utils.Constants.*;
import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.utils.VerificationType.*;

public class SwaggerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SwaggerBuilder.class);
    public static NodeList<ImportDeclaration> getImports(NodeList<ImportDeclaration> imports) {
        LOG.info("Copying imports");
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
        LOG.info("Adding swagger imports");
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

    public static NormalAnnotationExpr buildTagAnnotation(Class<?> controller) {
        LOG.info("Building TAG Annotation");
        //Adicionar tag do swagger
        NodeList<MemberValuePair> memberValuePairs = new NodeList<>();
        createAnnotation(memberValuePairs, controller);
        return new NormalAnnotationExpr(
                new Name(TAG),
                memberValuePairs
        );
    }

    private static void createAnnotation(NodeList<MemberValuePair> memberValuePairs, Class<?> controller) {
        LOG.info("Adding name and description of TAG annotation");
        memberValuePairs
                .add(new MemberValuePair("name", new StringLiteralExpr(controller.getSimpleName())));
        memberValuePairs
                .add(new MemberValuePair("description", new StringLiteralExpr("Interface para " + controller.getSimpleName())));
    }

    public static void addDocumentationOfSwaggerForMethod(MethodDeclaration methodDeclaration, Structure methodStruct) {
        LOG.info("Checking if the mapping is contained in the method");
        if(!isMappingType(methodDeclaration.getAnnotations(), methodStruct.getVerbHttp())) return;

        //Criar tag Operation
        LOG.info("Create @Operation of method ", methodDeclaration.getNameAsString());
        var operation = buildAnnotationOperation(methodStruct, VerbHttp.valueOf(methodStruct.getVerbHttp()));
        methodDeclaration.addAnnotation(operation);

        //Criar tag ApiApplication
        LOG.info("Create @Application(s) of method ", methodDeclaration.getNameAsString());
        var apiResponseAnnotation = buildApiResponseAnnotation(methodStruct);
        methodDeclaration.addAnnotation(apiResponseAnnotation);
    }

    private static NormalAnnotationExpr buildAnnotationOperation(Structure structure, VerbHttp verbHttp) {
        var methodName = formatarCamelCaseParaEspacado(structure.getMethodName());

        boolean hasBody = hasBody(structure.getParameters().get(TypeParameter.BODY));

        String object = !structure.getParameters().get(TypeParameter.BODY).isEmpty() ?
                structure.getParameters().get(TypeParameter.BODY).get(0).getType() : OBJECT;
        structure.addObjectName(object);

        LOG.info("Building annotation @Operation of method " + structure.getMethodName());
        LOG.info("Defining summary of method operation " + structure.getMethodName());
        var summary = createSummaryAndDescription(structure)[SUMMARY].concat(hasBody ? object :
                extractControllerName(structure.getControllerName()));

        LOG.info("Defining description of method operation " + structure.getMethodName());
        var description = createSummaryAndDescription(structure)[DESCRIPTION]
                .concat(methodName.concat(" ")
                        .concat(hasBody ?  object :
                                extractControllerName(structure.getControllerName())));

        var operation = buildOperation(summary, description, structure.getVerbHttp());

        LOG.info("Checking if method " + structure.getMethodName() + "has parameter(s)");
        if(hasParameters(structure.getParameters())) {
            LOG.info("Building annotation @Parameter(s) of " + structure.getMethodName());
            var parameter = buildParametersDocumentation(structure);
            operation.getPairs().add(parameter);
        }
        LOG.info("Checking if the method has a body");
        if(hasBody)
            operation.getPairs().add(buildModelRequestsAndResponse(object, true));
        return operation;
    }

    private static boolean hasParameters(Map<TypeParameter, List<Clazz>> parameters) {
        return !parameters.get(TypeParameter.PARAMETER).isEmpty();
    }

    private static MemberValuePair buildParametersDocumentation(Structure method) {
        var parameters = method.getParameters();
        ArrayInitializerExpr annotataionParameter = null;
        //TODO: NESTE MÉTODO QUE ESTÁ DANDO ERRO
        LOG.info("Checking annotation @Parameter(s) of " + method.getMethodName());
        if(hasManyParameters(method.getParameters().get(TypeParameter.PARAMETER))) {
            LOG.info("Initialize construction of @Parameters annotations of method " + method.getMethodName());
            annotataionParameter = buildManyParemeters(method.getParameters().get(TypeParameter.PARAMETER));
        } else if(hasSingleParameter(method.getParameters().get(TypeParameter.PARAMETER))) {
            LOG.info("Initialize construction of @Parameter annotation of method " + method.getMethodName());
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
        LOG.info("Build properties of parameter " + parameter.getName());
        var typeParameter = parameter.getTypeParameter().equals("PathVariable") ? buildFieldAccess(PARAMETER_IN, PATH) :
                buildFieldAccess(PARAMETER_IN, QUERY);
        parameters.add(buildMemberValueStringLiteralExpr(NAME, parameter.getName()));
        parameters.add(buildMemberValueStringLiteralExpr("description", parameter.getName()));
        if(isNotBlank(parameter.getTypeParameter())) {
            parameters.add(new MemberValuePair("example", valueOfType(parameter.getType())));
        }
        parameters.add(new MemberValuePair("required", new BooleanLiteralExpr(true)));
        parameters.add(new MemberValuePair("in", typeParameter));
        return parameters;
    }
    public static StringLiteralExpr valueOfType(String type) {
        return buildStringLiteralExpr( TYPE_MAP.get(type));
    }
    private static ArrayInitializerExpr buildManyParemeters(List<Clazz> parameters) {
        NodeList<AnnotationExpr> annotationsParameter = new NodeList<>();
        LOG.info("Building a list of method parameters");
        parameters.forEach(parameter -> {
            var propertiesAndValue = buildContentParameter(parameter);
            annotationsParameter.add(new NormalAnnotationExpr(buildName(PARAMETER), propertiesAndValue));
        });
        var array = new ArrayInitializerExpr();
        array.getValues().addAll(annotationsParameter);
        return array;
    }

    public static NormalAnnotationExpr buildApiResponseAnnotation(Structure type) {
        LOG.info("Initializing construction of api responses annotations");
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
        LOG.info("Initializing construction of api responses annotations for status code 200");
        var apiResponse = buildApiResponse("200", "Success");
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
        LOG.info("Initializing construction of model request and response");
        //Construir exemples
        LOG.info("Building content of annotation @ExampleObject");
        var examplesPairs = newInstanceMemberValuePair();
        examplesPairs.add(new MemberValuePair(NAME, buildStringLiteralExpr(object)));
        examplesPairs.add(new MemberValuePair(REF, buildStringLiteralExpr(PATH_EXAMPLE_OBJECT
                .concat(object.concat(DOT_JSON)))));

        //Construir schema
        LOG.info("Building content of annotation @Schema");
        var schemaPairs = newInstanceMemberValuePair();
        schemaPairs.add(new MemberValuePair(NAME, buildStringLiteralExpr(object)));
        schemaPairs.add(new MemberValuePair(IMPLEMENTATIONS, buildClassExpr(object)));

        //Construir o content
        LOG.info("Building content of annotation @Content");
        var contentPairs = newInstanceMemberValuePair();
        contentPairs.add(buildContentTypePair(MEDIA_TYPE));
        contentPairs.add(buildSchemaPair(SCHEMA, schemaPairs));
        contentPairs.add(buildExampleObjectPair(EXAMPLE_OBJECT, examplesPairs));


        //Criar ArrayContent
        LOG.info("Adding content in array of annotation @Content");
        var contentArray = new ArrayInitializerExpr();
        contentArray.getValues().add(buildContentArrayExpr(CONTENT, contentPairs));

        //Retorna se for requestBody
        LOG.info("Checking if has request body in operation of method");
        if (!requestBody) return new MemberValuePair(CONTENT[0], contentArray);

        //Adicionar RequestBody
        LOG.info("Building propertie requestBody of annotation @Operation");
        var requestBodyPairs = newInstanceMemberValuePair();
        requestBodyPairs.add(new MemberValuePair(CONTENT[0], contentArray));

        //Return requestBody ou Objeto de response
        return new MemberValuePair(REQUEST_BODY[0],
                new NormalAnnotationExpr(buildName(REQUEST_BODY[1]), requestBodyPairs));
    }


    public static NormalAnnotationExpr buildOperation(String summary, String description, String verbHttp) {
        LOG.info("Building operation annotation");
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
        LOG.info("Building api response annotation");
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
        LOG.info("Building example object annotation");
        return new MemberValuePair(
                EXAMPLE_OBJECT[0],
                new NormalAnnotationExpr(buildName(EXAMPLE_OBJECT[1]),
                        buildSchemaAndExamples(requestBody, EXAMPLE_OBJECT[0])
                )
        );
    }

    private static MemberValuePair buildExampleObjectPair(String[] exampleObject, NodeList<MemberValuePair> examplesPairs) {
        LOG.info("Building propertie example annotation");
        return new MemberValuePair(exampleObject[0], new NormalAnnotationExpr(buildName(exampleObject[1]), examplesPairs));
    }


    private static MemberValuePair buildSchema(String requestBody) {
        LOG.info("Building schema propertie and schema annotation");
        return new MemberValuePair(
                SCHEMA[0],
                new NormalAnnotationExpr(buildName(SCHEMA[1]),
                        buildSchemaAndExamples(requestBody, SCHEMA[0])
                )
        );
    }

    private static FieldAccessExpr buildFieldAccess(String type, String enumeration) {
        LOG.info("Building field access");
        return new FieldAccessExpr(buildNameExpr(type), enumeration);
    }

    private static NodeList<MemberValuePair> buildSchemaAndExamples(String object, String typeExample) {
        LOG.info("Initializing construction of schema and exampleObject annotations");
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
        LOG.info("Initializing construction of api responses annotations for status code 204");
        return buildApiResponse("204", "Not Found");
    }

    private static NormalAnnotationExpr buildNoContentAnnotation() {
        LOG.info("Initializing construction of api responses annotations for status code 400");
        return buildApiResponse("404", "No Content");
    }

    private static NormalAnnotationExpr buildBadRequestAnnotation() {
        LOG.info("Initializing construction of api responses annotations for status code 400");
        return buildApiResponse("400", "Bad Request");
    }

    private static NormalAnnotationExpr buildInternalErrorAnnotation() {
        LOG.info("Initializing construction of api responses annotations for status code 500");
        return buildApiResponse("500", "Internal Server Error");
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
}
