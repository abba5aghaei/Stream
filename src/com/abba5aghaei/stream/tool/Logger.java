package com.abba5aghaei.stream.tool;

import java.time.LocalTime;

public class Logger {

    private FileLogger fileLogger;

    public Logger() {
        fileLogger = new FileLogger(System.getProperty("user.home") + "/.stream/logs.txt");
    }

    public void error(String message) {
        if (!message.isEmpty()) {
            System.out.println(String.format("[%s] Error: ", LocalTime.now().toString()) + message);
            fileLogger.error(message);
        }
    }

    public void info(String message) {
        if (!message.isEmpty()) {
            System.out.println(String.format("[%s] Info: ", LocalTime.now().toString()) + message);
            fileLogger.info(message);
        }
    }

    public void warn(String message) {
        if (!message.isEmpty()) {
            System.out.println(String.format("[%s] Warning: ", LocalTime.now().toString()) + message);
            fileLogger.warn(message);
        }
    }
}
