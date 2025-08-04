package com.dssid.dev.view.components;

import com.dssid.dev.outros.GeneratorInterface;

import javax.swing.*;
import java.util.Set;

public class Panel {
    private static final String CONTROLLER_PATH = "controllers";
    private static final String RESOURCES_PATH = "resources";
    private JPanel inputPanel;
    private JTextArea logArea;
    private JTextField pathToControllersField;
    private JTextField pathToResourcesField;
    private JButton scanButton;
    private JButton buildButton;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private boolean processCancelled = false;

    private Set<Class<?>> controllersClasses;

    private GeneratorInterface swaggerDocumentation;
/*
    public Panel(JTextField pathToControllersField, JTextField pathToResourcesField, JButton scanButton, GeneratorInterface swaggerDocumentation, JTextArea logArea) {
        this.inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));;
        this.pathToControllersField = pathToControllersField;
        this.pathToResourcesField = pathToResourcesField;
        this.scanButton = scanButton;
        this.swaggerDocumentation = swaggerDocumentation;
        this.logArea = logArea;
    }

    public JPanel openPanel() {

        inputPanel.add(new Label("Insert the path of the controller (Optional):"));
        pathToControllersField = new JTextField();
        JButton controllerBrowseButton = new JButton("Browse...");
        controllerBrowseButton.addActionListener(e-> browseForPath(pathToControllersField));

        JPanel controllersPanel = new JPanel(new BorderLayout(5, 5));
        controllersPanel.add(pathToControllersField, BorderLayout.CENTER);
        controllersPanel.add(controllerBrowseButton, BorderLayout.EAST);
        inputPanel.add(controllersPanel);

        inputPanel.add(new JLabel("Insert the path resources (optional):"));
        pathToResourcesField = new JTextField();
        JButton resourcesBrowseButton = new JButton("Browse...");
        resourcesBrowseButton.addActionListener(e -> browseForPath(pathToResourcesField));

        JPanel resourcesPanel = new JPanel(new BorderLayout(5, 5));
        resourcesPanel.add(pathToResourcesField, BorderLayout.CENTER);
        resourcesPanel.add(resourcesBrowseButton, BorderLayout.EAST);
        inputPanel.add(resourcesPanel);

        scanButton = new JButton("Scan Project");
        scanButton.addActionListener(e -> scanProject());
        inputPanel.add(scanButton);

        return inputPanel;
    }

    private void scanProject() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                logMessage("Starting project scan ...", logArea);
                controllersClasses.clear();
                var packageInformed = pathToControllersField.getText().trim()
                        .replace(";", "").toLowerCase();

                if(!isNotBlank(packageInformed))
                    packageInformed = searchForControllers();

                controllersClasses = checkPakageController(packageInformed);

                if(controllersClasses.isEmpty()) logMessage("No controller found at specified or default path");

                return null;
            }

            @Override
            public void done() {
                if(controllersClasses.isEmpty()) {
                    logMessage("Scan completed. No Controllers found", logArea);
                    buildButton.setEnabled(false);
                } else {
                    logMessage("Scan completed. Found " + controllersClasses.size() + " controllers", logArea);
                    buildButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void browseForPath(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser(FileSystemView
                .getFileSystemView().getHomeDirectory());

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int value = fileChooser.showOpenDialog(null);
        if(value ==  JFileChooser.APPROVE_OPTION)
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
    }
    */
}
