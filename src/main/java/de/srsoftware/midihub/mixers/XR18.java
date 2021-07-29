package de.srsoftware.midihub.mixers;

import com.illposed.osc.OSCMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Vector;

public class XR18 extends XSeries{
    private static final Logger LOG = LoggerFactory.getLogger(XR18.class);
    public static final String MODEL = "XR18";

    public XR18(String host, int port) throws IOException {
        super(host, port, 10+1, 21);
    }

    @Override
    protected String gainAddress(int num) {
        return "/headamp/" + channel(num) + "/gain";
    }

    void processChannelMessage(int channel, Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        switch (head) {
            case "mix":
                processChannelBusMessage(channel,address,msg);
                break;
            default:
                LOG.info("Unknown path {} / {}", head, address);
        }
    }
}
