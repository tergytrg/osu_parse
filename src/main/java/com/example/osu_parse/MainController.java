package com.example.osu_parse;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

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
    private CheckBox emptyColumnCheck;
    @FXML
    private CheckBox scoreTypeCheck;
    @FXML
    private CheckBox mapsOutsideOfPoolCheck;

    @FXML
    private TextArea matchLinksArea;
    @FXML
    private TextArea userListArea;
    @FXML
    private TextArea mapPoolArea;

    @FXML
    private Button pathButton;
    @FXML
    private Button helpButton;

    public void initialize() {
        this.matchParser = new MatchParser();
    }

    @FXML
    protected void onParse() {
        String matchLinks = matchLinksArea.getText();
        String userList = userListArea.getText();
        String mapPool = mapPoolArea.getText();
        boolean[] settings = {scoreCheck.isSelected(),
                            accCheck.isSelected(),
                            modsCheck.isSelected(),
                            emptyColumnCheck.isSelected(),
                            scoreTypeCheck.isSelected(),
                            mapsOutsideOfPoolCheck.isSelected()};
        String path = pathButton.getText();
        if (path.equals("Select output directory")) {
            path = getPath();
        }
        try {
            alertSucces(matchParser.parseMatch(matchLinks, userList, mapPool, path, settings));
        } catch (Exception e) {
            error(e);
        }
    }

    private void alertSucces(String info) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yay!");
        alert.setHeaderText("Parse successful!");
        alert.setContentText(info);
        alert.show();
    }

    private void error(Exception e) {
        String error = e.toString();
        System.out.println(e.getClass().toString());
        switch (e.getClass().toString()) {
            case "class java.net.MalformedURLException" -> error += "\nPlease check the match links.";
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oh no!");
        alert.setHeaderText("Sorry! I could not parse that...");
        alert.setContentText(error);
        alert.show();
    }

    @FXML
    protected void help() {
        if (helpButton.getText().equals("Sorry")) {
            helpButton.setText("Forgive me");
        } else {
            helpButton.setText("Sorry");
        }
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