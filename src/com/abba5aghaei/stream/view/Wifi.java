package com.abba5aghaei.stream.view;

public class Wifi {

    private short signal;
    private String name;

    public Wifi(String name, String signal) {
        this.name = name;
        try {
            this.signal = Short.parseShort(signal);
        }
        catch (Exception e) {
            this.signal = 0;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getSignal() {
        return signal;
    }

    public void setSignal(short signal) {
        this.signal = signal;
    }
}
