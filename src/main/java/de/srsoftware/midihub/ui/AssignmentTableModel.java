package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.controllers.ControllerInfo;
import de.srsoftware.midihub.mixers.MixerInfo;
import de.srsoftware.midihub.threads.DeviceExplorer;
import de.srsoftware.midihub.threads.MixerExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;

public class AssignmentTableModel extends DefaultTableModel {
    private static Logger LOG = LoggerFactory.getLogger(AssignmentTableModel.class);

    private HashMap<Integer,HashMap<Integer,String>> values = new HashMap<>();

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
        HashMap<Integer, String> map = values.get(row);
        if (map == null) return "+";
        String val = map.get(col);
        if (val == null) return "+";
        return val;
    }

    public void update(){
        LOG.debug("{}.update()",getClass().getSimpleName());
        fireTableStructureChanged();
    }

    public MixerInfo getMixer(int i) {
        return MixerExplorer.mixerList()[i];
    }

    public ControllerInfo getController(int i) {
        return DeviceExplorer.deviceList()[i];
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        HashMap<Integer, String> map = values.get(row);
        if (map == null) values.put(row,map = new HashMap<>());
        map.put(column, aValue.toString());
        fireTableCellUpdated(row,column);
    }
}
