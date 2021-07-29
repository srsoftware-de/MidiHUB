package de.srsoftware.midihub.mixers;

import java.io.IOException;

public class XR18 extends XSeries{

    public static final String MODEL = "XR18";

    public XR18(String host, int port) throws IOException {
        super(host, port, 10, 21);
    }

    @Override
    protected String gainAddress(int num) {
        return "/headamp/" + channel(num) + "/gain";
    }
}
