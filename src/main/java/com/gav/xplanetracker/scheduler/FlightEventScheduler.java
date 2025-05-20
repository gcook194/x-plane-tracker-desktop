package com.gav.xplanetracker.scheduler;

import com.gav.xplanetracker.service.FlightService;
import com.gav.xplanetracker.service.XPlaneService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightEventScheduler {

    private static FlightEventScheduler INSTANCE;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final FlightService flightService;
    private final XPlaneService xPlaneService;

    public FlightEventScheduler() {
        this.flightService = FlightService.getInstance();
        this.xPlaneService = XPlaneService.getInstance();
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
                    System.out.println("X-Plane must be running to capture events");
                    return;
                }

//                flightService.getCurrentFlight().ifPresentOrElse(
//                        eventService::create,
//                        () -> System.out.println("No flights in progress - no events created")
//                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stopFetching() {
        scheduler.shutdownNow();
    }

}
