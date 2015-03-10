/*
 * This class is for an object that contains relevant information
 * for a drive such as total space, used space, and free space.
 */

package filesearcher;

import java.io.File;

/**
 *
 * @author Luis Medina
 */
public class DriveInfo {

    private long totalSize;
    private long usedSize;
    private long freeSpace;
    private File drive;

    public DriveInfo(long totalSize, long usedSize, long freeSpace, File drive) {
        this.totalSize = totalSize;
        this.usedSize = usedSize;
        this.freeSpace = freeSpace;
        this.drive = drive;
    }

    public File getDrive() {
        return drive;
    }

    public void setDrive(File drive) {
        this.drive = drive;
    }

    public String getFormattedFreeSpace() {
        Size s = new Size(freeSpace);
        return s.getFormattedLength();
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public String getFormattedTotalSize() {
        Size s = new Size(totalSize);
        return s.getFormattedLength();
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getFormattedUsedSize() {
        Size s = new Size(usedSize);
        return s.getFormattedLength();
    }

    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }
    
    public long getFreeSpace(){
        return freeSpace;
    }
    
    public long getTotalSize(){
        return totalSize;
    }
    
    public long getUsedSize(){
        return usedSize;
    }
}
