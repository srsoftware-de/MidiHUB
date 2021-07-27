package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import javax.swing.*;
import java.util.HashMap;

public class DeviceList extends JList<Device> {

    private static Logger LOG = LoggerFactory.getLogger(DeviceList.class);
    private LogList logger;

    public DeviceList(){
        thread().start();
    }

    private Thread thread() {
        return new Thread(){
            @Override
            public void run() {
                while (true) {
                    HashMap<String, Device> devices = new HashMap<>();
                    MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
                    for (MidiDevice.Info info : infos){
                        try {
                            MidiDevice device = MidiSystem.getMidiDevice(info);
                            String type = device.getClass().getSimpleName();
                            String name = info.getName();
                            LOG.debug("Discovered {} \"{}\"",type,name);
                            Device midiInfo = devices.get(name);
                            switch (type){
                                case "MidiOutDevice":
                                    if (midiInfo == null) devices.put(name,midiInfo = new Device(name));
                                    midiInfo.setOutDevice(device);
                                    break;
                                case "MidiInDevice":
                                    if (midiInfo == null) devices.put(name,midiInfo = new Device(name));
                                    midiInfo.setInDevice(device);
                                    break;
                                default:
                                    LOG.debug(" → unsupported");
                            }
                        } catch (MidiUnavailableException e) {
                            e.printStackTrace();
                        }
                    }

                    LOG.debug("Devices: {}",devices);
                    setListData(devices.values().toArray(new Device[0]));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
