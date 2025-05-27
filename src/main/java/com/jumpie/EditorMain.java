package com.jumpie;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class EditorMain extends Application implements TextAppender {
    private TabManager tabManager;
    private FileManager fileManager;
    private VoiceRecognitionService voiceService;
    private EditorMenuBar editorMenuBar;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Jumpie TextNote");

        // Инициализация компонентов
        tabManager = new TabManager();
        fileManager = new FileManager(primaryStage, tabManager);
        voiceService = new VoiceRecognitionService(primaryStage, "voicemodels/voskSmallRu0.22");
        editorMenuBar = new EditorMenuBar(this, fileManager, tabManager, voiceService);

        // Настройка обработчика закрытия окна
        primaryStage.setOnCloseRequest(e -> {
            voiceService.dispose();
            primaryStage.close();
        });

        // Создание основного layout
        BorderPane root = new BorderPane();

        // Создаем контейнер для меню и панели инструментов
        HBox topContainer = new HBox();
        topContainer.getChildren().addAll(editorMenuBar.getMenuBar(), editorMenuBar.getToolBar());

        root.setTop(topContainer);
        root.setCenter(tabManager.getTabPane());

        // Настройка сцены
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/com/jumpie/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void appendText(String text) {
        Tab currentTab = tabManager.getCurrentTab();
        if (currentTab != null && currentTab.getContent() instanceof ScrollPane scrollPane) {
            if (scrollPane.getContent() instanceof TextArea textArea) {
                textArea.appendText(text);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}