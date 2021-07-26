package de.srsoftware.midihub.mixers;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;

public class M32C implements Mixer {
    private static final Logger LOG = LoggerFactory.getLogger(M32C.class);
    private static final int CHANNELS = 32;
    private final OSCPortOut server;
    private de.srsoftware.midihub.ui.Logger logger;

    private static final int MAIN = 0;
    private static final int BUS1 = 1;
    private static final int BUS2 = 2;
    private static final int BUS3 = 3;
    private static final int BUS4 = 4;
    private static final int BUS5 = 5;
    private static final int BUS6 = 6;
    private static final int BUS7 = 7;
    private static final int BUS8 = 8;
    private static final int BUS9 = 9;
    private static final int BUS10 =10;
    private static final int BUS11 =11;
    private static final int BUS12 =12;
    private static final int BUS13 =13;
    private static final int BUS14 =14;
    private static final int BUS15 =15;
    private static final int BUS16 =16;

    private int offset=0;
    private int bus = MAIN;
    private float [][] channels = new float[17][CHANNELS];
    private float[] gain = new float[CHANNELS];
    private float[] pan = new float[CHANNELS];
    private int lastChannel = 0;

    public M32C(String host, int port, de.srsoftware.midihub.ui.Logger logger) throws IOException {
        this.logger = logger;
        server = new OSCPortOut(InetAddress.getByName(host),port);
        logger.log("Connected to {} @ {}:{}",getClass().getSimpleName(),host,port);
        for (int i=0; i<CHANNELS;i++) pan[i] = 0.5f;
    }

    @Override
    public void disconnect() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void changeMarker(int delta) {
        unhighlightBus(bus);
        bus+=delta;
        if (bus> BUS16) bus = MAIN;
        if (bus< MAIN) bus = BUS16;
        highlightBus(bus);
    }

    public void unhighlightBus(int bus) {
        try {
            String channel = (bus < 10 ? "0" : "") + bus;
            Vector<Integer> args = new Vector<>();
            args.add(1);
            OSCMessage message = new OSCMessage("/bus/"+channel+"/config/color", args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),args);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    public void highlightBus(int bus) {
        try {
            String channel = (bus < 10 ? "0" : "") + bus;
            Vector<Integer> args = new Vector<>();
            args.add(3);
            OSCMessage message = new OSCMessage("/bus/"+channel+"/config/color", args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),args);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void changeTrack(int delta) {
        int count = Math.abs(delta);
        unhighlightFaderGroup(count);
        offset = (offset+delta);
        if (offset+delta>CHANNELS) offset=0;
        if (offset<0) offset = CHANNELS+delta;
        highlightFaderGroup(count);
    }

    public void highlightFaderGroup(int count) {
        for (int i=offset+1; i<=offset+count; i++){
            try {
                lastChannel = 0;
                String channel = (i < 10 ? "0" : "") + i;
                Vector<Integer> args = new Vector<>();
                args.add(9);
                OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
                server.send(message);
                logger.log("sent OSC: {}  : {}",message.getAddress(),args);
            } catch (IOException | OSCSerializeException e) {
                e.printStackTrace();
            }
        }
    }

    public void unhighlightFaderGroup(int count) {
        for (int i=offset+1; i<=offset+count; i++){
            try {
                String channel = (i < 10 ? "0" : "") + i;
                Vector<Integer> args = new Vector<>();
                args.add(8);
                OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
                server.send(message);
                logger.log("sent OSC: {}  : {}",message.getAddress(),args);
            } catch (IOException | OSCSerializeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleSolo(int num, boolean enabled) {
        try {
            num += offset;
            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(enabled?1:0);
            OSCMessage message = new OSCMessage("/-stat/solosw/"+channel, args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),enabled?"ON":"OFF");
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRec(int num, boolean enabled) {

    }

    @Override
    public void handleMute(int num, boolean enabled) {
        try {
            num += offset;
            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(enabled?0:1);
            OSCMessage message = new OSCMessage("/ch/"+channel+"/mix/on", args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),enabled?"ON":"OFF");
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handlePoti(int num, float percent) {
        if (bus == MAIN) handleGain(num,percent); else handlePano(num,percent);

    }

    private void handlePano(int num, float percent) {
        try {

            num += offset;

            float new_val = percent / 100;
            float old_val = pan[num-1];

            if (Math.abs(new_val - old_val)>0.02) return;
            pan[num-1] = new_val;

            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(new_val);

            String address = bus == MAIN ? "/ch/" + channel + "/preamp/trim" : "/ch/" + channel + "/mix/pan";

            OSCMessage message = new OSCMessage(address, args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",address,new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    private void handleGain(int num, float percent) {
        try {

            num += offset;

            float new_val = percent / 100;
            float old_val = gain[num-1];

            if (Math.abs(new_val - old_val)>0.02) return;
            gain[num-1] = new_val;

            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(new_val);

            String address = bus == MAIN ? "/ch/" + channel + "/preamp/trim" : "/ch/" + channel + "/mix/pan";

            OSCMessage message = new OSCMessage(address, args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",address,new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleFader(int num, float percent) {

        num += offset;
        if (num != lastChannel) {
            unhighlightChannel(lastChannel);
            highlightChannel(num);
            lastChannel = num;
        }

        float new_val = percent / 100;
        float old_val = channels[bus][num-1];

        if (Math.abs(new_val - old_val)>0.02) return;
        channels[bus][num-1] = new_val;

        String channel = (num < 10 ? "0" : "") + num;
        String mixbus = (bus < 10 ? "0" : "") + bus;

        Vector<Object> args = new Vector<>();
        args.add(new_val);

        String address = bus == MAIN ? "/ch/"+channel+"/mix/fader" : "/ch/"+channel+"/mix/"+mixbus+"/level";
        OSCMessage message = new OSCMessage(address, args);
        try {
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),args);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void highlightChannel(int num) {
        try {
            Vector<Object> args = new Vector<>();
            args.add(1);
            String channel = (num < 10 ? "0" : "") + num;
            OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),args);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unhighlightChannel(int num) {
        try {
            Vector<Object> args = new Vector<>();
            args.add(9);
            String channel = (num < 10 ? "0" : "") + num;
            OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),args);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }

    }
}
