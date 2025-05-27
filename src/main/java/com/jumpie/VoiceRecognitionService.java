package com.jumpie;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.vosk.*;

import javax.sound.sampled.*;
import java.io.File;

public class VoiceRecognitionService {
    private static final int SAMPLE_RATE = 16000;
    private static final int BUFFER_SIZE = 4096;

    private final Stage parentStage;
    private final AudioFormat format;
    private final Recognizer recognizer;

    private volatile TargetDataLine microphone;
    private volatile boolean isListening = false;

    private Runnable onStateChange;

    public VoiceRecognitionService(Stage stage, String modelPath) {
        this.parentStage = stage;
        this.format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        this.recognizer = initializeRecognizer(modelPath);
    }

    private Recognizer initializeRecognizer(String modelPath) {
        try {
            File modelDir = new File(modelPath);
            if (!modelDir.exists()) {
                showError("Папка с моделью не найдена: " + modelPath);
                return null;
            }

            Model model = new Model(modelPath);
            return new Recognizer(model, SAMPLE_RATE);
        } catch (Exception e) {
            showError("Ошибка загрузки модели: " + e.getMessage());
            return null;
        }
    }

    public synchronized void toggleRecognition(TextAppender appender) {
        if (isListening) {
            stopRecognition();
        } else {
            startRecognition(appender);
        }
    }

    private void startRecognition(TextAppender appender) {
        if (isListening || recognizer == null) return;

        new Thread(() -> {
            try {
                openMicrophone();

                byte[] buffer = new byte[BUFFER_SIZE];
                while (isListening) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        processAudio(buffer, bytesRead, appender);
                    }
                }

            } catch (Exception e) {
                showError("Ошибка распознавания: " + e.getMessage());
            } finally {
                cleanupAfterStop(appender);
            }
        }).start();
    }

    private synchronized void openMicrophone() throws LineUnavailableException {
        if (microphone != null) closeMicrophone();

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            showError("Микрофон не поддерживает нужный формат");
            return;
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        isListening = true;
        notifyStateChanged();
    }

    private void processAudio(byte[] buffer, int bytesRead, TextAppender appender) {
        if (recognizer.acceptWaveForm(buffer, bytesRead)) {
            String result = recognizer.getResult();
            String text = extractTextFromResult(result);
            if (!text.isEmpty()) {
                javafx.application.Platform.runLater(() -> appender.appendText(text));
            }
        }
    }

    public synchronized void stopRecognition() {
        if (!isListening) return;

        isListening = false;
        closeMicrophone();
        notifyStateChanged();
    }

    private void cleanupAfterStop(TextAppender appender) {
        synchronized (this) {
            closeMicrophone();
            isListening = false;
            notifyStateChanged();

            if (recognizer != null) {
                String finalText = extractTextFromResult(recognizer.getFinalResult());
                if (!finalText.isEmpty()) {
                    javafx.application.Platform.runLater(() -> appender.appendText(finalText));
                }
            }
        }
    }

    private synchronized void closeMicrophone() {
        if (microphone != null) {
            try {
                microphone.stop();
                microphone.close();
            } catch (Exception e) {
                System.err.println("Ошибка при закрытии микрофона: " + e.getMessage());
            } finally {
                microphone = null;
            }
        }
    }

    private String extractTextFromResult(String jsonResult) {
        if (jsonResult == null || !jsonResult.contains("\"text\" : \"")) return "";

        int start = jsonResult.indexOf("\"text\" : \"") + 10;
        int end = jsonResult.indexOf("\"", start);
        return (start > 10 && end > start) ? jsonResult.substring(start, end) + " " : "";
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void notifyStateChanged() {
        if (onStateChange != null) {
            javafx.application.Platform.runLater(onStateChange);
        }
    }

    public synchronized boolean isListening() {
        return isListening;
    }

    public void setOnStateChangeListener(Runnable listener) {
        this.onStateChange = listener;
    }

    public synchronized void dispose() {
        stopRecognition();
        if (recognizer != null) {
            recognizer.close();
        }
    }
}