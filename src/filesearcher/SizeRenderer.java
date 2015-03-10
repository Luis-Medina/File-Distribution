/*
 * This class renders the size of a file/folder on the results table
 * as a formatted instance of the class Size.
 */

package filesearcher;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Luis Medina
 */
public class SizeRenderer extends JLabel implements TableCellRenderer{

    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    
    public SizeRenderer(boolean isBordered) {
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        long length = ((Long) value).longValue();
        Size s = new Size(length);
        setText(" " + s.getFormattedLength());
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
