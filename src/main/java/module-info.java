module com.jumpie.jumpietextnotefx {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires vosk;
    requires java.desktop;
    requires org.fxmisc.richtext;
    requires de.jensd.fx.glyphs.fontawesome;

    opens com.jumpie to javafx.fxml;
    exports com.jumpie;
}