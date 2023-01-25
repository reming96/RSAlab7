module lab7.data.fxlab7 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens lab7.data.fxlab7 to javafx.fxml;
    exports lab7.data.fxlab7;
}