package com.example.osu_parse;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainController {

    private MatchParser matchParser = null;

    @FXML
    private GridPane rootPane;

    @FXML
    private CheckBox scoreCheck;
    @FXML
    private CheckBox accCheck;
    @FXML
    private CheckBox modsCheck;
    @FXML
    private CheckBox norScoreCheck;
    @FXML
    private CheckBox scoreTypeCheck;
    @FXML
    private CheckBox totalCheck;

    @FXML
    private TextArea matchLinksArea;
    @FXML
    private TextArea userListArea;
    @FXML
    private TextArea mapPoolArea;

    @FXML
    private Button pathButton;

    public void initialize() {
        this.matchParser = new MatchParser();
    }

    @FXML
    protected void onParse() {
        String matchLinks = matchLinksArea.getText();
        String userList = userListArea.getText();
        String mapPool = mapPoolArea.getText();
        Boolean[] settings = {scoreCheck.isSelected(),
                            accCheck.isSelected(),
                            modsCheck.isSelected(),
                            norScoreCheck.isSelected(),
                            scoreTypeCheck.isSelected()};
        String path = pathButton.getText();
        if (path.equals("Select output directory")) {
            path = getPath();
        }
        try {
            alertParseInfo(matchParser.parseMatch(matchLinks, userList, mapPool, path, settings));
        } catch (Exception e) {
            e.printStackTrace();
            error(e.getMessage());
        }
    }

    private void alertParseInfo(String info) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yay!");
        alert.setHeaderText("Parse successful!");
        alert.setContentText(info);
        alert.show();
    }

    private void error(String info) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oh no!");
        alert.setHeaderText("I'm sorry, I could not parse that");
        alert.setContentText(info);
        alert.show();
    }

    @FXML
    protected void help() {
    }

    @FXML
    protected void selectPath() {
        pathButton.setText(getPath());
    }

    private String getPath() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please select output directory");
        File selectedDirectory = directoryChooser.showDialog(stage);
        return selectedDirectory.getAbsolutePath();
    }
}