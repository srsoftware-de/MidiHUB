package de.srsoftware.midihub;

import com.illposed.osc.OSCMessage;
import de.srsoftware.midihub.mixers.Mixer;
import de.srsoftware.midihub.mixers.XR18;
import de.srsoftware.midihub.threads.MixerExplorer;
import de.srsoftware.midihub.ui.LogList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MixerInfo implements Comparable<MixerInfo> {
    private static final Logger LOG = LoggerFactory.getLogger(MixerInfo.class);
    private final String address,name,model,version;
    private final int port;

    public MixerInfo(OSCMessage message, int port) {
        this(message.getArguments(),port);
    }

    public MixerInfo(List<Object> args, int port) {
        if (args.size() != 4) throw new IllegalArgumentException("OCS message expected to contain 4 arguments");
        address = (String) args.get(0);
        name = (String) args.get(1);
        model = (String) args.get(2);
        version = (String) args.get(3);
        this.port = port;
    }

    @Override
    public int compareTo(MixerInfo other) {
        return this.toString().compareTo(other.toString());
    }

    @Override
    public String toString() {
        return model+" / "+address;
    }

    public Mixer getMixer() {
        try {
            switch (model) {
                case XR18.MODEL:
                    return new XR18(address, port);
            }
        } catch (Exception e){
            LOG.warn("Failed to instatiate mixer {}",this,e);
        }
        return null;
    }
}
