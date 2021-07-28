package de.srsoftware.midihub;

import com.illposed.osc.OSCMessage;

import java.util.List;

public class MixerInfo implements Comparable<MixerInfo> {
    private final String address,name,model,version;

    public MixerInfo(OSCMessage message) {
        this(message.getArguments());
    }

    public MixerInfo(List<Object> args) {
        if (args.size() != 4) throw new IllegalArgumentException("OCS message expected to contain 4 arguments");
        address = (String) args.get(0);
        name = (String) args.get(1);
        model = (String) args.get(2);
        version = (String) args.get(3);
    }

    @Override
    public int compareTo(MixerInfo other) {
        return this.toString().compareTo(other.toString());
    }

    @Override
    public String toString() {
        return model+" / "+address;
    }
}
