package instru;

import javafx.util.StringConverter;

import java.text.SimpleDateFormat;

public class UnlabeledFormatter extends StringConverter<Number> {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("HH mm ss");

    @Override
    public String toString(Number object) {
        return "";

        //var xVal = (double) object;
        //return dateFormat.format(xVal);
    }

    @Override
    public Number fromString(String string) {
        return null;
    }
}
