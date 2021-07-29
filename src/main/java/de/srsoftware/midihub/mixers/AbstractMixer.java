package de.srsoftware.midihub.mixers;

import com.illposed.osc.*;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import de.srsoftware.midihub.ui.LogList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractMixer implements Mixer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMixer.class);
    final OSCPortOut sink;
    final OSCPortIn source;
    final int buses;
    final int channels;

    private byte[] mutes, solos;
    private float[] gains,panos;
    private float[][]faders;

    public AbstractMixer(String host, int port, int buses, int channels) throws IOException {
        this.buses = buses;
        this.channels = channels;
        mutes = new byte[channels];
        solos = new byte[channels];
        gains = new float[channels];
        panos = new float[channels];
        faders = new float[buses][channels];
        InetSocketAddress remoteSocket = new InetSocketAddress(host, port);
        SocketAddress localSocket = new InetSocketAddress(port);
        sink = new OSCPortOut(new OSCSerializerAndParserBuilder(), remoteSocket, localSocket);
        source = new OSCPortIn(localSocket);
        source.getDispatcher().addListener(new OSCPatternAddressMessageSelector(""),this::processMessage);
        source.startListening();
        LogList.add("Connected to {} @ {}:{}", getClass().getSimpleName(), host, port);
    }



    protected Vector<String> address(OSCMessage msg){
        String path = msg.getAddress();
        if (path.charAt(0) == '/') path = path.substring(1);
        return new Vector<>(Arrays.asList(path.split("/")));
    }

    public float getFader(int channel, int bus) {
        float result = (bus < buses && channel < channels) ? faders[bus][channel] : 0f;
        LOG.debug("getFader(bus {}, chnl {}) → {}",bus,channel,result);
        return result;
    }

    public float getGain(int channel) {
        float result = (channel < channels) ? gains[channel] : 0f;
        LOG.debug("getGain(chnl {}) → {}",channel,result);
        return result;
    }

    public float getPano(int channel) {
        float result = (channel < channels) ? panos[channel] : 0f;
        LOG.debug("getPano(chnl {}) → {}",channel,result);
        return result;
    }

    protected abstract void processMessage(OSCMessageEvent event);


    synchronized List<Object> request(OSCPortIn source, String address) {
        OSCPacketDispatcher dispatcher = source.getDispatcher();
        OSCPatternAddressMessageSelector selector = new OSCPatternAddressMessageSelector(address);
        CompletableFuture<List<Object>> promise = new CompletableFuture<>();
        OSCMessageListener listener = event -> promise.complete(event.getMessage().getArguments());
        dispatcher.addListener(selector, listener);
        try {
            send(address);
            return promise.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dispatcher.removeListener(selector,listener);
        }
        return null;
    }

    protected void send(String address,Object... args) {
        List<Object> list = Arrays.asList(args);
        try {
            OSCMessage message = new OSCMessage(address, list);
            sink.send(message);
            LogList.add("OSC.sent: {} {}", message.getAddress(), args.length > 0 ? message.getArguments() : "");
        } catch (OSCSerializeException | IOException e) {
            LOG.warn("send({},{}) failed",address,list,e);
        }
    }

    protected void setFader(int channel, int bus, float normalized) {
        LOG.debug("setFader(chnl {}, bus {} → {})",channel,bus,normalized);
        faders[bus][channel] = normalized;
    }

    protected void setGain(int channel, float normalized) {
        LOG.debug("setGain(chnl {}: → {})",channel,normalized);
        gains[channel] = normalized;
    }

    protected void setPano(int channel, float normalized) {
        LOG.debug("setPano(chnl {}: → {})",channel,normalized);
        panos[channel] = normalized;
    }
}
