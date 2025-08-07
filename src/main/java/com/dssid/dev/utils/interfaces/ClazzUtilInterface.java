package com.dssid.dev.utils.interfaces;

import com.dssid.dev.domain.model.Payload;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public interface ClazzUtilInterface {


    Payload getPayloadProperties(ClassOrInterfaceDeclaration classDecl, ClassOrInterfaceDeclaration entity);

    Payload getInstanceProperties(ClassOrInterfaceDeclaration entityClass);
}
