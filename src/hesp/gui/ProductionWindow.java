package hesp.gui;

import hesp.agents.JobProgress;
import hesp.agents.ProductionAgent;
import hesp.agents.ProductionListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.Timer;

public class ProductionWindow extends JFrame implements ProductionListener {

    private static final int TIME_BEFORE_REMOVAL = 2000;
    private static final int TIME_BEFORE_REMOVAL_FAIL = 2000;

    private ProductionAgent agent;
    private List<JobProgress> jobsInProgress = new ArrayList<>();

    private ProductionControlPanel controlPanel;
    private LogPanel logPanel;
    
    private ProgressPanel progressPanel = new ProgressPanel(jobsInProgress);
    
    /**
     * @return row containing job with given id, or -1 if there is none
     */
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

        controlPanel = new ProductionControlPanel(agent);
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                progressPanel, controlPanel);
        topSplit.setResizeWeight(0.5);
        topSplit.setDividerSize(5);
        progressPanel.setMinimumSize(new Dimension(200, 100));

        logPanel = new LogPanel();
        logPanel.setMinimumSize(new Dimension(100, 50));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                topSplit, logPanel);
        mainSplit.setResizeWeight(0.9);
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

    public ProductionWindow(ProductionAgent agent) {
        super(agent.getLocalName());
        this.agent = agent;
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
            // Send the notification immediately to get nice "100%" in % column
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

}
