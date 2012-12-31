package hesp.gui;

import hesp.agents.JobProgress;
import hesp.agents.JobStatus;
import hesp.agents.ProductionListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ProductionWindow extends JFrame implements ProductionListener {

    private static final int TIME_BEFORE_REMOVAL = 2000;
    private static final int TIME_BEFORE_REMOVAL_FAIL = 2000;

    private List<JobProgress> jobsInProgress = new ArrayList<>();

    private JPanel controlPanel;
    private LogPanel logPanel;
    
    private ProgressPanel progressPanel = new ProgressPanel();
    
    
    private int findJob(long id) {
        int i = 0;
        for (JobProgress job : jobsInProgress) {
            if (job.getId() == id) {
                return i;
            } else {
                ++i;
            }
        }
        return -1;
    }
    
    private void setupUI() {
        setMinimumSize(new Dimension(500, 350));
        setPreferredSize(new Dimension(650, 450));
        setLocationByPlatform(true);
        setLayout(new BorderLayout());

        controlPanel = new JPanel();
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                progressPanel, controlPanel);
        topSplit.setResizeWeight(0.5);
        topSplit.setDividerSize(5);
        progressPanel.setMinimumSize(new Dimension(200, 100));

        logPanel = new LogPanel();
        logPanel.setMinimumSize(new Dimension(100, 50));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                topSplit, logPanel);
        mainSplit.setResizeWeight(0.8);
        mainSplit.setDividerSize(5);

        // View logView = new View("Log", null, logScroll);
        //
        // ViewMap viewMap = new ViewMap();
        // viewMap.addView(0, new View("Tasks", null, panelScroll));
        // viewMap.addView(1, logView);
        // viewMap.addView(2, new View("Control", null, controlPanel));
        //
        // RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, true);
        //
        // rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
        // rootWindow.getWindowBar(Direction.DOWN).addTab(logView);
        //
        //
        // DockingWindowsTheme theme = new ShapedGradientDockingTheme();
        // rootWindow.getRootWindowProperties().addSuperObject(
        // theme.getRootWindowProperties());
        // RootWindowProperties titleBarStyleProperties =
        // PropertiesUtil.createTitleBarStyleRootWindowProperties();
        // // Enable title bar style
        // rootWindow.getRootWindowProperties().addSuperObject(
        // titleBarStyleProperties);

        // add(rootWindow);

        add(mainSplit, BorderLayout.CENTER);
    }

    public ProductionWindow(String name) {
        super(name);
        setupUI();
    }

    @Override
    public void jobAdded(JobProgress job) {
        jobsInProgress.add(job);
        progressPanel.rowAdded(jobsInProgress.size() - 1);
    }

    @Override
    public void jobUpdate(JobProgress job) {
        int row = findJob(job.getId());
        jobsInProgress.set(row, job);
        progressPanel.rowUpdated(row);
    }

    @Override
    public void jobFinished(final JobProgress job) {
        boolean success = job.hasSucceeded();
        int time;
        if (success) {
            time = TIME_BEFORE_REMOVAL;
            int row = findJob(job.getId());
            progressPanel.rowUpdated(row);
        } else {
            time = TIME_BEFORE_REMOVAL_FAIL;
        }
        Timer timer = new Timer(time, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = findJob(job.getId());
                jobsInProgress.remove(row);
                progressPanel.rowDeleted(row);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public LogPanel getLogger() {
        return logPanel;
    }

    /**
     * Model of table showing job progress and state. 
     */
    private static class JobProgressModel extends AbstractTableModel {

        private static final String[] columnNames = {
            "Job id", "% done", "Progress"
        };
        
        public static class Value {
            public final JobStatus status;
            public final Object value;
            
            public Value(JobStatus status, Object value) {
                this.status = status;
                this.value = value;
            }
        }
        
        private final List<JobProgress> jobsInProgress;
        
        public JobProgressModel(List<JobProgress> jobsInProgress) {
            this.jobsInProgress = jobsInProgress;
        }
        
        @Override
        public String getColumnName(int col) {
            return columnNames[col].toString();
        }
        
        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return jobsInProgress.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            JobProgress job = jobsInProgress.get(rowIndex);
            return job;
        }

    }

    private class JobProgressBarRenderer extends JPanel implements
            TableCellRenderer {

        private JProgressBar progressBar = new JProgressBar();
        
        public JobProgressBarRenderer() {
            setLayout(new BorderLayout());
            add(progressBar);
            progressBar.setMinimum(0);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            JobProgress status = (JobProgress) value;
            progressBar.setMaximum(status.getWorkRequired());
            progressBar.setValue(status.getWorkDone());
            return this;
        }

    }
    
    /**
     * Displays text information about job progress with appropriate background.
     */
    private class JobProgressCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean selected, boolean focused, int row,
                int column) {
            JobProgress status = (JobProgress) value;
            if (status.hasSucceeded()) {
                setBackground(Color.GREEN);
            } else if (status.hasFailed()) {
                setBackground(Color.RED);
            } else {
                setBackground(null);
            }
            setOpaque(true);
            Object actualValue = null;
            switch (column) {
            case 0:
                actualValue = status.getId();
                setHorizontalAlignment(SwingConstants.LEFT);
                break;
            case 1:
                actualValue = String.format("%.2f%%", status.getCompletionPercent());
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
            super.getTableCellRendererComponent(table, actualValue, selected,
                    focused, row, column);

            return this;
        }
    }
    
    private class ProgressPanel extends JPanel {

        private JTable table;
        private AbstractTableModel model = new JobProgressModel(jobsInProgress);

        public ProgressPanel() {
            setLayout(new BorderLayout());
            table = new JTable(model);
            table.setFillsViewportHeight(true);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
            
            // Renderer is currently position-based, and reordering columns
            // displays weird, if consistent and predictable, behaviour.
            JTableHeader header = table.getTableHeader();
            header.setReorderingAllowed(false);
            
            JScrollPane scroller = new JScrollPane(table);
            add(scroller);
            
            TableColumnModel column = table.getColumnModel();
            TableColumn idCol = column.getColumn(0);
            TableColumn percentageCol = column.getColumn(1);

            TableCellRenderer textRenderer = new JobProgressCellRenderer();
            idCol.setCellRenderer(textRenderer);
            percentageCol.setCellRenderer(textRenderer);

            idCol.setMinWidth(80);
            percentageCol.setMinWidth(50);
            
            TableColumn progressCol = column.getColumn(2);
            progressCol.setCellRenderer(new JobProgressBarRenderer());
        }
        
        public void rowAdded(int row) {
            model.fireTableRowsDeleted(row, row);
        }
        
        public void rowUpdated(int row) {
            model.fireTableRowsUpdated(row, row);
        }
        
        public void rowDeleted(int row) {
            model.fireTableRowsDeleted(row, row);
        }
    }

}
