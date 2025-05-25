package com.gav.xplanetracker.scheduler;

import com.gav.xplanetracker.service.EventService;
import com.gav.xplanetracker.service.FlightService;
import com.gav.xplanetracker.service.XPlaneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightEventScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FlightEventScheduler.class);

    private static FlightEventScheduler INSTANCE;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final FlightService flightService;
    private final XPlaneService xPlaneService;
    private final EventService eventService;

    public FlightEventScheduler() {
        this.flightService = FlightService.getInstance();
        this.xPlaneService = XPlaneService.getInstance();
        this.eventService = EventService.getInstance();
    }

    public static FlightEventScheduler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlightEventScheduler();
        }
        return INSTANCE;
    }

    public void startFetching() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!xPlaneService.isSimulatorRunning()) {
                    logger.info("X-Plane must be running to capture events");
                    return;
                }

                flightService.getCurrentFlight().ifPresentOrElse(
                        eventService::create,
                        () -> logger.info("No flights in progress - no events created")
                );

            } catch (Exception e) {
                logger.error("Error when executing scheduled task: ", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stopFetching() {
        logger.info("Stopping scheduler");
        scheduler.shutdownNow();
    }

}
