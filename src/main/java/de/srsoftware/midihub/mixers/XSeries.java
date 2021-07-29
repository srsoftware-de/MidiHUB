package de.srsoftware.midihub.mixers;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import de.srsoftware.midihub.ui.LogList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public abstract class XSeries extends AbstractMixer {
    private static final Logger LOG = LoggerFactory.getLogger(XSeries.class);
    private static final float TRAP = 0.03f;
    private static final int MAIN = 0;


    private int offset = 0;
    private int bus = MAIN;
    private int lastChannel = 0;

    public XSeries(String host, int port, int buses, int channels) throws IOException {
        super(host, port, buses, channels);
        requestFaders();
    }


    public void changeMarker(int delta) {
        unhighlightBus(bus);
        bus += delta;
        if (bus > buses) bus = 0;
        if (bus < 0) bus = buses;
        highlightBus(bus);
    }


    @Override
    public void changeTrack(int delta) {
        int count = Math.abs(delta);
        unhighlightFaderGroup(count);
        offset = (offset + delta);
        if (offset + delta > channels) offset = 0;
        if (offset < 0) offset = channels + delta;
        highlightFaderGroup(count);
    }

    String channel(int num){
        return (num < 10 ? "0" : "")+num;
    }

    @Override
    public void close() {

    }

    @Override
    public void disconnect() {
        try {
            sink.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract String gainAddress(int num);

    private synchronized float getGain(int num) {
        try {
            String address = gainAddress(num);
            List<Object> result = request(source, address);
            if (result == null || result.isEmpty()) return 0f;
            Object val = result.get(0);
            return val instanceof Float ? (Float) val : 0f;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }



    @Override
    public boolean getMute(int num) {
        num += offset;
        String address = "/ch/" + channel(num) + "/mix/on";
        List<Object> result = request(source, address);
        LogList.add("Received {} from console", result);
        if (result == null || result.isEmpty()) return false;
        return result.get(0).equals(0);
    }

    private float getPano(int num) {
        String address = "/ch/" + channel(num) + "/mix/pan";
        List<Object> result = request(source,address);
        if (result == null || result.isEmpty()) return 0f;
        Object val = result.get(0);
        return val instanceof Float ? (Float) val : 0f;
    }

    @Override
    public boolean getSolo(int num) {
        num += offset;
        String address = "/-stat/solosw/" + channel(num);
        List<Object> result = request(source, address);
        LogList.add("Received {} from console", result);
        if (result == null || result.isEmpty()) return false;
        return result.get(0).equals(1);
    }



    @Override
    public void handleCycle() {
        try {
            send("/xremote");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void handleFader(int num, float normalized) {
        LOG.debug("handleFader({}: → {})",num,normalized);
        int channel = num + offset;
        if (channel != lastChannel) {
            unhighlightChannel(lastChannel);
            highlightChannel(channel);
            lastChannel = channel;
        }

        float new_val = normalized;
        float old_val = getFader(channel-1,bus);

        if (Math.abs(new_val - old_val) > TRAP) return;
        setFader(channel-1,bus,new_val);

        String chnl = channel(channel);

        String address = bus == MAIN ? "/ch/" + chnl + "/mix/fader" : "/ch/" + chnl + "/mix/" + channel(bus) + "/level";
        send(address,new_val);
    }

    private void handleGain(int num, float percent) {
        num += offset;
        if (num != lastChannel) {
            unhighlightChannel(lastChannel);
            highlightChannel(num);
            lastChannel = num;
        }

        float new_val = percent / 100;
        float old_val = getGain(num);

        if (Math.abs(new_val - old_val) > TRAP) return;

        send(gainAddress(num),new_val);
    }

    @Override
    public boolean handleMute(int num, boolean enabled) {
        enabled = !getMute(num);
        num += offset;
        if (num != lastChannel) {
            unhighlightChannel(lastChannel);
            highlightChannel(num);
            lastChannel = num;
        }
        send("/ch/" + channel(num) + "/mix/on",enabled ? 0 : 1);
        return enabled;
    }

    private void handlePano(int num, float percent) {
        num += offset;
        if (num != lastChannel) {
            unhighlightChannel(lastChannel);
            highlightChannel(num);
            lastChannel = num;
        }

        float new_val = percent / 100;
        float old_val = getPano(num);

        if (Math.abs(new_val - old_val) > TRAP) return;

        send("/ch/" + channel(num) + "/mix/pan",new_val);
    }

    @Override
    public void handlePoti(int num, float percent) {
        if (bus == MAIN) handleGain(num, percent);
        else handlePano(num, percent);

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
    public boolean handleSolo(int num, boolean enabled) {
        num += offset;
        if (num != lastChannel) {
            unhighlightChannel(lastChannel);
            highlightChannel(num);
            lastChannel = num;
        }
        send("/-stat/solosw/" + channel(num),enabled?1:0);

        return enabled;
    }

    public void highlightBus(int bus) {
        send("/bus/" + channel(bus) + "/config/color",3);
        LOG.debug("Activated bus {}",bus);
    }

    @Override
    public void highlightChannel(int num) {
        send("/ch/" + channel(num) + "/config/color",9);
    }

    public void highlightFaderGroup(int count) {
        for (int i = offset + 1; i <= offset + count; i++) {
            lastChannel = 0;
            send("/ch/" + channel(i) + "/config/color",1);
        }
    }

    protected void processMessage(OSCMessageEvent event) {
        OSCMessage msg = event.getMessage();
        Vector<String> address = address(msg);
        LOG.debug("processMessage {}",address);
        String head = address.remove(0);
        switch (head) {
            case "ch":
                processChannelMessage(address, msg);
                break;
            default:
                LOG.info("No action defined for {}…/{}", head, address);
        }
    }

    private void processChannelMessage(Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        try {
            int channel = Integer.parseInt(head)-1;
            processChannelMessage(channel,address,msg);
        } catch (NumberFormatException nfe){
            LOG.warn("{} is not a channel number!");
        }
    }

    private void processChannelMessage(int channel, Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        switch (head) {
            case "mix":
                processChannelMessage(channel,MAIN,address,msg);
                break;
            default:
                LOG.info("Unknown path {} / {}", head, address);
        }
    }

    private void processChannelMessage(int channel, int bus, Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        List<Object> args = msg.getArguments();
        switch (head) {
            case "fader":
                if (args.size()==1) setFader(channel,bus,args.get(0));
                break;
            default:
                LOG.info("Unknown path {} / {}", head, address);
        }
    }

    private void requestFaders(){
        for (int channel = 1; channel<=channels; channel++)
        send("/ch/"+channel(channel)+"/mix/fader");
    }

    private void setFader(int channel, int bus, Object o) {
        if (o instanceof Float){
            setFader(channel,bus,(float)o);
        } else {
            LOG.debug("setFader() no possible, val instance of {}",o.getClass().getSimpleName());
        }
    }

    @Override
    public void stop(int val) {

    }


    public void unhighlightBus(int bus) {
        send("/bus/" + channel(bus) + "/config/color",1);
    }


    @Override
    public void unhighlightChannel(int num) {
        send("/ch/" + channel(num) + "/config/color",1);
    }


    public void unhighlightFaderGroup(int count) {
        for (int i = offset + 1; i <= offset + count; i++) {
            send("/ch/" + channel(i) + "/config/color",8);
        }
    }
}
