package de.srsoftware.midihub;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCMessageListener;
import com.illposed.osc.OSCPacketDispatcher;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WatchDog implements OSCMessageListener {
    private final OSCPacketDispatcher dispatcher;
    private final OSCPatternAddressMessageSelector selector;
    private CompletableFuture<List<Object>>  result;

    public WatchDog(OSCPortIn source, String address) {
        dispatcher = source.getDispatcher();
        selector = new OSCPatternAddressMessageSelector(address);
        dispatcher.addListener(selector, this);
        result = new CompletableFuture<>();
    }

    @Override
    public void acceptMessage(OSCMessageEvent event) {
        OSCMessage message = event.getMessage();
        result.complete(message.getArguments());
    }

    public List<Object> getResult() {
        try {
            return result.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            dispatcher.removeListener(selector,this);
        }
    }
}
