package com.dssid.dev.utils;

public class ConstantsTemplates {

    public static final String TEMPLATE_DOCUMENTATION_SWAGGER_FOR_METHOD_POST = """
            @Operation(sumary="Register new Object", 
             description="Endpoint to save Objcet", 
             requestBody = @RequestBody(
                 content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                     schema = @Schema(name="Object", implementation=Object.class), 
                     examples = @Example(
                         name="Object", 
                         ref="@components/examples/Object.json"
                         ))})
            )
             @ApiResponses(value = {
                     @ApiResponse(responseCode = "200", description = "Payload saved or updated successfully",
                             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema = @Schema(implementation = DirectCashbackPayloadsEntity.class),
                             examples =  @ExampleObject(
                                     name = "DirectCashbackPayloadsEntity",
                                     ref = "@components/examples/DirectCashbackPayloadsEntity.json"
                             ))),
                     @ApiResponse(responseCode = "400", description = "Bad Request"),
                     @ApiResponse(responseCode = "500", description = "Internal Server Error")
             })
             """;

    public static final String TEMPLATE_DOCUMENTATION_SWAGGER_FOR_METHOD_PUT = """
            @Operation(sumary="Update new Object", 
             description="Endpoint to update a Object", 
             requestBody = @RequestBody(
                 content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                 schema = @Schema(name="Object", implementation=Object.class), 
                 examples = @Example(
                     name="Object", 
                     ref="@components/examples/Object.json"
                     ))}
             ), 
             @Parameter(name = "Object", description = "Object", example = "Object", required = true, in = ParameterIn.Object)
            )
             @ApiResponses(value = {
                     @ApiResponse(responseCode = "200", description = "Payload saved or updated successfully",
                             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,,
                                     schema = @Schema(implementation = DirectCashbackPayloadsEntity.class),
                             examples =  @ExampleObject(
                                     name = "DirectCashbackPayloadsEntity",
                                     ref = "@components/examples/DirectCashbackPayloadsEntity.json"
                             ))),
                     @ApiResponse(responseCode = "400", description = "Bad Request"),
                     @ApiResponse(responseCode = "404", description = "Bad Request"),
                     @ApiResponse(responseCode = "500", description = "Internal Server Error")
             })
             """;

    public static final String TEMPLATE_DOCUMENTATION_SWAGGER_FOR_METHOD_GET_SINGLE = """
            @Operation(sumary="Return a Object", 
             description="Endpoint to return a Object", 
             @Parameter(name = "Object", description = "Object", example = "Object", required = true, in = ParameterIn.Object)
            )
             @ApiResponses(value = {
                     @ApiResponse(responseCode = "200", description = "Payload saved or updated successfully",
                             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,,
                                     schema = @Schema(implementation = DirectCashbackPayloadsEntity.class),
                             examples =  @ExampleObject(
                                     name = "DirectCashbackPayloadsEntity",
                                     ref = "@components/examples/DirectCashbackPayloadsEntity.json"
                             ))),
                     @ApiResponse(responseCode = "400", description = "Bad Request"),
                     @ApiResponse(responseCode = "404", description = "Bad Request"),
                     @ApiResponse(responseCode = "500", description = "Internal Server Error")
             })
             """;

    public static final String TEMPLATE_DOCUMENTATION_SWAGGER_FOR_METHOD_GET_COLLECTION = """
            @Operation(sumary="Return a collection of Object", 
             description="Endpoint to list Object", 
             @Parameter(name = "Object", description = "Object", example = "Object", required = true, in = ParameterIn.Object)
            )
             @ApiResponses(value = {
                     @ApiResponse(responseCode = "200", description = "Payload saved or updated successfully",
                             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     array = @ArraySchema(arraySchema = @Schema(implementation = DirectCashbackPayloadsEntity.class)),
                             examples =  @ExampleObject(
                                     name = "Object",
                                     ref = "@components/examples/Object.json"
                             ))),
                     @ApiResponse(responseCode = "204", description = "No Content"),
                     @ApiResponse(responseCode = "400", description = "Bad Request"),
                     @ApiResponse(responseCode = "404", description = "Bad Request"),
                     @ApiResponse(responseCode = "500", description = "Internal Server Error")
             })
             """;

    public static final String TEMPLATE_DOCUMENTATION_SWAGGER_FOR_METHOD_DELETE = """
            @Operation(sumary = "Remove a Object",
             description="Endpoint to remove a Object", 
             @Parameter(name = "Object", description = "Object", example = "Object", required = true, in = ParameterIn.Object)
            )
             @ApiResponses(value = {
                     @ApiResponse(responseCode = "200", description = "Payload saved or updated successfully"),
                     @ApiResponse(responseCode = "400", description = "Bad Request"),
                     @ApiResponse(responseCode = "404", description = "Bad Request"),
                     @ApiResponse(responseCode = "500", description = "Internal Server Error")
             })
             """;

}
