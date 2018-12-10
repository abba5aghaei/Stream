//INOG
package com.abba5aghaei.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import com.abba5aghaei.wifi.InOut;
import com.abba5aghaei.wifi.WifiManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import com.abba5aghaei.stream.aspect.FileManager;
import com.abba5aghaei.stream.aspect.Network;
import com.abba5aghaei.stream.tool.Logger;
import com.abba5aghaei.stream.tool.Toast;
import com.abba5aghaei.stream.view.Transfer;

/*
 * author @abba5aghaei
 */

public class Main extends Application {

    public static Stage stage;
    public static Scene scene;
    public static boolean connected;
    public static boolean manual;
    public static boolean currentConnection;
    public static boolean support = true;
    public static Label titleLabel;
    public static Label emptyLabel;
    public static Label nameLabel;
    public static Label keyLabel;
    public static Label portLabel;
    public static Label ipLabel;
    public static Label statusLabel;
    public static Label wifiStatusLabel;
    public static Thread sendThread;
    public static ProgressIndicator serverWaiter;
    public static ProgressIndicator clientWaiter;
    public static int BUFFER_SIZE = 10485760; //10 MB
    public static WifiManager wifiManager;
    public static ListView<AnchorPane> sentList;
    public static ListView<AnchorPane> receivedList;
    private static ArrayList<Transfer> sl = new ArrayList<>();
    private static ArrayList<Transfer> rl = new ArrayList<>();
    public static Logger logger;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        initialize();
        show();
    }

    private void initialize() {
        logger = new Logger();
        try {
            wifiManager = new WifiManager(new InOut() {
                @Override
                public void error(String s) {
                    Main.error(s);
                }

                @Override
                public void log(String s) {
                    logger.info(s);
                }

                @Override
                public void warn(String s) {
                    Main.error(s);
                }

                @Override
                public String getPassword() {
                    String password;
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Authentication");
                    dialog.setHeaderText("Enter the password:");
                    dialog.initOwner(Main.stage);
                    try {
                        password = dialog.showAndWait().get();
                    }
                    catch (Exception e) {
                        return null;
                    }
                    if(password.length()<8) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.initOwner(Main.stage);
                        alert.setTitle("Stream");
                        alert.setHeaderText("The password isn't correct.Please try again.");
                        alert.showAndWait();
                        password = getPassword();
                    }
                    return password;
                }
            });
        }
        catch (Exception e) {
            logger.error("On Main.initialize: " + e.getMessage());
            if(e.getMessage().contains("support")){
                Main.support = false;
                if(!Main.support) {
                    Main.manual = true;
                    Main.currentConnection = true;
                    Toast.make("No wifi device found");
                }
            }
        }
        loadSetting();
    }

    private void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/main.fxml"));
            AnchorPane pane = loader.load();
            scene = new Scene(pane);
            scene.setOnDragOver((event)->{
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                } else {
                    event.consume();
                }
            });
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.getIcons().add(new Image(getClass().getResource("view/resources/images/img-icon.png").toExternalForm()));
            stage.setTitle("Stream");
            stage.setWidth(560.0d);
            stage.setHeight(690.0d);
            stage.show();
        }
        catch (Exception e) {
            logger.error("On Main.show: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void loadSetting() {
        try {
            File setting = new File(System.getProperty("user.home") + "/.stream/setting.abs");
            if (setting.exists()) {
                Scanner scanner = new Scanner(new FileInputStream(setting));
                scanner.useDelimiter("%");
                if (scanner.hasNext()) {
                    String manualString = decode(scanner.next());
                    String port = decode(scanner.next());
                    String inbox = decode(scanner.next());
                    try {
                        manual = Boolean.parseBoolean(manualString);
                    }
                    catch (Exception e) {
                        try {
                            manual = manualString.equalsIgnoreCase("true") || manualString.equals("1");
                        } catch (Exception ex) { manual = false; }
                    }
                    try {
                        Network.PORT = Integer.parseInt(port);
                    }
                    catch (Exception e) {
                        Network.PORT = 5200;
                    }
                    FileManager.DIRECTORY = inbox;
                }
                scanner.close();
            }
        }
        catch (Exception e) {
            logger.error("On loading setting: " + e.getMessage());
        }
    }

    public static void saveSetting() {
        try {
            File setting = new File(System.getProperty("user.home") + "/.stream/setting.abs");
            if (!setting.getParentFile().exists()) {
                if (!setting.getParentFile().mkdir()) {
                    throw new Exception("Can't create setting file");
                }
            }
            if (!setting.exists() || !setting.isFile()) {
                if (!setting.createNewFile()) {
                    throw new Exception("Can't create setting file");
                }
            }
            PrintWriter writer = new PrintWriter(setting);
            writer.write(encode(String.valueOf(manual)));
            writer.write("%");
            writer.write(encode(String.valueOf(Network.PORT)));
            writer.write("%");
            writer.write(encode(FileManager.DIRECTORY));
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            logger.error("On saving setting: " + e.getMessage());
        }
    }

    public static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Stream");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
//
//    public static void alert(String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Stream");
//        alert.setHeaderText(message);
//        alert.showAndWait();
//    }

    public static void setConnected() {
        Platform.runLater(() -> {
            titleLabel.setText("Connected");
            wifiStatusLabel.setText("Available Networks");
            serverWaiter.setVisible(false);
            clientWaiter.setVisible(false);
        });
    }

    public static void setDisconnected() {
        new Thread(()->{
            Platform.runLater(() -> {
                titleLabel.setText("Disconnected");
                wifiStatusLabel.setText("Available Networks");
                serverWaiter.setVisible(false);
                clientWaiter.setVisible(false);
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
            Platform.runLater(() -> titleLabel.setText("Stream"));
        }).start();
        try {
            if(sendThread!=null) {
                sendThread.interrupt();
                sendThread = null;
            }
        }
        catch (Exception e) {
            logger.error("Main.disconnect: " + e.getMessage());
        }
    }

    public static void addSentList(String name, long size) {
        Platform.runLater(() -> {
            Transfer transfer = new Transfer(name, size);
            sl.add(transfer);
            sentList.getItems().add(transfer.pane);
        });
    }

    public static void addReceivedList(String name, long size) {
        Platform.runLater(() -> {
            emptyLabel.setVisible(false);
            Transfer transfer = new Transfer(name, size);
            rl.add(transfer);
            receivedList.getItems().add(transfer.pane);
        });
    }

    public static void sentProgress(int index , double progress) {
        Platform.runLater(() -> {
            sl.get(index).setProgress(progress);
            sl.get(index).setIndicator(progress);
        });
    }

    public static void receivedProgress(int index , double progress) {
        Platform.runLater(() -> {
            rl.get(index).setProgress(progress);
            rl.get(index).setIndicator(progress);
        });
    }

    public static void sentStatus(int index, String status) {
        Platform.runLater(() -> {
            sl.get(index).setStatus(status);
            sentList.scrollTo(index);
        });
    }

    public static void receivedStatus(int index, String status) {
        Platform.runLater(() -> {
            rl.get(index).setStatus(status);
            receivedList.scrollTo(index);
        });
    }

    private static String encode(String normal) {
        try {
            byte[] buf = normal.getBytes();
            StringBuffer sb = new StringBuffer();
            for (byte b : buf) {
                sb.append(String.format("%x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            logger.error("On WifiManager.encode: " + e.getMessage());
            return "";
        }
    }

    private static String decode(String encoded) {
        try {
            StringBuffer sb = new StringBuffer();
            for (int i=0;i<encoded.length()-1;i+=2) {
                String output = encoded.substring(i, (i+2));
                int decimal = Integer.parseInt(output, 16);
                sb.append((char)decimal);
            }
            return sb.toString();
        }
        catch (Exception e) {
            logger.error("On WifiManager.decode: " + e.getMessage());
            return "";
        }
    }
}
