package com.jumpie;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.fxmisc.richtext.StyleClassedTextArea;

public class WordCountPanel {
    private final HBox statusBar;
    private final Label charCountLabel;
    private final Label charNoSpaceCountLabel;
    private final Label wordCountLabel;
    private final Label lineCountLabel;
    private final Label selectionLabel;
    private final Label autoSaveLabel; // НОВОЕ

    public WordCountPanel() {
        statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);

        // НОВОЕ: Индикатор автосохранения слева
        autoSaveLabel = createStatusLabel("Автосохранение: включено");
        autoSaveLabel.getStyleClass().add("autosave-label");

        // Spacer для выравнивания элементов вправо
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Labels для статистики
        lineCountLabel = createStatusLabel("Строк: 0");
        wordCountLabel = createStatusLabel("Слов: 0");
        charCountLabel = createStatusLabel("Символов: 0");
        charNoSpaceCountLabel = createStatusLabel("Без пробелов: 0");
        selectionLabel = createStatusLabel("");

        // Разделители
        Separator sepAuto = createVerticalSeparator();
        Separator sep1 = createVerticalSeparator();
        Separator sep2 = createVerticalSeparator();
        Separator sep3 = createVerticalSeparator();
        Separator sep4 = createVerticalSeparator();

        statusBar.getChildren().addAll(
                autoSaveLabel, sepAuto, // НОВОЕ
                spacer,
                lineCountLabel, sep1,
                wordCountLabel, sep2,
                charCountLabel, sep3,
                charNoSpaceCountLabel, sep4,
                selectionLabel
        );
    }

    private Label createStatusLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("status-label");
        return label;
    }

    private Separator createVerticalSeparator() {
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        return separator;
    }

    public HBox getStatusBar() {
        return statusBar;
    }

    // НОВОЕ: Обновление статуса автосохранения
    public void updateAutoSaveStatus(String status) {
        autoSaveLabel.setText(status);
    }

    public void updateStats(StyleClassedTextArea textArea) {
        if (textArea == null) {
            resetStats();
            return;
        }

        String text = textArea.getText();
        String selectedText = textArea.getSelectedText();

        // Подсчет для всего текста
        int lines = countLines(text);
        int words = countWords(text);
        int chars = text.length();
        int charsNoSpace = countCharsWithoutSpaces(text);

        lineCountLabel.setText("Строк: " + lines);
        wordCountLabel.setText("Слов: " + words);
        charCountLabel.setText("Символов: " + chars);
        charNoSpaceCountLabel.setText("Без пробелов: " + charsNoSpace);

        // Подсчет для выделения
        if (selectedText != null && !selectedText.isEmpty()) {
            int selWords = countWords(selectedText);
            int selChars = selectedText.length();
            selectionLabel.setText(String.format("Выделено: %d слов, %d символов", selWords, selChars));
        } else {
            selectionLabel.setText("");
        }
    }

    private void resetStats() {
        lineCountLabel.setText("Строк: 0");
        wordCountLabel.setText("Слов: 0");
        charCountLabel.setText("Символов: 0");
        charNoSpaceCountLabel.setText("Без пробелов: 0");
        selectionLabel.setText("");
    }

    private int countLines(String text) {
        if (text.isEmpty()) return 0;
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private int countWords(String text) {
        if (text.trim().isEmpty()) return 0;
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    private int countCharsWithoutSpaces(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                count++;
            }
        }
        return count;
    }
}
