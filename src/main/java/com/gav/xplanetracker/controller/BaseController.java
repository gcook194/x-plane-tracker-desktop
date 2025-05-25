package com.gav.xplanetracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @FXML
    private StackPane mainContent;

    @FXML
    private Button settingsButton;

    @FXML
    private Button dashboardButton;

    @FXML
    public void initialize() {
        settingsButton.setOnAction(event -> loadView("/com/gav/xplanetracker/settings-view.fxml"));
        dashboardButton.setOnAction(event -> loadView("/com/gav/xplanetracker/start-flight-view.fxml"));

        loadView("/com/gav/xplanetracker/start-flight-view.fxml");
    }

    private void loadView(final String view) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
            final Parent settingsRoot = loader.load();

            mainContent.getChildren().setAll(settingsRoot); // Replace current content
        } catch (IOException e) {
            logger.error("Error when switching views: ", e);
        }
    }
}
