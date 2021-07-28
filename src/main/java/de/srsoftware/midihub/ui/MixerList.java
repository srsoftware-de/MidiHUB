package de.srsoftware.midihub.ui;

import com.illposed.osc.*;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import de.srsoftware.midihub.MixerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.List;

public class MixerList extends JList<MixerInfo> implements OSCMessageListener {
    private static final String ADDRESS = "/xinfo";
    private static final String BROADCAST = "255.255.255.255";
    private static final Logger LOG = LoggerFactory.getLogger(MixerList.class);
    private static final int PORT_LOCAL = 10020;
    private static final InetSocketAddress SOCK_LOCAL = new InetSocketAddress(PORT_LOCAL);

    private LogList logger;
    private Set<MixerInfo> mixers = new TreeSet<>();

    public MixerList() throws IOException {
        setPreferredSize(new Dimension(300,600));
        OSCPortIn source = new OSCPortIn(SOCK_LOCAL);
        OSCPatternAddressMessageSelector selector = new OSCPatternAddressMessageSelector(ADDRESS);
        source.getDispatcher().addListener(selector, this);
        source.startListening();
        startAutoDiscovery();
        System.err.println("Listening for packs");
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
                    } catch (IOException | OSCSerializeException e) {
                        e.printStackTrace();
                    }


                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }

    @Override
    public void acceptMessage(OSCMessageEvent event) {
        OSCMessage message = event.getMessage();
        mixers.add(new MixerInfo(message));
        setListData(new Vector<>(mixers));
    }

}
