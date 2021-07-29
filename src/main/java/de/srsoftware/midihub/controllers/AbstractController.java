package de.srsoftware.midihub.controllers;

import de.srsoftware.midihub.mixers.Mixer;
import de.srsoftware.midihub.ui.LogList;

import javax.sound.midi.*;

public abstract class AbstractController implements Controller, Transmitter {
    private final ControllerInfo device;
    Mixer mixer;
    Receiver receiver;

    public AbstractController(ControllerInfo device) throws MidiUnavailableException {
        this.device = device;
        device.getInDevice().open();
        device.getInDevice().getTransmitter().setReceiver(this);

        MidiDevice out = device.getOutDevice();
        out.open();
        receiver = out.getReceiver();
    }

    @Override
    public boolean assign(Mixer mixer) {
        if (mixer == null) return false;
        this.mixer = mixer;
        return true;
    }

    @Override
    public void close() {
        device.getInDevice().close();
        device.getOutDevice().close();
    }

    @Override
    public Controller disconnect() {
        mixer.close();
        mixer = null;
        return this;
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    protected abstract void handle(ShortMessage midiMessage);

    @Override
    public void send(MidiMessage midiMessage, long l) {
        if (midiMessage instanceof ShortMessage) {
            handle((ShortMessage)midiMessage);
            return;
        }
        LogList.add("Message received: {} ({})",midiMessage,midiMessage.getClass().getSimpleName());
    }


    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }
}
