package com.abba5aghaei.stream.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalTime;

public class FileLogger {

    private String fileName;
    private File file;

    public FileLogger(String fileName) {
        this.fileName = fileName;
        this.file = new File(fileName);
    }

    public void error(String message) {
        write(message, "error");
    }

    public void info(String message) {
        write(message, "info");
    }

    public void warn(String message) {
        write(message, "warn");
    }

    private void write(String message, String level) {
        try {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdir()) {
                    throw new Exception("Can't create log file");
                }
            }
            if (!file.exists() || !file.isFile()) {
                if (!file.createNewFile()) {
                    throw new Exception("Can't create log file");
                }
            }
            if (!file.canWrite()) {
                throw new Exception("Can't write in log file");
            }
            PrintWriter writer = new PrintWriter(new FileWriter(file, true));
            writer.println(String.format("[%s] %s: ", LocalTime.now().toString(), level) + message);
            writer.flush();
            try {
                writer.close();
            }
            catch (Exception e) {
                System.out.println("Exception in closing log com.abba5aghaei.stream: " + e.getMessage());
            }
        }
        catch (Exception e) {
            System.out.println("Exception in logging: " + e.getMessage());
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.file = new File(fileName);
    }
}
