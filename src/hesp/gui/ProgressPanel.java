package hesp.gui;

import hesp.agents.JobProgress;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author marcinlos
 * 
 * Panel displaying progress of jobs.
 */
class ProgressPanel extends JPanel {

    private JTable table;
    private AbstractTableModel model;
    private final List<JobProgress> jobsInProgress;
    
    public ProgressPanel(List<JobProgress> jobsInProgress) {
        setLayout(new BorderLayout());
        this.jobsInProgress = jobsInProgress;
        model = new JobProgressModel();
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
        
        table.setShowGrid(false);
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
    
    /**
     * Renders progress bar as a cell in the table.
     */
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
            // Set the background color depending on execution status
            if (status.hasSucceeded()) {
                setBackground(Colors.SUCCESS);
            } else if (status.hasFailed()) {
                setBackground(Colors.FAILURE);
            } else {
                setBackground(null);
            }
            setOpaque(true);
            Object actualValue = null;
            switch (column) {
            case 0:
                actualValue = String.format("%x", status.getId());
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

    /**
     * Model of table showing job progress and state. 
     */
    private class JobProgressModel extends AbstractTableModel {
    
        private final String[] columnNames = {
            "Job id", "% done", "Progress"
        };
    
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
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
    
}