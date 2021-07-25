package de.srsoftware.midihub;


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
    private static final int POTI1 = 16;
    private static final int POTI2 = 17;
    private static final int POTI3 = 18;
    private static final int POTI4 = 19;
    private static final int POTI5 = 20;
    private static final int POTI6 = 21;
    private static final int POTI7 = 22;
    private static final int POTI8 = 23;
    private static final int MUTE1 = 48;
    private static final int MUTE2 = 49;
    private static final int MUTE3 = 50;
    private static final int MUTE4 = 51;
    private static final int MUTE5 = 52;
    private static final int MUTE6 = 53;
    private static final int MUTE7 = 54;
    private static final int MUTE8 = 55;
    private static final int RECORD1 = 64;
    private static final int RECORD2 = 65;
    private static final int RECORD3 = 66;
    private static final int RECORD4 = 67;
    private static final int RECORD5 = 68;
    private static final int RECORD6 = 69;
    private static final int RECORD7 = 70;
    private static final int RECORD8 = 71;  
    private static final int SOLO1 = 32;
    private static final int SOLO2 = 33;
    private static final int SOLO3 = 34;
    private static final int SOLO4 = 35;
    private static final int SOLO5 = 36;
    private static final int SOLO6 = 37;
    private static final int SOLO7 = 38;
    private static final int SOLO8 = 39;
    private static final int MARKER_L = 61;
    private static final int MARKER_R = 62;
    private static final int TRACK_L = 58;
    private static final int TRACK_R = 59;
    private static final int SET = 60;
    private static final int CYCLE = 46;
    private static final int RWND = 43;
    private static final int FFWD = 44;
    private static final int STOP = 42;
    private static final int PLAY = 41;
    private static final int REC = 45;

    private final MidiDevice device;
    private Logger logger;

    public NanoKontrol2(MidiDevice device) throws MidiUnavailableException {
        this.device = device;
        device.open();
        device.getTransmitter().setReceiver(this);
    }

    @Override
    public void send(MidiMessage midiMessage, long l) {
        logger.log("Message received: {} ({})",midiMessage,midiMessage.getClass().getSimpleName());

        if (midiMessage instanceof ShortMessage) {
            handle((ShortMessage)midiMessage);
            return;
        }
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
                handleFader(data1-FADER1+1,data2);
                break;
            case POTI1:
            case POTI2:
            case POTI3:
            case POTI4:
            case POTI5:
            case POTI6:
            case POTI7:
            case POTI8:
                handlePoti(data1-POTI1+1,data2);
                break;
            case MUTE1:
            case MUTE2:
            case MUTE3:
            case MUTE4:
            case MUTE5:
            case MUTE6:
            case MUTE7:
            case MUTE8:
                handleMute(data1-MUTE1+1,data2>0);
                break;
            case RECORD1:
            case RECORD2:
            case RECORD3:
            case RECORD4:
            case RECORD5:
            case RECORD6:
            case RECORD7:
            case RECORD8:
                handleRec(data1-RECORD1+1,data2>0);
                break;
            case SOLO1:
            case SOLO2:
            case SOLO3:
            case SOLO4:
            case SOLO5:
            case SOLO6:
            case SOLO7:
            case SOLO8:
                handleSolo(data1-SOLO1+1,data2>0);
                break;
            case TRACK_L:
                changeTrack(-1);
                break;
            case TRACK_R:
                changeTrack(+1);
                break;
            case MARKER_L:
                changeMarker(-1);
                break;
            case MARKER_R:
                changeMarker(+1);
                break;
            default:
                logger.log("ControlChange @ channel {}: {} / {}",channel,data1,data2);
        }
    }

    private void changeMarker(int delta) {
        logger.log("{} marker",delta > 0 ? "increasing":"decreasing");
    }
    private void changeTrack(int delta) {
        logger.log("{} track",delta > 0 ? "increasing":"decreasing");
    }

    private void handleRec(int num, boolean b) {
        logger.log("Record {} → {}",num,b ? "on" : "off");
    }

    private void handleSolo(int num, boolean b) {
        logger.log("Solo {} → {}",num,b ? "on" : "off");
    }

    private void handleMute(int num, boolean b) {
        logger.log("Mute {} → {}",num,b ? "on" : "off");
    }

    private void handlePoti(int num, int level) {
        logger.log("Poti {} → {}",num,level);
    }

    private void handleFader(int num, int level) {
        logger.log("Fader {} → {}",num,level);
    }

    @Override
    public void close() {
        device.close();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
