package com.jumpie;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class FileManager {
    private final TabManager tabManager;
    private final Stage parentStage;

    public FileManager(Stage stage, TabManager tabManager) {
        this.parentStage = stage;
        this.tabManager = tabManager;
    }

    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Jumpie's Piece of Paper", "*.jpop"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(parentStage);
        if (file == null) return;

        tabManager.addNewTab();
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) return;

        if (file.getName().toLowerCase().endsWith(".jpop")) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                StyledDocument doc = (StyledDocument) ois.readObject();
                textArea.replaceText(doc.getText());

                for (TextStyle style : doc.getStyles()) {
                    applyStyleToTextArea(textArea, style);
                }

                tabManager.getCurrentTab().setUserData(file);
                tabManager.updateTabTitle(file.getName());
            } catch (Exception ex) {
                showError("File Error", "Ошибка при открытии файла: " + ex.getMessage());
            }
        } else {
            // Handle plain text files
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                textArea.replaceText(content.toString());
                tabManager.getCurrentTab().setUserData(file);
                tabManager.updateTabTitle(file.getName());
            } catch (Exception ex) {
                showError("File Error", "Ошибка при открытии текстового файла: " + ex.getMessage());
            }
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
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Jumpie's Piece of Paper", "*.jpop"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        if (currentFile != null) {
            fileChooser.setInitialFileName(currentFile.getName());
        } else {
            fileChooser.setInitialFileName("Записка.jpop");
        }

        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            // Don't force .jpop extension if user selected .txt
            if (file.getName().toLowerCase().endsWith(".jpop") ||
                    !file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".jpop");
            }
            writeFile(textArea, file);
            tabManager.getCurrentTab().setUserData(file);
            tabManager.updateTabTitle(file.getName());
        }
    }

    private void writeFile(StyleClassedTextArea textArea, File file) {
        if (file.getName().toLowerCase().endsWith(".jpop")) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                StyledDocument doc = tabManager.createStyledDocument();
                doc.optimizeStyles();
                oos.writeObject(doc);
            } catch (IOException ex) {
                showError("File Error", "Ошибка при сохранении файла: " + ex.getMessage());
            }
        } else {
            // Save as plain text
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(textArea.getText());
            } catch (IOException ex) {
                showError("File Error", "Ошибка при сохранении текстового файла: " + ex.getMessage());
            }
        }
    }

    private void applyStyleToTextArea(StyleClassedTextArea textArea, TextStyle style) {
        Set<String> styles = new HashSet<>();

        if (style.isBold()) styles.add("text-bold");
        if (style.isItalic()) styles.add("text-italic");
        if (style.isUnderline()) styles.add("text-underline");
        if (style.isStrikethrough()) styles.add("text-strikethrough");

        styles.add("font-family:" + style.getFontFamily());
        styles.add("size-" + style.getFontSize());

        textArea.setStyle(style.getStart(), style.getEnd() + 1, styles);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void savePreferences(Theme currentTheme) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("preferences.jumpie"))) {
            oos.writeObject(currentTheme);
        } catch (IOException ex) {
            System.err.println("Не удалось сохранить настройки: " + ex.getMessage());
        }
    }

    public Theme loadPreferences() {
        File prefsFile = new File("preferences.jumpie");
        if (prefsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(prefsFile))) {
                return (Theme) ois.readObject();
            } catch (Exception ex) {
                System.err.println("Не удалось загрузить настройки: " + ex.getMessage());
            }
        }
        return Theme.LIGHT; // Тема по умолчанию
    }
}