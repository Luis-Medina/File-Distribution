/*
 * This class converts a file/folder size into a formatted value
 * with the appropriate GB, MB, KB, or B denomination.
 * Ex. 60.04 GB 
 */

package filesearcher;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;

/**
 *
 * @author Luis Medina
 */
public class Size {

    private long length;
      
    public Size(long length) {
        this.length = length;
    }

    public String getFormattedLength() {
        if(length > 1024 && length < 1048576){
            BigDecimal file = new BigDecimal(Long.toString(length));
            BigDecimal drive = new BigDecimal("1024");
            BigDecimal result = file.divide(drive, new MathContext(6));
            DecimalFormat df = new DecimalFormat("0.00 KB");
            String formatted = df.format(result.doubleValue());
            return formatted;
        }
        if(length > 1048576 && length < 1073741824){
            BigDecimal file = new BigDecimal(Long.toString(length));
            BigDecimal drive = new BigDecimal("1048576");
            BigDecimal result = file.divide(drive, new MathContext(6));
            DecimalFormat df = new DecimalFormat("0.00 MB");
            String formatted = df.format(result.doubleValue());
            return formatted;
        }
        if(length > 1073741824){
            BigDecimal file = new BigDecimal(Long.toString(length));
            BigDecimal drive = new BigDecimal("1073741824");
            BigDecimal result = file.divide(drive, new MathContext(6));
            DecimalFormat df = new DecimalFormat("0.00 GB");
            String formatted = df.format(result.doubleValue());
            return formatted;
        }
        return length + " B";
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
    
    
    
}
