/*
 * This class is the table model used for the results table.
 */
package filesearcher;

import java.io.File;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Luis Medina
 */
public class DirectoryModel extends AbstractTableModel {

    private ArrayList<DirectoryInfo> datalist;
    private String[] columns = {"Filename", "Size", "Percent", "Files", "Directories"};

    public int getRowCount() {
        return datalist.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    public DirectoryModel() {
        datalist = new ArrayList<DirectoryInfo>();
    }

    public DirectoryModel(ArrayList<DirectoryInfo> list) {   
        if (Main.getIsTypeScan() == true) {
            ArrayList<DirectoryInfo> temp = new ArrayList<DirectoryInfo>();
            for (DirectoryInfo di : list) {
                if (di.getSize() != 0) {
                    temp.add(di);
                }
            }
            datalist = temp;
        } else {
            datalist = list;
        }
    }

    public void addRow(DirectoryInfo info) {
        datalist.add(info);
    }

    public Object getValueAt(int row, int col) {
        DirectoryInfo fileInfo = datalist.get(row);
        switch (col) {
            case 0:
                return fileInfo.getDirectory();
            case 1:
                return fileInfo.getSize();
            case 2:
                return fileInfo.getPercent();
            case 3:
                return fileInfo.getFiles();
            case 4:
                return fileInfo.getDirectories();
            default:
                return null;
        }
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 0:
                return File.class;
            case 1:
                return Long.class;
            case 2:
                return Double.class;
            case 3:
                return Long.class;
            case 4:
                return Long.class;
            default:
                return Object.class;
        }
    }

    public boolean isCellEditable() {
        return false;
    }

    public ArrayList<DirectoryInfo> getList() {
        return datalist;
    }

    public void clear() {
        datalist.clear();
    }
}
