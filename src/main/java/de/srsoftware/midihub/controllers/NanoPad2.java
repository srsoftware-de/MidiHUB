package de.srsoftware.midihub.controllers;

import de.srsoftware.midihub.ui.LogList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

public class NanoPad2 extends AbstractController {
    private static final Logger LOG = LoggerFactory.getLogger(NanoPad2.class);
    public static final String TYPE = "nanoPAD2";

    public NanoPad2(ControllerInfo device) throws MidiUnavailableException {
        super(device);
    }

    @Override
    protected void handle(ShortMessage msg) {
        int command = msg.getCommand();
        switch (command){
            case ShortMessage.NOTE_ON:
                break;
            case ShortMessage.NOTE_OFF:
                handleButton(msg.getData1());
            default:
                LogList.add("Unexpected command type: {}",command);
                LOG.info("Command: {}, channel {}, data 1: {}, data 2: {}",command,msg.getChannel(),msg.getData1(),msg.getData2());
        }
    }

    private void handleButton(int btn) {
        switch (btn){
            case 36:
            case 37:
            case 38:
            case 39:
                mixer.handleMute(btn-27,!mixer.getMute(btn-27));
                break;
            case 40:
            case 41:
            case 42:
            case 43:
                mixer.handleMute(btn-39,!mixer.getMute(btn-39));
                break;
            case 44:
            case 45:
            case 46:
            case 47:
                mixer.handleMute(btn-31,!mixer.getMute(btn-31));
                break;
            case 48:
            case 49:
            case 50:
            case 51:
                mixer.handleMute(btn-43,!mixer.getMute(btn-43));
                break;
        }
    }
}
