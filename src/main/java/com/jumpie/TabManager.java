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
                textArea.setStyle(start, end, Collections.singleton("font-family:" + fontFamily));
            } else {
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
                textArea.setStyle(start, end, Collections.singleton("size-" + size));
            } else {
                String currentStyle = textArea.getStyle();
                textArea.setStyle((currentStyle == null ? "" : currentStyle) +
                        " -fx-font-size: " + size + "px;");
            }
        }
    }

    public void toggleSelectionBold() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            if (textArea.getSelection().getLength() > 0) {
                int start = textArea.getSelection().getStart();
                int end = textArea.getSelection().getEnd();

                Set<String> styles = new HashSet<>();
                if (textArea.getStyleOfChar(start) != null) {
                    styles.addAll((Set<String>) textArea.getStyleOfChar(start));
                }

                boolean isBold = styles.contains("text-bold") || styles.contains("text-bold-italic");
                boolean isItalic = styles.contains("text-italic");

                textArea.clearStyle(start, end);

                Set<String> newStyles = new HashSet<>();
                // Сохраняем размер шрифта
                for (String style : styles) {
                    if (style.startsWith("size-") || style.startsWith("font-family:")) {
                        newStyles.add(style);
                    }
                }

                if (!isBold && isItalic) {
                    newStyles.add("text-bold-italic");
                } else if (!isBold) {
                    newStyles.add("text-bold");
                } else if (isItalic) {
                    newStyles.add("text-italic");
                }

                textArea.setStyle(start, end, newStyles);
            } else {
                String currentStyle = textArea.getStyle();
                boolean isBold = currentStyle != null && currentStyle.contains("-fx-font-weight: bold");

                if (isBold) {
                    textArea.setStyle(currentStyle.replace("-fx-font-weight: bold;", ""));
                } else {
                    textArea.setStyle((currentStyle == null ? "" : currentStyle) + " -fx-font-weight: bold;");
                }
            }
        }
    }

    public void toggleSelectionItalic() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            if (textArea.getSelection().getLength() > 0) {
                int start = textArea.getSelection().getStart();
                int end = textArea.getSelection().getEnd();

                Set<String> styles = new HashSet<>();
                if (textArea.getStyleOfChar(start) != null) {
                    styles.addAll((Set<String>) textArea.getStyleOfChar(start));
                }

                boolean isItalic = styles.contains("text-italic") || styles.contains("text-bold-italic");
                boolean isBold = styles.contains("text-bold");

                textArea.clearStyle(start, end);

                Set<String> newStyles = new HashSet<>();
                // Сохраняем размер шрифта
                for (String style : styles) {
                    if (style.startsWith("size-") || style.startsWith("font-family:")) {
                        newStyles.add(style);
                    }
                }

                if (!isItalic && isBold) {
                    newStyles.add("text-bold-italic");
                } else if (!isItalic) {
                    newStyles.add("text-italic");
                } else if (isBold) {
                    newStyles.add("text-bold");
                }

                textArea.setStyle(start, end, newStyles);
            } else {
                String currentStyle = textArea.getStyle();
                boolean isItalic = currentStyle != null && currentStyle.contains("-fx-font-style: italic");

                if (isItalic) {
                    textArea.setStyle(currentStyle.replace("-fx-font-style: italic;", ""));
                } else {
                    textArea.setStyle((currentStyle == null ? "" : currentStyle) + " -fx-font-style: italic;");
                }
            }
        }
    }
}