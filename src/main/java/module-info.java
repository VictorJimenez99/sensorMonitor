module instru {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;

    opens instru to javafx.fxml;
    exports instru;
}