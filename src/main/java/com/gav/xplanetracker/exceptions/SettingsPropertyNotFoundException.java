package com.gav.xplanetracker.exceptions;

public class SettingsPropertyNotFoundException extends RuntimeException {
    public SettingsPropertyNotFoundException(String message) {
        super(message);
    }
}
