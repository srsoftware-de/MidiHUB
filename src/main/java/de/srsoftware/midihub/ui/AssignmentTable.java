package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.controllers.Controller;
import de.srsoftware.midihub.controllers.ControllerInfo;
import de.srsoftware.midihub.controllers.NanoKontrol2;
import de.srsoftware.midihub.controllers.NanoPad2;
import de.srsoftware.midihub.mixers.Mixer;
import de.srsoftware.midihub.mixers.MixerInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class AssignmentTable extends JTable implements MouseListener {
    private final AssignmentTableModel model;
    private HashMap<String, Controller> assignedControllers = new HashMap<>();

    public AssignmentTable() {
        super();
        model = new AssignmentTableModel();
        setModel(model);
        setPreferredSize(new Dimension(1200,600));
        addMouseListener(this);
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                disconnectDevices();
            }
        });
    }

    private void disconnectDevices() {
        for (Controller controller : assignedControllers.values()) controller.disconnect().close();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        if (row > 0 && col > 0) {
            MixerInfo mixer = model.getMixer(row - 1);
            ControllerInfo device = model.getController(col - 1);
            for (int r = 1; r < model.getRowCount(); r++) model.setValueAt("+",r,col);
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

    private boolean assign(ControllerInfo device, MixerInfo mixerInfo) {
        Controller controller = assignedControllers.get(device.getName());
        if (controller != null) {
            controller.disconnect();
            controller.assign(mixerInfo.getMixer());
            return true;
        }

        try {
            switch (device.shortName()){
                case NanoKontrol2.TYPE:
                    controller = new NanoKontrol2(device);
                    if (controller.assign(mixerInfo.getMixer())){
                        assignedControllers.put(device.getName(),controller);
                        return true;
                    }
                    break;
                case NanoPad2.TYPE:
                    controller = new NanoPad2(device);
                    if (controller.assign(mixerInfo.getMixer())){
                        assignedControllers.put(device.getName(),controller);
                        return true;
                    }
                    break;

                default:
                    LogList.add("Unknown device: {}",device.shortName());
                    return false;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
