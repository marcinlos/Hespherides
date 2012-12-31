package hesp.gui;

import hesp.agents.Computation;
import hesp.agents.ProductionAgent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
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
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(200, 100));
        
        tabPanel = new JTabbedPane();
        add(tabPanel);
        
        computationPanel = new ComputationPanel();
        policyPanel = new PolicyPanel();
        tabPanel.addTab("Computation", computationPanel);
        tabPanel.addTab("Policy", policyPanel);
    }
    
    public ProductionControlPanel(ProductionAgent agent) {
        this.agent = agent;
        this.computation = agent.getComputation();
        setupUI();
    }
    
    
    private class ComputationPanel extends JPanel {
        
        public ComputationPanel() {
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);

            GridBagConstraints c = new GridBagConstraints();
            JLabel label = new JLabel("Processors");
            final JSlider slider = 
                    new JSlider(SwingConstants.HORIZONTAL, 0, 20, 1);
            slider.setMajorTickSpacing(5);
            slider.setMinorTickSpacing(1);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setSnapToTicks(true);
            
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (! slider.getValueIsAdjusting()) {
                        computation.setProcessors(slider.getValue());
                    }
                }
            });
            add(label);
            add(slider);
        }
        
    }
    
    
    private class PolicyPanel extends JPanel {
        
        public PolicyPanel() {
            
        }
        
    }
    
}
