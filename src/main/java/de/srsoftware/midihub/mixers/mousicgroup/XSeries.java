package de.srsoftware.midihub.mixers.mousicgroup;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import de.srsoftware.midihub.mixers.AbstractMixer;
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
    private final int[] colors;

    public static final int INVERTED_BLACK = 0;
    public static final int INVERTED_RED = 1;
    public static final int INVERTED_GREEN = 2;
    public static final int INVERTED_YELLOW = 3;
    public static final int INVERTED_BLUE = 4;
    public static final int INVERTED_PINK = 5;
    public static final int INVERTED_CYAN = 6;
    public static final int INVERTED_WHITE = 7;
    public static final int BLACK = 8;
    public static final int RED = 9;
    public static final int GREEN = 10;
    public static final int YELLOW = 11;
    public static final int BLUE = 12;
    public static final int PINK = 13;
    public static final int CYAN = 14;
    public static final int WHITE = 15;

    private int offset = 0;
    private int bus = MAIN;
    private int lastChannel = 0;

    public XSeries(String host, int port, int buses, int channels) throws IOException {
        super(host, port, buses, channels);
        colors = new int[channels];
        requestSettings();
    }


    public void changeMarker(int delta) {
        unhighlightBus(bus);
        bus += delta;
        if (bus >= buses) bus = 0;
        if (bus < 0) bus = buses-1;
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

    public int getColor(int channel){
        LOG.debug("getColor({})",channel);
        return colors[channel];
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
            if (lastChannel != 0) highlightChannel(lastChannel);
            unhighlightChannel(channel);

            lastChannel = channel;
        }

        float old_val = getFader(channel-1,bus);

        if (Math.abs(normalized - old_val) > TRAP) return;
        setFader(channel-1,bus,normalized);

        String chnl = channel(channel);

        String address = bus == MAIN ? "/ch/" + chnl + "/mix/fader" : "/ch/" + chnl + "/mix/" + channel(bus) + "/level";
        send(address,normalized);
    }

    private void handleGain(int num, float normalized) {
        int channel = num + offset;
        if (channel != lastChannel) {
            if (lastChannel != 0) highlightChannel(lastChannel);
            unhighlightChannel(channel);
            lastChannel = channel;
        }

        float old_val = getGain(channel-1);

        if (Math.abs(normalized - old_val) > TRAP) return;
        setGain(channel-1,normalized);

        send(gainAddress(channel),normalized);
    }

    @Override
    public boolean handleMute(int num, boolean enabled) {
        enabled = !getMute(num);
        int channel = num + offset;
        if (channel != lastChannel) {
            if (lastChannel != 0) highlightChannel(lastChannel);
            unhighlightChannel(channel);
            lastChannel = channel;
        }
        send("/ch/" + channel(channel) + "/mix/on",enabled ? 0 : 1);
        return enabled;
    }

    private void handlePano(int num, float normalized) {
        int channel = num + offset;
        if (channel != lastChannel) {
            if (lastChannel != 0) highlightChannel(lastChannel);
            unhighlightChannel(channel);
            lastChannel = channel;
        }

        float old_val = getPano(channel-1);

        if (Math.abs(normalized - old_val) > TRAP) return;
        setPano(channel-1,normalized);

        send("/ch/" + channel(channel) + "/mix/pan",normalized);
    }

    @Override
    public void handlePoti(int num, float normalized) {
        if (bus == MAIN) handleGain(num, normalized);
        else handlePano(num, normalized);

    }

    @Override
    public boolean handleRec(int num, boolean enabled) {
        if (enabled && num == 0) send("/xremote");
        return false;
    }


    @Override
    public void handleSet() {
        changeMarker(-bus);
    }

    @Override
    public boolean handleSolo(int num, boolean enabled) {
        int channel = num + offset;
        if (channel != lastChannel) {
            if (lastChannel != 0) highlightChannel(lastChannel);
            unhighlightChannel(channel);

            lastChannel = channel;
        }
        send("/-stat/solosw/" + channel(channel),enabled?1:0);

        return enabled;
    }

    public void highlightBus(int bus) {
        send("/bus/" + channel(bus) + "/config/color",3);
        LOG.debug("Activated bus {}",bus);
    }

    @Override
    public void highlightChannel(int channel) {
        LOG.debug("highlightChannel({})",channel);
        lastChannel = 0;
        int color = getColor(channel-1);
        LOG.debug("old color: {}",color);
        if (!isHighlighted(color)) color = invertColor(color);
        LOG.debug("new color: {}",color);
        send("/ch/" + channel(channel) + "/config/color",color);
        setColor(channel-1,color);
    }

    public void highlightFaderGroup(int count) {
        for (int channel = offset + 1; channel <= offset + count; channel++) highlightChannel(channel);
    }

    private int invertColor(int color) {
        switch (color){
            case INVERTED_BLACK:
                return BLACK;
            case INVERTED_RED:
                return RED;
            case INVERTED_GREEN:
                return GREEN;
            case INVERTED_YELLOW:
                return YELLOW;
            case INVERTED_BLUE:
                return BLUE;
            case INVERTED_PINK:
                return PINK;
            case INVERTED_CYAN:
                return CYAN;
            case INVERTED_WHITE:
                return WHITE;
            case BLACK:
                return INVERTED_BLACK;
            case RED:
                return INVERTED_RED;
            case GREEN:
                return INVERTED_GREEN;
            case YELLOW:
                return INVERTED_YELLOW;
            case BLUE:
                return INVERTED_BLUE;
            case PINK:
                return INVERTED_PINK;
            case CYAN:
                return INVERTED_CYAN;
            default:
                return INVERTED_WHITE;
        }
    }

    public boolean isHighlighted(int color){
        return color == INVERTED_BLACK || color > BLACK;
    }

    @Override
    public void play() {
    }

    private void processAction(Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        LOG.debug("precessAction({},{})",address,msg.getArguments());
        if (head.equals("setrtasrc")){
            processSetRtaSrc(msg);
        }
    }

    void processGain(int channel, OSCMessage msg) {
        List<Object> args = msg.getArguments();
        if (args.isEmpty()) return;
        Object o = args.get(0);
        if (o instanceof Float){
            setGain(channel,(float)o);
        } else {
            LOG.debug("processGain(chnl {}) expected float value, but got {}", channel,o.getClass().getSimpleName());
        }
    }

    protected void processMessage(OSCMessageEvent event) {
        OSCMessage msg = event.getMessage();
        Vector<String> address = address(msg);
        LOG.debug("processMessage({}): {}",address,msg.getArguments());
        String head = address.remove(0);
        switch (head) {
            case "-action":
                processAction(address,msg);
                break;
            case "-stat":
                processStatusMessage(address,msg);
                break;
            case "ch":
                processChannelMessage(address, msg);
                break;
            case "xinfo":
                processXINfo(msg);
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
            LOG.warn("{} is not a channel number!",head);
        }
    }

    abstract void processChannelMessage(int channel, Vector<String> address, OSCMessage msg);

    void processChannelBusMessage(int channel, Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        List<Object> args = msg.getArguments();
        switch (head) {
            case "fader":
                if (args.size()==1) setFader(channel,MAIN,args.get(0));
                break;
            case "pan":
                processPano(channel,msg);
                break;
            default:
                try {
                    int bus = Integer.parseInt(head);
                    processChannelBusMessage(channel,bus,address,msg);
                } catch (NumberFormatException e) {
                    LOG.info("Unknown path {} / {}", head, address);
                }
        }
    }

    protected void processChannelConfig(int channel, Vector<String> address, OSCMessage msg) {
    String head = address.remove(0);
        List<Object> args = msg.getArguments();
        switch (head) {
            case "color":
                if (args.size() == 1) setColor(channel, args.get(0));
                break;
            default:
                LOG.info("Unknown path {} / {}", head, address);
        }
    }

    private void processChannelBusMessage(int channel, int bus, Vector<String> address, OSCMessage msg) {
        String head = address.remove(0);
        List<Object> args = msg.getArguments();
        switch (head) {
            case "level":
                if (args.size() == 1) setFader(channel, bus, args.get(0));
                break;
            default:
                LOG.info("Unknown path {} / {}", head, address);
        }
    }

    void processPano(int channel, OSCMessage msg) {
        List<Object> args = msg.getArguments();
        if (args.isEmpty()) return;
        Object o = args.get(0);
        if (o instanceof Float){
            setPano(channel,(float)o);
        } else {
            LOG.debug("processGain(chnl {}) expected float value, but got {}", channel,o.getClass().getSimpleName());
        }
    }

    private void processRtaSource(OSCMessage msg) {
        LOG.debug("processRtaSource({})",msg.getArguments());
    }

    private void processSetRtaSrc(OSCMessage msg) {
        LOG.debug("processSetRtaSrc({})",msg.getArguments());
    }

    private void processStatusMessage(Vector<String> address, OSCMessage msg) {
        LOG.debug("processStatusMessage({},{})",address,msg.getArguments());
        String head = address.remove(0);
        if (head.equals("rtasource")){
            processRtaSource(msg);
        } else LOG.debug("processStatusMessage({},{}) has no handler for {}",address,msg,head);
    }



    private void processXINfo(OSCMessage msg) {
        LOG.debug("processXInfo({})",msg.getArguments());
    }

    private void requestSettings(){
        for (int channel = 1; channel<=channels; channel++) {
            for (int bus = 1; bus<=buses; bus++) {
                send("/ch/"+channel(channel)+"/mix/"+channel(bus)+"/level");
            }
        }
        for (int channel = 1; channel<=channels; channel++) {
            send("/ch/" + channel(channel) + "/mix/fader");
            send(gainAddress(channel));
            send("/ch/" + channel(channel) + "/mix/pan");

        }
        for (int channel = 1; channel<=channels; channel++) {
            send("/ch/" + channel(channel) + "/config/color");
        }
    }
    
    private void setColor(int channel, Object o) {
        if (o instanceof Integer){
            setColor(channel,(int)o);
        } else {
            LOG.debug("setFader() no possible, val instance of {}",o.getClass().getSimpleName());
        }
    }

    protected void setColor(int channel, int color) {
        LOG.debug("setColor(chnl {}: → {})",channel,color);
        colors[channel] = color;
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
    public void unhighlightChannel(int channel) {
        LOG.debug("unhighlightChannel({})",channel);
        lastChannel = 0;
        int color = getColor(channel-1);
        if (isHighlighted(color)) color = invertColor(color);
        send("/ch/" + channel(channel) + "/config/color",color);
        setColor(channel-1,color);
    }


    public void unhighlightFaderGroup(int count) {
        for (int channel = offset + 1; channel <= offset + count; channel++) unhighlightChannel(channel);
    }
}
