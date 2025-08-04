package com.dssid.dev.view.components;

import javax.swing.*;

public class LogMessage {

    static JTextArea logArea;

    public static void logMessage(String message, JTextArea jTextArea) {
        logArea = jTextArea;
        if(logArea == null) return;
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void cleanLog(JTextArea jTextLog) {
        logArea = jTextLog;
        logArea.removeAll();
    }


}
