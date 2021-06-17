package instru;

import com.fazecast.jSerialComm.SerialPort;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;

import java.util.Objects;


public class MainViewController {
    public Label furnaceTempLabel;
    public Label distanceLabel;
    public Label ambientTempLabel;
    public LineChart<Number, Number> ambientTempChart;
    public LineChart<Number, Number> furnaceTempChart;
    public LineChart<Number, Number> distanceChart;
    public GridPane container;
    public ProgressBar ambientThermometer;
    public Box wall;

    @FXML
    public void initialize() {
        System.out.println("Inicio");
        var ports = SerialPort.getCommPorts();
        SerialPort comPort = null;
        try{
            comPort = ports[0];
        }catch (IndexOutOfBoundsException e) {
            System.out.println("No serial port detected");
            for(var port: ports) {
                System.out.println(port);
            }
        }
        // removes the connection once the program finishes
        App.setSerialPort(comPort);

        var material = new PhongMaterial();
        material.setDiffuseMap(
                new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("wall.jpg")))
        );


        ambientTempChart.setLegendVisible(false);
        furnaceTempChart.setLegendVisible(false);
        distanceChart.setLegendVisible(false);

        wall.setMaterial(material);
        comPort.openPort();
        comPort.addDataListener(
                new SerialPortListener(
                        comPort,
                        ambientTempLabel,
                        ambientTempChart,
                        ambientThermometer,
                        furnaceTempLabel,
                        furnaceTempChart,
                        distanceLabel,
                        distanceChart,
                        wall));

    }


}
