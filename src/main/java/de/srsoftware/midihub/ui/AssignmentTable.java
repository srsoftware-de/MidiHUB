package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.AssignmentTableModel;
import de.srsoftware.midihub.Device;
import de.srsoftware.midihub.MixerInfo;
import de.srsoftware.midihub.controllers.NanoKontrol2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AssignmentTable extends JTable implements MouseListener {
    private static final String NANOKONTROL2 = "nanoKONTROL2";
    private final AssignmentTableModel model;

    public AssignmentTable() {
        super();
        model = new AssignmentTableModel();
        setModel(model);
        setPreferredSize(new Dimension(1200,600));
        addMouseListener(this);
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        if (row > 0 && col > 0) {
            MixerInfo mixer = model.getMixer(row - 1);
            Device device = model.getController(col - 1);
            if (assign(device,mixer)) model.setValueAt("connected",row,col);
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    private boolean assign(Device device, MixerInfo mixerInfo) {
        try {
            switch (device.shortName()){
                case NANOKONTROL2:
                    NanoKontrol2 receiver = new NanoKontrol2(device);
                    return receiver.assign(mixerInfo.getMixer());
                default:
                    LogList.add("Unknown device: {}",device.shortName());
                    return false;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
