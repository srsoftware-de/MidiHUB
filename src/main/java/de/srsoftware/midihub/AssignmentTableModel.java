package de.srsoftware.midihub;

import de.srsoftware.midihub.threads.DeviceExplorer;
import de.srsoftware.midihub.threads.MixerExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;

public class AssignmentTableModel extends DefaultTableModel {
    private static Logger LOG = LoggerFactory.getLogger(AssignmentTableModel.class);
    public AssignmentTableModel(){
        DeviceExplorer.addListener(newDevices -> update());
        MixerExplorer.addListener(mixerDiscovered -> update());
    }

    @Override
    public int getRowCount() {
        return MixerExplorer.mixerList().length+1;
    }

    @Override
    public int getColumnCount() {
        return DeviceExplorer.deviceList().length+1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row == 0) {
            if (col == 0) return "";
            return DeviceExplorer.deviceList()[col-1].shortName();
        }
        if (col == 0){
            return MixerExplorer.mixerList()[row-1].toString();
        }
        return "col "+col+", row "+row;
    }

    public void update(){
        LOG.debug("{}.update()",getClass().getSimpleName());
        fireTableStructureChanged();
    }

    public MixerInfo getMixer(int i) {
        return MixerExplorer.mixerList()[i];
    }

    public Device getController(int i) {
        return DeviceExplorer.deviceList()[i];
    }
}
