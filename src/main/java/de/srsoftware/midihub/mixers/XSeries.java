package de.srsoftware.midihub.mixers;

import com.illposed.osc.OSCSerializeException;
import de.srsoftware.midihub.WatchDog;
import de.srsoftware.midihub.ui.LogList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public abstract class XSeries extends AbstractMixer {
    private static final Logger LOG = LoggerFactory.getLogger(XSeries.class);
    private static final float TRAP = 0.03f;
    private static final int MAIN = 0;


    private int offset = 0;
    private int bus = MAIN;
    private int lastChannel = 0;

    public XSeries(String host, int port, int buses, int channels) throws IOException {
        super(host, port, buses, channels);
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

            WatchDog wd = new WatchDog(source, address);
            send(address);
            List<Object> result = wd.getResult();
            if (result == null || result.isEmpty()) return 0f;
            Object val = result.get(0);
            return val instanceof Float ? (Float) val : 0f;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    private synchronized float getFader(int bus, int num) {
        try {
            String channel = channel(num);
            String address = bus == MAIN ? "/ch/" + channel + "/mix/fader" : "/ch/" + channel + "/mix/" + channel(bus) + "/level";

            WatchDog wd = new WatchDog(source, address);
            send(address);
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
    public synchronized boolean getMute(int num) {
        try {
            num += offset;
            String address = "/ch/" + channel(num) + "/mix/on";

            WatchDog wd = new WatchDog(source, address);
            send(address);
            List<Object> result = wd.getResult();
            LogList.add("Received {} from console", result);
            if (result == null || result.isEmpty()) return false;
            return result.get(0).equals(0);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized float getPano(int num) {
        try {
            String address = "/ch/" + channel(num) + "/mix/pan";

            WatchDog wd = new WatchDog(source, address);
            send(address);
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
    public synchronized boolean getSolo(int num) {
        try {
            num += offset;
            String address = "/-stat/solosw/" + channel(num);

            WatchDog wd = new WatchDog(source, address);
            send(address);
            List<Object> result = wd.getResult();
            LogList.add("Received {} from console", result);
            if (result == null || result.isEmpty()) return false;
            return result.get(0).equals(1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return false;
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
    public void handleFader(int num, float percent) {

        num += offset;
        if (num != lastChannel) {
            unhighlightChannel(lastChannel);
            highlightChannel(num);
            lastChannel = num;
        }

        float new_val = percent / 100;
        float old_val = getFader(bus, num);

        if (Math.abs(new_val - old_val) > TRAP) return;

        String channel = channel(num);

        String address = bus == MAIN ? "/ch/" + channel + "/mix/fader" : "/ch/" + channel + "/mix/" + channel(bus) + "/level";
        try {
            send(address,new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
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

            if (Math.abs(new_val - old_val) > TRAP) return;

            send(gainAddress(num),new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMute(int num, boolean enabled) {
        try {
            enabled = !getMute(num);
            num += offset;
            if (num != lastChannel) {
                unhighlightChannel(lastChannel);
                highlightChannel(num);
                lastChannel = num;
            }
            send("/ch/" + channel(num) + "/mix/on",enabled ? 0 : 1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return enabled;
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

            if (Math.abs(new_val - old_val) > TRAP) return;

            send("/ch/" + channel(num) + "/mix/pan",new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
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
        try {
            num += offset;
            if (num != lastChannel) {
                unhighlightChannel(lastChannel);
                highlightChannel(num);
                lastChannel = num;
            }
            send("/-stat/solosw/" + channel(num),enabled?1:0);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return enabled;
    }

    public void highlightBus(int bus) {
        try {
            send("/bus/" + channel(bus) + "/config/color",3);
            LOG.debug("Activated bus {}",bus);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void highlightChannel(int num) {
        try {
            send("/ch/" + channel(num) + "/config/color",9);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    public void highlightFaderGroup(int count) {
        for (int i = offset + 1; i <= offset + count; i++) {
            try {
                lastChannel = 0;
                send("/ch/" + channel(i) + "/config/color",1);
            } catch (IOException | OSCSerializeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop(int val) {

    }


    public void unhighlightBus(int bus) {
        try {
            send("/bus/" + channel(bus) + "/config/color",1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void unhighlightChannel(int num) {
        try {
            send("/ch/" + channel(num) + "/config/color",1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }


    public void unhighlightFaderGroup(int count) {
        for (int i = offset + 1; i <= offset + count; i++) {
            try {
                send("/ch/" + channel(i) + "/config/color",8);
            } catch (IOException | OSCSerializeException e) {
                e.printStackTrace();
            }
        }
    }
}
