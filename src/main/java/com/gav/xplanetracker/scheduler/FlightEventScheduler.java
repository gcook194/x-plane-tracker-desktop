package com.gav.xplanetracker.scheduler;

import com.gav.xplanetracker.service.EventService;
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
                    System.out.println("X-Plane must be running to capture events");
                    return;
                }

                flightService.getCurrentFlight().ifPresentOrElse(
                        eventService::create,
                        () -> System.out.println("No flights in progress - no events created")
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stopFetching() {
        System.out.println(this.getClass().getName() + " Stopping scheduler");
        scheduler.shutdownNow();
    }

}
