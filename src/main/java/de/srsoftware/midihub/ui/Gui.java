package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.AssignmentTableModel;
import de.srsoftware.midihub.Device;
import de.srsoftware.midihub.MixerInfo;
import de.srsoftware.midihub.controllers.Control;
import de.srsoftware.midihub.controllers.NanoKontrol2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;

public class Gui extends JFrame {
    private static Logger LOG = LoggerFactory.getLogger(Gui.class);
    private static final String NANOKONTROL2 = "nanoKONTROL2";
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
        add(logScroll,BorderLayout.NORTH);

        AssignmentTableModel model = new AssignmentTableModel();
        JTable table = new JTable(model);
        table.setPreferredSize(new Dimension(1200,600));
        add(table,BorderLayout.CENTER);

        table.setCellSelectionEnabled(false);
        //table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row > 0 && col > 0) {
                    MixerInfo mixer = model.getMixer(row - 1);
                    Device device = model.getController(col - 1);
                    assign(device,mixer);
                }
            }
        });


        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        //deviceList.addListSelectionListener(valChanged -> monitorDevice(deviceList.getSelectedValue()));
    }

    private void assign(Device device, MixerInfo mixerInfo) {
        try {
            switch (device.shortName()){
                case NANOKONTROL2:
                    NanoKontrol2 receiver = new NanoKontrol2(device);
                    receiver.assign(mixerInfo.getMixer());
                    receiver.setLogger(logList);
                    break;
                default:
                    logList.log("Unknown device: {}",device.shortName());
                    return;
            }
            logList.log("Monitoring {}.",device);
        } catch (Exception e) {
        }
    }
}
