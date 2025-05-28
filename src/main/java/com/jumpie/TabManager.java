package com.jumpie;

import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.scene.control.*;
import javafx.scene.input.*;
import java.util.*;

public class TabManager {
    private final TabPane tabPane;
    private double currentZoom = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_STEP = 0.1;

    public TabManager() {
        this.tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");
        setupKeyShortcuts();
    }

    private void setupKeyShortcuts() {
        tabPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN).match(event)) {
                addNewTab();
                event.consume();
            } else if (new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN).match(event)) {
                closeCurrentTab();
                event.consume();
            }
        });
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public void addNewTab() {
        StyleClassedTextArea textArea = new StyleClassedTextArea();
        textArea.getStyleClass().add("styled-text-area");
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px; -fx-text-fill: white;");

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Tab tab = new Tab("New Document", scrollPane);
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public void closeCurrentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            tabPane.getTabs().remove(selectedTab);
        }
    }

    public Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    public StyleClassedTextArea getCurrentTextArea() {
        Tab currentTab = getCurrentTab();
        if (currentTab != null && currentTab.getContent() instanceof ScrollPane scrollPane) {
            if (scrollPane.getContent() instanceof StyleClassedTextArea textArea) {
                return textArea;
            }
        }
        return null;
    }

    public void updateTabTitle(String title) {
        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            currentTab.setText(title);
        }
    }

    public void zoomIn() {
        setZoom(currentZoom + ZOOM_STEP);
    }

    public void zoomOut() {
        setZoom(currentZoom - ZOOM_STEP);
    }

    public void resetZoom() {
        setZoom(1.0);
    }

    private void setZoom(double newZoom) {
        newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        if (Math.abs(currentZoom - newZoom) > 0.01) {
            StyleClassedTextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                double currentSize = Double.parseDouble(textArea.getStyle().replaceAll(".*-fx-font-size: ([0-9]+)px;.*", "$1"));
                double newSize = currentSize * (newZoom / currentZoom);
                textArea.setStyle(textArea.getStyle().replaceFirst("-fx-font-size: [0-9]+px;", "-fx-font-size: " + newSize + "px;"));
                currentZoom = newZoom;
            }
        }
    }

    public void cut() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textArea.getSelectedText());
            clipboard.setContent(content);
            textArea.replaceSelection("");
        }
    }

    public void copy() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textArea.getSelectedText());
            clipboard.setContent(content);
        }
    }

    public void paste() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                textArea.replaceSelection(clipboard.getString());
            }
        }
    }

    public void print() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            System.out.println("Printing: " + textArea.getText());
        }
    }

    public void changeSelectionFontFamily(String fontFamily) {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            if (textArea.getSelection().getLength() > 0) {
                int start = textArea.getSelection().getStart();
                int end = textArea.getSelection().getEnd();

                // Получаем текущие стили
                Set<String> currentStyles = new HashSet<>();
                if (textArea.getStyleOfChar(start) != null) {
                    currentStyles.addAll((Set<String>) textArea.getStyleOfChar(start));
                }

                // Удаляем старое семейство шрифтов
                Set<String> newStyles = new HashSet<>();
                for (String style : currentStyles) {
                    if (!style.startsWith("font-family:")) {
                        newStyles.add(style);
                    }
                }

                // Добавляем новое
                newStyles.add("font-family:" + fontFamily);

                // Применяем
                textArea.clearStyle(start, end);
                textArea.setStyle(start, end, newStyles);
            } else {
                // Для всего текста, если нет выделения
                String currentStyle = textArea.getStyle();
                textArea.setStyle((currentStyle == null ? "" : currentStyle) +
                        " -fx-font-family: '" + fontFamily + "';");
            }
        }
    }

    public void changeSelectionFontSize(int size) {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            if (textArea.getSelection().getLength() > 0) {
                int start = textArea.getSelection().getStart();
                int end = textArea.getSelection().getEnd();

                // Получаем текущие стили
                Set<String> currentStyles = new HashSet<>();
                if (textArea.getStyleOfChar(start) != null) {
                    currentStyles.addAll(textArea.getStyleOfChar(start));
                }

                // Удаляем старый размер
                Set<String> newStyles = new HashSet<>();
                for (String style : currentStyles) {
                    if (!style.startsWith("size-")) {
                        newStyles.add(style);
                    }
                }

                // Добавляем новый
                newStyles.add("size-" + size);

                // Применяем
                textArea.clearStyle(start, end);
                textArea.setStyle(start, end, newStyles);
            } else {
                // Для всего текста, если нет выделения
                String currentStyle = textArea.getStyle();
                textArea.setStyle((currentStyle == null ? "" : currentStyle) +
                        " -fx-font-size: " + size + "px;");
            }
        }
    }


    public void toggleSelectionBold() {
        applyTextStyle("bold");
    }

    public void toggleSelectionItalic() {
        applyTextStyle("italic");
    }

    public void toggleSelectionUnderline() {
        applyTextStyle("underline");
    }

    public void toggleSelectionStrikethrough() {
        applyTextStyle("strikethrough");
    }

    private void applyTextStyle(String styleType) {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null && textArea.getSelection().getLength() > 0) {
            int start = textArea.getSelection().getStart();
            int end = textArea.getSelection().getEnd();

            // Получаем текущие стили первого символа выделения
            Set<String> currentStyles = new HashSet<>();
            Object styleObject = textArea.getStyleOfChar(start);
            if (styleObject instanceof Collection<?>) {
                @SuppressWarnings("unchecked")
                Collection<String> styles = (Collection<String>) styleObject;
                currentStyles.addAll(styles);
            }

            // Определяем текущее состояние стилей
            boolean isBold = currentStyles.contains("text-bold") ||
                    currentStyles.stream().anyMatch(s -> s.contains("bold"));
            boolean isItalic = currentStyles.contains("text-italic") ||
                    currentStyles.stream().anyMatch(s -> s.contains("italic"));
            boolean isUnderline = currentStyles.contains("text-underline") ||
                    currentStyles.stream().anyMatch(s -> s.contains("underline"));
            boolean isStrikethrough = currentStyles.contains("text-strikethrough") ||
                    currentStyles.stream().anyMatch(s -> s.contains("strikethrough"));

            // Обновляем состояние в зависимости от запрошенного стиля
            switch (styleType) {
                case "bold":
                    isBold = !isBold;
                    break;
                case "italic":
                    isItalic = !isItalic;
                    break;
                case "underline":
                    isUnderline = !isUnderline;
                    break;
                case "strikethrough":
                    isStrikethrough = !isStrikethrough;
                    break;
            }

            // Формируем новый набор стилей
            Set<String> newStyles = new HashSet<>();
            if (isBold) {
                newStyles.add("text-bold");
            }
            if (isItalic) {
                newStyles.add("text-italic");
            }
            if (isUnderline) {
                newStyles.add("text-underline");
            }
            if (isStrikethrough) {
                newStyles.add("text-strikethrough");
            }

            // Применяем стили
            textArea.clearStyle(start, end);
            textArea.setStyle(start, end, newStyles);
        }
    }

}