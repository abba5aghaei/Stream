package com.abba5aghaei.stream.view;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.abba5aghaei.stream.Main;
import com.abba5aghaei.stream.aspect.FileManager;
import com.abba5aghaei.stream.aspect.HTTPS;
import com.abba5aghaei.stream.aspect.Network;
import com.abba5aghaei.stream.aspect.OSCheck;
import com.abba5aghaei.stream.tool.Toast;

public class Controller implements Initializable {

    @FXML
    private Label titleLabel;
    @FXML
    private Button scanButton;
    @FXML
    private Label dragLabel;
    @FXML
    private Label emptyLabel;
    @FXML
    private Label wifiStatusLabel;
    @FXML
    private ImageView scannerImage;
    @FXML
    private ImageView toggleImage;
    @FXML
    private VBox drawer;
    @FXML
    private ProgressIndicator serverWaiter;
    @FXML
    private ProgressIndicator clientWaiter;
    @FXML
    private ComboBox<String> interfacesCombo;
    @FXML
    private ListView<Info> infoList;
    @FXML
    private ListView<Wifi> wifiList;
    @FXML
    private ListView<AnchorPane> sentList;
    @FXML
    private ListView<AnchorPane> receivedList;
    private TranslateTransition openNav;
    private TranslateTransition closeNav;
    private ArrayList<ArrayList<String>> pairList;
    private ObservableList<Info> infoObservableList;
    private ObservableList<Wifi> wifiObservableList;
    private Queue<File> sendQueue;
    private File lastDirectory;
    private int fileIndex;
    private boolean isSending;

    public Controller() {
        wifiObservableList = FXCollections.observableArrayList();
        infoObservableList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setVariables();
            new Thread(this::initializeUtils).start();
            setListeners();
        }
        catch (Exception e) {
            Main.error("On initializing");
        }
    }

    private void initHotspot() {
        ArrayList<ArrayList<String>> info = new ArrayList<>();
        ArrayList<String> ssid = new ArrayList<>();
        ArrayList<String> key = new ArrayList<>();
        ArrayList<String> ip = new ArrayList<>();
        ArrayList<String> port = new ArrayList<>();
        ArrayList<String> os = new ArrayList<>();
        ArrayList<String> mode = new ArrayList<>();
        ArrayList<String> max = new ArrayList<>();
        ArrayList<String> auth = new ArrayList<>();
        ArrayList<String> cipher = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();
        ssid.add("Name");
        key.add("Password");
        ip.add("IP");
        port.add("Port");
        os.add("OS");
        mode.add("Mode");
        max.add("Max number of clients");
        auth.add("Authentication");
        cipher.add("Cipher");
        status.add("Status");
        if(Main.support) {
            ssid.add(Main.wifiManager.getSSID());
            key.add(Main.wifiManager.getKey());
            mode.add(Main.wifiManager.getMode());
            max.add(Main.wifiManager.getMaxClients());
            auth.add(Main.wifiManager.getAuthentication());
            cipher.add(Main.wifiManager.getCipher());
            status.add(Main.wifiManager.getStatus());
        }
        else {
            ssid.add("Not available");
            key.add("Not available");
            mode.add("Not available");
            max.add("Not available");
            auth.add("Not available");
            cipher.add("Not available");
            status.add("Not available");
        }
        ip.add(Network.HOST);
        port.add(String.valueOf(Network.PORT));
        os.add(System.getProperty("os.name", "generic"));
        info.add(ssid);
        info.add(key);
        info.add(ip);
        info.add(port);
        info.add(os);
        info.add(mode);
        info.add(max);
        info.add(auth);
        info.add(cipher);
        info.add(status);
        Platform.runLater(() -> {
            infoObservableList.clear();
            for (ArrayList<String> map : info) {
                infoObservableList.add(new Info(map.get(0), map.get(1)));
            }
        });
    }

    public void showWifiList() {
        flash(scannerImage);
        new Thread(() -> {
            if (Main.wifiManager.isHotspotActive()) {
                Main.wifiManager.turnOffHotspot();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pairList = Main.wifiManager.list();
            Platform.runLater(() -> {
                wifiObservableList.clear();
                for (ArrayList<String> pair : pairList) {
                    wifiObservableList.add(new Wifi(pair.get(0), pair.get(1)));
                }
                scannerImage.setVisible(false);
            });
        }).start();
    }

    public void createServer() {
        if (Main.connected) {
            int r = question("Disconnect current connection?", "");
            if (r == 1) {
                disconnect();
            }
        } else {
            titleLabel.setText("Starting hotspot...");
            serverWaiter.setVisible(true);
                if (Main.manual) {
                    Main.currentConnection = true;
                    forceCreateServer();
                } else {
                    if (Main.wifiManager.isHotspotSupport()) {
                        if (!Main.wifiManager.isHotspotActive()) {
                            if (Main.wifiManager.turnOnHotspot()) {
                                Main.currentConnection = false;
                                forceCreateServer();
                            } else {
                                AtomicInteger r = new AtomicInteger();
                                r.set(question("Failed to create hotspot", "Do you want to start server and make connection manually?"));
                                if (r.get() == 1) {
                                    Main.currentConnection = true;
                                    forceCreateServer();
                                }
                                else {
                                    Main.setDisconnected();
                                }
                            }
                        } else {
                            forceCreateServer();
                        }
                    } else {
                        AtomicInteger r = new AtomicInteger();
                        r.set(question("Wi-Fi device does't support hosted network", "Do you want to start server and make connection manually?"));
                        if (r.get() == 1) {
                            Main.currentConnection = true;
                            forceCreateServer();
                        }
                        else {
                            Main.setDisconnected();
                        }
                    }
                }
        }
    }

    private void forceCreateServer() {
        Platform.runLater(() ->titleLabel.setText("Awaiting peer to join..."));
        String ip = "";
        for(String item : Network.getIPs()) {
            try {
                Integer.parseInt(item.substring(0, item.indexOf(".")));
                ip = item;
            } catch (Exception e) { continue; }
        }
        if (ip.length() == 0) {
            Network.HOST = "127.0.0.1";
        }
        else {
            Network.HOST = ip;
        }
        Platform.runLater(() -> Main.ipLabel.setText(Network.HOST));
        Main.connected = true;
        Network.createServer();
    }

    public void join() {
        if (Main.connected) {
            Toast.make("You already connected");
        } else {
            titleLabel.setText("Connecting...");
            clientWaiter.setVisible(true);
                if (Main.manual) {
                    Main.currentConnection = true;
                    forceJoin();
                } else {
                    if (wifiList.getItems().size() != 0) {
                        int index = wifiList.getSelectionModel().getSelectedIndex();
                        if (index > -1) {
                            String name = pairList.get(index).get(0);
                            String authString = pairList.get(index).get(2);
                            int auth = 1;
                            if (authString.equals("Open") || authString.equals("--"))
                                auth = 0;
                            Platform.runLater(() -> wifiStatusLabel.setText("Connecting..."));
                            if (Main.wifiManager.connect(name, name, auth)) {
                                Main.logger.info("Connect result is true");
                                Main.currentConnection = false;
                                forceJoin();
                            } else {
                                Main.logger.info("Connect result is false");
                                AtomicInteger r = new AtomicInteger();
                                r.set(question("Connection failed!", "Do you want to join manually?"));
                                if (r.get() == 1) {
                                    Main.currentConnection = true;
                                    forceJoin();
                                }
                                else {
                                    Main.setDisconnected();
                                }
                                Platform.runLater(() -> wifiStatusLabel.setText("Available Networks"));
                            }
                        } else {
                            AtomicInteger r = new AtomicInteger();
                            r.set(question("Connection failed!", "Do you want to join manually?"));
                            if (r.get() == 1) {
                                Main.currentConnection = true;
                                forceJoin();
                            }
                            else {
                                Main.setDisconnected();
                            }
                        }
                    } else {
                        AtomicInteger r = new AtomicInteger();
                        r.set(question("Connection failed!", "Do you want to join manually?"));
                        if (r.get() == 1) {
                            Main.currentConnection = true;
                            forceJoin();
                        }
                        else {
                            Main.setDisconnected();
                        }
                    }
                }
        }
    }

    private void forceJoin() {
        Main.connected = true;
        Platform.runLater(() ->titleLabel.setText("Joining..."));
        if(!Main.currentConnection) {
            setHostIPAddress();
        }
        else {
            getManualInfo();
        }
    }

    private void getManualInfo(String... s) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.initOwner(Main.stage);
            alert.setTitle("Set Host ip and port");
            alert.setHeaderText("IP:\n\nPort:");
            if (s != null)
                if (s.length != 0)
                    alert.setContentText(s[0]);
            AnchorPane aboutPane = new AnchorPane();
            TextField ipFeild = new TextField();
            String ip = "";
            for (String item : Network.getIPs()) {
                try {
                    Integer.parseInt(item.substring(0, item.indexOf(".")));
                    ip = item;
                } catch (Exception e) {
                    continue;
                }
            }
            if (ip.length() == 0)
                ip = "127.0.0.1";
            else {
                ip = ip.substring(0, ip.lastIndexOf("."));
                ip += ".1";
            }
            ipFeild.setText(ip);
            ipFeild.setPrefWidth(200.0d);
            ipFeild.setLayoutX(0.0d);
            ipFeild.setLayoutY(10.0d);
            ipFeild.setFocusTraversable(false);
            TextField portFeild = new TextField();
            portFeild.setText("5200");
            portFeild.setPrefWidth(200.0d);
            portFeild.setLayoutX(0.0d);
            portFeild.setLayoutY(48.0d);
            portFeild.setFocusTraversable(false);
            aboutPane.getChildren().addAll(ipFeild, portFeild);
            alert.setGraphic(aboutPane);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(new ButtonType("Submit"));
            try {
                if (alert.showAndWait().isPresent()) {
                    ip = ipFeild.getText();
                    if (ip.equals("localhost") || !(ip.matches("\\d*.\\d*.\\d*.\\d*"))) throw new Exception();
                    String port = portFeild.getText();
                    if (port.length() < 4) throw new Exception();
                    Network.HOST = ip;
                    Network.PORT = Integer.parseInt(port);
                    Network.joinServer();
                } else {
                    Main.connected = false;
                    Main.setDisconnected();
                }
            } catch (Exception e) {
                Toast.make("Invalid port or ip");
                getManualInfo();
            }
        });
    }

    private void setHostIPAddress() {
        String ip = "";
        for(String item : Network.getIPs()) {
            try {
                Integer.parseInt(item.substring(0, item.indexOf(".")));
                ip = item;
            } catch (Exception e) { continue; }
        }
        if (ip.length() == 0) {
            getManualInfo("Can't obtain server ip!\nPlease set Host ip manually and try again.");
        }
        else {
            ip = ip.substring(0, ip.lastIndexOf("."));
            ip += ".1";
            Network.HOST = ip;
            Network.joinServer();
        }
        Platform.runLater(() ->Main.ipLabel.setText(Network.HOST));
    }

    public void disconnect() {
        Network.disconnect();
    }

    public void changeInterface() {
        Main.wifiManager.setInterface(interfacesCombo.getSelectionModel().getSelectedItem());
    }

    public void browse() {
        if (Main.connected) {
            FileChooser browser = new FileChooser();
            browser.setInitialDirectory(lastDirectory);
            List<File> files = browser.showOpenMultipleDialog(Main.stage);
            sendList(files);
            enQueueFiles(files);
        } else {
            Toast.make("Device is disconnected");
        }
    }

    private void enQueueFiles(List<File> files) {
        if (files != null) {
            lastDirectory = files.get(0).getParentFile();
            sendQueue.addAll(files);
            dragLabel.setVisible(false);
            if(!isSending) {
                Main.sendThread = new Thread(() -> {
                    isSending = true;
                    while (!sendQueue.isEmpty()) {
                        sendFile(sendQueue.peek(), fileIndex);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            Main.logger.error("On Controller.enQueueFiles: " + ie.getMessage());
                        }
                        sendQueue.remove();
                        fileIndex++;
                    }
                    isSending = false;
                });
                Main.sendThread.start();
            }
        }
    }

    private void sendList(List<File> files) {
        LinkedTransferQueue<File> listSendQueue = new LinkedTransferQueue<>();
        listSendQueue.addAll(files);
        while (!listSendQueue.isEmpty()) {
            Network.send("2", false);
            Network.send(listSendQueue.peek().getName(), false);
            Network.send(String.valueOf(listSendQueue.peek().length()), false);
            Main.addSentList(listSendQueue.peek().getName(), listSendQueue.peek().length());
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                Main.logger.error("On Controller.sendList: " + ie.getMessage());
            }
            listSendQueue.remove();
        }
    }

    private boolean sendFile(File file, int index) {
        Network.send("1", false);
        Network.send(file.getName(), true);
        Network.send(String.valueOf(index), true);
        Network.send(String.valueOf(file.length()), true);
        return Network.send(file, index);
    }

    public void handleDraggedFiles(DragEvent event) {
        if (Main.connected) {
            Dragboard dragboard = event.getDragboard();
            List<File> files = dragboard.getFiles();
            files.forEach((file)->{
                if(file.isDirectory()) {
                    files.remove(file);
                    Toast.make("Directory is not supported");
                }
            });
            sendList(files);
            enQueueFiles(files);
            event.setDropCompleted(true);
            event.consume();
        } else {
            Toast.make("Device is disconnected");
        }
    }

    public void doDraw() {
        if (drawer.getTranslateX() != 256) {
            openNav.play();
        } else {
            closeNav.play();
        }
    }

    public void openDrawer() {
        if (drawer.getTranslateX() != 256)
            openNav.play();
    }

    public void closeDrawer() {
        if (drawer.getTranslateX() == 256)
            closeNav.play();
    }

    private void flash(Node node) {
        node.setVisible(true);
        FadeTransition flasher = new FadeTransition(new Duration(500), node);
        flasher.setFromValue(100);
        flasher.setToValue(0);
        flasher.setCycleCount(10);
        flasher.play();
    }

    public void alwaysOnTop() {
        if (Main.stage.isAlwaysOnTop()) {
            Main.stage.setAlwaysOnTop(false);
            toggleImage.setImage(new Image(getClass().getResource("resources/images/img-toggle-off.png").toExternalForm()));
        }
        else {
            Main.stage.setAlwaysOnTop(true);
            toggleImage.setImage(new Image(getClass().getResource("resources/images/img-toggle-on.png").toExternalForm()));
        }
    }

    public void setting() {
        new Setting(Main.stage);
        closeDrawer();
    }

//    public void createDesktopEntry() {
//        try {
//            OSCheck.OSType os = OSCheck.getOperatingSystemType();
//            if(os == OSCheck.OSType.Linux) {
//                File shortcut = new File("/usr/share/applications/Stream.desktop");
//                PrintWriter writer = new PrintWriter(shortcut);
//                writer.println("[Desktop Entry]\n" +
//                        "Version=1.0\n" +
//                        "Type=Application\n" +
//                        "Name=Stream\n" +
//                        "Icon=/opt/eclipse/icon.xpm\n" +
//                        "Exec=\"/home/abba5aghaei/Stream.sh\" %f\n" +
//                        "Comment=File Sharing tool\n" +
//                        "Categories=Utility;\n" +
//                        "Terminal=false");
//                writer.flush();
//                writer.close();
//            }
//        }
//        catch (Exception e) {
//            Main.logger.error("On Controller.createDesktopEntry: " + e.getMessage());
//        }
//    }

    public void fileReceived() {
        try {
            OSCheck.OSType os = OSCheck.getOperatingSystemType();
            if(os == OSCheck.OSType.Linux) {
                Desktop desktop = Desktop.getDesktop();
                if(desktop.isSupported(Desktop.Action.OPEN)) {
                    URI uri = new URI("file://" + FileManager.DIRECTORY);
                    new Thread(()->{
                        try {
                            desktop.open(new File(uri.getPath()));
                        } catch (Exception e) {
                            Main.logger.error("On Controller.fileReceived: " + e.getMessage());
                        }
                    }).start();
                }
                else {
                    Toast.make("Your desktop is not support this operation");
                }
            }
            else {
                new Thread(()->{
                    try {
                        Desktop.getDesktop().open(new File(FileManager.DIRECTORY));
                    } catch (Exception e) {
                        Main.logger.error("On Controller.fileReceived: " + e.getMessage());
                    }
                }).start();
            }
        } catch (Exception e) {
            Main.logger.error("On Controller.fileReceived: " + e.getMessage());
        }
        closeDrawer();
    }

    public void feedback() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("Stream");
        alert.setHeaderText("Please rate this app:\n\nWrite your comment:");
        alert.setContentText("Thanks for your feedback");
        AnchorPane aboutPane = new AnchorPane();
        Slider slider = new Slider();
        slider.setMin(-5);
        slider.setMax(5);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.setValue(0);
        slider.setPrefWidth(200.0d);
        slider.setLayoutX(0.0d);
        slider.setLayoutY(10.0d);
        slider.setFocusTraversable(false);
        TextField comment = new TextField();
        comment.setPrefWidth(200.0d);
        comment.setLayoutX(0.0d);
        comment.setLayoutY(48.0d);
        comment.setFocusTraversable(false);
        aboutPane.getChildren().addAll(slider, comment);
        alert.setGraphic(aboutPane);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(new ButtonType("Send"));
        closeDrawer();
        try {
            if(alert.showAndWait().isPresent()) {
                String cm = comment.getText();
                String rate = String.valueOf(slider.getValue());
                String params = String.format("rate=%s&comment=%s", rate, cm);
                HTTPS.post("google.com", params);
                Toast.make("Thanks for your feedback");
            }
        }
        catch (Exception e) {
            Toast.make("Bad connection");
        }
    }

    public void share() {
        String ip = "";
        for(String item : Network.getIPs()) {
            try {
                Integer.parseInt(item.substring(0, item.indexOf(".")));
                ip = item;
            } catch (Exception e) { continue; }
        }
        if (ip.length() == 0)
            ip = "No network detected";
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("Share");
        alert.setContentText("Your ip: " + ip);
        if (Main.wifiManager.isHotspotSupport())
            alert.setHeaderText("Support result: YES");
        else alert.setHeaderText("Support result: NO");
        alert.show();
        closeDrawer();
    }

    public void help() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("Help");
        alert.setHeaderText("Get started from zero");
        alert.setContentText("Step 1, A and B runs com.abba5aghaei.stream\n" +
                "Step 2, A clicks \"Create server\"\n" +
                "Step 3, B clicks on \"Receive\" then \"Join\"\n" +
                "Step 4, Choose the files or drag and drop into the list\n" +
                "Step 5, Wait for the transfer to be completed\n");
        alert.show();
        closeDrawer();
    }

    public void about() {
        Stage aboutStage = new Stage();
        AnchorPane aboutPane = new AnchorPane();
        Scene aboutScene = new Scene(aboutPane);
        ImageView image = new ImageView(getClass().getResource("resources/images/img-logo.png").toExternalForm());
        Button ok = new Button("Close");
        ok.setOnAction((e)->{aboutStage.close();});
        String content = "Stream v1.0 (Beta)\nCross platform file sharing\n"
                +"Built: June 11 2018\nAuthor: Sayyid abbas aghaei\n\n"
                +"Please report any bugs to me\n"
                +"Social networks: @abba5aghaei\n"
                +"Email: abba5aghaei@outlook.com";
        Text text = new Text();
        text.setFont(Font.font("System", FontWeight.NORMAL, 16));
        Animation animation = new Transition() { {
            setCycleDuration(Duration.millis(10000));
        }
            protected void interpolate(double frac) {
                final int length = content.length();
                final int n = Math.round(length * (float) frac);
                text.setText(content.substring(0, n));
            }
        };
        image.setLayoutX(250.0d);
        image.setLayoutY(10.0d);
        image.setFitWidth(100.0d);
        image.setFitHeight(100.0d);
        text.setLayoutX(10.0d);
        text.setLayoutY(20.0d);
        ok.setPrefWidth(70.0d);
        ok.setPrefHeight(25.0d);
        ok.setLayoutX(150.0d);
        ok.setLayoutY(185.0d);
        aboutPane.getChildren().addAll(image, text, ok);
        aboutStage.setScene(aboutScene);
        aboutStage.initOwner(Main.stage);
        aboutStage.initModality(Modality.WINDOW_MODAL);
        aboutStage.setResizable(false);
        aboutStage.setWidth(380.0d);
        aboutStage.setHeight(250.0d);
        aboutStage.getIcons().addAll(Main.stage.getIcons());
        aboutStage.setTitle("About");
        aboutStage.show();
        animation.play();
        closeDrawer();
    }

    private int question(String header, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("Stream");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.NO);
        alert.getButtonTypes().add(ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();
        int r = 0;
        if(result.isPresent()) {
            if (result.get().equals(ButtonType.NO))
                r = -1;
            else if (result.get().equals(ButtonType.YES))
                r = 1;
            return r;
        }
        else return r;
    }

    public void exit() {
        disconnect();
        new Thread(()->{
            try {
                if(Main.support) {
                    if (Main.wifiManager.isWifiConnected())
                        Main.wifiManager.disconnect();
                    if (Main.wifiManager.isHotspotActive())
                        Main.wifiManager.turnOffHotspot();
                }
            }
            catch (Exception e) {
                Main.logger.error("On Controller.exit: " + e.getMessage());
            }
            System.exit(0);
        }).start();
        Main.stage.close();
    }

    private void setVariables() {
        openNav = new TranslateTransition(new Duration(350), drawer);
        openNav.setToX(256);
        closeNav = new TranslateTransition(new Duration(350), drawer);
        closeNav.setToX(0);
        sendQueue = new LinkedTransferQueue<>();
        fileIndex = 0;
        lastDirectory = new FileChooser().getInitialDirectory();
        Main.titleLabel = this.titleLabel;
        Main.emptyLabel = this.emptyLabel;
        Main.wifiStatusLabel = this.wifiStatusLabel;
        Main.serverWaiter = this.serverWaiter;
        Main.clientWaiter = this.clientWaiter;
        Main.sentList = this.sentList;
        Main.receivedList = this.receivedList;
        Main.stage.setOnCloseRequest((we)->{exit();});
        infoList.setItems(infoObservableList);
        wifiList.setItems(wifiObservableList);
        if(!Main.support) {
            scanButton.setDisable(true);
        }
    }

    private void initializeUtils() {
        FileManager.initialize();
        Network.initialize();
        initHotspot();
        if(Main.support) {
            Platform.runLater(() -> {
                try {
                    ObservableList<String> ifaces = interfacesCombo.getItems();
                    ifaces.addAll(Main.wifiManager.getInterfaces());
                    if (!ifaces.isEmpty()) {
                        for (String iface : ifaces) {
                            if (iface.toLowerCase().startsWith("w")) {
                                interfacesCombo.getSelectionModel().select(iface);
                                break;
                            } else {
                                interfacesCombo.getSelectionModel().select(0);
                                break;
                            }
                        }
                        Main.wifiManager.setInterface(interfacesCombo.getSelectionModel().getSelectedItem());
                    }
                } catch (NullPointerException ne) {
                    Main.error("On initializing interfaces.");
                } catch (Exception e) {
                    Main.error(e.getMessage());
                }
            });
        }
    }

    private void setListeners() {
        wifiList.setCellFactory(listView -> {
            WifiCell cell = new WifiCell();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem joinItem = new MenuItem();
            joinItem.textProperty().bind(Bindings.format("Join"));
            joinItem.setOnAction(event -> {
                join();
            });
            MenuItem connectItem = new MenuItem();
            connectItem.textProperty().bind(Bindings.format("Connect only"));
            connectItem.setOnAction(event -> {
                int index = wifiList.getSelectionModel().getSelectedIndex();
                if (index > -1) {
                    new Thread(()->{
                        String name = pairList.get(index).get(0);
                        String authString = pairList.get(index).get(2);
                        int auth = 1;
                        if (authString.equals("Open") || authString.equals("--"))
                            auth = 0;
                        Platform.runLater(() -> wifiStatusLabel.setText("Connecting..."));
                        if (Main.wifiManager.connect(name, name, auth)) {
                            Main.logger.info("Connect result is true");
                            Toast.make("Connected");
                        } else {
                            Main.logger.info("Connect result is false");
                            Toast.make("Failed to connect wifi");
                        }
                    }).start();
                    Platform.runLater(() -> wifiStatusLabel.setText("Available Networks"));
                }
            });
            MenuItem desItem = new MenuItem();
            desItem.textProperty().bind(Bindings.format("Deselect"));
            desItem.setOnAction(event -> {
                int index = cell.getIndex();
                if (wifiList.getSelectionModel().getSelectedIndices().contains(index)) {
                    wifiList.getSelectionModel().clearSelection(index);
                }
            });
            contextMenu.getItems().addAll(joinItem, connectItem, desItem);
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
        infoList.setCellFactory(listView -> new InfoCell());
        infoList.setSelectionModel(new MultipleSelectionModel<Info>() {
            @Override
            public ObservableList<Integer> getSelectedIndices() {
                return FXCollections.emptyObservableList();
            }
            @Override
            public ObservableList<Info> getSelectedItems() {
                return FXCollections.emptyObservableList();
            }
            @Override
            public void selectIndices(int index, int... indices) {}
            @Override
            public void selectAll() {}
            @Override
            public void selectFirst() {}
            @Override
            public void selectLast() {}
            @Override
            public void clearAndSelect(int index) {}
            @Override
            public void select(int index) {}
            @Override
            public void select(Info obj) {}
            @Override
            public void clearSelection(int index) {}
            @Override
            public void clearSelection() {}
            @Override
            public boolean isSelected(int index) {return false;}
            @Override
            public boolean isEmpty() {return true;}
            @Override
            public void selectPrevious() {}
            @Override
            public void selectNext() {}
        });
//        ContextMenu menu = new ContextMenu();
//        MenuItem clearItem = new MenuItem("Clear");
//        clearItem.setOnAction((event) -> sentList.getItems().clear());
//        menu.getItems().add(clearItem);
//        sentList.setContextMenu(menu);
//        ContextMenu menu2 = new ContextMenu();
//        MenuItem clearItem2 = new MenuItem("Clear");
//        clearItem.setOnAction((event) -> receivedList.getItems().clear());
//        menu2.getItems().add(clearItem2);
//        receivedList.setContextMenu(menu2);
    }
}