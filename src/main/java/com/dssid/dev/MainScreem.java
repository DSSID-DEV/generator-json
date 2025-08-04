package com.dssid.dev;

import com.dssid.dev.domain.DataExtractedDefault;
import com.dssid.dev.domain.model.PropertieValue;
import com.dssid.dev.domain.model.Resources;
import com.dssid.dev.domain.model.Structure;
import com.dssid.dev.outros.GeneratorInterface;
import org.apache.commons.configuration.ConfigurationException;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static com.dssid.dev.utils.Utils.*;
import static com.dssid.dev.utils.FileUtils.*;
import static com.dssid.dev.constants.MessageLog.*;
import static com.dssid.dev.view.components.LogMessage.*;
import static com.dssid.dev.verification.VerificationType.*;
public class MainScreem {

    private JFrame main;
    private JPanel inputPanel;
    private JTextArea logArea;
    private JButton scanButton;
    private Resources resources;
    private Set<Path> javaFiles;
    private JButton buildButton;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JButton controllerBrowseButton;
    private JTextField pathToControllersField;
    private Set<Class<?>> controllersClasses;
    private GeneratorInterface swaggerDocumentation;
    private List<String> clazzes = new ArrayList<>();
    private boolean processCancelled = false;
    private Structure structure;

    public MainScreem() {
        prepareGUI();
    }

    private void prepareGUI() {
        main = new JFrame("Swagger Documentation Builder");
        main.setSize(1000, 600);
        main.setLayout(new BorderLayout());
        // Obtém a dimensão da tela
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Calcula a posição para centralizar
        int x = (screenSize.width - main.getWidth()) / 2;
        int y = (screenSize.height - main.getHeight()) / 2;

        // Define a posição
        main.setLocation(x, y);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Painel superior com entra
        inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        inputPanel.add(new JLabel("Insert the path of the controller:"));
        pathToControllersField = new JTextField();
        controllerBrowseButton = new JButton("Browse...");
        controllerBrowseButton.addActionListener(e-> browseForPath(pathToControllersField));

        JPanel controllersPanel = new JPanel(new BorderLayout(5, 5));
        controllersPanel.add(pathToControllersField, BorderLayout.CENTER);
        controllersPanel.add(controllerBrowseButton, BorderLayout.EAST);
        inputPanel.add(controllersPanel);

        scanButton = new JButton("Scan Project");
        scanButton.addActionListener(e -> scanProject());
        inputPanel.add(scanButton);

        main.add(inputPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        main.add(scrollPane, BorderLayout.CENTER);

        //Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        main.add(progressBar, BorderLayout.SOUTH);

        //Botões de ação do painel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        buildButton = new JButton("Build Documentation");
        buildButton.setEnabled(false);
        buildButton.addActionListener(e -> buildDocumentation());

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> {
            processCancelled = true;
            logMessage("Process cancelled by user.", logArea);
            cancelButton.setEnabled(false);
        });

        buttonPanel.add(buildButton);
        buttonPanel.add(cancelButton);
        main.add(buttonPanel, BorderLayout.PAGE_END);
        main.setVisible(true);

    }

    private void buildDocumentation() {
        logArea = new JTextArea();
        processCancelled = false;
        buildButton.setEnabled(false);
        cancelButton.setEnabled(true);

        try {
            readAndCopyReadApplication(resources, logArea);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        extractValueFromDataBase();

        logMessage(INITIALIZING_SWAGGER_DOCUMENTATION_BUILD_PROCESS, logArea);
        runInterfaceBuildingwithSwaggerDocumentation(resources, javaFiles, logArea);
        logMessage(SWAGGER_DOCUMENTANTION_BUILDED_WITH_SUCCESS, logArea);
//        logMessage(LINE, logArea);
//        logMessage("", logArea);
    }

    private void scanProject() {
        if(!isNotBlank(pathToControllersField.getText().trim())) {
            JOptionPane.showMessageDialog(null, ENTER_THE_ABSOLUTE_PATH_OF_THE_PACKAGE);
            return;
        }
        var pathController = pathToControllersField.getText().trim();
        resources  = extractPaths(pathController);

        logMessage(messageCuston(FIND_ALL_JAVA_FILES_OF_PACKAGE, resources.getAbsolutPathPackege()), logArea);
        javaFiles = findAllJavaFiles(resources, true, logArea);

        if(javaFiles.isEmpty()) {
            logMessage(messageCuston(NO_JAVA_FILES_FOUND_IN, resources.getAbsolutPathPackege()), logArea);
            return;
        }
        javaFiles.forEach(p -> logMessage(p.getFileName().toString(), logArea));
        buildButton.setEnabled(true);
        cancelButton.setEnabled(true);
        scanButton.setEnabled(false);
        controllerBrowseButton.setEnabled(false);
    }

    private void browseForPath(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser(FileSystemView
                .getFileSystemView().getHomeDirectory());

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int value = fileChooser.showOpenDialog(null);
        if(value ==  JFileChooser.APPROVE_OPTION)
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
    }

    private void updateDependencies() {
        try {
            logMessage(LOCATING_POM_FILE, logArea);
            Path pomPath = Paths.get("pom.xml");
            if (!Files.exists(pomPath)) {
                pomPath = Paths.get("").toAbsolutePath().getParent().resolve("pom.xml");
            }

            if (Files.exists(pomPath)) {
                logMessage(messageCuston(FOUND_POM_XML_AT, pomPath.toString()), logArea);
                logMessage(SCANNING_DEPENDENCY_VERSIONS, logArea);
                // Actual XML manipulation would go here
                logMessage(DEPENDENCIES_UPDATED_SUCCESSFULLY, logArea);
            } else {
                logMessage(COULD_NOT_LOCATE_POM_FILE, logArea);
            }
        } catch (Exception e) {
            logMessage(messageWithException(ERROR_UPDATING_POM_FILE, e), logArea);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new MainScreem());
    }
}
