/*
 * This class displays the GUI and handles all operations that update the GUI.
 */
package filesearcher;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 *
 * @author Luis Medina
 */
public class Main extends javax.swing.JFrame implements PropertyChangeListener {

    private Vector driveNames;
    private static HashMap map = new HashMap();
    private SwingWorker scanTask;
    private SwingWorker folderTask;
    private DirectoryModel model;
    private DirectoryInfo parentFolder;
    private Stack<DirectoryInfo> parentStack = new Stack<DirectoryInfo>();
    private Stack<ArrayList<DirectoryInfo>> listStack = new Stack<ArrayList<DirectoryInfo>>();
    private JFreeChart chart;
    private Main current = this;
    private ChartPanel chartPanel;
    private static boolean isTypeSearch;
    private CardLayout cl;
    private String[] music = {"mp3", "flac", "m4a", "aiff", "wav", "wma"
        + "m4b", "m4p", "3gp", "aac", "ra", "ram"
        + "ogg", "mid", "ac3", "midi", "mpa"
    };
    private String[] video = {"avi", "mpeg", "mpg", "wmv", "mov", "qt"
        + "divx", "dvr-ms", "flv", "m4v", "mkv", "mp4", "rmvb", "asf"
    };
    private String[] picture = {"bmp", "jpeg", "jpg", "img", "tiff", "gif", "png", "tif"};
    private String[] extensions;
    private static Icon driveIcon;
    private String currentScanFolder = "";

    /**
     * Creates new form Main
     */
    public Main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        FileSystemView view = FileSystemView.getFileSystemView();
        driveNames = new Vector();
        File[] drives = File.listRoots();
        driveIcon = view.getSystemIcon(drives[0]);
        for (File fi : drives) {
            String s = view.getSystemDisplayName(fi);
            if (!s.equals("")) {
                long total = fi.getTotalSpace();
                long free = fi.getFreeSpace();
                DriveInfo di = new DriveInfo(total, total - free, free, fi);
                map.put(s, di);
                driveNames.add(s);
            }
        }
        chartPanel = new ChartPanel(null);
        ImageIcon ImageIcon = new ImageIcon(getClass().getResource("/images/Search16.png"));
        Image image = ImageIcon.getImage();
        setIconImage(image);
        initComponents();
        setRadios();
        setLocationRelativeTo(null);
        jComboBox1.setRenderer(new ComboBoxRenderer());
        jPanel4.add(new JPanel(), "empty");
        jPanel4.add(chartPanel, "chart");
        cl = (CardLayout) jPanel4.getLayout();
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = jTable1.getSelectedRow();
                    if (viewRow != -1) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        int modelRow = jTable1.convertRowIndexToModel(viewRow);
                        File file = (File) jTable1.getModel().getValueAt(modelRow, 0);
                        //System.out.println(file);
                        if (file.isFile()) {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().open(file);
                                    //System.out.println("opened file " + file);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(current, "I can't open this file. Sorry!");
                                }
                            } else {
                                JOptionPane.showMessageDialog(current, "Not supported!");
                            }
                        } else {
                            long numberOfFolders = ((Long) jTable1.getModel().getValueAt(modelRow, 4)).longValue();
                            long numberOfFiles = ((Long) jTable1.getModel().getValueAt(modelRow, 3)).longValue();
                            if (numberOfFolders == 0 && numberOfFiles == 0) {
                            } else {
                                ArrayList<DirectoryInfo> parentList = ((DirectoryModel) jTable1.getModel()).getList();
                                parentFolder = parentList.get(modelRow);
                                parentStack.push(parentFolder);
                                listStack.push(parentList);
                                model = new DirectoryModel();
                                setTable();
                                
                                jButton2.setEnabled(false);
                                jButton3.setEnabled(true);
                                jButton1.setEnabled(false);
                                
                                if (!isTypeSearch) {
                                    folderTask = new ScanTask(ScanTask.FOLDER_SCAN);
                                    folderTask.addPropertyChangeListener(current);
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    folderTask.execute();
                                } else {
                                    folderTask = new ScanTask(ScanTask.FOLDER_EXTENSION_SCAN);
                                    folderTask.addPropertyChangeListener(current);
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    folderTask.execute();
                                }
                            }
                        }
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }

                }
            }
        });
    }

    /*
     * Inner class which registers which type of scan was selected.
     */
    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Scan by type")) {
                jCheckBox1.setEnabled(true);
                jCheckBox2.setEnabled(true);
                jCheckBox3.setEnabled(true);
                jTextField1.setEnabled(false);
            } else {
                if (e.getActionCommand().equals("Specific extension")) {
                    jTextField1.setEnabled(true);
                } else {
                    jTextField1.setEnabled(false);
                }
                jCheckBox1.setEnabled(false);
                jCheckBox2.setEnabled(false);
                jCheckBox3.setEnabled(false);
            }
        }
    }

    /*
     * Method to add listeners to each radio button.
     */
    private void setRadios() {
        ButtonListener listener = new ButtonListener();
        jRadioButton1.addActionListener(listener);
        jRadioButton2.addActionListener(listener);
        jRadioButton3.addActionListener(listener);
    }

    public static HashMap getDriveMap() {
        return map;
    }

    public static boolean getIsTypeScan() {
        return isTypeSearch;
    }

    public static Icon getDriveIcon() {
        return driveIcon;
    }

    /*
     * Method to create the dataset for the pie chart.
     */
    private PieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        double totalPercent = 0;
        int count = 0;
        for (DirectoryInfo f : model.getList()) {
            if (count >= 7) {
                break;
            } else {
                double percent = f.getPercent();
                if (percent >= 2) {
                    dataset.setValue(f.getDirectory().getName(), percent);
                    totalPercent += percent;
                    count++;
                }
            }
        }
        if (totalPercent == 0) {
            dataset.setValue("No Data", 100);
        } else {
            double remainingPercent = 100 - totalPercent;
            dataset.setValue("Others", remainingPercent);
        }
        return dataset;
    }

    /**
     * Creates a pie chart
     */
    private JFreeChart createChart(PieDataset dataset) {
        JFreeChart tempChart = ChartFactory.createPieChart(
                "", // chart title
                dataset, // data
                false, // include legend
                true,
                false);

        PiePlot plot = (PiePlot) tempChart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        return tempChart;
    }

    /*
     * Customizes the results table to sort files by size and sets
     * the appropiate cell renderers.
     */
    private void setTable() {
        jTable1.setModel(model);
        TableRowSorter<DirectoryModel> sorter = new TableRowSorter<DirectoryModel>(model);
        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        jTable1.setRowSorter(sorter);
        TableColumnModel tcm = jTable1.getColumnModel();
        TableColumn tc = tcm.getColumn(2);
        tc.setCellRenderer(new PercentRenderer());
        tc = tcm.getColumn(1);
        tc.setCellRenderer(new SizeRenderer(false));
        tc = tcm.getColumn(0);
        tc.setPreferredWidth(155);
        tc.setCellRenderer(new NameRenderer(false));
    }

    class ScanTask extends SwingWorker<ArrayList<DirectoryInfo>, DirectoryInfo> {

        public static final int DRIVE_SCAN = 0;
        public static final int DRIVE_EXTENSION_SCAN = 1;
        public static final int FOLDER_SCAN = 2;
        public static final int FOLDER_EXTENSION_SCAN = 3;
        private int scanType;

        public ScanTask(int type) {
            scanType = type;
        }

        @Override
        protected ArrayList<DirectoryInfo> doInBackground() throws Exception {
            int progress = 0;
            setProgress(0);
            ArrayList<DirectoryInfo> dirs = new ArrayList<DirectoryInfo>();
            String drive = (String) jComboBox1.getSelectedItem();
            DriveInfo di = (DriveInfo) map.get(drive);
            File file = di.getDrive();
            File[] fileList;
            BigDecimal parentSize;
            if (scanType == DRIVE_SCAN || scanType == DRIVE_EXTENSION_SCAN) {
                fileList = file.listFiles();
                jTextField2.setText(di.getDrive().getAbsolutePath());
                parentSize = new BigDecimal(Long.toString(di.getUsedSize()));
            } else {
                fileList = parentFolder.getDirectory().listFiles();
                jTextField2.setText(parentFolder.getDirectory().getAbsolutePath());
                parentSize = new BigDecimal(Long.toString(parentFolder.getSize()));
            }
            int interval = 100 / fileList.length;

            for (File f : fileList) {
                if (isCancelled()) {
                    break;
                }
                BaseFilter filter;
                if (scanType == DRIVE_EXTENSION_SCAN || scanType == FOLDER_EXTENSION_SCAN) {
                    filter = new BaseFilter(extensions);
                } else {
                    filter = new BaseFilter(null);
                }
                setProgress(progress);
                if (f.isDirectory()) {
                    FileUtils.listFiles(f, filter, TrueFileFilter.INSTANCE);
                    long directories = filter.getDirectoryCount();
                    long files = filter.getFileCount();
                    long size = filter.getSize();
                    double percent;
                    if (size == 0) {
                        percent = 0;
                    } else {
                        BigDecimal fileSize = new BigDecimal(Long.toString(size));
                        BigDecimal result = fileSize.divide(parentSize, new MathContext(10)).multiply(
                                BigDecimal.valueOf(100)).stripTrailingZeros();
                        percent = result.doubleValue();
                    }
                    DirectoryInfo dInfo = new DirectoryInfo(files, directories, size, f, percent);
                    dirs.add(dInfo);
                    publish(dInfo);
                } else {
                    if (filter.accept(f)) {
                        BigDecimal fileSize = new BigDecimal(Long.toString(f.length()));
                        BigDecimal result = fileSize.divide(parentSize, new MathContext(10)).multiply(
                                BigDecimal.valueOf(100)).stripTrailingZeros();
                        double percent = result.doubleValue();
                        DirectoryInfo dInfo = new DirectoryInfo(0, 0, f.length(), f, percent);
                        dirs.add(dInfo);
                        publish(dInfo);
                    }
                }
                progress += interval;
                currentScanFolder = f.getAbsolutePath();
                setProgress(progress);
            }
            return dirs;
        }

        @Override
        protected void process(List<DirectoryInfo> chunks) {
            for (DirectoryInfo info : chunks) {
                if ((scanType == DRIVE_EXTENSION_SCAN || scanType == FOLDER_EXTENSION_SCAN) && info.getSize() != 0) {
                    model.addRow(info);
                }
            }
            setTable();
        }

        @Override
        public void done() {
            currentScanFolder = "";
            if (!this.isCancelled()) {
                setProgress(100);
                chart = createChart(createDataset());
                chartPanel.setChart(chart);
                cl.last(jPanel4);
            }
            jButton2.setEnabled(true);
            setCursor(null);
            jButton3.setEnabled(false);
            jLabel9.setText("");
            if (listStack != null) {
                if (!listStack.empty()) {
                    jButton1.setEnabled(true);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase("progress")) {
            int progress = (Integer) evt.getNewValue();
            jProgressBar2.setValue(progress);
            if (scanTask != null) {
                if (scanTask.isCancelled()) {
                    jLabel9.setText("");
                } else {
                    jLabel9.setText(currentScanFolder);
                }
            }
        }
    }

    /*
     * Updates the drive information displayed below the combo box
     * when the selected drive is changed.
     */
    private void setComboBoxInfo() {
        String drive = (String) jComboBox1.getSelectedItem();
        DriveInfo di = (DriveInfo) map.get(drive);
        jLabel5.setText(di.getFormattedTotalSize());
        jLabel6.setText(di.getFormattedUsedSize());
        jLabel7.setText(di.getFormattedFreeSpace());
        BigDecimal fileSize = new BigDecimal(Long.toString(di.getUsedSize()));
        BigDecimal driveSize = new BigDecimal(Long.toString(di.getTotalSize()));
        BigDecimal result = fileSize.divide(driveSize, new MathContext(6)).multiply(
                BigDecimal.valueOf(100)).stripTrailingZeros();
        jProgressBar1.setValue(result.intValue());
        DecimalFormat df = new DecimalFormat("0.00");
        String formatted = df.format(result.doubleValue());
        jProgressBar1.setString(formatted + " %");
        jProgressBar1.setStringPainted(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new JComboBox(driveNames);
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jRadioButton3 = new javax.swing.JRadioButton();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jProgressBar2 = new javax.swing.JProgressBar();
        jButton3 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("File Distribution");

        jSplitPane1.setDividerLocation(320);

        jLabel1.setText("Select Drive:");

        jComboBox1.setMaximumRowCount(3);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        jComboBox1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBox1PropertyChange(evt);
            }
        });

        jLabel2.setText("Total Space:");

        jLabel3.setText("Used Space:");

        jLabel4.setText("Free Space:");

        jProgressBar1.setForeground(new java.awt.Color(204, 0, 51));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, 194, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(30, 30, 30))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel5, jLabel6, jLabel7});

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30))))
        );

        jPanel4.setLayout(new java.awt.CardLayout());

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Full Scan");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Scan by type");

        jCheckBox1.setText("Music");
        jCheckBox1.setEnabled(false);

        jCheckBox2.setText("Video");
        jCheckBox2.setEnabled(false);

        jCheckBox3.setText("Pictures");
        jCheckBox3.setEnabled(false);

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Specific extension");

        jTextField1.setToolTipText("Specify an extension (eg. doc). Separate multiple extensions with ;");
        jTextField1.setEnabled(false);

        jButton2.setText("Scan");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox2)
                            .addComponent(jCheckBox1)
                            .addComponent(jCheckBox3)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jRadioButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addContainerGap(215, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton3)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Current Directory");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/my arrow.jpg"))); // NOI18N
        jButton1.setToolTipText("Go up a directory");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField2.setEditable(false);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jLabel11.setText("Progress");

        jProgressBar2.setForeground(new java.awt.Color(51, 0, 255));
        jProgressBar2.setToolTipText("Scan progress");
        jProgressBar2.setStringPainted(true);

        jButton3.setText("Cancel");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addGap(10, 10, 10)
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "File", "Size", "Percent", "Files", "Folders"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 923, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
    setComboBoxInfo();
}//GEN-LAST:event_jComboBox1ActionPerformed

private void jComboBox1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBox1PropertyChange
    try {
        setComboBoxInfo();
    } catch (Exception e) {
    }
}//GEN-LAST:event_jComboBox1PropertyChange

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    boolean doTask = false;
    if (jRadioButton1.isSelected()) {
        scanTask = new ScanTask(ScanTask.DRIVE_SCAN);
        isTypeSearch = false;
        doTask = true;
    } else if (jRadioButton2.isSelected()) {
        ArrayList<String[]> tempExtensions = new ArrayList<String[]>();
        if (jCheckBox1.isSelected()) {
            tempExtensions.add(music);
        }
        if (jCheckBox2.isSelected()) {
            tempExtensions.add(video);
        }
        if (jCheckBox3.isSelected()) {
            tempExtensions.add(picture);
        }
        ArrayList<String> allExtensions = new ArrayList<String>();
        for (String[] set : tempExtensions) {
            allExtensions.addAll(Arrays.asList(set));
        }
        if (tempExtensions.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a type first.");
            doTask = false;
        } else {
            isTypeSearch = true;
            extensions = allExtensions.toArray(new String[0]);
            scanTask = new ScanTask(ScanTask.DRIVE_EXTENSION_SCAN);
            doTask = true;
        }
    } else {
        String text = jTextField1.getText();
        if (text.equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter an extension first.");
        } else {
            extensions = text.split(";");
            for (int i = 0; i < extensions.length; i++) {
                extensions[i] = extensions[i].trim();
            }
            isTypeSearch = true;
            scanTask = new ScanTask(ScanTask.DRIVE_EXTENSION_SCAN);
            doTask = true;
        }
    }
    if (doTask) {
        model = new DirectoryModel();
        setTable();
        chartPanel.setChart(null);
        jButton2.setEnabled(false);
        jButton3.setEnabled(true);
        listStack.removeAllElements();
        jButton1.setEnabled(false);
        scanTask.addPropertyChangeListener(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        scanTask.execute();
    }
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    if (scanTask != null) {
        scanTask.cancel(true);
    }
    scanTask = null;
    jButton3.setEnabled(false);
    jLabel9.setText("");
    //jProgressBar2.setValue(0);
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    parentFolder = parentStack.pop();
    model = new DirectoryModel(listStack.pop());
    setTable();
    chart = createChart(createDataset());
    chartPanel.setChart(chart);
    cl.last(jPanel4);
    if (listStack.empty()) {
        jButton1.setEnabled(false);
    }
    jTextField2.setText(parentFolder.getDirectory().getParentFile().getAbsolutePath());
}//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
