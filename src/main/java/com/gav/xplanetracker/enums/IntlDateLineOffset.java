package com.gav.xplanetracker.enums;

public enum IntlDateLineOffset {

    NONE(0), // hasn't crossed the international date line
    WESTBOUND(-360), // crossed the IDL going east to west
    EASTBOUND(360); // crossed the IDL going west to east

    final int offset;

    IntlDateLineOffset(int offset) {
        this.offset = offset;
    }

    public final int getOffset() {
        return this.offset;
    }
}
