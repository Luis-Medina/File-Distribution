/*
 * This class is to render an appropriate icon next to the filename
 * on the first column of the results table.
 */
package filesearcher;

import java.awt.Component;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Luis Medina
 */
public class NameRenderer extends JLabel implements TableCellRenderer {

    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;

    public NameRenderer(boolean isBordered) {
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        File file = (File) value;
        setText("" + file.getName());
        FileSystemView view = FileSystemView.getFileSystemView();
        Icon icon = null;
        try{
            icon = view.getSystemIcon(file);
        }
        catch(Exception e){}
        setIcon(icon);
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getBackground());
                }
                setBorder(unselectedBorder);
            }
        }
        else{
            if(isSelected){
                setBackground(table.getSelectionBackground());
            }
            else{
                setBackground(table.getBackground());
            }
        }
        return this;
    }
}
