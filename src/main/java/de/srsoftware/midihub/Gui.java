package de.srsoftware.midihub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Locale;

public class Gui extends JFrame {
    private static Logger LOG = LoggerFactory.getLogger(Gui.class);
    private static final String NANOKONTROL2 = "nanokontrol2";
    private final DeviceList deviceList;
    private final HashMap<MidiDevice, Receiver> devices = new HashMap<>();
    Gui(){
        super("MidiHub");

        setLayout(new BorderLayout());

        deviceList = new DeviceList();
        deviceList.setPreferredSize(new Dimension(300,600));
        add(deviceList,BorderLayout.WEST);
        add(new JLabel("Test"),BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        deviceList.addListSelectionListener(valChanged -> monitorDevice(deviceList.getSelectedValue()));
    }

    private void monitorDevice(MidiDevice.Info deviceInfo) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);

            if (devices.containsKey(device)){
                LOG.info("Already connected to {}.",deviceInfo);
                return;
            }

            String name = deviceInfo.getName().split(" ", 2)[0].toLowerCase(Locale.ROOT);
            switch (name){
                case NANOKONTROL2:
                    NanoKontrol2 receiver = new NanoKontrol2(device);
                    devices.put(device,receiver);
                    break;
                default:
                    LOG.info("Unknown device: {}, Class: {}",name,device.getClass().getSimpleName());
                    return;
            }
            LOG.info("Monitoring {}. Class: {}",deviceInfo,device.getClass().getSimpleName());
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }
}
