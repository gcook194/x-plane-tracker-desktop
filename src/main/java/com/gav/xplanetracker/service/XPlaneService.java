package com.gav.xplanetracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class XPlaneService {

    private static final Logger logger = LoggerFactory.getLogger(XPlaneService.class);

    private static XPlaneService INSTANCE;

    private final HttpClient client;

    public XPlaneService() {
        this.client = HttpClient.newHttpClient();
    }

    public static XPlaneService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new XPlaneService();
        }
        return INSTANCE;
    }

    public boolean isSimulatorRunning(String xplaneHost) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xplaneHost))
                .GET()
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            logger.warn("Simulator is not running or may be exposing a different port - check X-Plane settings");
            return false;
        }

        logger.info("Simulator is running");
        return true;
    }
}
