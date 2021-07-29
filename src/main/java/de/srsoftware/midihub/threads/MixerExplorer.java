package de.srsoftware.midihub.threads;

import com.illposed.osc.*;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import de.srsoftware.midihub.mixers.MixerInfo;
import de.srsoftware.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MixerExplorer implements OSCMessageListener {
    private static final String ADDRESS = "/xinfo";
    private static final String BROADCAST = "255.255.255.255";
    private static final Logger LOG = LoggerFactory.getLogger(MixerExplorer.class);
    private static final int PORT_LOCAL = 10020;
    private static final InetSocketAddress SOCK_LOCAL = new InetSocketAddress(PORT_LOCAL);

    public static final MixerExplorer singleton = new MixerExplorer();

    private Set<MixerInfo> mixers = new TreeSet<>();
    private Set<Listener> listeners = new HashSet<>();

    public static MixerInfo[] mixerList() {
        return singleton.mixers.toArray(MixerInfo[]::new);
    }


    @Override
    public void acceptMessage(OSCMessageEvent event) {

        OSCMessage message = event.getMessage();
        int oldSize = mixers.size();
        Object source = event.getSource();
        Integer port = Tools.unwrap(Tools.unwrap(source,"channel"),"cachedSenderPort");

        if (port != null) {
            MixerInfo mx = new MixerInfo(message,port);
            if (!mixers.contains(mx)){
                LOG.debug("new Mixer: mx");
                mixers.add(mx);
                if (mixers.size() != oldSize) listeners.forEach(l -> l.mixerDiscovered(mx));
            }
        }
    }

    public static void addListener(Listener listener) {
        singleton.listeners.add(listener);
    }

    public interface Listener{
        void mixerDiscovered(MixerInfo mixer);
    }

    private MixerExplorer() {
        try {
            OSCPortIn source = new OSCPortIn(SOCK_LOCAL);
            OSCPatternAddressMessageSelector selector = new OSCPatternAddressMessageSelector(ADDRESS);
            source.getDispatcher().addListener(selector, this);
            source.startListening();
            startAutoDiscovery();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void startAutoDiscovery() {
        new Thread(){
            @Override
            public void run() {

                OSCMessage message = new OSCMessage(ADDRESS, List.of());
                while (true) {
                    for (int port : List.of(10023,10024)) try {
                        InetSocketAddress socket = new InetSocketAddress(BROADCAST, port);
                        OSCPortOut broadcast = new OSCPortOut(new OSCSerializerAndParserBuilder(), socket, SOCK_LOCAL);
                        broadcast.send(message);
                        broadcast.close();
                        sleep(5000);
                    } catch (IOException | OSCSerializeException | InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }
}
