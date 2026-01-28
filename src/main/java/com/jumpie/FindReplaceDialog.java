package com.jumpie;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FindReplaceDialog {
    private final Stage dialog;
    private final TextField findField;
    private final TextField replaceField;
    private final CheckBox regexCheckBox;
    private final CheckBox caseSensitiveCheckBox;
    private final CheckBox wholeWordCheckBox;
    private final Label statusLabel;
    private final TabManager tabManager;

    private int currentMatchIndex = -1;
    private List<MatchPosition> matches = new ArrayList<>();

    private static class MatchPosition {
        int start;
        int end;

        MatchPosition(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public FindReplaceDialog(Stage owner, TabManager tabManager) {
        this.tabManager = tabManager;
        this.dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Найти и Заменить");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("find-replace-dialog");

        // Find field
        Label findLabel = new Label("Найти:");
        findField = new TextField();
        findField.setPromptText("Текст для поиска...");
        findField.setPrefWidth(300);

        // Replace field
        Label replaceLabel = new Label("Заменить на:");
        replaceField = new TextField();
        replaceField.setPromptText("Текст для замены...");
        replaceField.setPrefWidth(300);

        // Options
        regexCheckBox = new CheckBox("Регулярные выражения");
        caseSensitiveCheckBox = new CheckBox("Учитывать регистр");
        wholeWordCheckBox = new CheckBox("Целое слово");

        // Buttons
        Button findNextBtn = new Button("Найти далее");
        Button findPrevBtn = new Button("Найти предыдущее");
        Button replaceBtn = new Button("Заменить");
        Button replaceAllBtn = new Button("Заменить всё");
        Button closeBtn = new Button("Закрыть");

        findNextBtn.setOnAction(e -> findNext());
        findPrevBtn.setOnAction(e -> findPrevious());
        replaceBtn.setOnAction(e -> replaceCurrent());
        replaceAllBtn.setOnAction(e -> replaceAll());
        closeBtn.setOnAction(e -> dialog.close());

        // Status label
        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");

        // Layout
        grid.add(findLabel, 0, 0);
        grid.add(findField, 1, 0, 2, 1);

        grid.add(replaceLabel, 0, 1);
        grid.add(replaceField, 1, 1, 2, 1);

        HBox optionsBox = new HBox(10);
        optionsBox.getChildren().addAll(regexCheckBox, caseSensitiveCheckBox, wholeWordCheckBox);
        grid.add(optionsBox, 0, 2, 3, 1);

        HBox buttonBox = new HBox(5);
        buttonBox.getChildren().addAll(findNextBtn, findPrevBtn, replaceBtn, replaceAllBtn, closeBtn);
        grid.add(buttonBox, 0, 3, 3, 1);

        grid.add(statusLabel, 0, 4, 3, 1);

        GridPane.setHgrow(findField, Priority.ALWAYS);
        GridPane.setHgrow(replaceField, Priority.ALWAYS);

        Scene scene = new Scene(grid);
        dialog.setScene(scene);
        dialog.setResizable(false);

        // Reset search on text change
        findField.textProperty().addListener((obs, old, newVal) -> {
            matches.clear();
            currentMatchIndex = -1;
            statusLabel.setText("");
        });

        regexCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            matches.clear();
            currentMatchIndex = -1;
            statusLabel.setText("");
        });

        caseSensitiveCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            matches.clear();
            currentMatchIndex = -1;
            statusLabel.setText("");
        });

        wholeWordCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            matches.clear();
            currentMatchIndex = -1;
            statusLabel.setText("");
        });
    }

    public void show() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea != null && textArea.getSelection().getLength() > 0) {
            findField.setText(textArea.getSelectedText());
        }
        dialog.show();
        findField.requestFocus();
    }

    private void findMatches() {
        matches.clear();
        currentMatchIndex = -1;

        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) {
            statusLabel.setText("Нет активного текстового поля");
            return;
        }

        String searchText = findField.getText();
        if (searchText.isEmpty()) {
            statusLabel.setText("Введите текст для поиска");
            return;
        }

        String content = textArea.getText();

        try {
            if (regexCheckBox.isSelected()) {
                findWithRegex(content, searchText);
            } else {
                findWithPlainText(content, searchText);
            }

            if (matches.isEmpty()) {
                statusLabel.setText("Совпадений не найдено");
            } else {
                statusLabel.setText("Найдено совпадений: " + matches.size());
            }
        } catch (PatternSyntaxException e) {
            statusLabel.setText("Ошибка в регулярном выражении: " + e.getMessage());
        }
    }

    private void findWithRegex(String content, String regex) {
        int flags = caseSensitiveCheckBox.isSelected() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            matches.add(new MatchPosition(matcher.start(), matcher.end()));
        }
    }

    private void findWithPlainText(String content, String searchText) {
        String text = content;
        String search = searchText;

        if (!caseSensitiveCheckBox.isSelected()) {
            text = text.toLowerCase();
            search = search.toLowerCase();
        }

        int index = 0;
        while ((index = text.indexOf(search, index)) != -1) {
            if (wholeWordCheckBox.isSelected()) {
                if (!isWholeWord(content, index, index + search.length())) {
                    index++;
                    continue;
                }
            }

            matches.add(new MatchPosition(index, index + searchText.length()));
            index++;
        }
    }

    private boolean isWholeWord(String text, int start, int end) {
        boolean startOk = start == 0 || !Character.isLetterOrDigit(text.charAt(start - 1));
        boolean endOk = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end));
        return startOk && endOk;
    }

    private void findNext() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) return;

        if (matches.isEmpty()) {
            findMatches();
            if (matches.isEmpty()) return;
        }

        int caretPos = textArea.getCaretPosition();

        // Find next match after caret
        int nextIndex = -1;
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).start >= caretPos) {
                nextIndex = i;
                break;
            }
        }

        // Wrap around if needed
        if (nextIndex == -1) {
            nextIndex = 0;
        }

        currentMatchIndex = nextIndex;
        highlightMatch(textArea, matches.get(currentMatchIndex));
        updateStatus();
    }

    private void findPrevious() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) return;

        if (matches.isEmpty()) {
            findMatches();
            if (matches.isEmpty()) return;
        }

        int caretPos = textArea.getCaretPosition();

        // Find previous match before caret
        int prevIndex = -1;
        for (int i = matches.size() - 1; i >= 0; i--) {
            if (matches.get(i).start < caretPos) {
                prevIndex = i;
                break;
            }
        }

        // Wrap around if needed
        if (prevIndex == -1) {
            prevIndex = matches.size() - 1;
        }

        currentMatchIndex = prevIndex;
        highlightMatch(textArea, matches.get(currentMatchIndex));
        updateStatus();
    }

    private void replaceCurrent() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) return;

        if (matches.isEmpty()) {
            findMatches();
            if (matches.isEmpty()) return;
        }

        if (currentMatchIndex == -1) {
            findNext();
            if (currentMatchIndex == -1) return;
        }

        MatchPosition match = matches.get(currentMatchIndex);
        String replacement = replaceField.getText();

        // Apply replacement
        textArea.replaceText(match.start, match.end, replacement);

        // Update matches after replacement
        int lengthDiff = replacement.length() - (match.end - match.start);
        matches.remove(currentMatchIndex);

        // Adjust subsequent match positions
        for (int i = currentMatchIndex; i < matches.size(); i++) {
            MatchPosition m = matches.get(i);
            m.start += lengthDiff;
            m.end += lengthDiff;
        }

        statusLabel.setText("Заменено. Осталось совпадений: " + matches.size());

        // Find next
        if (!matches.isEmpty()) {
            if (currentMatchIndex >= matches.size()) {
                currentMatchIndex = 0;
            }
            if (!matches.isEmpty()) {
                highlightMatch(textArea, matches.get(currentMatchIndex));
            }
        } else {
            currentMatchIndex = -1;
        }
    }

    private void replaceAll() {
        StyleClassedTextArea textArea = tabManager.getCurrentTextArea();
        if (textArea == null) return;

        findMatches();
        if (matches.isEmpty()) return;

        String replacement = replaceField.getText();
        int count = matches.size();

        // Replace from end to start to avoid index shifting issues
        for (int i = matches.size() - 1; i >= 0; i--) {
            MatchPosition match = matches.get(i);
            textArea.replaceText(match.start, match.end, replacement);
        }

        matches.clear();
        currentMatchIndex = -1;
        statusLabel.setText("Заменено совпадений: " + count);
    }

    private void highlightMatch(StyleClassedTextArea textArea, MatchPosition match) {
        textArea.selectRange(match.start, match.end);
        textArea.requestFollowCaret();
    }

    private void updateStatus() {
        if (!matches.isEmpty() && currentMatchIndex != -1) {
            statusLabel.setText("Совпадение " + (currentMatchIndex + 1) + " из " + matches.size());
        }
    }
}
