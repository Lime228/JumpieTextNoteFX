package com.jumpie;

import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.scene.control.*;
import javafx.scene.input.*;
import java.util.*;

public class TabManager {
    private final TabPane tabPane;

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
        textArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px;");

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

                Set<String> currentStyles = new HashSet<>();
                if (textArea.getStyleOfChar(start) != null) {
                    currentStyles.addAll((Set<String>) textArea.getStyleOfChar(start));
                }

                Set<String> newStyles = new HashSet<>();
                for (String style : currentStyles) {
                    if (!style.startsWith("font-family:")) {
                        newStyles.add(style);
                    }
                }

                newStyles.add("font-family:" + fontFamily);

                textArea.clearStyle(start, end);
                textArea.setStyle(start, end, newStyles);
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

                Set<String> currentStyles = new HashSet<>();
                if (textArea.getStyleOfChar(start) != null) {
                    currentStyles.addAll(textArea.getStyleOfChar(start));
                }

                Set<String> newStyles = new HashSet<>();
                for (String style : currentStyles) {
                    if (!style.startsWith("size-")) {
                        newStyles.add(style);
                    }
                }

                newStyles.add("size-" + size);

                textArea.clearStyle(start, end);
                textArea.setStyle(start, end, newStyles);
            } else {
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

            Set<String> currentStyles = new HashSet<>();
            Object styleObject = textArea.getStyleOfChar(start);
            if (styleObject instanceof Collection<?>) {
                @SuppressWarnings("unchecked")
                Collection<String> styles = (Collection<String>) styleObject;
                currentStyles.addAll(styles);
            }

            boolean isBold = currentStyles.contains("text-bold") ||
                    currentStyles.stream().anyMatch(s -> s.contains("bold"));
            boolean isItalic = currentStyles.contains("text-italic") ||
                    currentStyles.stream().anyMatch(s -> s.contains("italic"));
            boolean isUnderline = currentStyles.contains("text-underline") ||
                    currentStyles.stream().anyMatch(s -> s.contains("underline"));
            boolean isStrikethrough = currentStyles.contains("text-strikethrough") ||
                    currentStyles.stream().anyMatch(s -> s.contains("strikethrough"));

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

    public StyledDocument createStyledDocument() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        if (textArea == null) return new StyledDocument();

        String text = textArea.getText();
        List<TextStyle> styles = getCurrentTextStyles();
        return new StyledDocument(text, styles);
    }

    public List<TextStyle> getCurrentTextStyles() {
        StyleClassedTextArea textArea = getCurrentTextArea();
        List<TextStyle> styles = new ArrayList<>();

        if (textArea != null && textArea.getLength() > 0) {
            String fullText = textArea.getText();
            int length = fullText.length();

            if (length == 0) return styles;

            int start = 0;
            Set<String> currentStyles = getEffectiveStyles(textArea, 0);

            for (int i = 1; i < length; i++) {
                Set<String> newStyles = getEffectiveStyles(textArea, i);

                if (!stylesEqual(currentStyles, newStyles)) {
                    styles.add(createTextStyle(start, i-1, currentStyles));
                    start = i;
                    currentStyles = newStyles;
                }
            }

            styles.add(createTextStyle(start, length-1, currentStyles));
        }

        return styles;
    }

    private Set<String> getEffectiveStyles(StyleClassedTextArea textArea, int position) {
        Set<String> styles = new HashSet<>();

        Object styleObj = textArea.getStyleOfChar(position);
        if (styleObj instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<String> charStyles = (Set<String>) styleObj;
            styles.addAll(charStyles);
        }

        return styles;
    }

    private TextStyle createTextStyle(int start, int end, Set<String> styles) {
        String fontFamily = "Consolas";
        int fontSize = 14;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;

        for (String style : styles) {
            if (style.startsWith("font-family:")) {
                fontFamily = style.substring("font-family:".length());
            } else if (style.startsWith("size-")) {
                fontSize = Integer.parseInt(style.substring("size-".length()));
            } else if (style.equals("text-bold")) {
                bold = true;
            } else if (style.equals("text-italic")) {
                italic = true;
            } else if (style.equals("text-underline")) {
                underline = true;
            } else if (style.equals("text-strikethrough")) {
                strikethrough = true;
            }
        }

        return new TextStyle(start, end, fontFamily, fontSize, bold, italic, underline, strikethrough);
    }

    private boolean stylesEqual(Set<String> s1, Set<String> s2) {
        if (s1 == s2) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }

}