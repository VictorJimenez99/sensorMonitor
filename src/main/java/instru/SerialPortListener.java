package instru;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.shape.Box;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SerialPortListener implements SerialPortDataListener {
    private final SerialPort comPort;
    private final Label ambientTempLabel;
    private final LineChart<Number, Number> ambientTempChart;
    private final XYChart.Series<Number, Number> ambientSeries;
    private final Label furnaceTempLabel;
    private final LineChart<Number, Number> furnaceTempChart;
    private final XYChart.Series<Number,Number> furnaceSeries;
    private final Label distanceLabel;
    private final LineChart<Number, Number> distanceChart;
    private final XYChart.Series<Number, Number> distanceSeries;
    private final Box wall;
    private final WeatherImages weatherViwer;
    private StringBuilder completePayload;
    private final ProgressBar ambientThermometer;

    private static final DecimalFormat format = new DecimalFormat("##.##");
    long counter = -1;

    private static final SimpleDateFormat simpleDateFormat
            = new SimpleDateFormat("hh:mm:ss");


    public SerialPortListener(SerialPort comPort,
                              Label ambientTempLabel,
                              LineChart<Number, Number> ambientTempChart,
                              ProgressBar ambientThermometer,
                              Label furnaceTempLabel,
                              LineChart<Number, Number> furnaceTempChart,
                              Label distanceLabel,
                              LineChart<Number, Number> distanceChart,
                              Box wall,
                              WeatherImages weatherImages) {
        this.comPort = comPort;
        this.ambientTempLabel = ambientTempLabel;
        this.ambientTempChart = ambientTempChart;
        this.ambientThermometer = ambientThermometer;
        this.furnaceTempLabel = furnaceTempLabel;
        this.furnaceTempChart = furnaceTempChart;
        this.distanceLabel = distanceLabel;
        this.distanceChart = distanceChart;
        this.wall = wall;
        this.completePayload = new StringBuilder();
        this.weatherViwer = weatherImages;

        ambientSeries = new XYChart.Series<>();
        ambientSeries.setName("temperatura");

        ambientTempChart.getData().add(ambientSeries);
        var xAxisAmbient = (NumberAxis) ambientTempChart.getXAxis();
        xAxisAmbient.setLabel("");
        xAxisAmbient.setAnimated(true);
        xAxisAmbient.setAutoRanging(false);
        xAxisAmbient.setLowerBound(0);
        xAxisAmbient.setUpperBound(10);
        var labelFormat = new UnlabeledFormatter();
        xAxisAmbient.setTickLabelFormatter(labelFormat);


        var yAxisAmbient = (NumberAxis) ambientTempChart.getYAxis();
        yAxisAmbient.setLabel("Temperatura");
        yAxisAmbient.setAnimated(false);
        yAxisAmbient.setAutoRanging(false);


        distanceSeries = new XYChart.Series<>();
        distanceSeries.setName("distancia");
        distanceChart.getData().add(distanceSeries);

        var xAxisDistance = (NumberAxis) distanceChart.getXAxis();
        xAxisDistance.setLabel("");
        xAxisDistance.setAnimated(true);
        xAxisDistance.setAutoRanging(false);
        xAxisDistance.setLowerBound(0);
        xAxisDistance.setUpperBound(10);
        xAxisDistance.setTickLabelFormatter(labelFormat);

        var yAxisDistance = (NumberAxis) distanceChart.getYAxis();
        yAxisDistance.setLabel("Centímetros");
        yAxisDistance.setAnimated(false);
        yAxisDistance.setAutoRanging(false);

        var distLine = distanceSeries
                .getNode().lookup(".chart-series-line");
        distLine.getStyleClass().add("distanceSeries");

        furnaceSeries = new XYChart.Series<>();
        furnaceSeries.setName("temperatura");

        furnaceTempChart.getData().add(furnaceSeries);
        var xAxisFurnace = (NumberAxis) furnaceTempChart.getXAxis();
        xAxisFurnace.setLabel("");
        xAxisFurnace.setAnimated(false);
        xAxisFurnace.setAutoRanging(false);
        xAxisFurnace.setLowerBound(0);
        xAxisFurnace.setUpperBound(10);
        xAxisFurnace.setTickLabelFormatter(labelFormat);


        var yAxisFurnace = (NumberAxis) furnaceTempChart.getYAxis();
        yAxisFurnace.setLabel("Temperatura");
        yAxisFurnace.setAnimated(false);
        yAxisFurnace.setAutoRanging(false);

        var furnaceLine = furnaceSeries
                .getNode().lookup(".chart-series-line");
        furnaceLine.getStyleClass().add("furnaceSeries");
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
            return;
        var newData = new byte[comPort.bytesAvailable()];
        var numRead = comPort.readBytes(newData, newData.length);
        if(numRead != newData.length) {
            System.out.println("Error");
        }
        var partial = new String(newData);
        completePayload.append(partial);
        //System.out.println("partial data transmission: " + partial);
        partial = completePayload.toString();
        if(partial.endsWith("\n")){
            //System.out.print("End of package transmission...\nparsing...");
            //removes \n
            partial = partial.trim();
            var pieces = partial.split(",");
            if(pieces.length != 3) {
                System.out.println("corrupted payload(num_data: " +
                                pieces.length + "), RESETING");
                completePayload = new StringBuilder();
                return;
            }
            //System.out.println("Done.");

            try {
                double distanceCM     = Double.parseDouble(pieces[0].trim());
                double furnaceVoltage = Double.parseDouble(pieces[1].trim());
                double ambientVoltage = Double.parseDouble(pieces[2].trim());

                // Ambient & furnace Voltage Conversion
                // 0V - 0°C & 5V - 100°C
                double ambientTemp = ambientVoltage * 100 / 5;
                double furnaceTemp = furnaceVoltage * 100 / 5;


                updateGUI(ambientTemp, furnaceTemp, distanceCM);

            } catch (NumberFormatException e){
                System.out.println("corrupted payload(NaN)");
            }

            completePayload = new StringBuilder();
        }
    }

    private void updateGUI(double ambientTemp, double furnaceTemp, double distance) {
        var ambientText = format.format(ambientTemp);
        var furnaceText = format.format(furnaceTemp);
        var distanceText = format.format(distance);
        Platform.runLater(()->{
            ambientTempLabel.setText("Temp: " + ambientText+ "°C");
            furnaceTempLabel.setText("Temp: "+furnaceText +"°C");
            distanceLabel.setText(distanceText + "cm");
        });
        //Ambient
        Platform.runLater(()-> {

            var ambientNodeList =
                    ambientSeries.getData();
            ambientNodeList.add(new XYChart.Data<>(counter, ambientTemp));
            var axis = (NumberAxis)ambientTempChart.getXAxis();
            if(ambientNodeList.size() > 11) {
                ambientNodeList.remove(0);
                axis.setLowerBound(counter-10);
                axis.setUpperBound(counter-1);
            }
            var size = ambientThermometer.getStyleClass().size();
            if(size > 2) {
                ambientThermometer.getStyleClass().remove(1);
            }
            if(ambientTemp < 15) {
                ambientThermometer.getStyleClass().add("thermometer-cold");
            }else if(ambientTemp>15 && ambientTemp < 50) {
                ambientThermometer.getStyleClass().add("thermometer-normal");
            }else {
                ambientThermometer.getStyleClass().add("thermometer-hot");
            }

            var thermometerValue = ambientTemp/100;
            ambientThermometer.setProgress(thermometerValue);

            if(ambientTemp < 15) {
                weatherViwer.displayColdImage();
            }else if(ambientTemp > 15 && ambientTemp < 30) {
                weatherViwer.displayNormalImage();
            }else {
                weatherViwer.displayHotImage();
            }

        });
        //Distance
        Platform.runLater(()->{
            wall.setLayoutX(130+(distance*10));
            var distanceList =
                    distanceSeries.getData();
            var point = new XYChart.Data<Number, Number>(counter, distance);
            distanceList.add(point);
            // We can only change the color of the node, once the point is rendered
            point.getNode()
                    .getStyleClass().add("distancePoint");

            //System.out.println(line);

            var axis = (NumberAxis)distanceChart.getXAxis();
            if(distanceList.size() > 11) {
                distanceList.remove(0);
                axis.setLowerBound(counter-10);
                axis.setUpperBound(counter-1);
            }
        });

        // Furnace
        Platform.runLater(()-> {
            var furnaceNodeList =
                    furnaceSeries.getData();
            var millis = new Date().getTime();
            var point = new XYChart.Data<Number, Number>(counter, furnaceTemp);
            furnaceNodeList.add(point);
            point.getNode()
                    .getStyleClass().add("furnacePoint");
            var axis = (NumberAxis)furnaceTempChart.getXAxis();
            if(furnaceNodeList.size() > 11) {
                furnaceNodeList.remove(0);
                axis.setLowerBound(counter-10);
                axis.setUpperBound(counter-1);
            }
            axis.setLabel(simpleDateFormat.format(millis));
        });
        counter++;

    }

}
