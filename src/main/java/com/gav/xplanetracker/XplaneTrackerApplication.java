package com.gav.xplanetracker;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.database.DatabaseMigration;
import com.gav.xplanetracker.scheduler.FlightEventScheduler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class XplaneTrackerApplication extends Application {

    private final FlightEventScheduler scheduler;

    public XplaneTrackerApplication() {
        this.scheduler = FlightEventScheduler.getInstance();
    }

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseConnection.setupDatabase();

        final Path databasePath = DatabaseConnection.getDatabasePath();
        DatabaseMigration.runMigrations(databasePath.toString());

        FXMLLoader fxmlLoader = new FXMLLoader(XplaneTrackerApplication.class.getResource("base-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("X-Plane Flight Tracker");
        stage.setScene(scene);
        stage.show();

        scheduler.startFetching();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        scheduler.stopFetching();
    }

    public static void main(String[] args) {
        launch();
    }
}