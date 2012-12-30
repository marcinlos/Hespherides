package hesp.gui;

import hesp.agents.Computation.JobStatus;
import hesp.agents.ProductionListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.Timer;


public class ProductionWindow extends JFrame implements ProductionListener {

    private static final int TIME_BEFORE_REMOVAL = 2000;
    private static final int TIME_BEFORE_REMOVAL_FAIL = 2000;

    private List<TaskPanel> panels = new ArrayList<TaskPanel>();
    private Map<Long, TaskPanel> panelMap = new HashMap<>();

    private JPanel progressPanel;
    private JPanel controlPanel;
    private LogPanel log;

    private void setupUI() {
        setMinimumSize(new Dimension(500, 350));
        setPreferredSize(new Dimension(650, 450));
        setLocationByPlatform(true);
        setLayout(new BorderLayout());

        // Top-left panel (progress bars)
        progressPanel = new JPanel();
        JScrollPane panelScroll = new JScrollPane(progressPanel);
        panelScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

        controlPanel = new JPanel();

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panelScroll, controlPanel);
        topSplit.setResizeWeight(0.5);
        topSplit.setDividerSize(5);
        panelScroll.setMinimumSize(new Dimension(200, 100));

        log = new LogPanel();
        log.setMinimumSize(new Dimension(100, 50));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                topSplit, log);
        mainSplit.setResizeWeight(0.8);
        mainSplit.setDividerSize(5);
        
//        View logView = new View("Log", null, logScroll);
//        
//        ViewMap viewMap = new ViewMap();
//        viewMap.addView(0, new View("Tasks", null, panelScroll));
//        viewMap.addView(1, logView);
//        viewMap.addView(2, new View("Control", null, controlPanel));
//
//        RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, true);
//        
//        rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
//        rootWindow.getWindowBar(Direction.DOWN).addTab(logView);
//        
//        
//        DockingWindowsTheme theme = new ShapedGradientDockingTheme();
//        rootWindow.getRootWindowProperties().addSuperObject(
//        theme.getRootWindowProperties());
//        RootWindowProperties titleBarStyleProperties =
//        PropertiesUtil.createTitleBarStyleRootWindowProperties();
//        // Enable title bar style
//        rootWindow.getRootWindowProperties().addSuperObject(
//        titleBarStyleProperties);
        
        //add(rootWindow);
        
        add(mainSplit, BorderLayout.CENTER);
    }

    public ProductionWindow(String name) {
        super(name);
        setupUI();
    }

    @Override
    public void jobAdded(JobStatus job) {
        TaskPanel taskPanel = new TaskPanel(job);
        taskPanel.setAlignmentX(0.5f);
        panelMap.put(job.getId(), taskPanel);
        panels.add(taskPanel);
        progressPanel.add(taskPanel);
    }

    @Override
    public void jobUpdate(JobStatus job) {
        TaskPanel taskPanel = panelMap.get(job.getId());
        taskPanel.setValue(job.getDone());
        progressPanel.revalidate();
        progressPanel.repaint();
    }


    @Override
    public void jobFinished(JobStatus job) {
        boolean success = job.success();
        final long id = job.getId();
        final TaskPanel taskPanel = panelMap.get(id);
        taskPanel.setValue(job.getDone());
        int time;
        if (success) {
            time = TIME_BEFORE_REMOVAL;
            taskPanel.setBackground(Color.GREEN);
        } else {
            time = TIME_BEFORE_REMOVAL_FAIL;
            taskPanel.setBackground(Color.RED);
        }
        new Timer(time, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelMap.remove(id);
                progressPanel.remove(taskPanel);
                panels.remove(taskPanel);
                progressPanel.revalidate();
                progressPanel.repaint();
            }
        }).start();
    }

    /**
     * Panel with information about job progress.
     */
    private class TaskPanel extends JPanel {

        private JLabel label;
        private JProgressBar progressBar;

        public TaskPanel(JobStatus job) {
            SpringLayout layout = new SpringLayout();
            setLayout(layout);

            label = new JLabel("Job " + job.getId());
            progressBar = new JProgressBar(0, job.getRequired());
            add(label);
            add(progressBar);

            SpringLayout.Constraints cons = layout.getConstraints(label);
            cons.setX(Spring.constant(5));
            cons.setY(Spring.constant(5));
            cons.setWidth(Spring.constant(100));

            cons = layout.getConstraints(progressBar);
            cons.setY(Spring.constant(5));

            layout.putConstraint(SpringLayout.WEST, progressBar, 3,
                    SpringLayout.EAST, label);

            layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST,
                    progressBar);

            Spring height = cons.getHeight();
            height = Spring.sum(Spring.constant(10), height);
            cons = layout.getConstraints(this);
            cons.setHeight(height);
        }

        public void setValue(int value) {
            progressBar.setValue(value);
        }
    }

}
