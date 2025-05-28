package com.jumpie;

import java.io.Serializable;
import java.util.Set;

public class TextStyle implements Serializable {
    private static final long serialVersionUID = 1L;

    private int start;
    private int end;
    private String fontFamily;
    private int fontSize;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;

    public TextStyle(int start, int end, String fontFamily, int fontSize,
                     boolean bold, boolean italic, boolean underline, boolean strikethrough) {
        this.start = start;
        this.end = end;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
    }

    public int getStart() { return start; }
    public int getEnd() { return end; }
    public String getFontFamily() { return fontFamily; }
    public int getFontSize() { return fontSize; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isUnderline() { return underline; }
    public boolean isStrikethrough() { return strikethrough; }

    public boolean matches(TextStyle other) {
        if (other == null) return false;
        return this.bold == other.bold &&
                this.italic == other.italic &&
                this.underline == other.underline &&
                this.strikethrough == other.strikethrough &&
                this.fontFamily.equals(other.fontFamily) &&
                this.fontSize == other.fontSize;
    }

    public boolean matchesStyleSet(Set<String> styles) {
        boolean currentBold = styles.contains("text-bold");
        boolean currentItalic = styles.contains("text-italic");
        boolean currentUnderline = styles.contains("text-underline");
        boolean currentStrikethrough = styles.contains("text-strikethrough");

        String currentFontFamily = "Consolas";
        int currentFontSize = 14;

        for (String style : styles) {
            if (style.startsWith("font-family:")) {
                currentFontFamily = style.substring("font-family:".length());
            } else if (style.startsWith("size-")) {
                currentFontSize = Integer.parseInt(style.substring("size-".length()));
            }
        }

        return this.bold == currentBold &&
                this.italic == currentItalic &&
                this.underline == currentUnderline &&
                this.strikethrough == currentStrikethrough &&
                this.fontFamily.equals(currentFontFamily) &&
                this.fontSize == currentFontSize;
    }
}