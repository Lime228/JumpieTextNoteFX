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

    @Override
    public void start(Stage primaryStage) {
        try {
            tabManager = new TabManager();
            fileManager = new FileManager(primaryStage, tabManager);
            voiceService = new VoiceRecognitionService(primaryStage, "voicemodels/voskSmallRu0.22");

            editorMenuBar = new EditorMenuBar(this, fileManager, tabManager, voiceService);

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

            Scene scene = new Scene(root, 925, 600);

            try {
                String css = getClass().getResource("/com/jumpie/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (NullPointerException e) {
                System.err.println("CSS file not found. Using default styling.");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Jumpie TextNote");
            primaryStage.show();

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