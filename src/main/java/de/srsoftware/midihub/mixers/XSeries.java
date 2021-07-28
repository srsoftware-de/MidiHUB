package de.srsoftware.midihub.mixers;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializerAndParserBuilder;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import de.srsoftware.midihub.WatchDog;
import de.srsoftware.midihub.ui.LogList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public abstract class XSeries extends AbstractMixer {
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
            String channel = channel(num);
            String address = gainAddress(num);

            WatchDog wd = new WatchDog(source, address);
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            LogList.add("OSC.sent: {} {}", message.getAddress(), message.getArguments());
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
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            LogList.add("OSC.sent: {} {}", message.getAddress(), message.getArguments());
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
            String channel = channel(num);
            String address = "/ch/" + channel + "/mix/on";


            WatchDog wd = new WatchDog(source, address);
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            LogList.add("OSC.sent: {} {}", message.getAddress(), message.getArguments());
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
            String channel = channel(num);
            String address = "/ch/" + channel + "/mix/pan";

            WatchDog wd = new WatchDog(source, address);
            OSCMessage message = new OSCMessage(address, List.of());
            sink.send(message);
            LogList.add("OSC.sent: {} {}", message.getAddress(), message.getArguments());
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
            String channel = channel(num);
            String address = "/-stat/solosw/" + channel;

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
        // TODO
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

        Vector<Object> args = new Vector<>();
        args.add(new_val);

        String address = bus == MAIN ? "/ch/" + channel + "/mix/fader" : "/ch/" + channel + "/mix/" + channel(bus) + "/level";
        OSCMessage message = new OSCMessage(address, args);
        try {
            sink.send(message);
            LogList.add("OSC.send: {} {}", address, new_val);
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

            String channel = channel(num);

            Vector<Object> args = new Vector<>();
            args.add(new_val);

            String address = "/ch/" + channel + "/preamp/trim";

            OSCMessage message = new OSCMessage(address, args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", address, new_val);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
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
            String channel = channel(num);
            enabled = !getMute(num);
            Vector<Object> args = new Vector<>();
            args.add(enabled ? 0 : 1);
            OSCMessage message = new OSCMessage("/ch/" + channel + "/mix/on", args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", message.getAddress(), enabled ? "ON" : "OFF");
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

            String channel = channel(num);

            Vector<Object> args = new Vector<>();
            args.add(new_val);

            String address = "/ch/" + channel + "/mix/pan";

            OSCMessage message = new OSCMessage(address, args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", address, new_val);
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
            String channel = channel(num);

            Vector<Object> args = new Vector<>();
            args.add(enabled ? 1 : 0);
            OSCMessage message = new OSCMessage("/-stat/solosw/" + channel, args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", message.getAddress(), enabled ? "ON" : "OFF");
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
        return enabled;
    }

    public void highlightBus(int bus) {
        try {
            String channel = channel(bus);
            Vector<Integer> args = new Vector<>();
            args.add(3);
            OSCMessage message = new OSCMessage("/bus/" + channel + "/config/color", args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", message.getAddress(), 3);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void highlightChannel(int num) {
        try {
            Vector<Object> args = new Vector<>();
            args.add(9);
            String channel = channel(num);
            OSCMessage message = new OSCMessage("/ch/" + channel + "/config/color", args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", message.getAddress(), 1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    public void highlightFaderGroup(int count) {
        for (int i = offset + 1; i <= offset + count; i++) {
            try {
                lastChannel = 0;
                String channel = channel(i);
                Vector<Integer> args = new Vector<>();
                args.add(1);
                OSCMessage message = new OSCMessage("/ch/" + channel + "/config/color", args);
                sink.send(message);
                LogList.add("OSC.send: {} {}", message.getAddress(), 9);
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
            String channel = channel(bus);
            Vector<Integer> args = new Vector<>();
            args.add(1);
            OSCMessage message = new OSCMessage("/bus/" + channel + "/config/color", args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", message.getAddress(), 1);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void unhighlightChannel(int num) {
        try {
            Vector<Object> args = new Vector<>();
            args.add(1);
            String channel = channel(num);
            OSCMessage message = new OSCMessage("/ch/" + channel + "/config/color", args);
            sink.send(message);
            LogList.add("OSC.send: {} {}", message.getAddress(), 9);
        } catch (IOException | OSCSerializeException e) {
            e.printStackTrace();
        }
    }


    public void unhighlightFaderGroup(int count) {
        for (int i = offset + 1; i <= offset + count; i++) {
            try {
                String channel = channel(i);
                Vector<Integer> args = new Vector<>();
                args.add(8);
                OSCMessage message = new OSCMessage("/ch/" + channel + "/config/color", args);
                sink.send(message);
                LogList.add("OSC.send: {} {}", message.getAddress(), 8);
            } catch (IOException | OSCSerializeException e) {
                e.printStackTrace();
            }
        }
    }
}
