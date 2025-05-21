module com.gav.xplanetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.httpserver;
    requires okhttp;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires flyway.core;
    requires javafx.web;


    opens com.gav.xplanetracker to javafx.fxml;
    exports com.gav.xplanetracker;
    exports com.gav.xplanetracker.controller;
    opens com.gav.xplanetracker.controller to javafx.fxml;
    exports com.gav.xplanetracker.scheduler;
    opens com.gav.xplanetracker.scheduler to javafx.fxml;
    opens db.migration;
}