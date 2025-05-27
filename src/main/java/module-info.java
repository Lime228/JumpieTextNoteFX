module com.jumpie.jumpietextnotefx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires vosk;
    requires java.desktop;

    opens com.jumpie to javafx.fxml;
    exports com.jumpie;
}