package edu.curtin.saed.assignment1;

import javafx.scene.control.TextArea;
import javafx.application.Platform;

public class Logger {
    private TextArea loggerTextArea;

    public Logger(TextArea loggerTextArea) {
        this.loggerTextArea = loggerTextArea;
    }

    public void log(String message) {
        Platform.runLater(() -> loggerTextArea.appendText(message + "\n"));
    }
}