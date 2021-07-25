package de.srsoftware.midihub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import java.util.HashSet;

public class NanoKontrol2 implements Receiver {

    private static final int FADER1 = 0;
    private static final int FADER2 = 1;
    private static final int FADER3 = 2;
    private static final int FADER4 = 3;
    private static final int FADER5 = 4;
    private static final int FADER6 = 5;
    private static final int FADER7 = 6;
    private static final int FADER8 = 7;

    private static Logger LOG = LoggerFactory.getLogger(NanoKontrol2.class);
    private final MidiDevice device;

    public NanoKontrol2(MidiDevice device) throws MidiUnavailableException {
        this.device = device;
        device.open();
        device.getTransmitter().setReceiver(this);
    }

    @Override
    public void send(MidiMessage midiMessage, long l) {
        LOG.info("Message received: {} ({})",midiMessage,midiMessage.getClass().getSimpleName());
        if (midiMessage instanceof ShortMessage) handle((ShortMessage)midiMessage);

    }

    private void handle(ShortMessage msg) {
        int command = msg.getCommand();
        switch (command){
            case ShortMessage.CONTROL_CHANGE:
                handleControlChange(msg.getChannel(),msg.getData1(),msg.getData2());
        }
    }

    private void handleControlChange(int channel, int data1, int data2) {
        switch (data1) {
            case FADER1:
            case FADER2:
            case FADER3:
            case FADER4:
            case FADER5:
            case FADER6:
            case FADER7:
            case FADER8:
                handleFader(data1+1,data2);
                break;
            default:
                LOG.info("ControlChange @ channel {}: {} / {}",channel,data1,data2);
        }
    }

    private void handleFader(int num, int level) {
        LOG.info("Fader {} → {}",num,level);
    }

    @Override
    public void close() {
        device.close();
    }

}
