/*
 * This class is to render a progress bar to graphically show what
 * percent of used space on the drive the respective file or folder takes up.
 */
package filesearcher;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Luis Medina
 */
public class PercentRenderer extends JProgressBar implements TableCellRenderer {
    
    public PercentRenderer(){
        super(0,100);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        double percent = ((Double) value).doubleValue();
        DecimalFormat df = new DecimalFormat("0.00");
        String formatted = df.format(percent);
        this.setString(formatted + " %");
        this.setValue((int) percent);
        this.setStringPainted(true);
        this.setForeground(new Color(0, 204, 51));
        return this;
    }
}
