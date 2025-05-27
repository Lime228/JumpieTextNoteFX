package com.jumpie;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class EditorMenuBar {
    private final MenuBar menuBar;
    private final HBox toolBar;
    private final Button voiceButton;
    private final ComboBox<String> fontCombo;
    private final ComboBox<Integer> sizeCombo;
    private final ToggleButton boldBtn;
    private final ToggleButton italicBtn;

    public EditorMenuBar(EditorMain editorMain, FileManager fileManager, TabManager tabManager, VoiceRecognitionService voiceService) {
        menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        // Создание меню File
        Menu fileMenu = new Menu("File");
        MenuItem newTabItem = new MenuItem("New Tab");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As");
        MenuItem printItem = new MenuItem("Print");
        MenuItem closeTabItem = new MenuItem("Close Tab");
        fileMenu.getItems().addAll(newTabItem, openItem, saveItem, saveAsItem, printItem, closeTabItem);

        // Создание меню Edit
        Menu editMenu = new Menu("Edit");
        MenuItem cutItem = new MenuItem("Cut");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");
        MenuItem zoomInItem = new MenuItem("Zoom In");
        MenuItem zoomOutItem = new MenuItem("Zoom Out");
        MenuItem zoomResetItem = new MenuItem("Reset Zoom");
        editMenu.getItems().addAll(cutItem, copyItem, pasteItem, zoomInItem, zoomOutItem, zoomResetItem);

        menuBar.getMenus().addAll(fileMenu, editMenu);

        // Панель инструментов
        toolBar = new HBox(5);
        toolBar.getStyleClass().add("tool-bar");

        // Кнопка голосового ввода
        voiceButton = new Button("Record");
        voiceButton.setTooltip(new Tooltip("Start/Stop voice input"));

        // Кнопки масштабирования
        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");
        Button zoomResetButton = new Button("100%");

        // Выбор шрифта и размера
        fontCombo = new ComboBox<>();
        fontCombo.getItems().addAll(Font.getFontNames());
        fontCombo.setValue("Consolas");
        fontCombo.setTooltip(new Tooltip("Select font"));

        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll(8, 10, 12, 14, 16, 18, 20, 24);
        sizeCombo.setValue(14);
        sizeCombo.setTooltip(new Tooltip("Select font size"));

        // Кнопки стилей
        boldBtn = new ToggleButton("B");
        italicBtn = new ToggleButton("I");

        // Добавляем элементы на панель инструментов
        toolBar.getChildren().addAll(
                voiceButton,
                new Separator(),
                new Label("Font:"), fontCombo,
                new Separator(),
                new Label("Size:"), sizeCombo,
                new Separator(),
                boldBtn, italicBtn,
                new Separator(),
                zoomInButton, zoomOutButton, zoomResetButton
        );

        // Добавление обработчиков событий
        newTabItem.setOnAction(e -> tabManager.addNewTab());
        openItem.setOnAction(e -> fileManager.openFile());
        saveItem.setOnAction(e -> fileManager.saveFile(false));
        saveAsItem.setOnAction(e -> fileManager.saveFile(true));
        closeTabItem.setOnAction(e -> tabManager.closeCurrentTab());
        cutItem.setOnAction(e -> tabManager.cut());
        copyItem.setOnAction(e -> tabManager.copy());
        pasteItem.setOnAction(e -> tabManager.paste());
        printItem.setOnAction(e -> tabManager.print());
        zoomInItem.setOnAction(e -> tabManager.zoomIn());
        zoomOutItem.setOnAction(e -> tabManager.zoomOut());
        zoomResetItem.setOnAction(e -> tabManager.resetZoom());

        zoomInButton.setOnAction(e -> tabManager.zoomIn());
        zoomOutButton.setOnAction(e -> tabManager.zoomOut());
        zoomResetButton.setOnAction(e -> tabManager.resetZoom());

        voiceButton.setOnAction(e -> voiceService.toggleRecognition(editorMain));

        fontCombo.setOnAction(e -> tabManager.changeFontFamily(fontCombo.getValue()));
        sizeCombo.setOnAction(e -> tabManager.changeFontSize(sizeCombo.getValue()));
        boldBtn.setOnAction(e -> tabManager.toggleFontStyle(FontWeight.BOLD));
        italicBtn.setOnAction(e -> tabManager.toggleFontStyle(FontPosture.ITALIC));
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public HBox getToolBar() {
        return toolBar;
    }

    public Button getVoiceButton() {
        return voiceButton;
    }

    public void updateVoiceButtonState(boolean isListening) {
        voiceButton.setText(isListening ? "Stop" : "Record");
        voiceButton.setTooltip(new Tooltip(isListening ? "Stop voice input" : "Start voice input"));
    }
}