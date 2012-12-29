package hesp.gui;

import hesp.agents.Computation.JobStatus;

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
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.Timer;



public class ResourceWindow extends JFrame {
    
    private static final int TIME_BEFORE_REMOVAL = 2000;
    private static final int TIME_BEFORE_REMOVAL_FAIL = 2000;

    private JPanel panel;
    private List<TaskPanel> panels = new ArrayList<TaskPanel>();
    private Map<Long, TaskPanel> panelMap = new HashMap<>();
    
    private void setupUI() {
        setMinimumSize(new Dimension(300, 150));
        setPreferredSize(new Dimension(450, 250));
        setLocationByPlatform(true);
        setLayout(new BorderLayout());
        panel = new JPanel();
        add(new JScrollPane(panel));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        setSize(getPreferredSize());
        //setSize(400, 250);
    }

    public ResourceWindow(String name) {
        super("Resource: " + name);
        setupUI();
    }
    
    public void addJob(JobStatus job) {
        TaskPanel taskPanel = new TaskPanel(job);
        taskPanel.setAlignmentX(0.5f);
        panelMap.put(job.getId(), taskPanel);
        panels.add(taskPanel);
        panel.add(taskPanel);
    }
    
    public void update(JobStatus job) {
        TaskPanel taskPanel = panelMap.get(job.getId());                    
        taskPanel.setValue(job.getDone());
        panel.revalidate();
        panel.repaint();
    }
    
    public void finished(JobStatus job, boolean success) {
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
                panel.remove(taskPanel);
                panels.remove(taskPanel);
                panel.revalidate();
                panel.repaint();
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
            cons.setWidth(Spring.constant(80));

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
