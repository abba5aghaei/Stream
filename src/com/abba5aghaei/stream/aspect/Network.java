package com.abba5aghaei.stream.aspect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import com.abba5aghaei.stream.Main;
import com.abba5aghaei.stream.tool.Toast;

public class Network {

    public static String HOST = "127.0.0.1";
    public static int PORT = 5200;
    private static ServerSocket welcomeSocket;
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static Socket transferSocket;
    private static DataInputStream input;
    private static DataOutputStream output;
    private static DataInputStream fileInput;
    private static DataOutputStream fileOutput;
    private static Thread thread;
    private static Thread receiveThread;
    private static boolean running;

    public static void initialize() {
        String ip = "";
        for(String item : getIPs()) {
            try {
                Integer.parseInt(item.substring(0, item.indexOf(".")));
                ip = item;
            } catch (Exception e) { continue; }
        }
        if(ip.length()!=0)
            HOST = ip;
    }

    public static void createServer() {
        new Thread(() -> {
            try {
                welcomeSocket = new ServerSocket(PORT);
                welcomeSocket.setSoTimeout(Integer.MAX_VALUE);
                socket = welcomeSocket.accept();
                Main.setConnected();
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                serverSocket = new ServerSocket(52001);
                serverSocket.setSoTimeout(Integer.MAX_VALUE);
                transferSocket = serverSocket.accept();
                fileInput = new DataInputStream(transferSocket.getInputStream());
                fileOutput = new DataOutputStream(transferSocket.getOutputStream());
                thread = new Thread(Network::listen);
                thread.start();
            } catch (Exception e) {
                Toast.make("Connection closed");
                disconnect();
                Main.logger.error("On Network.createServer: " + e.getMessage());
            }
        }).start();
    }

    public static void joinServer() {
        new Thread(() -> {
            try {
                running = true;
            	while(running) {
            		socket = new Socket(HOST, PORT);
            		if(socket==null) {
            			try {Thread.sleep(1000);}catch (InterruptedException e) {}
            		}
            		else break;
            	}
                Main.connected = true;
                Main.setConnected();
                socket.setSoTimeout(Integer.MAX_VALUE);
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                transferSocket = new Socket(HOST, 52001);
                transferSocket.setSoTimeout(Integer.MAX_VALUE);
                fileInput = new DataInputStream(transferSocket.getInputStream());
                fileOutput = new DataOutputStream(transferSocket.getOutputStream());
                thread = new Thread(Network::listen);
                thread.start();
            } catch (Exception e) {
                Toast.make("Connection closed");
                disconnect();
                Main.logger.error("On Network.joinServer: " + e.getMessage());
            }
        }).start();
    }

    private static void listen() {
        String line = "";
        try {
            while (socket != null) {
                line = input.readUTF();
                if (line.equals("1")) {
                    receiveThread = new Thread(Network::receiveFile);
                    receiveThread.start();
                }
                else if (line.equals("2"))
                    receiveList();
                else
                    Main.logger.error(line);
            }
        } catch (Exception e) {
            disconnect();
            Toast.make("Target Device Exited");
        }
    }

    public static boolean send(File file, int index) {
        FileManager.initReader(file);
        Main.sentStatus(index, "sending...");
        long size = file.length();
        byte[] data = new byte[Main.BUFFER_SIZE];
        long read = 0;
        int byteRead = 0;
        try {
            while (read < size) {
                byteRead = FileManager.read(data);
                fileOutput.write(data, 0, byteRead);
                fileOutput.flush();
                read += byteRead;
                Main.sentProgress(index, (double) read / size);
            }
        } catch (Exception e) {
            Main.logger.error("On Network.send(file): " + e.getMessage());
            return false;
        }
        FileManager.finiReader();
        Main.sentStatus(index, "sent");
        return true;
    }

    public static void send(String msg, boolean s) {
        try {
            if(s) {
                fileOutput.writeUTF(msg);
            }
            else {
                output.writeUTF(msg);
            }
        } catch (Exception e) {
            Main.logger.error("On Network.send(msg): " + e.getMessage());
        }
    }

    private static void receiveFile() {
        String name = "";
        try {
            try {
                name = fileInput.readUTF();
            }
            catch (Exception e) {
                name = fileInput.readUTF();
            }
            int index = Integer.parseInt(fileInput.readUTF());
            Main.receivedStatus(index, "receiving...");
            FileManager.initWriter(name);
            long size = Long.parseLong(fileInput.readUTF());
            byte[] data = new byte[Main.BUFFER_SIZE];
            long wrote = 0;
            int byteRead = 0;
            while (wrote < size) {
                byteRead = fileInput.read(data, 0, Main.BUFFER_SIZE);
                FileManager.write(data, byteRead);
                wrote += byteRead;
                Main.receivedProgress(index, (double) wrote / size);
            }
            FileManager.write(data, -1);
            Main.receivedStatus(index, "received");
        } catch (Exception e) {
            Main.logger.error("On Network.receiveFile: " + e.getMessage());
        }
    }

    private static void receiveList() {
        try {
            String name = input.readUTF();
            String size = input.readUTF();
            Main.addReceivedList(name, Long.parseLong(size));
        } catch (Exception e) {
            Main.logger.error("On Network.receiveList: " + e.getMessage());
        }
    }

    public static void disconnect() {
    	running = false;
        Main.connected = false;
        try {
            if (socket != null)
                if (!socket.isConnected()) {
                    socket.getOutputStream().close();
                    socket.getInputStream().close();
                    socket.close();
                    socket = null;
                }
        } catch (Exception e) {
            Main.logger.error("On Network.disconnect: " + e.getMessage());
        }
        try {
            if (transferSocket != null)
                if (!transferSocket.isConnected()) {
                    transferSocket.getOutputStream().close();
                    transferSocket.getInputStream().close();
                    transferSocket.close();
                    transferSocket = null;
                }
        } catch (Exception e) {
            Main.logger.error("On Network.disconnect(-1): " + e.getMessage());
        }
        try {
            if (serverSocket != null)
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                    serverSocket = null;
                }
        } catch (Exception e) {
            Main.logger.error("On Network.disconnect(-2): " + e.getMessage());
        }
        try {
            if (fileInput != null) {
                fileInput.close();
                fileInput = null;
            }
        } catch (Exception ex) {}
        try {
            if (fileOutput != null) {
                fileOutput.close();
                fileOutput = null;
            }
        } catch (Exception exc) {}
        try {
            if (input != null) {
                input.close();
                input = null;
            }
        } catch (Exception ex) {}
        try {
            if (output != null) {
                output.close();
                output = null;
            }
        } catch (Exception exc) {}
        try {
            if (welcomeSocket != null)
                if (!welcomeSocket.isClosed()) {
                    welcomeSocket.close();
                    welcomeSocket = null;
                }
        } catch (Exception e) {
            Main.logger.error("On Network.disconnect(2): " + e.getMessage());
        }
        try {
            if(receiveThread!=null) {
                receiveThread.interrupt();
                receiveThread = null;
            }
        }
        catch (Exception e) {
            Main.logger.error("On Network.disconnect(-3): " + e.getMessage());
        }
        try {
        	if(thread!=null) {
	            thread.interrupt();
	            thread = null;
        	}
        }
        catch (Exception e) {
            Main.logger.error("On Network.disconnect(3): " + e.getMessage());
        }
        Main.setDisconnected();
    }

    public static ArrayList<String> getIPs() {
        ArrayList<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.getHostAddress().matches("\\d*.\\d*.\\d*.\\d*")) {
                        ips.add(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            Main.logger.error("On Network.getIPs: " + e.getMessage());
        }
        return ips;
    }
}
