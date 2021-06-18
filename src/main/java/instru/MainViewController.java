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
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;

import java.util.Arrays;
import java.util.Objects;


public class MainViewController {
    public Label furnaceTempLabel;
    public Label distanceLabel;
    public Label ambientTempLabel;
    public LineChart<Number, Number> ambientTempChart;
    public LineChart<Number, Number> furnaceTempChart;
    public LineChart<Number, Number> distanceChart;
    public GridPane container;
    public ProgressBar furnaceThermometer;
    public Box wall;
    public ImageView imageViewer;

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
        var imageTextureStream = getClass()
                .getResourceAsStream("wall.jpg");
        if (imageTextureStream == null) {
            System.out.println("Couldn't load texture. setting a color instead");
            material.setDiffuseColor(Color.DARKBLUE);
        }else {
            var texture = new Image(imageTextureStream);
            material.setDiffuseMap(texture);
            material.setDiffuseColor(Color.web("#a5654c"));
        }

        wall.setMaterial(material);



        var weatherImages = new Image[3];
        var weatherImageStream = getClass()
                .getResourceAsStream("cold.png");
        assert weatherImageStream != null;
        weatherImages[0] = new Image(weatherImageStream);
        weatherImageStream = getClass().getResourceAsStream("normal.png");
        assert weatherImageStream != null;
        weatherImages[1] = new Image(weatherImageStream);
        weatherImageStream = getClass().getResourceAsStream("sunny.png");
        assert weatherImageStream != null;
        weatherImages[2] = new Image(weatherImageStream);

        System.out.println(Arrays.toString(weatherImages));
        imageViewer.setImage(weatherImages[2]);
        imageViewer.setImage(null);
        var weatherViwer = new WeatherImages(imageViewer,
                weatherImages[0], weatherImages[1], weatherImages[2]);


        ambientTempChart.setLegendVisible(false);
        furnaceTempChart.setLegendVisible(false);
        distanceChart.setLegendVisible(false);

        comPort.openPort();
        comPort.addDataListener(
                new SerialPortListener(
                        comPort,
                        ambientTempLabel,
                        ambientTempChart,
                        furnaceTempLabel,
                        furnaceTempChart,
                        furnaceThermometer,
                        distanceLabel,
                        distanceChart,
                        wall, weatherViwer));

    }


}
