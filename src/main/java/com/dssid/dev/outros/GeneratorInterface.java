package com.dssid.dev.outros;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class GeneratorInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorInterface.class);
    private String packageController;
    private String basePath;

    private String src;

    private Path pathInterfaces;
    private Set<Class<?>> controllers = new HashSet<>();

//    public boolean checkPakageController(String packageController) {
//        //Ajustar path substituindo '.' por '/'
//        this.packageController = packageController;
//        var basePath = SRC_JAVA.concat("/").concat(packageController.replace(".", "/"));
//
//        //Cria o path para criar o subpacote interfaces se não existir
//        var strInterface = basePath.concat(INTERFACES);
//
//        //Verificar se existe subpacote interfaces, se não existir criar
//        this.pathInterfaces = Paths.get(strInterface);
//        createDirectories(this.pathInterfaces);
//
//        //Listar classes controladoras do pacote controller
//        controllers = findAllControllers(this.packageController);
//        return true;
//    }

//    public static void executeGenerateInterface() {
//        execute(this.pathInterfaces, this.controllers);
//    }

    public String getPackageController() {
        return packageController;
    }

    public void setPackageController(String packageController) {
        this.packageController = packageController;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}
