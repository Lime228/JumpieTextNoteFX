package com.jumpie;

public enum Theme {
    LIGHT("Светлая", "/com/jumpie/light.css"),
    DARK("Темная", "/com/jumpie/dark.css"),
    BLUE("Синяя", "/com/jumpie/blue.css"),
    GREEN("Зеленая", "/com/jumpie/green.css");

    private final String name;
    private final String cssPath;

    Theme(String name, String cssPath) {
        this.name = name;
        this.cssPath = cssPath;
    }

    public String getName() { return name; }
    public String getCssPath() { return cssPath; }

    @Override
    public String toString() { return name; }
}