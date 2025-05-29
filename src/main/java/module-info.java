module com.gav.xplanetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.httpserver;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires javafx.web;
    requires org.slf4j;
    requires java.desktop;
    requires java.naming;
    requires java.net.http;
    requires flyway.core;


    opens com.gav.xplanetracker to javafx.fxml;
    exports com.gav.xplanetracker;
    exports com.gav.xplanetracker.controller;
    opens com.gav.xplanetracker.controller to javafx.fxml;
    exports com.gav.xplanetracker.scheduler;
    opens com.gav.xplanetracker.scheduler to javafx.fxml;
    opens db.migration;
    opens com.gav.xplanetracker.dto.navigraph to com.fasterxml.jackson.databind;
    exports com.gav.xplanetracker.dto.xplane to com.fasterxml.jackson.databind;
}