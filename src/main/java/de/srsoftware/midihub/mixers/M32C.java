package de.srsoftware.midihub.mixers;

import com.illposed.osc.*;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import de.srsoftware.midihub.WatchDog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Vector;

public class M32C implements Mixer {
    private static final Logger LOG = LoggerFactory.getLogger(M32C.class);
    private static final int CHANNELS = 32;
    private final OSCPortOut sink;
    private final OSCPortIn source;
    private de.srsoftware.midihub.ui.Logger logger;

    private static final int MAIN = 0;

    private int offset=0;
    private int bus = MAIN;
    private int lastChannel = 0;

    public M32C(String host, int port, de.srsoftware.midihub.ui.Logger logger) throws IOException {
        this.logger = logger;
        InetSocketAddress socket = new InetSocketAddress(host, port);
        SocketAddress local = new InetSocketAddress(port);
        sink = new OSCPortOut(new OSCSerializerAndParserBuilder(),socket,local);
        source = new OSCPortIn(local);
        source.startListening();
        logger.log("Connected to {} @ {}:{}",getClass().getSimpleName(),host,port);

    }

    @Override
    public boolean getMute(int num) {
        try {
            num+=offset;
            String channel = (num < 10 ? "0" : "") + num;
            String address = "/ch/" + channel + "/mix/on";


            WatchDog wd = new WatchDog(source, address);
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            logger.log("OSC.sent: {} {}",message.getAddress(),message.getArguments());
            List<Object> result = wd.getResult();
            logger.log("Received {} from console",result);
            if (result == null || result.isEmpty()) return false;
            return result.get(0).equals(0);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean getSolo(int num) {
        try {
            num+=offset;
            String channel = (num < 10 ? "0" : "") + num;
            String address = "/-stat/solosw/"+channel;
            OSCMessage message = new OSCMessage(address, List.of());

            WatchDog wd = new WatchDog(source, address);
            sink.send(message);
            logger.log("OSC.sent: {} {}",message.getAddress(),message.getArguments());
            List<Object> result = wd.getResult();
            logger.log("Received {} from console",result);
            if (result == null || result.isEmpty()) return false;
            return result.get(0).equals(1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void disconnect() {
        try {
            sink.close();
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
        if (bus> 16) bus = 0;
        if (bus< 0) bus = 16;
        highlightBus(bus);
    }

    public void unhighlightBus(int bus) {
        try {
            String channel = (bus < 10 ? "0" : "") + bus;
            Vector<Integer> args = new Vector<>();
            args.add(1);
            OSCMessage message = new OSCMessage("/bus/"+channel+"/config/color", args);
            sink.send(message);
            logger.log("OSC.send: {} {}",message.getAddress(),1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop(int val){

    }


    public void highlightBus(int bus) {
        try {
            String channel = (bus < 10 ? "0" : "") + bus;
            Vector<Integer> args = new Vector<>();
            args.add(3);
            OSCMessage message = new OSCMessage("/bus/"+channel+"/config/color", args);
            sink.send(message);
            logger.log("OSC.send: {} {}",message.getAddress(),3);
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
                args.add(1);
                OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
                sink.send(message);
                logger.log("OSC.send: {} {}",message.getAddress(),9);
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
                sink.send(message);
                logger.log("OSC.send: {} {}",message.getAddress(),8);
            } catch (IOException | OSCSerializeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean handleSolo(int num, boolean enabled) {
        try {
            num += offset;
            if (num != lastChannel) {
                unhighlightChannel(lastChannel);
                highlightChannel(num);
                lastChannel = num;
            }
            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(enabled?1:0);
            OSCMessage message = new OSCMessage("/-stat/solosw/"+channel, args);
            sink.send(message);
            logger.log("OSC.send: {} {}",message.getAddress(),enabled?"ON":"OFF");
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return enabled;
    }

    @Override
    public boolean handleRec(int num, boolean enabled) {
        return false;
    }

    @Override
    public void handleSet() {
        changeMarker(-bus);
    }

    @Override
    public boolean handleMute(int num, boolean enabled) {
        try {
            num += offset;
            if (num != lastChannel) {
                unhighlightChannel(lastChannel);
                highlightChannel(num);
                lastChannel = num;
            }
            String channel = (num < 10 ? "0" : "") + num;
            enabled = !getMute(num);
            Vector<Object> args = new Vector<>();
            args.add(enabled?0:1);
            OSCMessage message = new OSCMessage("/ch/"+channel+"/mix/on", args);
            sink.send(message);
            logger.log("OSC.send: {} {}",message.getAddress(),enabled?"ON":"OFF");
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return enabled;
    }

    @Override
    public void handlePoti(int num, float percent) {
        if (bus == MAIN) handleGain(num,percent); else handlePano(num,percent);

    }

    private void handlePano(int num, float percent) {
        try {

            num += offset;
            if (num != lastChannel) {
                unhighlightChannel(lastChannel);
                highlightChannel(num);
                lastChannel = num;
            }

            float new_val = percent / 100;
            float old_val = getPano(num);

            if (Math.abs(new_val - old_val)>0.02) return;

            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(new_val);

            String address = "/ch/" + channel + "/mix/pan";

            OSCMessage message = new OSCMessage(address, args);
            sink.send(message);
            logger.log("OSC.send: {} {}",address,new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    private float getPano(int num) {
        try {
            String channel = (num < 10 ? "0" : "") + num;
            String mixbus = (bus < 10 ? "0" : "") + bus;
            String address = "/ch/" + channel + "/mix/pan";

            WatchDog wd = new WatchDog(source, address);
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            logger.log("OSC.sent: {} {}", message.getAddress(), message.getArguments());
            List<Object> result = wd.getResult();
            if (result == null || result.isEmpty()) return 0f;
            Object val = result.get(0);
            return val instanceof Float ? (Float) val : 0f;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    private void handleGain(int num, float percent) {
        try {

            num += offset;
            if (num != lastChannel) {
                unhighlightChannel(lastChannel);
                highlightChannel(num);
                lastChannel = num;
            }

            float new_val = percent / 100;
            float old_val = getGain(num);

            if (Math.abs(new_val - old_val)>0.02) return;

            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(new_val);

            String address = "/ch/" + channel + "/preamp/trim" ;

            OSCMessage message = new OSCMessage(address, args);
            sink.send(message);
            logger.log("OSC.send: {} {}",address,new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    private float getGain(int num) {
        try {
            String channel = (num < 10 ? "0" : "") + num;
            String mixbus = (bus < 10 ? "0" : "") + bus;
            String address = "/ch/" + channel + "/preamp/trim" ;

            WatchDog wd = new WatchDog(source, address);
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            logger.log("OSC.sent: {} {}", message.getAddress(), message.getArguments());
            List<Object> result = wd.getResult();
            if (result == null || result.isEmpty()) return 0f;
            Object val = result.get(0);
            return val instanceof Float ? (Float) val : 0f;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
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
        float old_val = getFader(bus,num);

        if (Math.abs(new_val - old_val)>0.02) return;

        String channel = (num < 10 ? "0" : "") + num;
        String mixbus = (bus < 10 ? "0" : "") + bus;

        Vector<Object> args = new Vector<>();
        args.add(new_val);

        String address = bus == MAIN ? "/ch/"+channel+"/mix/fader" : "/ch/"+channel+"/mix/"+mixbus+"/level";
        OSCMessage message = new OSCMessage(address, args);
        try {
            sink.send(message);
            logger.log("OSC.send: {} {}",address,new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    private float getFader(int bus, int num) {
        try {
            String channel = (num < 10 ? "0" : "") + num;
            String mixbus = (bus < 10 ? "0" : "") + bus;
            String address = bus == MAIN ? "/ch/" + channel + "/mix/fader" : "/ch/" + channel + "/mix/" + mixbus + "/level";

            WatchDog wd = new WatchDog(source, address);
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            logger.log("OSC.sent: {} {}", message.getAddress(), message.getArguments());
            List<Object> result = wd.getResult();
            if (result == null || result.isEmpty()) return 0f;
            Object val = result.get(0);
            return val instanceof Float ? (Float) val : 0f;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    @Override
    public void highlightChannel(int num) {
        try {
            Vector<Object> args = new Vector<>();
            args.add(9);
            String channel = (num < 10 ? "0" : "") + num;
            OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
            sink.send(message);
            logger.log("OSC.send: {} {}",message.getAddress(),1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unhighlightChannel(int num) {
        try {
            Vector<Object> args = new Vector<>();
            args.add(1);
            String channel = (num < 10 ? "0" : "") + num;
            OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
            sink.send(message);
            logger.log("OSC.send: {} {}",message.getAddress(),9);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }

    }
}
