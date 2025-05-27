package com.jumpie;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

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
            } else if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
                // Сохранение будет обработано в FileManagerFX
                event.consume();
            }
        });
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public void addNewTab() {
        TextArea textArea = createTextArea();
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Tab tab = new Tab("New Document " + (tabPane.getTabs().size() + 1), scrollPane);
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

    public TextArea getCurrentTextArea() {
        Tab currentTab = getCurrentTab();
        if (currentTab != null && currentTab.getContent() instanceof ScrollPane scrollPane) {
            if (scrollPane.getContent() instanceof TextArea textArea) {
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
            TextArea textArea = getCurrentTextArea();
            if (textArea != null) {
                Font currentFont = textArea.getFont();
                double newSize = currentFont.getSize() * (newZoom / currentZoom);
                textArea.setFont(Font.font(
                        currentFont.getFamily(),
                        FontWeight.findByName(currentFont.getStyle()),
                        FontPosture.findByName(currentFont.getStyle()),
                        newSize
                ));
                currentZoom = newZoom;
            }
        }
    }

    public void cut() {
        TextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textArea.getSelectedText());
            clipboard.setContent(content);
            textArea.replaceSelection("");
        }
    }

    public void copy() {
        TextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textArea.getSelectedText());
            clipboard.setContent(content);
        }
    }

    public void paste() {
        TextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                textArea.replaceSelection(clipboard.getString());
            }
        }
    }

    public void print() {
        TextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            // Здесь должна быть реализация печати
            System.out.println("Printing: " + textArea.getText());
        }
    }

    public void changeFontFamily(String fontFamily) {
        TextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Font currentFont = textArea.getFont();
            textArea.setFont(Font.font(
                    fontFamily,
                    FontWeight.findByName(currentFont.getStyle()),
                    FontPosture.findByName(currentFont.getStyle()),
                    currentFont.getSize()
            ));
        }
    }

    public void changeFontSize(int size) {
        TextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Font currentFont = textArea.getFont();
            textArea.setFont(Font.font(
                    currentFont.getFamily(),
                    FontWeight.findByName(currentFont.getStyle()),
                    FontPosture.findByName(currentFont.getStyle()),
                    size
            ));
        }
    }

    public void toggleFontStyle(Object style) {
        TextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            Font currentFont = textArea.getFont();

            FontWeight weight = FontWeight.findByName(currentFont.getStyle());
            FontPosture posture = FontPosture.findByName(currentFont.getStyle());

            if (style instanceof FontWeight) {
                weight = (weight == FontWeight.BOLD) ? FontWeight.NORMAL : FontWeight.BOLD;
            } else if (style instanceof FontPosture) {
                posture = (posture == FontPosture.ITALIC) ? FontPosture.REGULAR : FontPosture.ITALIC;
            }

            textArea.setFont(Font.font(
                    currentFont.getFamily(),
                    weight,
                    posture,
                    currentFont.getSize()
            ));
        }
    }

    private TextArea createTextArea() {
        TextArea textArea = new TextArea();
        textArea.getStyleClass().add("text-area");
        textArea.setWrapText(true);
        textArea.setFont(Font.font("Consolas", 14));
        return textArea;
    }
}