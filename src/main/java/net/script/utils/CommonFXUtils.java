package net.script.utils;

import com.jfoenix.animation.alert.JFXAlertAnimation;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class CommonFXUtils {
    public static Optional noDataPopup(String title, String body, Scene scene) {
        JFXAlert alert = new JFXAlert((Stage) scene.getWindow());
        JFXDialogLayout layout = new JFXDialogLayout();
        JFXButton closeButton = new JFXButton("Close");
        closeButton.setButtonType(JFXButton.ButtonType.FLAT);
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        layout.setHeading(new Label(title));
        layout.setBody(new Label(body));
        layout.setActions(closeButton);
        alert.setAnimation(JFXAlertAnimation.CENTER_ANIMATION);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setOverlayClose(true);
        alert.setContent(layout);
        return alert.showAndWait();
    }
}
