package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.controllers.Controller;
import de.srsoftware.midihub.controllers.ControllerInfo;
import de.srsoftware.midihub.controllers.NanoKontrol2;
import de.srsoftware.midihub.controllers.NanoPad2;
import de.srsoftware.midihub.mixers.MixerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class AssignmentTable extends JTable implements MouseListener {
    private static final Logger LOG = LoggerFactory.getLogger(AssignmentTable.class);
    private final AssignmentTableModel model;
    private HashMap<String, Controller> assignedControllers = new HashMap<>();
    private boolean autoConnect = false;

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

    private void assign(int row, int col){
        MixerInfo mixer = model.getMixer(row );
        ControllerInfo device = model.getController(col);

        boolean dummy = autoConnect;
        autoConnect = false; // prevent recusion, as table changes with the next lines
        for (int r = 1; r < model.getRowCount(); r++) model.setValueAt("+",r,col);
        if (assign(device,mixer)) model.setValueAt("connected", row, col);
        autoConnect = dummy;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        if (row > 0 && col > 0) assign(row-1,col-1);
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
        LogList.add("Failed to assign {} to {}!",device.getName(),mixerInfo);
        return false;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }


    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (autoConnect){
            int rows = model.getRowCount();
            int cols = model.getColumnCount();
            if (cols>1 && rows>1) assign(rows-2,cols-2);
        }
    }
}
