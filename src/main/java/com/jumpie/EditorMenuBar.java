package com.jumpie;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.Set;

public class EditorMenuBar {
    private final MenuBar menuBar;
    private final HBox toolBar;
    private final Button voiceButton;
    private final ComboBox<String> fontCombo;
    private final ComboBox<Integer> sizeCombo;
    private final ToggleButton boldBtn;
    private final ToggleButton italicBtn;
    private final ToggleButton underlineBtn;
    private final ToggleButton strikethroughBtn;

    public EditorMenuBar(EditorMain editorMain, FileManager fileManager, TabManager tabManager,
                         VoiceRecognitionService voiceService) {
        menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        // Создание меню File
        Menu fileMenu = createMenu("File", "New Tab", "Open", "Save", "Save As", "Print", "Close Tab");
        // Создание меню Edit
        Menu editMenu = createMenu("Edit", "Cut", "Copy", "Paste");

        menuBar.getMenus().addAll(fileMenu, editMenu);

        // Панель инструментов
        toolBar = new HBox(5);
        toolBar.getStyleClass().add("tool-bar");
        toolBar.setFillHeight(true);
        voiceService.setOnStateChangeListener(() -> updateVoiceButtonState(voiceService.isListening()));
        voiceButton = createRecordButton();
        voiceButton.setOnAction(e -> {
            voiceService.toggleRecognition(editorMain);
            updateVoiceButtonState(voiceService.isListening());
        });

        // Настройка выпадающих списков
        fontCombo = createFontComboBox();
        sizeCombo = createSizeComboBox();

        boldBtn = createStyleToggleButton(FontAwesomeIcon.BOLD, "button-bold");
        italicBtn = createStyleToggleButton(FontAwesomeIcon.ITALIC, "button-italic");
        underlineBtn = createStyleToggleButton(FontAwesomeIcon.UNDERLINE, "button-underline");
        strikethroughBtn = createStyleToggleButton(FontAwesomeIcon.STRIKETHROUGH, "button-strikethrough");

        toolBar.getChildren().addAll(
                voiceButton,
                createSeparator(),
                createLabel("Font:"), fontCombo,
                createSeparator(),
                createLabel("Size:"), sizeCombo,
                createSeparator(),
                boldBtn, italicBtn, underlineBtn, strikethroughBtn,
                createSeparator()
        );

        // Настройка обработчиков событий
        setupEventHandlers(editorMain, fileManager, tabManager, voiceService);
    }

    private Menu createMenu(String title, String... items) {
        Menu menu = new Menu(title);
        for (String item : items) {
            MenuItem menuItem = new MenuItem(item);
            menu.getItems().add(menuItem);
        }
        return menu;
    }

    private Button createToolButton() {
        Button button = new Button("Record");
        button.setTooltip(new Tooltip("Start/Stop voice input"));
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private ComboBox<String> createFontComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(Font.getFamilies());
        combo.setValue("Consolas");
        combo.setTooltip(new Tooltip("Select font"));
        combo.setMinWidth(120);
        combo.setMaxWidth(120);
        return combo;
    }

    private ComboBox<Integer> createSizeComboBox() {
        ComboBox<Integer> combo = new ComboBox<>();
        for (int i = 8; i <= 40; i += 2) {
            combo.getItems().add(i);
        }
        combo.setValue(14);
        combo.setTooltip(new Tooltip("Select font size"));
        combo.setMinWidth(60);
        combo.setMaxWidth(60);
        return combo;
    }


//    private ToggleButton createStyleToggleButton(String text, String styleClass) {
//        ToggleButton button = new ToggleButton();
//        button.getStyleClass().add(styleClass);
//        button.setMinWidth(30);
//        button.setMaxWidth(30);
//        return button;
//    }

    private ToggleButton createStyleToggleButton(FontAwesomeIcon icon, String styleClass) {
        ToggleButton button = new ToggleButton();
        button.setGraphic(new FontAwesomeIconView(icon));
        button.getStyleClass().add(styleClass);
        return button;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-padding: 0 5 0 0;");
        return label;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        return separator;
    }

    private void setupEventHandlers(EditorMain editorMain, FileManager fileManager,
                                    TabManager tabManager, VoiceRecognitionService voiceService) {
        // Обработчики для меню File
        menuBar.getMenus().getFirst().getItems().get(0).setOnAction(e -> tabManager.addNewTab());
        menuBar.getMenus().getFirst().getItems().get(1).setOnAction(e -> fileManager.openFile());
        menuBar.getMenus().getFirst().getItems().get(2).setOnAction(e -> fileManager.saveFile(false));
        menuBar.getMenus().getFirst().getItems().get(3).setOnAction(e -> fileManager.saveFile(true));
        menuBar.getMenus().get(0).getItems().get(4).setOnAction(e -> tabManager.print());
        menuBar.getMenus().get(0).getItems().get(5).setOnAction(e -> tabManager.closeCurrentTab());

        // Обработчики для меню Edit
        menuBar.getMenus().get(1).getItems().get(0).setOnAction(e -> tabManager.cut());
        menuBar.getMenus().get(1).getItems().get(1).setOnAction(e -> tabManager.copy());
        menuBar.getMenus().get(1).getItems().get(2).setOnAction(e -> tabManager.paste());

        voiceButton.setOnAction(e -> voiceService.toggleRecognition(editorMain));

        fontCombo.setOnAction(e -> {
            String selectedFont = fontCombo.getSelectionModel().getSelectedItem();
            if (selectedFont != null) {
                tabManager.changeSelectionFontFamily(selectedFont);
                updateStyleButtons(tabManager);
            }
        });

        sizeCombo.setOnAction(e -> {
            Integer selectedSize = sizeCombo.getSelectionModel().getSelectedItem();
            if (selectedSize != null) {
                tabManager.changeSelectionFontSize(selectedSize);
                updateStyleButtons(tabManager);
            }
        });

        boldBtn.setOnAction(e -> {
            tabManager.toggleSelectionBold();
            updateStyleButtons(tabManager);
        });

        italicBtn.setOnAction(e -> {
            tabManager.toggleSelectionItalic();
            updateStyleButtons(tabManager);
        });

        underlineBtn.setOnAction(e -> {
            tabManager.toggleSelectionUnderline();
            updateStyleButtons(tabManager);
        });

        strikethroughBtn.setOnAction(e -> {
            tabManager.toggleSelectionStrikethrough();
            updateStyleButtons(tabManager);
        });
    }

    private void updateStyleButtons(TabManager tabManager) {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea != null && textArea.getSelection().getLength() > 0) {
            int pos = textArea.getSelection().getStart();
            Set<String> styles = (Set<String>) textArea.getStyleOfChar(pos);

            boolean isBold = styles.stream().anyMatch(s -> s.contains("bold"));
            boolean isItalic = styles.stream().anyMatch(s -> s.contains("italic"));
            boolean isUnderline = styles.stream().anyMatch(s -> s.contains("underline"));
            boolean isStrikethrough = styles.stream().anyMatch(s -> s.contains("strikethrough"));

            boldBtn.setSelected(isBold);
            italicBtn.setSelected(isItalic);
            underlineBtn.setSelected(isUnderline);
            strikethroughBtn.setSelected(isStrikethrough);
        } else {
            boldBtn.setSelected(false);
            italicBtn.setSelected(false);
            underlineBtn.setSelected(false);
            strikethroughBtn.setSelected(false);
        }
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public HBox getToolBar() {
        return toolBar;
    }


    private Button createRecordButton() {
        Button button = createToolButton();
        button.getStyleClass().add("record-button");
        return button;
    }

    public void updateVoiceButtonState(boolean isListening) {
        javafx.application.Platform.runLater(() -> {
            if (isListening) {
                voiceButton.setText("Stop");
                voiceButton.getStyleClass().remove("record-button");
                voiceButton.getStyleClass().add("recording-button");
                voiceButton.setTooltip(new Tooltip("Stop voice input"));
            } else {
                voiceButton.setText("Record");
                voiceButton.getStyleClass().remove("recording-button");
                voiceButton.getStyleClass().add("record-button");
                voiceButton.setTooltip(new Tooltip("Start voice input"));
            }
        });
    }
}