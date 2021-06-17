package instru;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static SerialPort pointerToSerialPort;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("MainView"));
        stage.setScene(scene);
        stage.setTitle("");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    static void setSerialPort(SerialPort serialPort) {
        pointerToSerialPort = serialPort;
    }

    @Override
    public void stop() {
        if (pointerToSerialPort != null) {
            pointerToSerialPort.closePort();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}