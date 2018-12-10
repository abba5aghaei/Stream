package com.abba5aghaei.stream.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class WifiCell extends ListCell<Wifi> {

    @FXML
    private ImageView image;
    @FXML
    private Label label;
    @FXML
    private HBox box;
    private FXMLLoader loader;

    @Override
    protected void updateItem(Wifi wifi, boolean empty) {
        super.updateItem(wifi, empty);
        if(empty || wifi == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("wifi.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (Exception e) {
                    System.out.println("Error in WifiGraphic: " + e.getMessage());
                }
            }
            label.setText(wifi.getName());
            int signal;
            if (wifi.getSignal() > 90)
                signal = 5;
            else if (wifi.getSignal() > 70)
                signal = 4;
            else if (wifi.getSignal() > 45)
                signal = 3;
            else if (wifi.getSignal() > 20)
                signal = 2;
            else signal = 1;
            image.setImage(new Image(getClass().getResource("resources/images/wifi-" + signal + ".png").toExternalForm()));
            setText(null);
            setGraphic(box);
        }
    }
}
