package de.srsoftware.midihub.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;

public class ControllerInfo {
    private static Logger LOG = LoggerFactory.getLogger(ControllerInfo.class);

    private final String name;
    private MidiDevice inDevice;
    private MidiDevice outDevice;

    public ControllerInfo(String name) throws MidiUnavailableException {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (inDevice != null || outDevice != null){
            sb.append(" (");
            if (inDevice != null) sb.append("IN");
            if (outDevice != null) {
                if (inDevice != null) sb.append(" / ");
                sb.append("OUT");
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public String getName(){
        return name;
    }

    public String shortName() {
        return name.split(" ",2)[0].trim();
    }

    public void setOutDevice(MidiDevice device) {
        outDevice = device;
    }

    public void setInDevice(MidiDevice device) {
        inDevice = device;
    }

    public MidiDevice getOutDevice() {
        return outDevice;
    }

    public MidiDevice getInDevice() {
        return inDevice;
    }
}
