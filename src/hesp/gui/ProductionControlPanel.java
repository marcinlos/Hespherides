package hesp.gui;

import hesp.agents.Computation;
import hesp.agents.ProductionAgent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author marcinlos
 *
 * Panel with elements controlling behaviour and parameters of Production
 * Agent, and displaying its state.
 */
public class ProductionControlPanel extends JPanel {
    
    private ProductionAgent agent;
    private Computation computation;
    
    private JTabbedPane tabPanel;
    private ComputationPanel computationPanel;
    private PolicyPanel policyPanel;
    private WorkloadBar workloadBar;
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(200, 100));
        
        tabPanel = new JTabbedPane();
        computationPanel = new ComputationPanel();
        policyPanel = new PolicyPanel();
        tabPanel.addTab("Computation", computationPanel);
        tabPanel.addTab("Policy", policyPanel);
        int workload = computation.workload();
        int freeProcessors = computation.freeProcessors();
        workloadBar = new WorkloadBar(workload, freeProcessors);
        
        add(tabPanel);
        add(workloadBar, BorderLayout.PAGE_END);
    }
    
    public ProductionControlPanel(ProductionAgent agent) {
        this.agent = agent;
        this.computation = agent.getComputation();
        setupUI();
    }
    
    
    private class ComputationPanel extends JPanel {
        
        private JLabel processorsLabel;
        private JSlider slider;
        private JLabel failLabel;
        private JSpinner failSpinner;

        public ComputationPanel() {
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            
            createWidgets();
            fillLayout();
        }

        private void createWidgets() {
            processorsLabel = new JLabel("Processors");
            processorsLabel.setToolTipText("Number of independent " + 
                    "processing units");
            processorsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            
            failLabel = new JLabel("Fail chance");
            failLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            failLabel.setToolTipText("Probability (0-1) of failure in each " + 
                    "time slice");
            
            int procs = agent.getComputation().getProcessors();
            slider = new JSlider(SwingConstants.HORIZONTAL, 0, 20, procs);
            slider.setMajorTickSpacing(5);
            slider.setMinorTickSpacing(1);
            slider.setPaintTicks(true);
            //slider.setPaintLabels(true);
            slider.setSnapToTicks(true);
            //slider.setBorder(BorderFactory.createLineBorder(Color.GREEN));
            
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (! slider.getValueIsAdjusting()) {
                        int value = slider.getValue();
                        computation.setProcessors(value);
                        int workload = computation.workload();
                        workloadChanged(workload, value);
                    }
                }
            });
            
            double chance = computation.getFailChance();
            failSpinner = new JSpinner(
                    new SpinnerNumberModel(chance, 0, 1, 0.01));
            failSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    double value = (double) failSpinner.getValue();
                    computation.setFailChance(value);
                }
            });
        }

        private void fillLayout() {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(15, 10, 5, 2);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_END;
            c.ipadx = c.ipady = 0;
            c.ipadx = 10;
            add(processorsLabel, c);
            c.gridx = 1;
            c.weightx = 0.8;
            c.insets = new Insets(15, 2, 5, 5);
            c.ipadx = c.ipady = 0;
            add(slider, c);

            c.gridy = 1;
            c.gridx = 0;
            c.weightx = 0.0;
            c.insets.top = 5;
            add(failLabel, c);
            c.gridx = 1;
            add(failSpinner, c);
            
            c = new GridBagConstraints();
            c.gridy = 8;
            c.fill = GridBagConstraints.VERTICAL;
            c.weighty = 1.0;
            add(Box.createVerticalGlue(), c);
        }
        
    }
    
    public void workloadChanged(int workload, int max) {
        workloadBar.setMaxValue(max);
        workloadBar.setValue(workload);
    }
    
    
    private class PolicyPanel extends JPanel {
        
        public PolicyPanel() {
            
        }
        
    }
    
}
