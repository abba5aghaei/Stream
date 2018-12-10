package com.abba5aghaei.stream.aspect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.abba5aghaei.stream.Main;

public class FileManager {

    public static String DIRECTORY = "";
    private static FileInputStream input;
    private static FileOutputStream output;

    public static void initialize() {
        try {
            if(DIRECTORY.length()==0) {
                switch (OSCheck.getOperatingSystemType()) {
                    case Windows:
                        DIRECTORY = System.getProperty("user.home") + "\\Downloads\\Stream";
                        break;
                    case MacOS:
                        DIRECTORY = System.getProperty("user.home") + "/Stream";
                        break;
                    case Linux:
                        DIRECTORY = System.getProperty("user.home") + "/Downloads/Stream";
                        break;
                    default:
                        DIRECTORY = System.getProperty("user.home") + "/Stream";
                        break;
                }
            }
            File dir = new File(DIRECTORY);
            if (dir.exists()) {
                if (dir.canWrite()) {
                    if (!dir.isDirectory())
                        throw new Exception("On creating inbox directoy, please set the inbox manully");
                } else
                    throw new Exception("Directory is unWritable");
            } else {
                dir.mkdir();
            }
        } catch (Exception e) {
            Main.logger.error("FileManager: " + e.getMessage());
        }
    }

    public static ArrayList<String> getFilesList() {
        File[] files = new File(DIRECTORY).listFiles();
        ArrayList<String> names = new ArrayList<String>();
        for(File file : files)
            names.add(file.getName());
        return names;
    }

    public static void initWriter(String name) {
        try {
            File file = new File(DIRECTORY + "/" + name);
            try {
	            short i = 2;
	            String onlyName = file.getName();
	            int index = onlyName.lastIndexOf(".");
	            String format = "";
	            if(index>0) {
	            	format = onlyName.substring(index);
	            	onlyName = onlyName.substring(0, index);
	            }
	            while(true) {
		            if(file.exists()) {
		            	file = new File(DIRECTORY + "/" + onlyName + "_" + i + format);
		            	i++;
		            }
		            else {
		            	break;
		            }
	            }
            }
            catch (Exception e) {
                Main.logger.error("On FileManager.initWriter: " + e.getMessage());
			}
            file.createNewFile();
            output = new FileOutputStream(file);
        } catch (IOException e) {
            Main.logger.error("On FileManager.initWriter(2): " + e.getMessage());
        }
    }

    public static void initReader(File file) {
        try {
            input = new FileInputStream(file);
        } catch (IOException e) {
            Main.logger.error("On FileManager.initReader: " + e.getMessage());
        }
    }

    public static void write(byte[] data, int byteRead) {
        try {
            if (byteRead > 0)
                output.write(data, 0, byteRead);
            else {
                output.flush();
                output.close();
            }
        } catch (IOException e) {
            Main.logger.error("On FileManager.write: " + e.getMessage());
        }
    }

    public static int read(byte[] data) {
        int byteReaded = 0;
        try {
            byteReaded = input.read(data, 0, Main.BUFFER_SIZE);
        } catch (IOException e) {
            Main.logger.error("On FileManager.read: " + e.getMessage());
        }
        return byteReaded;
    }

    public static void finiReader() {
        try {
            if (input != null)
                input.close();

        } catch (IOException e) {
            Main.logger.error("On FileManager.finiReader: " + e.getMessage());
        }
    }
}