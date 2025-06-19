package com.dssid.dev;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static com.dssid.dev.utils.Constants.INTERFACES;
import static com.dssid.dev.utils.Constants.SRC_JAVA;
import static com.dssid.dev.utils.FileUtils.execute;
import static com.dssid.dev.utils.FileUtils.findAllControllers;
import static com.dssid.dev.utils.Utils.createDirectories;
public class GeneratorInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorInterface.class);
    private String packageController;
    private String basePath;

    private String src;

    private Path pathInterfaces;
    private Set<Class<?>> controllers = new HashSet<>();

    public boolean checkPakageController(String packageController) {
        //Ajustar path substituindo '.' por '/'
        this.packageController = packageController;
        var basePath = SRC_JAVA.concat("/").concat(packageController.replace(".", "/"));

        //Cria o path para criar o subpacote interfaces se não existir
        var strInterface = basePath.concat(INTERFACES);

        //Verificar se existe subpacote interfaces, se não existir criar
        this.pathInterfaces = Paths.get(strInterface);
        createDirectories(this.pathInterfaces);

        //Listar classes controladoras do pacote controller
        this.controllers = findAllControllers(this.packageController);
        return true;
    }

    public void executeGenerateInterface() {
        execute(this.pathInterfaces, this.controllers);
    }
}
