package com.dssid.dev.utils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dssid.dev.constants.Constants.*;
import static com.dssid.dev.constants.MessageLog.messageCuston;
import static com.dssid.dev.verification.VerificationType.containsAnnotation;
import static com.dssid.dev.verification.VerificationType.isEntity;
import static com.dssid.dev.view.components.LogMessage.cleanLog;
import static com.dssid.dev.view.components.LogMessage.logMessage;
import static com.dssid.dev.constants.MessageLog.ERROR_TRYING_TO_GET_CLASS_FILE;

public abstract class SearchFileJava {
    static JTextArea logArea;

    public static List<Path> findEntityClasses(Path pathRoot, JTextArea jTextArea) {
        logArea = jTextArea;
        cleanLog(logArea);
        logMessage(STARTING_ENTITY_SEARCH, logArea);

        List<Path> classFiles = new ArrayList<>();
        try {
            Files.walkFileTree(pathRoot, new SimpleFileVisitor<>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if(file.toString().endsWith(DOT_JAVA)) {
                        classFiles.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return classFiles.stream()
                .filter(file -> containsAnnotation(file, logArea))
                .map(Path::toAbsolutePath)
                .collect(Collectors.toList());
    }


    public static Path findClassFiles(Path pathRoot, String normalizedClassName, JTextArea jTextArea) {
        logArea = jTextArea;
        List<Path> foundPaths = new ArrayList<>();
        try {
            Files.walkFileTree(pathRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if(file.getFileName().toString().equals(normalizedClassName)) {
                        foundPaths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logMessage(messageCuston(ERROR_TRYING_TO_GET_CLASS_FILE, normalizedClassName), logArea);
            throw new RuntimeException(e);
        }
        if(foundPaths.isEmpty()) return null;
        return foundPaths.get(0);
    }

    public static Optional<Path> searchFile(Path pathRoot, List<String> canditates, boolean searchEntity, JTextArea jTextArea) {
        logArea = jTextArea;
        for(var canditate : canditates) {
            try(var stream = Files.walk(pathRoot)) {
                String finalCanditate = canditate.contains(DOT_JAVA)
                        || canditate.contains(DOT_CLASS) ? canditate
                        : canditate.concat(DOT_JAVA);
                Optional<Path> result = stream.filter(p -> !Files.isDirectory(p))
                        .filter(p -> {
                            String file = p.getFileName().toString();
                            if(searchEntity) return file.equals(finalCanditate) && isEntity(p);
                            else return file.equals(finalCanditate);
                        }).findFirst();
                if(result.isPresent()) return result;
            } catch (IOException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
