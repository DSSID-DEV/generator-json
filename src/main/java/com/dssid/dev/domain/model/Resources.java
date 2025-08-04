package com.dssid.dev.domain.model;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resources {
    private String project;
    private String pathProject;
    private String mainDirJava;
    private String mainDirResources;
    private String packageController;
    private String absolutPathPackege;
    private String packageIntefaceOfController;

    @Override
    public String toString() {
        var templateMessage = """
                Directory of project: %s
                Directory main java: %s
                Package of controllers: %s
                Directory main resources: %s
                Absolute path of the package: %s""";
        return String.format(templateMessage, pathProject, mainDirJava, packageController, mainDirResources, absolutPathPackege);
    }
}
