package instru;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;

public class WeatherImages {

    enum CUR_IMG_ENUM{
        COLD,
        HOT,
        NORMAL,
        NONE,
    }

    private final SequentialTransition sequenceCold;
    private final SequentialTransition sequenceNormal;
    private final SequentialTransition sequenceHot;
    private CUR_IMG_ENUM cur_img;

    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    public WeatherImages(ImageView viewPort,
                         Image coldImage,
                         Image normalImage,
                         Image hotImage) {
        var transitionLeave = new TranslateTransition(
                Duration.millis(1_500), viewPort);
        transitionLeave.setByY(1000);
        var transitionReturn = new TranslateTransition(
                Duration.millis(1_500), viewPort);
        transitionReturn.setByY(-1000);
        transitionLeave.setOnFinished(e-> viewPort.setImage(coldImage));
        sequenceCold = new SequentialTransition(
                transitionLeave, transitionReturn);
        sequenceCold.setOnFinished(e->isLocked.set(false));
        transitionLeave = new TranslateTransition(
                Duration.millis(1_500), viewPort);
        transitionLeave.setByY(1000);
        transitionReturn = new TranslateTransition(
                Duration.millis(1_500), viewPort);
        transitionReturn.setByY(-1000);
        transitionLeave.setOnFinished(e-> viewPort.setImage(normalImage));
        sequenceNormal = new SequentialTransition(
                transitionLeave, transitionReturn);
        sequenceNormal.setOnFinished(e->isLocked.set(false));
        transitionLeave = new TranslateTransition(
                Duration.millis(1_500), viewPort);
        transitionLeave.setByY(1000);
        transitionReturn = new TranslateTransition(
                Duration.millis(1_500), viewPort);
        transitionReturn.setByY(-1000);
        transitionLeave.setOnFinished(e-> viewPort.setImage(hotImage));
        sequenceHot = new SequentialTransition(
                transitionLeave, transitionReturn);
        sequenceHot.setOnFinished(e->isLocked.set(false));

        cur_img = CUR_IMG_ENUM.NONE;
    }

    public void displayColdImage() {
        if(isLocked.get() || cur_img == CUR_IMG_ENUM.COLD)
            return;
        isLocked.set(true);
        cur_img = CUR_IMG_ENUM.COLD;
        sequenceCold.play();
    }
    public void displayHotImage() {
        if(isLocked.get() || cur_img == CUR_IMG_ENUM.HOT)
            return;
        isLocked.set(true);
        cur_img = CUR_IMG_ENUM.HOT;
        sequenceHot.play();
    }
    public void displayNormalImage(){
        if(isLocked.get() || cur_img == CUR_IMG_ENUM.NORMAL)
            return;
        isLocked.set(true);
        cur_img = CUR_IMG_ENUM.NORMAL;
        sequenceNormal.play();
    }
}
