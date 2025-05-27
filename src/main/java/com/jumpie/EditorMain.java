package com.jumpie;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

public class EditorMain extends Application implements TextAppender {
    private TabManager tabManager;
    private FileManager fileManager;
    private VoiceRecognitionService voiceService;
    private EditorMenuBar editorMenuBar;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Сначала инициализируем основные компоненты
            tabManager = new TabManager();
            fileManager = new FileManager(primaryStage, tabManager);
            voiceService = new VoiceRecognitionService(primaryStage, "voicemodels/voskSmallRu0.22");

            // 2. Затем создаем меню, которое зависит от предыдущих компонентов
            editorMenuBar = new EditorMenuBar(this, fileManager, tabManager, voiceService);

            // 3. Настройка основного интерфейса
            BorderPane root = new BorderPane();

            // Создаем контейнер для верхней панели (меню + инструменты)
            HBox topContainer = new HBox();
            topContainer.getChildren().addAll(
                    editorMenuBar.getMenuBar(),
                    editorMenuBar.getToolBar()
            );

            root.setTop(topContainer);
            root.setCenter(tabManager.getTabPane());

            // Настройка сцены
            Scene scene = new Scene(root, 925, 600);

            // Загрузка CSS стилей
            try {
                String css = getClass().getResource("/com/jumpie/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (NullPointerException e) {
                System.err.println("CSS file not found. Using default styling.");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Jumpie TextNote");
            primaryStage.show();

            // Настройка обработчика закрытия окна
            primaryStage.setOnCloseRequest(e -> {
                voiceService.dispose();
                primaryStage.close();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError(primaryStage, "Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void showError(Stage owner, String title, String message) {
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
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}