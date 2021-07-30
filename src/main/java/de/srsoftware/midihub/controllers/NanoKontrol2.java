package de.srsoftware.midihub.controllers;


import de.srsoftware.midihub.mixers.Mixer;
import de.srsoftware.midihub.ui.LogList;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;

public class NanoKontrol2 extends AbstractController {
    public static final String TYPE = "nanoKONTROL2";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NanoKontrol2.class);
    private static final int LANES = 8;
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



    public NanoKontrol2(ControllerInfo device) throws MidiUnavailableException {
        super(device);
    }

    @Override
    public boolean assign(Mixer mixer) {
        if (super.assign(mixer)) {
            mixer.highlightFaderGroup(LANES);
            return true;
        }
        return false;
    }

    @Override
    public Controller disconnect() {
        mixer.unhighlightFaderGroup(LANES);
        return super.disconnect();
    }

    private void getChannelButtons() {
        for (int i = 1; i<=8; i++) {
            setLed(MUTE1+i-1,mixer.getMute(i));
            setLed(SOLO1+i-1,mixer.getSolo(i));
        }
    }

    protected void handle(ShortMessage msg) {
        int command = msg.getCommand();
        if (command == ShortMessage.CONTROL_CHANGE ) {
            handleControlChange(msg.getChannel(), msg.getData1(), msg.getData2());
        } else {
            LogList.add("Unexpected command type: {}",command);
            LOG.info("Command: {}, channel {}, data 1: {}, data 2: {}",command,msg.getChannel(),msg.getData1(),msg.getData2());
        }
    }

    protected void handleControlChange(int channel, int data1, int data2) {
        if (mixer == null) {
            LogList.add("received control change {}/{} on channel, but no device is assigned!",data1,data2,channel);
            return;
        }
        boolean enabled;
        switch (data1) {
            case FADER1:
            case FADER2:
            case FADER3:
            case FADER4:
            case FADER5:
            case FADER6:
            case FADER7:
            case FADER8:
                mixer.handleFader(data1-FADER1+1,data2/127f);
                break;
            case POTI1:
            case POTI2:
            case POTI3:
            case POTI4:
            case POTI5:
            case POTI6:
            case POTI7:
            case POTI8:
                mixer.handlePoti(data1-POTI1+1,data2/127f);
                break;
            case MUTE1:
            case MUTE2:
            case MUTE3:
            case MUTE4:
            case MUTE5:
            case MUTE6:
            case MUTE7:
            case MUTE8:
                enabled = mixer.handleMute(data1-MUTE1+1,data2>0);
                setLed(data1,enabled);
                break;
            case RECORD1:
            case RECORD2:
            case RECORD3:
            case RECORD4:
            case RECORD5:
            case RECORD6:
            case RECORD7:
            case RECORD8:
                mixer.handleRec(data1-RECORD1+1,data2>0);
                break;
            case SOLO1:
            case SOLO2:
            case SOLO3:
            case SOLO4:
            case SOLO5:
            case SOLO6:
            case SOLO7:
            case SOLO8:
                enabled = mixer.handleSolo(data1-SOLO1+1,data2>0);
                setLed(data1,enabled);
                break;
            case TRACK_L:
                mixer.changeTrack(-LANES);
                getChannelButtons();
                break;
            case TRACK_R:
                mixer.changeTrack(+LANES);
                getChannelButtons();
                break;
            case MARKER_L:
                mixer.changeMarker(-1);
                break;
            case MARKER_R:
                mixer.changeMarker(+1);
                break;
            case STOP:
                mixer.stop(data2);
                break;
            case SET:
                mixer.handleSet();
                break;
            case PLAY:
                if (data2 > 0) mixer.play();
                break;
            case CYCLE:
                mixer.handleCycle();
                break;
            case REC:
                mixer.handleRec(0,data2>0);
                break;
            default:
                LogList.add("ControlChange @ channel {}: {} / {}",channel,data1,data2);
        }
    }

    public void setLed(int led,boolean enable){
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(ShortMessage.CONTROL_CHANGE, 0, led, enable ? 127 : 0);
            receiver.send(message, -1);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
}
