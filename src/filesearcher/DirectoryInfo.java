/*
 * This class is for an object that contains relevant information about a directory
 * such as number of files, number of directories, size, and percent of used space.
 */

package filesearcher;

import java.io.File;

/**
 *
 * @author Luis Medina
 */
public class DirectoryInfo {

    private long files;
    private long size;
    private File directory;
    private long directories;
    private double percent;

    public DirectoryInfo(long files, long directories, long size, File directory, double percent) {
        this.files = files;
        this.size = size;
        this.directory = directory;
        this.directories = directories;
        this.percent = percent;
    }
    
    public double getPercent(){
        return percent;
    }

    public long getDirectories() {
        return directories;
    }

    public void setDirectories(int directories) {
        this.directories = directories;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public long getFiles() {
        return files;
    }

    public void setFiles(int files) {
        this.files = files;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

}
