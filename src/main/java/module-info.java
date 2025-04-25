module com.example.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;
    requires java.mail;
    requires jbcrypt;
    opens com.example.pidev to javafx.fxml;
    exports com.example.pidev;
}