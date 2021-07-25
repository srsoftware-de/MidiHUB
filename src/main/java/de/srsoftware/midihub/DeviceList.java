package de.srsoftware.midihub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import javax.swing.*;
import java.util.Vector;

public class DeviceList extends JList<MidiDevice.Info> {

    private static Logger LOG = LoggerFactory.getLogger(DeviceList.class);

    DeviceList(){
        thread().start();
    }

    private Thread thread() {
        return new Thread(){
            @Override
            public void run() {
                while (true) {
                    Vector<MidiDevice.Info> receivers = new Vector<>();
                    MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
                    for (MidiDevice.Info info : infos) try {
                        LOG.info("Found {}, trying to obtain transmitter...", info);
                        MidiSystem.getMidiDevice(info).getTransmitter();
                        receivers.add(info);
                        LOG.info("Transmitter aviailable.");
                    } catch (MidiUnavailableException e) {
                        LOG.info("No transmitter aviailable.");
                    }
                    setListData(receivers);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
