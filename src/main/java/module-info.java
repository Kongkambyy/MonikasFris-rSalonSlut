module com.example.projektopgave1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.projektopgave1 to javafx.fxml;
    exports com.example.projektopgave1;
    exports com.example.projektopgave1.Controller;
    opens com.example.projektopgave1.Controller to javafx.fxml;
}