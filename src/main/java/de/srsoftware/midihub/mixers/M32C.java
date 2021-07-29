package de.srsoftware.midihub.mixers;

import java.io.IOException;

public class M32C extends XSeries {
    public static final String MODEL = "M32C";

    public M32C(String host, int port) throws IOException {
        super(host, port, 16, 32);
    }

    @Override
    protected String gainAddress(int num) {
        return "/ch/" + channel(num) + "/preamp/trim";
    }
}
