module com.example.osu_parse {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.osu_parse to javafx.fxml;
    exports com.example.osu_parse;
}