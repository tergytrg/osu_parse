package com.example.osu_parse;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainController {

    @FXML
    private GridPane rootPane;

    public void initialize() {

    }

    @FXML
    protected void onParse() {
    }

    @FXML
    protected void selectPath() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please select output folder");
        File selectedDirectory = directoryChooser.showDialog(stage);
    }
}