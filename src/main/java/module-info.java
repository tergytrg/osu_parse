module com.example.osu_parse {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires org.json.simple;


    opens com.example.osu_parse to javafx.fxml;
    exports com.example.osu_parse;
}