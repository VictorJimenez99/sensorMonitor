package instru;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.shape.Box;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SerialPortListener implements SerialPortDataListener {
    private final SerialPort comPort;
    private final Label ambientTempLabel;
    private final LineChart<Number, Number> ambientTempChart;
    private final XYChart.Series<Number, Number> ambientSeries;
    private final Label furnaceTempLabel;
    private final LineChart<Number, Number> furnaceTempChart;
    private final Label distanceLabel;
    private final LineChart<Number, Number> distanceChart;
    private final Box wall;
    private StringBuilder completePayload;
    private final ProgressBar ambientThermometer;

    private static final DecimalFormat format = new DecimalFormat("##.##");

    public SerialPortListener(SerialPort comPort,
                              Label ambientTempLabel,
                              LineChart<Number, Number> ambientTempChart,
                              ProgressBar ambientThermometer,
                              Label furnaceTempLabel,
                              LineChart<Number, Number> furnaceTempChart,
                              Label distanceLabel,
                              LineChart<Number, Number> distanceChart,
                              Box wall) {
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

        ambientSeries = new XYChart.Series<>();
        ambientSeries.setName("temperatura");

        ambientTempChart.getData().add(ambientSeries);
        var xAxisAmbient = (NumberAxis) ambientTempChart.getXAxis();
        xAxisAmbient.setLabel("Muestra");
        xAxisAmbient.setAnimated(true);
        xAxisAmbient.setAutoRanging(false);
        xAxisAmbient.setLowerBound(new Date().getTime() + 1_500);
        xAxisAmbient.setUpperBound(new Date().getTime() + 10_000);
        var labelFormat = new ClockTicker();
        xAxisAmbient.setTickLabelFormatter(labelFormat);

        var yAxisAmbient = (NumberAxis) ambientTempChart.getYAxis();
        yAxisAmbient.setLabel("Temperatura");
        yAxisAmbient.setAnimated(false);
        yAxisAmbient.setAutoRanging(false);
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
        //System.out.println("Read " + numRead + " bytes.");
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
        Platform.runLater(()-> {
            ambientTempLabel.setText("Temp: " + ambientText+ "°C");
            furnaceTempLabel.setText("Temp: "+furnaceText +"°C");
            distanceLabel.setText(distanceText + "cm");

            var ambientNodeList =
                    ambientSeries.getData();
            var millis = new Date().getTime();
            ambientNodeList.add(new XYChart.Data<>(millis, ambientTemp));
            var axis = (NumberAxis)ambientTempChart.getXAxis();
            if(ambientNodeList.size() > 11) {
                ambientNodeList.remove(0);
                axis.setLowerBound(millis-10_000);
                axis.setUpperBound(millis-1000);
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

            axis.setLabel(new SimpleDateFormat("HH mm ss").format(millis));


            wall.setLayoutX(130+(distance*10));


        });
    }

}
