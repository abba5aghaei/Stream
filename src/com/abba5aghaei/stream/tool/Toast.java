package com.abba5aghaei.stream.tool;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.abba5aghaei.stream.Main;

public class Toast {

    private static Stage stage;
    private static Scene scene;
    private static Pane pane;
    private static Label label;
    private static boolean showing;

    private static void init() {
        label = new Label();
        label.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        label.setTextFill(Color.WHITE);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFont(new Font(20));
        pane = new AnchorPane(label);
        scene = new Scene(pane);
        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initOwner(Main.stage);
        stage.setScene(scene);
        stage.initModality(Modality.NONE);
        scene.setFill(Color.TRANSPARENT);
        pane.setOpacity(0.0d);
    }

    public static void make(String message) {
        if (!showing) {
            Platform.runLater(() -> {
                init();
                showing = true;
                label.setText(message);
                stage.show();
                stage.setY(stage.getY() + 250);
                new Thread(() -> {
                    for (short i = 0; i < 100; i++) {
                        Platform.runLater(() -> {
                            pane.setOpacity(pane.getOpacity() + 0.01);
                        });
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException e) {
                        }
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    for (short i = 0; i < 100; i++) {
                        Platform.runLater(() -> {
                            pane.setOpacity(pane.getOpacity() - 0.01);
                        });
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                        }
                    }
                    Platform.runLater(() -> {
                        stage.close();
                    });
                    showing = false;
                }).start();
            });
        }
    }
}
