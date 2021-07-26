package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.controllers.Control;
import de.srsoftware.midihub.controllers.NanoKontrol2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Gui extends JFrame {
    private static Logger LOG = LoggerFactory.getLogger(Gui.class);
    private static final String NANOKONTROL2 = "nanokontrol2";
    private final DeviceList deviceList;
    private final HashMap<MidiDevice, Control> devices = new HashMap<>();
    private final MixerPanel mixerPanel;
    private final LogList logList;

    public Gui(){
        super("MidiHub");

        setLayout(new BorderLayout());

        logList = new LogList();
        logList.setSize(new Dimension(600,600));
        logList.ensureIndexIsVisible(50);

        JScrollPane logScroll = new JScrollPane();
        logScroll.setPreferredSize(new Dimension(600,600));
        logScroll.add(logList);
        add(logScroll,BorderLayout.CENTER);

        deviceList = new DeviceList();
        deviceList.setPreferredSize(new Dimension(300,600));
        add(deviceList,BorderLayout.WEST);

        mixerPanel = new MixerPanel();
        mixerPanel.setLogger(logList);
        mixerPanel.onConnect(mixer -> {
            if (!devices.isEmpty()) {
                Control receiver = devices.values().iterator().next();
                receiver.assign(mixer);

            }
        });

        add(mixerPanel,BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        deviceList.addListSelectionListener(valChanged -> monitorDevice(deviceList.getSelectedValue()));

    }

    private void monitorDevice(MidiDevice.Info deviceInfo) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);

            if (devices.containsKey(device)){
                logList.log("Already connected to {}.",deviceInfo);
                return;
            }

            String name = deviceInfo.getName().split(" ", 2)[0].toLowerCase(Locale.ROOT);
            switch (name){
                case NANOKONTROL2:
                    NanoKontrol2 receiver = new NanoKontrol2(device);
                    receiver.setLogger(logList);
                    devices.put(device,receiver);
                    break;
                default:
                    logList.log("Unknown device: {}, Class: {}",name,device.getClass().getSimpleName());
                    return;
            }
            logList.log("Monitoring {}. Class: {}",deviceInfo,device.getClass().getSimpleName());
        } catch (Exception e) {
        }
    }
}