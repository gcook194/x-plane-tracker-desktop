package com.gav.xplanetracker.service;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

public class XPlaneService {

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

    public boolean isSimulatorRunning() {
        try {
            final Request request = new Request.Builder()
                    .url("http://localhost:8086/api/v2/datarefs")
                    .build();

            client.newCall(request).execute();
        } catch (IOException e) {
            System.out.println("Simulator is not running or may be exposing a different port - check X-Plane settings");
            return false;
        }

        System.out.println("Simulator is running");
        return true;
    }
}
