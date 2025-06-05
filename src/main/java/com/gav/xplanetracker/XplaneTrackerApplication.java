package com.gav.xplanetracker;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.database.DatabaseMigration;
import com.gav.xplanetracker.scheduler.FlightEventScheduler;
import com.gav.xplanetracker.scheduler.ScreenshotScheduler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class XplaneTrackerApplication extends Application {

    private final FlightEventScheduler flightEventScheduler;
    private final ScreenshotScheduler screenshotScheduler;

    public XplaneTrackerApplication() {
        this.flightEventScheduler = FlightEventScheduler.getInstance();
        this.screenshotScheduler = ScreenshotScheduler.getInstance();
    }

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseConnection.setupDatabase();

        final Path databasePath = DatabaseConnection.getDatabasePath();
        DatabaseMigration.runMigrations(databasePath.toString());

        // TODO create directories where required
        // screenshot directory

        FXMLLoader fxmlLoader = new FXMLLoader(XplaneTrackerApplication.class.getResource("base-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("X-Plane Flight Tracker");
        stage.setScene(scene);
        stage.show();

        flightEventScheduler.startFetching();
        screenshotScheduler.startCopyingScreenshots();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        flightEventScheduler.stopFetching();
        screenshotScheduler.stopCopyingScreenshots();
    }

    public static void main(String[] args) {
        launch();
    }
}