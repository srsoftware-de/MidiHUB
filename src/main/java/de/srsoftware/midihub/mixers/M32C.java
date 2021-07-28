package de.srsoftware.midihub.mixers;

import com.illposed.osc.*;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import de.srsoftware.midihub.WatchDog;
import de.srsoftware.midihub.ui.LogList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Vector;

public class M32C extends XSeries {
    public M32C(String host, int port) throws IOException {
        super(host, port, 16, 32);
    }

    @Override
    protected String gainAddress(int num) {
        return "/ch/" + channel(num) + "/preamp/trim";
    }
}
