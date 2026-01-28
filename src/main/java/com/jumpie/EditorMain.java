package com.jumpie;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

public class EditorMain extends Application implements TextAppender {
    private TabManager tabManager;
    private FileManager fileManager;
    private VoiceRecognitionService voiceService;
    private EditorMenuBar editorMenuBar;
    private WordCountPanel wordCountPanel;
    private AutoSaveManager autoSaveManager; // НОВОЕ
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;

            tabManager = new TabManager();
            fileManager = new FileManager(primaryStage, tabManager);
            voiceService = new VoiceRecognitionService(primaryStage, "voicemodels/voskSmallRu0.22");
            editorMenuBar = new EditorMenuBar(this, fileManager, tabManager, voiceService);
            wordCountPanel = new WordCountPanel();

            // НОВОЕ: Инициализация автосохранения
            autoSaveManager = new AutoSaveManager(tabManager, fileManager);
            autoSaveManager.setListener((success, message) -> {
                wordCountPanel.updateAutoSaveStatus(message);
            });

            BorderPane root = new BorderPane();
            HBox topContainer = new HBox();
            topContainer.getStyleClass().add("top-container");
            topContainer.getChildren().addAll(
                    editorMenuBar.getMenuBar(),
                    editorMenuBar.getToolBar()
            );

            HBox.setHgrow(editorMenuBar.getMenuBar(), Priority.ALWAYS);
            HBox.setHgrow(editorMenuBar.getToolBar(), Priority.ALWAYS);

            root.setTop(topContainer);
            root.setCenter(tabManager.getTabPane());
            root.setBottom(wordCountPanel.getStatusBar());

            // Обновление статистики при изменении текста
            tabManager.getTabPane().getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                updateWordCount();
                attachTextChangeListener();
            });

            // Загружаем тему после создания сцены
            Scene scene = new Scene(root, 925, 600);

            // Применяем тему
            Theme savedTheme = fileManager.loadPreferences();
            String themeCss = getClass().getResource(savedTheme.getCssPath()).toExternalForm();
            scene.getStylesheets().add(themeCss);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Jumpie TextNote");
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> {
                autoSaveManager.stop(); // НОВОЕ
                voiceService.dispose();
                primaryStage.close();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError(primaryStage, "Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void attachTextChangeListener() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea != null) {
            textArea.textProperty().addListener((obs, oldText, newText) -> updateWordCount());
            textArea.selectionProperty().addListener((obs, oldSel, newSel) -> updateWordCount());
        }
    }

    private void updateWordCount() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        wordCountPanel.updateStats(textArea);
    }

    // НОВОЕ: Геттер для AutoSaveManager
    public AutoSaveManager getAutoSaveManager() {
        return autoSaveManager;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showError(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void appendText(String text) {
        Tab currentTab = tabManager.getCurrentTab();
        if (currentTab != null && currentTab.getContent() instanceof ScrollPane scrollPane) {
            if (scrollPane.getContent() instanceof StyleClassedTextArea textArea) {
                textArea.appendText(text);
                updateWordCount();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
