package de.srsoftware.midihub;

import de.srsoftware.midihub.threads.DeviceExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;

public class AssignmentTableModel extends DefaultTableModel {
    private static Logger LOG = LoggerFactory.getLogger(AssignmentTableModel.class);
    public AssignmentTableModel(){
        DeviceExplorer.addListener(newDevices -> fireTableStructureChanged());
    }

    @Override
    public int getRowCount() {
        return 5;
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
        return "col "+col+", row "+row;
    }
}
