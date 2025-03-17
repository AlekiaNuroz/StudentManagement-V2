module com.example.studentmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires java.desktop;

    opens com.example.studentmanagementsystem to javafx.fxml;
    exports com.example.studentmanagementsystem;
}