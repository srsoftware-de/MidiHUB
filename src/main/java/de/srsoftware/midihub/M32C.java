package de.srsoftware.midihub;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

public class M32C implements Mixer {
    private static final Logger LOG = LoggerFactory.getLogger(M32C.class);
    private static final int CHANNELS = 32;
    private final OSCPortOut server;
    private de.srsoftware.midihub.Logger logger;

    private int offset=0;
    private float [] mains = new float[CHANNELS];

    public M32C(String host, int port, de.srsoftware.midihub.Logger logger) throws IOException {
        this.logger = logger;
        server = new OSCPortOut(InetAddress.getByName(host),port);
        logger.log("Connected to {} @ {}:{}",getClass().getSimpleName(),host,port);
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

    }

    @Override
    public void changeTrack(int delta) {
        int count = Math.abs(delta);
        unhighlight(count);
        offset = (offset+delta)%CHANNELS;
        highlight(count);
    }

    public void highlight( int count) {
        for (int i=offset+1; i<=offset+count; i++){
            try {
                String channel = (i < 10 ? "0" : "") + i;
                Vector<Integer> args = new Vector<Integer>();
                args.add(9);
                OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
                server.send(message);
                logger.log("sent OSC: {}  : {}",message.getAddress(),args);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (OSCSerializeException e) {
                e.printStackTrace();
            }
        }
    }

    public void unhighlight(int count) {
        for (int i=offset+1; i<=offset+count; i++){
            try {
                String channel = (i < 10 ? "0" : "") + i;
                Vector<Integer> args = new Vector<Integer>();
                args.add(8);
                OSCMessage message = new OSCMessage("/ch/"+channel+"/config/color", args);
                server.send(message);
                logger.log("sent OSC: {}  : {}",message.getAddress(),args);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (OSCSerializeException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OSCSerializeException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handlePoti(int num, float percent) {
        try {

            num += offset;

            float new_val = percent / 100;

            String channel = (num < 10 ? "0" : "") + num;

            Vector<Object> args = new Vector<>();
            args.add(new_val);

            OSCMessage message = new OSCMessage("/ch/"+channel+"/preamp/trim", args);
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),new_val);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OSCSerializeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleFader(int num, float percent) {

        num += offset;

        float new_val = percent / 100;
        float old_val = mains[num-1];

        if (Math.abs(new_val - old_val)>0.02) return;
        mains[num-1] = new_val;

        String channel = (num < 10 ? "0" : "") + num;

        Vector<Object> args = new Vector<>();
        args.add(new_val);

        OSCMessage message = new OSCMessage("/ch/"+channel+"/mix/fader", args);
        try {
            server.send(message);
            logger.log("sent OSC: {}  : {}",message.getAddress(),args);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OSCSerializeException e) {
            e.printStackTrace();
        }
    }
}
