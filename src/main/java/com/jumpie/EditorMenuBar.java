package com.jumpie;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.util.List;
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
    private FindReplaceDialog findReplaceDialog;

    public EditorMenuBar(EditorMain editorMain, FileManager fileManager, TabManager tabManager,
                         VoiceRecognitionService voiceService) {
        menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        // Создание меню
        Menu fileMenu = createFileMenu(fileManager, tabManager);
        Menu editMenu = createEditMenu(editorMain, tabManager);
        Menu exportMenu = createExportMenu(editorMain);
        Menu autoSaveMenu = createAutoSaveMenu(editorMain);
        Menu themeMenu = createThemeMenu();

        menuBar.getMenus().addAll(fileMenu, editMenu, exportMenu, autoSaveMenu, themeMenu);

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
                createLabel("Шрифт:"), fontCombo,
                createLabel("Размер:"), sizeCombo,
                boldBtn, italicBtn, underlineBtn, strikethroughBtn,
                createSeparator()
        );

        // Настройка обработчиков событий
        setupEventHandlers(editorMain, fileManager, tabManager, voiceService);
    }

    private Menu createFileMenu(FileManager fileManager, TabManager tabManager) {
        Menu menu = new Menu("Файл");

        MenuItem newTab = new MenuItem("Новая вкладка");
        MenuItem open = new MenuItem("Открыть");
        MenuItem save = new MenuItem("Сохранить");
        MenuItem saveAs = new MenuItem("Сохранить как");
        MenuItem closeTab = new MenuItem("Закрыть вкладку");

        // Обработчики
        newTab.setOnAction(e -> tabManager.addNewTab());
        open.setOnAction(e -> fileManager.openFile());
        save.setOnAction(e -> fileManager.saveFile(false));
        saveAs.setOnAction(e -> fileManager.saveFile(true));
        closeTab.setOnAction(e -> tabManager.closeCurrentTab());

        menu.getItems().addAll(newTab, open, save, saveAs, new SeparatorMenuItem(), closeTab);

        // Меню недавних файлов
        Menu recentMenu = new Menu("Недавние файлы");
        recentMenu.setDisable(true);

        updateRecentFilesMenu(recentMenu, fileManager);

        fileManager.getRecentFilesManager().addListener(() -> {
            javafx.application.Platform.runLater(() -> updateRecentFilesMenu(recentMenu, fileManager));
        });

        menu.getItems().add(recentMenu);

        // Очистить недавние файлы
        MenuItem clearRecent = new MenuItem("Очистить недавние файлы");
        clearRecent.setOnAction(e -> fileManager.getRecentFilesManager().clearRecentFiles());
        menu.getItems().add(clearRecent);

        return menu;
    }

    private Menu createEditMenu(EditorMain editorMain, TabManager tabManager) {
        Menu menu = new Menu("Редактировать");

        MenuItem undo = new MenuItem("Отменить");
        MenuItem redo = new MenuItem("Повторить");
        MenuItem cut = new MenuItem("Вырезать");
        MenuItem copy = new MenuItem("Копировать");
        MenuItem paste = new MenuItem("Вставить");
        MenuItem findReplace = new MenuItem("Найти и Заменить");

        // Обработчики
        undo.setOnAction(e -> tabManager.undo());
        redo.setOnAction(e -> tabManager.redo());
        cut.setOnAction(e -> tabManager.cut());
        copy.setOnAction(e -> tabManager.copy());
        paste.setOnAction(e -> tabManager.paste());
        findReplace.setOnAction(e -> showFindReplace(editorMain, tabManager));

        menu.getItems().addAll(undo, redo, new SeparatorMenuItem(), cut, copy, paste, new SeparatorMenuItem(), findReplace);
        return menu;
    }

    private Menu createExportMenu(EditorMain editorMain) {
        Menu menu = new Menu("Экспорт");

        MenuItem exportHTML = new MenuItem("Экспорт в HTML");
        MenuItem exportMarkdown = new MenuItem("Экспорт в Markdown");

        exportHTML.setOnAction(e -> editorMain.getExportManager().exportToHTML());
        exportMarkdown.setOnAction(e -> editorMain.getExportManager().exportToMarkdown());

        menu.getItems().addAll(exportHTML, exportMarkdown);
        return menu;
    }

    private Menu createAutoSaveMenu(EditorMain editorMain) {
        Menu menu = new Menu("Автосохранение");

        CheckMenuItem enableAutoSave = new CheckMenuItem("Включить автосохранение");
        enableAutoSave.setSelected(true);
        enableAutoSave.setOnAction(e -> {
            editorMain.getAutoSaveManager().setAutoSaveEnabled(enableAutoSave.isSelected());
        });

        Menu intervalMenu = new Menu("Интервал");
        ToggleGroup intervalGroup = new ToggleGroup();

        int[] intervals = {30, 60, 120, 300, 600};
        String[] labels = {"30 секунд", "1 минута", "2 минуты", "5 минут", "10 минут"};

        for (int i = 0; i < intervals.length; i++) {
            int interval = intervals[i];
            RadioMenuItem item = new RadioMenuItem(labels[i]);
            item.setToggleGroup(intervalGroup);
            if (interval == 60) item.setSelected(true);
            item.setOnAction(e -> editorMain.getAutoSaveManager().setAutoSaveInterval(interval));
            intervalMenu.getItems().add(item);
        }

        MenuItem forceSave = new MenuItem("Сохранить сейчас");
        forceSave.setOnAction(e -> editorMain.getAutoSaveManager().forceSave());

        menu.getItems().addAll(enableAutoSave, intervalMenu, new SeparatorMenuItem(), forceSave);
        return menu;
    }

    private Menu createThemeMenu() {
        Menu themeMenu = new Menu("Тема");
        ToggleGroup themeGroup = new ToggleGroup();

        for (Theme theme : Theme.values()) {
            RadioMenuItem themeItem = new RadioMenuItem(theme.getName());
            themeItem.setToggleGroup(themeGroup);
            themeItem.setOnAction(e -> {
                Scene scene = menuBar.getScene();
                if (scene != null) {
                    scene.getStylesheets().removeIf(url -> url.contains("/com/jumpie/"));
                    scene.getStylesheets().add(getClass().getResource(theme.getCssPath()).toExternalForm());
                }
            });
            themeMenu.getItems().add(themeItem);
        }
        return themeMenu;
    }

    private void updateRecentFilesMenu(Menu recentMenu, FileManager fileManager) {
        recentMenu.getItems().clear();

        List<File> recentFiles = fileManager.getRecentFilesManager().getRecentFiles();

        if (recentFiles.isEmpty()) {
            recentMenu.setDisable(true);
            MenuItem noFiles = new MenuItem("Нет недавних файлов");
            noFiles.setDisable(true);
            recentMenu.getItems().add(noFiles);
        } else {
            recentMenu.setDisable(false);

            for (int i = 0; i < recentFiles.size(); i++) {
                File file = recentFiles.get(i);
                String displayName = (i + 1) + ". " + file.getName();

                MenuItem item = new MenuItem(displayName);
                item.setOnAction(e -> fileManager.openFile(file));

                recentMenu.getItems().add(item);
            }
        }
    }

    private ComboBox<String> createFontComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(Font.getFamilies());
        combo.setValue("Consolas");
        combo.setTooltip(new Tooltip("Выбрать шрифт"));
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
        combo.setTooltip(new Tooltip("Выбрать размер шрифта"));
        combo.setMinWidth(60);
        combo.setMaxWidth(60);
        return combo;
    }

    private ToggleButton createStyleToggleButton(FontAwesomeIcon icon, String styleClass) {
        ToggleButton button = new ToggleButton();
        button.setGraphic(new FontAwesomeIconView(icon));
        button.getStyleClass().add(styleClass);
        return button;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle(".label");
        return label;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separator.setPadding(new javafx.geometry.Insets(0, 5, 0, 5));
        return separator;
    }

    private Button createRecordButton() {
        Button button = new Button("Запись");
        button.setTooltip(new Tooltip("Запустить/Остановить голосовой ввод"));
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.getStyleClass().add("record-button");
        return button;
    }

    private void setupEventHandlers(EditorMain editorMain, FileManager fileManager,
                                    TabManager tabManager, VoiceRecognitionService voiceService) {
        // Обработчики уже установлены в методах создания меню

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

    private void showFindReplace(EditorMain editorMain, TabManager tabManager) {
        if (findReplaceDialog == null) {
            findReplaceDialog = new FindReplaceDialog(editorMain.getPrimaryStage(), tabManager);
        }
        findReplaceDialog.show();
    }

    private void updateStyleButtons(TabManager tabManager) {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea != null && textArea.getSelection().getLength() > 0) {
            int pos = textArea.getSelection().getStart();
            @SuppressWarnings("unchecked")
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

    public void updateVoiceButtonState(boolean isListening) {
        javafx.application.Platform.runLater(() -> {
            if (isListening) {
                voiceButton.setText("Остановить");
                voiceButton.getStyleClass().remove("record-button");
                voiceButton.getStyleClass().add("recording-button");
                voiceButton.setTooltip(new Tooltip("Остановить голосовой ввод"));
            } else {
                voiceButton.setText("Запись");
                voiceButton.getStyleClass().remove("recording-button");
                voiceButton.getStyleClass().add("record-button");
                voiceButton.setTooltip(new Tooltip("Запустить голосовой ввод"));
            }
        });
    }
}
