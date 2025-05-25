package com.gav.xplanetracker.service;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class XPlaneService {

    private static final Logger logger = LoggerFactory.getLogger(XPlaneService.class);

    private static XPlaneService INSTANCE;

    private final OkHttpClient client;

    public XPlaneService() {
        this.client = new OkHttpClient();
    }

    public static XPlaneService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new XPlaneService();
        }
        return INSTANCE;
    }

    public boolean isSimulatorRunning(String xplaneHost) {
        final Request request = new Request.Builder()
                .url(xplaneHost)
                .build();

        try (ResponseBody body = client.newCall(request).execute().body()) {
            logger.info(body.string());
        } catch (IOException e) {
            logger.warn("Simulator is not running or may be exposing a different port - check X-Plane settings");
            return false;
        }

        logger.info("Simulator is running");
        return true;
    }
}
