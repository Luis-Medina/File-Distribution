/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesearcher;

import java.io.File;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 *
 * @author Luis
 */
public class BaseFilter implements IOFileFilter {

    private long fileCount;
    private long dirCount;
    private long size;
    private String[] suffixes;
    private final IOCase caseSensitivity;

    public BaseFilter(String[] extensions) {
        fileCount = 0;
        dirCount = 0;
        size = 0;
        suffixes = extensions;
        caseSensitivity = IOCase.INSENSITIVE;
    }

    public long getFileCount() {
        return fileCount;
    }

    public long getDirectoryCount() {
        return dirCount;
    }

    public long getSize() {
        return size;
    }

    public boolean accept(File arg0) {
        if (suffixes != null) {
            if (arg0.isFile()) {
                String name = arg0.getName();
                for (int i = 0; i < this.suffixes.length; i++) {
                    if (caseSensitivity.checkEndsWith(name, suffixes[i])) {
                        fileCount++;
                        size += arg0.length();
                        return true;
                    }
                }
            }
        } else {
            if (arg0.isFile()) {
                fileCount++;
                size += arg0.length();
                return true;
            }
            if (arg0.isDirectory()) {
                dirCount++;
                return true;
            }
        }
        return false;
    }

    public boolean accept(File file, String string) {
        return accept(file);
    }
}
