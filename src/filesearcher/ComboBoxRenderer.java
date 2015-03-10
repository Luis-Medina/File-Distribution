/*
 * This class renders an icon next to a drive in the combo box.
 * 
 */
package filesearcher;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Luis Medina
 */
public class ComboBoxRenderer extends JLabel
        implements ListCellRenderer {

    public ComboBoxRenderer() {
        setOpaque(true);
        setVerticalAlignment(CENTER);
    }

    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        String name = ((String) value).toString();
        setText(name);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setIcon(Main.getDriveIcon());
        return this;
    }
}

