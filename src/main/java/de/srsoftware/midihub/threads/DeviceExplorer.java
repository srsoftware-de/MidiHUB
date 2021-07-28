package de.srsoftware.midihub.threads;

import de.srsoftware.midihub.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.*;

public class DeviceExplorer {
    private static Logger LOG = LoggerFactory.getLogger(DeviceExplorer.class);
    public static final DeviceExplorer singleton = new DeviceExplorer();
    private HashMap<String, Device> devices = new HashMap<>();
    private HashSet<Listener> listeners = new HashSet<>();

    public static Device[] deviceList() {
        return singleton.devices.values().toArray(new Device[0]);
    }

    public static void addListener(Listener listener) {
        singleton.listeners.add(listener);
    }


    public interface Listener{
        void devicesDiscovered(Vector<Device> newDevices);
    }

    private DeviceExplorer(){
        new Thread(){
            @Override
            public void run() {
                Vector<Device> newDevices = new Vector<>();
                while (true) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
                                    if (midiInfo == null){
                                        devices.put(name,midiInfo = new Device(name));
                                        newDevices.add(midiInfo);
                                    }
                                    midiInfo.setOutDevice(device);
                                    break;
                                case "MidiInDevice":
                                    if (midiInfo == null) {
                                        devices.put(name,midiInfo = new Device(name));
                                        newDevices.add(midiInfo);
                                    }
                                    midiInfo.setInDevice(device);
                                    break;
                                default:
                                    LOG.debug(" → unsupported");
                            }
                        } catch (MidiUnavailableException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!newDevices.isEmpty()) listeners.forEach(listener -> listener.devicesDiscovered(newDevices));

                }
            }
        }.start();
    }
}
