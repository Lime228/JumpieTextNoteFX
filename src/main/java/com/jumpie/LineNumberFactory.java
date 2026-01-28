package com.jumpie;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.function.IntFunction;

public class LineNumberFactory {

    public static IntFunction<Node> get(StyleClassedTextArea textArea) {
        Val<Integer> nParagraphs = LiveList.sizeOf(textArea.getParagraphs());

        return lineNumber -> {
            Val<String> formatted = nParagraphs.map(n -> format(lineNumber + 1, n));

            Label lineLabel = new Label();
            lineLabel.setFont(Font.font("monospace", FontPosture.REGULAR, 11));
            lineLabel.getStyleClass().add("line-number");
            lineLabel.setPadding(new Insets(0, 10, 0, 5));
            lineLabel.setAlignment(Pos.CENTER_RIGHT);
            lineLabel.setMinWidth(40);
            lineLabel.setPrefWidth(40);

            // Привязываем текст к номеру строки
            formatted.addListener((obs, oldVal, newVal) -> lineLabel.setText(newVal));
            lineLabel.setText(formatted.getValue());

            return lineLabel;
        };
    }

    private static String format(int lineNumber, int totalLines) {
        int digits = (int) Math.log10(totalLines) + 1;
        return String.format("%" + digits + "d", lineNumber);
    }
}
