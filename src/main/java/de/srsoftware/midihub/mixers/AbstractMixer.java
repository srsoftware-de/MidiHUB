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

    public AbstractMixer(String host, int port, int buses, int channels) throws IOException {
        this.buses = buses;
        this.channels = channels;
        InetSocketAddress socket = new InetSocketAddress(host, port);
        SocketAddress local = new InetSocketAddress(port);
        sink = new OSCPortOut(new OSCSerializerAndParserBuilder(), socket, local);
        source = new OSCPortIn(local);
        source.startListening();
        LogList.add("Connected to {} @ {}:{}", getClass().getSimpleName(), host, port);
        source.getDispatcher().addListener(new OSCPatternAddressMessageSelector(""),event -> {
            OSCMessage msg = event.getMessage();
            LOG.debug("Received OSC message: {} @ {}",msg.getArguments(),msg.getAddress());
        });
    }

    synchronized List<Object> request(OSCPortIn source, String address) {
        OSCPacketDispatcher dispatcher = source.getDispatcher();
        OSCPatternAddressMessageSelector selector = new OSCPatternAddressMessageSelector(address);
        CompletableFuture<List<Object>> promise = new CompletableFuture<>();
        OSCMessageListener listener = event -> promise.complete(event.getMessage().getArguments());
        dispatcher.addListener(selector, listener);
        try {
            send(address);
            List<Object> result = promise.get(100, TimeUnit.MILLISECONDS);
            return result;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (OSCSerializeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dispatcher.removeListener(selector,listener);
        }
        return null;
    }

    protected void send(String address,Object... args) throws OSCSerializeException, IOException {
        List<Object> list = Arrays.asList(args);
        OSCMessage message = new OSCMessage(address, list);
        sink.send(message);
        LogList.add("OSC.sent: {} {}", message.getAddress(), args.length > 0 ? message.getArguments():"");
    }
}
