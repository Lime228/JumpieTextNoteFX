package com.jumpie;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ExportManager {
    private final Stage parentStage;
    private final TabManager tabManager;

    public ExportManager(Stage parentStage, TabManager tabManager) {
        this.parentStage = parentStage;
        this.tabManager = tabManager;
    }

    public void exportToHTML() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null || textArea.getText().isEmpty()) {
            showError("Нет текста для экспорта");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт в HTML");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML Files", "*.html")
        );
        fileChooser.setInitialFileName("export.html");

        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try {
                String html = convertToHTML(textArea);
                writeToFile(file, html);
                showSuccess("Экспорт в HTML завершен успешно");
            } catch (IOException e) {
                showError("Ошибка при экспорте в HTML: " + e.getMessage());
            }
        }
    }

    public void exportToMarkdown() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null || textArea.getText().isEmpty()) {
            showError("Нет текста для экспорта");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт в Markdown");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Markdown Files", "*.md")
        );
        fileChooser.setInitialFileName("export.md");

        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try {
                String markdown = convertToMarkdown(textArea);
                writeToFile(file, markdown);
                showSuccess("Экспорт в Markdown завершен успешно");
            } catch (IOException e) {
                showError("Ошибка при экспорте в Markdown: " + e.getMessage());
            }
        }
    }

    private String convertToHTML(StyleClassedTextArea textArea) {
        StringBuilder html = new StringBuilder();

        // HTML header
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ru\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Экспортированный документ</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Consolas', monospace; font-size: 14px; line-height: 1.6; padding: 20px; }\n");
        html.append("        .line { white-space: pre-wrap; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        String text = textArea.getText();
        String[] lines = text.split("\n", -1);

        // Обрабатываем каждую строку
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            html.append("    <div class=\"line\">");

            // Получаем позицию начала строки в общем тексте
            int lineStart = getLineStartPosition(text, lineIndex);

            if (!line.isEmpty()) {
                // Обрабатываем каждый символ в строке со стилями
                for (int i = 0; i < line.length(); i++) {
                    int position = lineStart + i;
                    char c = line.charAt(i);

                    @SuppressWarnings("unchecked")
                    Set<String> styles = (Set<String>) textArea.getStyleOfChar(position);

                    String styledChar = applyHTMLStyles(String.valueOf(c), styles);
                    html.append(styledChar);
                }
            } else {
                html.append("<br>");
            }

            html.append("</div>\n");
        }

        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private String applyHTMLStyles(String text, Set<String> styles) {
        if (styles == null || styles.isEmpty()) {
            return escapeHTML(text);
        }

        StringBuilder result = new StringBuilder();
        String escapedText = escapeHTML(text);

        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        String fontFamily = null;
        Integer fontSize = null;
        String textColor = null;
        String bgColor = null;

        for (String style : styles) {
            if (style.equals("text-bold")) bold = true;
            else if (style.equals("text-italic")) italic = true;
            else if (style.equals("text-underline")) underline = true;
            else if (style.equals("text-strikethrough")) strikethrough = true;
            else if (style.startsWith("font-family:")) fontFamily = style.substring("font-family:".length());
            else if (style.startsWith("size-")) fontSize = Integer.parseInt(style.substring("size-".length()));
            else if (style.startsWith("text-color:")) textColor = style.substring("text-color:".length());
            else if (style.startsWith("bg-color:")) bgColor = style.substring("bg-color:".length());
        }

        // Применяем стили
        StringBuilder styleAttr = new StringBuilder();
        if (fontFamily != null) styleAttr.append("font-family: '").append(fontFamily).append("'; ");
        if (fontSize != null) styleAttr.append("font-size: ").append(fontSize).append("px; ");
        if (textColor != null) styleAttr.append("color: ").append(textColor).append("; ");
        if (bgColor != null) styleAttr.append("background-color: ").append(bgColor).append("; ");

        if (styleAttr.length() > 0) {
            result.append("<span style=\"").append(styleAttr).append("\">");
        }

        if (bold) result.append("<strong>");
        if (italic) result.append("<em>");
        if (underline) result.append("<u>");
        if (strikethrough) result.append("<s>");

        result.append(escapedText);

        if (strikethrough) result.append("</s>");
        if (underline) result.append("</u>");
        if (italic) result.append("</em>");
        if (bold) result.append("</strong>");

        if (styleAttr.length() > 0) {
            result.append("</span>");
        }

        return result.toString();
    }

    private String convertToMarkdown(StyleClassedTextArea textArea) {
        StringBuilder markdown = new StringBuilder();
        String text = textArea.getText();
        String[] lines = text.split("\n", -1);

        // Обрабатываем каждую строку
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];

            int lineStart = getLineStartPosition(text, lineIndex);

            if (!line.isEmpty()) {
                // Обрабатываем каждый символ в строке со стилями
                for (int i = 0; i < line.length(); i++) {
                    int position = lineStart + i;
                    char c = line.charAt(i);

                    @SuppressWarnings("unchecked")
                    Set<String> styles = (Set<String>) textArea.getStyleOfChar(position);

                    String styledChar = applyMarkdownStyles(String.valueOf(c), styles);
                    markdown.append(styledChar);
                }
            }

            markdown.append("\n");
        }

        return markdown.toString();
    }

    private String applyMarkdownStyles(String text, Set<String> styles) {
        if (styles == null || styles.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;

        for (String style : styles) {
            if (style.equals("text-bold")) bold = true;
            else if (style.equals("text-italic")) italic = true;
            else if (style.equals("text-strikethrough")) strikethrough = true;
        }

        // Применяем стили Markdown
        if (bold) result.append("**");
        if (italic) result.append("*");
        if (strikethrough) result.append("~~");

        result.append(text);

        if (strikethrough) result.append("~~");
        if (italic) result.append("*");
        if (bold) result.append("**");

        return result.toString();
    }

    private int getLineStartPosition(String text, int lineIndex) {
        int position = 0;
        for (int i = 0; i < lineIndex; i++) {
            position = text.indexOf('\n', position) + 1;
        }
        return position;
    }

    private String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void writeToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.initOwner(parentStage);
        alert.setTitle("Ошибка экспорта");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.initOwner(parentStage);
        alert.setTitle("Экспорт");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
