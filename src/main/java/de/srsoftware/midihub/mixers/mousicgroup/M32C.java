package de.srsoftware.midihub.mixers.mousicgroup;

import com.illposed.osc.OSCMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Vector;

public class M32C extends XSeries {
    private static final Logger LOG = LoggerFactory.getLogger(M32C.class);
    public static final String MODEL = "M32C";

    public M32C(String host, int port) throws IOException {
        super(host, port, 16+1, 32);
    }

    @Override
    protected String gainAddress(int num) {
        return "/ch/" + channel(num) + "/preamp/trim";
    }

    void processChannelMessage(int channel, Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        switch (head) {
            case "config":
                processChannelConfig(channel,address,msg);
                break;
            case "mix":
                processChannelBusMessage(channel,address,msg);
                break;
            case "preamp":
                processPreamp(channel,address,msg);
                break;
            default:
                LOG.info("Unknown path {} / {}", head, address);
        }
    }

    private void processPreamp(int channel, Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        switch (head){
            case "trim":
                processGain(channel,msg);
                break;
            default:
                LOG.info("Unknown path {} / {}", head, address);
        }
    }
}
