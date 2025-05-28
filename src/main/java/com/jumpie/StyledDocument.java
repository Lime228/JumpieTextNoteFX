package com.jumpie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StyledDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    private String text;
    private List<TextStyle> styles;

    public StyledDocument() {
        this("", new ArrayList<>());
    }

    public StyledDocument(String text, List<TextStyle> styles) {
        this.text = text;
        this.styles = new ArrayList<>(styles);
    }

    public String getText() { return text; }
    public List<TextStyle> getStyles() { return styles; }

    public void setText(String text) { this.text = text; }
    public void setStyles(List<TextStyle> styles) { this.styles = new ArrayList<>(styles); }

    public void optimizeStyles() {
        if (styles.size() < 2) return;

        List<TextStyle> optimized = new ArrayList<>();
        TextStyle current = styles.get(0);

        for (int i = 1; i < styles.size(); i++) {
            TextStyle next = styles.get(i);

            if (current.getEnd() + 1 >= next.getStart() &&
                    current.matches(next)) {
                current = new TextStyle(
                        Math.min(current.getStart(), next.getStart()),
                        Math.max(current.getEnd(), next.getEnd()),
                        current.getFontFamily(),
                        current.getFontSize(),
                        current.isBold(),
                        current.isItalic(),
                        current.isUnderline(),
                        current.isStrikethrough()
                );
            } else {
                optimized.add(current);
                current = next;
            }
        }

        optimized.add(current);
        styles = optimized;
    }
}