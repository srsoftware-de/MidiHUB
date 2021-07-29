package de.srsoftware.midihub.controllers;

import de.srsoftware.midihub.mixers.Mixer;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class NanoPad2 extends AbstractController {
    public static final String TYPE = "nanoPAD2";

    public NanoPad2(ControllerInfo device) throws MidiUnavailableException {
        super(device);
    }




    @Override
    public boolean assign(Mixer mixer) {
        return false;
    }

    @Override
    protected void handle(ShortMessage midiMessage) {

    }
}
