package com.jumpie;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.util.Duration;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AutoSaveManager {
    private final TabManager tabManager;
    private final FileManager fileManager;
    private Timeline autoSaveTimeline;
    private int autoSaveIntervalSeconds = 60; // По умолчанию 60 секунд
    private boolean autoSaveEnabled = true;
    private AutoSaveListener listener;
    private LocalDateTime lastSaveTime;

    public interface AutoSaveListener {
        void onAutoSave(boolean success, String message);
    }

    public AutoSaveManager(TabManager tabManager, FileManager fileManager) {
        this.tabManager = tabManager;
        this.fileManager = fileManager;
        initAutoSave();
    }

    private void initAutoSave() {
        autoSaveTimeline = new Timeline(new KeyFrame(Duration.seconds(autoSaveIntervalSeconds), e -> performAutoSave()));
        autoSaveTimeline.setCycleCount(Timeline.INDEFINITE);

        if (autoSaveEnabled) {
            autoSaveTimeline.play();
        }
    }

    private void performAutoSave() {
        if (!autoSaveEnabled) return;

        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null || textArea.getText().isEmpty()) {
            return;
        }

        Tab currentTab = tabManager.getCurrentTab();
        if (currentTab == null) return;

        File currentFile = (File) currentTab.getUserData();

        try {
            if (currentFile != null) {
                // Автосохранение существующего файла
                fileManager.saveFileDirectly(textArea, currentFile);
                lastSaveTime = LocalDateTime.now();
                notifyListener(true, "Автосохранение: " + currentFile.getName());
            } else {
                // Создание временного автосохранения для новых файлов
                File autoSaveFile = createAutoSaveFile();
                fileManager.saveFileDirectly(textArea, autoSaveFile);
                lastSaveTime = LocalDateTime.now();
                notifyListener(true, "Автосохранение: " + autoSaveFile.getName());
            }
        } catch (Exception e) {
            notifyListener(false, "Ошибка автосохранения: " + e.getMessage());
        }
    }

    private File createAutoSaveFile() {
        // Создаем папку для автосохранений
        File autoSaveDir = new File("autosave");
        if (!autoSaveDir.exists()) {
            autoSaveDir.mkdirs();
        }

        // Генерируем имя файла с временной меткой
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = "autosave_" + timestamp + ".jpop";

        return new File(autoSaveDir, fileName);
    }

    public void setAutoSaveInterval(int seconds) {
        if (seconds < 10) seconds = 10; // Минимум 10 секунд
        this.autoSaveIntervalSeconds = seconds;

        // Перезапускаем таймер с новым интервалом
        autoSaveTimeline.stop();
        autoSaveTimeline = new Timeline(new KeyFrame(Duration.seconds(seconds), e -> performAutoSave()));
        autoSaveTimeline.setCycleCount(Timeline.INDEFINITE);

        if (autoSaveEnabled) {
            autoSaveTimeline.play();
        }
    }

    public void setAutoSaveEnabled(boolean enabled) {
        this.autoSaveEnabled = enabled;

        if (enabled) {
            autoSaveTimeline.play();
        } else {
            autoSaveTimeline.stop();
        }
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public int getAutoSaveInterval() {
        return autoSaveIntervalSeconds;
    }

    public void setListener(AutoSaveListener listener) {
        this.listener = listener;
    }

    private void notifyListener(boolean success, String message) {
        if (listener != null) {
            Platform.runLater(() -> listener.onAutoSave(success, message));
        }
    }

    public String getLastSaveTimeFormatted() {
        if (lastSaveTime == null) return "Никогда";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return lastSaveTime.format(formatter);
    }

    public void stop() {
        if (autoSaveTimeline != null) {
            autoSaveTimeline.stop();
        }
    }

    // Принудительное автосохранение
    public void forceSave() {
        performAutoSave();
    }
}
