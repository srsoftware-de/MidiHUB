package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.AssignmentTableModel;
import de.srsoftware.midihub.Device;
import de.srsoftware.midihub.controllers.Control;
import de.srsoftware.midihub.controllers.NanoKontrol2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class Gui extends JFrame {
    private static Logger LOG = LoggerFactory.getLogger(Gui.class);
    private static final String NANOKONTROL2 = "nanoKONTROL2";
    private final HashMap<Device, Control> devices = new HashMap<>();
    private final MixerList mixerPanel;
    private final LogList logList;

    public Gui() throws IOException {
        super("MidiHub");

        setLayout(new BorderLayout());

        logList = new LogList();
        logList.setSize(new Dimension(600,600));
        logList.ensureIndexIsVisible(50);

        JScrollPane logScroll = new JScrollPane();
        logScroll.setPreferredSize(new Dimension(600,600));
        logScroll.add(logList);
        add(logScroll,BorderLayout.SOUTH);

        mixerPanel = new MixerList();
        add(mixerPanel,BorderLayout.EAST);

        AssignmentTableModel model = new AssignmentTableModel();
        JTable table = new JTable(model);
        table.setPreferredSize(new Dimension(1200,600));
        add(table,BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        //deviceList.addListSelectionListener(valChanged -> monitorDevice(deviceList.getSelectedValue()));
    }

    private void monitorDevice(Device midiInfo) {
        try {

            if (devices.containsKey(midiInfo)){
                logList.log("Already connected to {}.",midiInfo);
                return;
            }

            switch (midiInfo.shortName()){
                case NANOKONTROL2:
                    NanoKontrol2 receiver = new NanoKontrol2(midiInfo);
                    receiver.setLogger(logList);
                    devices.put(midiInfo,receiver);
                    break;
                default:
                    logList.log("Unknown device: {}",midiInfo.shortName());
                    return;
            }
            logList.log("Monitoring {}.",midiInfo);
        } catch (Exception e) {
        }
    }
}
