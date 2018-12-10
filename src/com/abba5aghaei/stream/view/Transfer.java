package com.abba5aghaei.stream.view;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class Transfer {

    public AnchorPane pane;
    private Label status;
    private Label name;
    private Label size;
    private ProgressBar progress;
    private ProgressIndicator indicator;

    public Transfer(String name, long size) {
        try {
            pane = FXMLLoader.load(getClass().getResource("transfer.fxml"));
            ObservableList<Node> children = pane.getChildren();
            this.progress = (ProgressBar) children.get(0);
            this.size = (Label) children.get(1);
            this.name = (Label) children.get(2);
            this.status = (Label) children.get(3);
            this.indicator = (ProgressIndicator) children.get(4);
            setName(name);
            setSize(size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setSize(long size) {
    	String showable = "";
    	if(size < 1024) {
    		showable = size + " byte";
    	}
    	else if(size < 1048576) {
    		showable = String.format("%.1f KB", (double) size / 1024);
    	}
    	else if(size < 1073741824) {
    		showable = String.format("%.1f MB", (double) size / 1048576);
    	}
    	else {
    		showable = String.format("%.1f GB", (double) size / 1073741824);
    	}
        this.size.setText(showable);
    }

    public void setStatus(String status) {
        this.status.setText(status);
    }

    public void setProgress(double progress) {
        this.progress.setProgress(progress);
    }

    public void setIndicator(double indicator) {
        this.indicator.setProgress(indicator);
    }
}
