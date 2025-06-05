package com.gav.xplanetracker.scheduler;

import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import com.gav.xplanetracker.service.FlightService;
import com.gav.xplanetracker.service.ScreenshotService;
import com.gav.xplanetracker.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenshotScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotScheduler.class);

    private static ScreenshotScheduler INSTANCE;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SettingsService settingsService;
    private final FlightService flightService;
    private final ScreenshotService screenshotService;

    public ScreenshotScheduler() {
        this.settingsService = SettingsService.getInstance();
        this.flightService = FlightService.getInstance();
        this.screenshotService = ScreenshotService.getInstance();
    }

    public static ScreenshotScheduler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScreenshotScheduler();
        }
        return INSTANCE;
    }

    public void startCopyingScreenshots() {
        final ApplicationSettingsDTO settings = settingsService.getSettings();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!settings.monitorScreenshots()) {
                    logger.debug("Screenshot monitoring is switched off");
                    return;
                }

                flightService.getActiveFlight().ifPresent(flight -> {
                    logger.info("Checking for new screenshots to add to flight {} folder", flight.getId());
                    screenshotService.copyNewScreenshots(flight, settings.xPlaneScreenshotDirectory());
                });

            } catch (Exception e) {
                logger.error("Error when executing scheduled task: ", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stopCopyingScreenshots() {
        logger.info("Stopping scheduler");
        scheduler.shutdownNow();
    }
}
