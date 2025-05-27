package com.jumpie;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.*;

public class FileManager {
    private final TabManager tabManager;
    private final Stage parentStage;

    public FileManager(Stage stage, TabManager tabManager) {
        this.parentStage = stage;
        this.tabManager = tabManager;
    }

    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(parentStage);
        if (file == null) return;

        tabManager.addNewTab();
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textArea.replaceText(content.toString());
            tabManager.getCurrentTab().setUserData(file);
            tabManager.updateTabTitle(file.getName());
        } catch (IOException ex) {
            showError("File Error", ex.getMessage());
        }
    }

    public void saveFile(boolean saveAs) {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) return;

        File currentFile = (File) tabManager.getCurrentTab().getUserData();
        if (!saveAs && currentFile != null) {
            writeFile(textArea, currentFile);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            writeFile(textArea, file);
            tabManager.getCurrentTab().setUserData(file);
            tabManager.updateTabTitle(file.getName());
        }
    }

    private void writeFile(StyleClassedTextArea textArea, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
        } catch (IOException ex) {
            showError("File Error", ex.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}