package com.abba5aghaei.stream.view;

import java.io.File;
import java.io.IOException;
import javax.annotation.processing.FilerException;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.abba5aghaei.stream.Main;
import com.abba5aghaei.stream.aspect.FileManager;
import com.abba5aghaei.stream.aspect.Network;
import com.abba5aghaei.stream.aspect.OSCheck;
import com.abba5aghaei.stream.tool.Toast;

public class Setting {

    public Setting(Stage parent) {
        try {
            boolean current = Main.manual;
            Stage stage = new Stage();
            VBox pane = FXMLLoader.load(getClass().getResource("setting.fxml"));
            ObservableList<Node> nodes = pane.getChildren();
            TitledPane hostPane = (TitledPane) nodes.get(0);
            TitledPane manulPane = (TitledPane) nodes.get(1);
            TitledPane inboxPane = (TitledPane) nodes.get(2);
            AnchorPane actiontPane = (AnchorPane) nodes.get(3);
            TextField nameField = (TextField) ((AnchorPane) hostPane.getContent()).getChildren().get(0);
            TextField passField = (TextField) ((AnchorPane) hostPane.getContent()).getChildren().get(1);
            ImageView manualToggle = (ImageView) ((AnchorPane) hostPane.getContent()).getChildren().get(2);
            Label manualLabel = (Label) ((AnchorPane) hostPane.getContent()).getChildren().get(3);
            TextField hostField = (TextField) ((AnchorPane) manulPane.getContent()).getChildren().get(0);
            TextField portField = (TextField) ((AnchorPane) manulPane.getContent()).getChildren().get(1);
            TextField inboxField = (TextField) ((AnchorPane) inboxPane.getContent()).getChildren().get(0);
            Button browseButton = (Button) ((AnchorPane) inboxPane.getContent()).getChildren().get(1);
            Button cancelButton = (Button) actiontPane.getChildren().get(0);
            Button applyButton = (Button) actiontPane.getChildren().get(1);
            if(Main.support) {
                if (Main.manual) {
                    manualToggle.setImage(new Image(getClass().getResource("resources/images/img-toggle-off.png").toExternalForm()));
                    manualLabel.setText("LAN");
                } else {
                    manualToggle.setImage(new Image(getClass().getResource("resources/images/img-toggle-on.png").toExternalForm()));
                    manualLabel.setText("WiFi");
                }
                nameField.setText(Main.wifiManager.getSSID());
                passField.setText(Main.wifiManager.getKey());
                hostField.setText(Network.HOST);
                portField.setText(String.valueOf(Network.PORT));
                inboxField.setText(FileManager.DIRECTORY);
                manualToggle.setOnMouseClicked((event) -> {
                    if (Main.manual) {
                        manualToggle.setImage(new Image(getClass().getResource("resources/images/img-toggle-on.png").toExternalForm()));
                        manualLabel.setText("WiFi");
                        Main.manual = false;
                    } else {
                        manualToggle.setImage(new Image(getClass().getResource("resources/images/img-toggle-off.png").toExternalForm()));
                        manualLabel.setText("LAN");
                        Main.manual = true;
                    }
                });
                browseButton.setOnAction(ae -> {
                    DirectoryChooser browser = new DirectoryChooser();
                    File dir = browser.showDialog(Main.stage);
                    if (dir != null) {
                        inboxField.setText(dir.getAbsolutePath());
                    }
                });
                cancelButton.setOnAction(ae -> {
                    Main.manual = current;
                    stage.close();
                });
                applyButton.setOnAction(ae -> {
                    try {
                        if (nameField.getText().length() == 0 || nameField.getText().startsWith("#")) {
                            throw new ClassNotFoundException();
                        } else {
                            OSCheck.OSType os = OSCheck.getOperatingSystemType();
                            int l = 10;
                            if (os == OSCheck.OSType.Windows) l = 8;
                            if (passField.getText().length() < l) {
                                throw new ArrayStoreException();
                            } else {
                                if ((nameField.getText() + passField.getText()).equals(Main.wifiManager.getSSID() + Main.wifiManager.getKey())) {
                                } else {
                                    Main.wifiManager.setSSID(nameField.getText());
                                    Main.wifiManager.setKey(passField.getText());
                                    Main.wifiManager.setHotspotProfile(Main.wifiManager.getSSID(), Main.wifiManager.getKey());
                                    Toast.make("Successfully changed");
                                }
                            }
                        }
                        if (portField.getText().length() < 4)
                            throw new NumberFormatException();
                        Network.PORT = Integer.parseInt(portField.getText());
                        if (hostField.getText().equals("localhost") || hostField.getText().matches("\\d*.\\d*.\\d*.\\d*"))
                            Network.HOST = hostField.getText();
                        else
                            throw new NullPointerException();
                        File directory = new File(inboxField.getText());
                        if (directory.exists()) {
                            if (directory.canWrite())
                                FileManager.DIRECTORY = inboxField.getText();
                            else
                                throw new FilerException("");
                        } else {
                            directory.createNewFile();
                            FileManager.DIRECTORY = inboxField.getText();
                        }
                        if (FileManager.DIRECTORY.endsWith("\\"))
                            FileManager.DIRECTORY = FileManager.DIRECTORY.substring(0, FileManager.DIRECTORY.lastIndexOf("\\"));
                        stage.close();
                    } catch (ClassNotFoundException e) {
                        Toast.make("Invalid name for hotspot");
                        flash(nameField);
                    } catch (ArrayStoreException e) {
                        Toast.make("Invalid password for hotspot");
                        flash(passField);
                    } catch (NumberFormatException e) {
                        Toast.make("Invalid port number");
                        flash(portField);
                    } catch (NullPointerException e) {
                        Toast.make("Invalid host");
                        flash(hostField);
                    } catch (FilerException e) {
                        Toast.make("Directory is unWritable");
                        flash(inboxField);
                    } catch (IOException e) {
                        Toast.make("Invalid directory");
                        flash(inboxField);
                    }
                    try {
                        Main.nameLabel.setText(Main.wifiManager.getSSID());
                        Main.keyLabel.setText(Main.wifiManager.getKey());
                        Main.ipLabel.setText(Network.HOST);
                        Main.portLabel.setText(String.valueOf(Network.PORT));
                    } catch (Exception e) {
                        Toast.make("Error: " + e.getMessage());
                    }
                    Main.saveSetting();
                });
            }
            else {
                manualToggle.setImage(new Image(getClass().getResource("resources/images/img-toggle-off.png").toExternalForm()));
                manualLabel.setText("LAN");
                nameField.setText("Disable");
                nameField.setDisable(true);
                passField.setText("Disable");
                passField.setDisable(true);
                hostField.setText(Network.HOST);
                portField.setText(String.valueOf(Network.PORT));
                inboxField.setText(FileManager.DIRECTORY);
                manualToggle.setOnMouseClicked((event) -> Toast.make("No wifi device found"));
                browseButton.setOnAction(ae -> {
                    DirectoryChooser browser = new DirectoryChooser();
                    File dir = browser.showDialog(Main.stage);
                    if (dir != null) {
                        inboxField.setText(dir.getAbsolutePath());
                    }
                });
                cancelButton.setOnAction(ae -> {
                    Main.manual = current;
                    stage.close();
                });
                applyButton.setOnAction(ae -> {
                    try {
                        if (portField.getText().length() < 4)
                            throw new NumberFormatException();
                        Network.PORT = Integer.parseInt(portField.getText());
                        if (hostField.getText().equals("localhost") || hostField.getText().matches("\\d*.\\d*.\\d*.\\d*"))
                            Network.HOST = hostField.getText();
                        else
                            throw new NullPointerException();
                        File directory = new File(inboxField.getText());
                        if (directory.exists()) {
                            if (directory.canWrite())
                                FileManager.DIRECTORY = inboxField.getText();
                            else
                                throw new FilerException("");
                        } else {
                            directory.createNewFile();
                            FileManager.DIRECTORY = inboxField.getText();
                        }
                        if (FileManager.DIRECTORY.endsWith("\\"))
                            FileManager.DIRECTORY = FileManager.DIRECTORY.substring(0, FileManager.DIRECTORY.lastIndexOf("\\"));
                        stage.close();
                    } catch (NumberFormatException e) {
                    Toast.make("Invalid port number");
                    flash(portField);
                    } catch (NullPointerException e) {
                        Toast.make("Invalid host");
                        flash(hostField);
                    } catch (FilerException e) {
                        Toast.make("Directory is unWritable");
                        flash(inboxField);
                    } catch (IOException e) {
                        Toast.make("Invalid directory");
                        flash(inboxField);
                    }
                    try {
                        Main.ipLabel.setText(Network.HOST);
                        Main.portLabel.setText(String.valueOf(Network.PORT));
                    } catch (Exception e) {
                        Toast.make("Error: " + e.getMessage());
                    }
                    Main.saveSetting();
                });
            }
            Scene scene = new Scene(pane);
            stage.initOwner(parent);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.getIcons().addAll(parent.getIcons());
            stage.setTitle(parent.getTitle());
            stage.setWidth(410.0d);
            stage.setHeight(467.0d);
            stage.show();
        } catch (IOException e) {
            Main.logger.error("On Setting.constructor: " + e.getMessage());
        }
    }

    private void flash(Node node) {
        FadeTransition flasher = new FadeTransition(new Duration(500), node);
        flasher.setFromValue(0);
        flasher.setToValue(100);
        flasher.setCycleCount(10);
        flasher.play();
    }
}
