package de.srsoftware.midihub.mixers;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializerAndParserBuilder;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import de.srsoftware.midihub.ui.LogList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractMixer implements Mixer {

    final OSCPortOut sink;
    final OSCPortIn source;
    final int buses;
    final int channels;

    public AbstractMixer(String host, int port, int buses, int channels) throws IOException {
        InetSocketAddress socket = new InetSocketAddress(host, port);
        SocketAddress local = new InetSocketAddress(port);
        sink = new OSCPortOut(new OSCSerializerAndParserBuilder(), socket, local);
        source = new OSCPortIn(local);
        source.startListening();
        this.buses = buses;
        this.channels = channels;
        LogList.add("Connected to {} @ {}:{}", getClass().getSimpleName(), host, port);
    }

    protected void send(String address,Object... args) throws OSCSerializeException, IOException {
        List<Object> list = Arrays.asList(args);
        OSCMessage message = new OSCMessage(address, list);
        sink.send(message);
        LogList.add("OSC.sent: {} {}", message.getAddress(), message.getArguments());
    }
}
