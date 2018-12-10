package com.abba5aghaei.stream.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import com.abba5aghaei.stream.Main;

public class InfoCell extends ListCell<Info> {

    @FXML
    private ImageView image;
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private HBox box;
    private FXMLLoader loader;

    @Override
    protected void updateItem(Info info, boolean empty) {
        super.updateItem(info, empty);
        if(empty || info == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("info.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (Exception e) {
                    System.out.println("Error in InfoGraphic: " + e.getMessage());
                }
            }
            image.setImage(new Image(getClass().getResource("resources/images/img-"+info.getProperty()+".png").toExternalForm()));
            label1.setText(info.getProperty() + ":");
            label1.setStyle("-fx-font-size:12");
            label2.setText(info.getValue());
            label2.setStyle("-fx-font-size:14");
            switch (info.getProperty()) {
                case "Name":
                    Main.nameLabel = label2;
                    break;
                case "Password":
                    Main.keyLabel = label2;
                    break;
                case "IP":
                    Main.ipLabel = label2;
                    break;
                case "Port":
                    Main.portLabel = label2;
                    break;
                case "Status":
                    Main.statusLabel = label2;
                    break;
            }
            setText(null);
            setGraphic(box);
        }
    }
}
