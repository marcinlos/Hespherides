package hesp.gui;

import hesp.agents.ClientAgent;
import hesp.protocol.Job;
import hesp.protocol.JobParameters;
import jade.core.AID;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

class ClientControlPanel extends JPanel {
    
    private ClientAgent client;
    
    private Random random = new Random();
    private GridBagLayout layout = new GridBagLayout();

    // General widgets
    private JLabel agentLabel;
    private JLabel countLabel;
    
    private JComboBox<String> agentBox;
    private JSpinner countSpinner;
    private JCheckBox indefinitelyCheckbox;
    private JButton startButton;
    
    private JComboBox<String> jobBox;
    private JComboBox<String> timeBox;

    private JPanel jobParamPanel;
    private JPanel timeParameterPanel;

    // Uniform distribution widgets
    private JLabel minCostLabel;
    private JLabel maxCostLabel;
    private JLabel timeLabel;
    
    private RangeSpinnerModel rangeModel;
    private JSpinner lowerSpinner;
    private JSpinner upperSpinner;
    private JSpinner timeSpinner;
    
    // Poisson distribution
    private JLabel densityLabel;
    
    private JSpinner densitySpinner;
    

    public ClientControlPanel(ClientAgent client) {
        this.client = client;
        setLayout(layout);
        createWidgets();
        fillLayout();
        
        /*JobRequestTask task = new JobRequestTask(0, new ExpJobGenerator(100), new PoissonProces(5./1000), new JobRequestListener() {
            
            @Override
            public void requestIssued(Job job) {
                client.postJob(new AID("Res", AID.ISLOCALNAME), job);
            }
        });
        task.start();*/
    }


    private void fillLayout() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(25, 10, 10, 5);
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        add(agentLabel, c);
        
        c.insets.top = 5;
        c.insets.bottom = 5;
        //c.ipady = 5;
        c.gridy = 1;
        add(minCostLabel, c);
        c.gridy = 2;
        add(maxCostLabel, c);
        c.gridy = 3;
        add(countLabel, c);
        c.gridy = 5;
        add(timeLabel, c);
        
        // TODO: 
        /*
         * Złota myśl sylwestra:
         * Wiele lepiej już nie będzie,
         * coś się stanie i coś będzie
         */

        c.insets.right = 10;
        c.insets.left = 5;
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 40;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        add(agentBox, c);

        c.insets.top = 0;
        c.insets.bottom = 0;
        c.ipady = 0;
        c.gridy = 1;
        add(lowerSpinner, c);
        c.gridy = 2;
        add(upperSpinner, c);
        c.gridy = 3;
        add(countSpinner, c);
        c.gridy = 4;
        add(indefinitelyCheckbox, c);
        c.gridy = 5;
        add(timeSpinner, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 3;
        c.weightx = 0.7;
        c.insets = new Insets(0, 0, 0, 10);
        add(startButton, c);

        c = new GridBagConstraints();
        c.gridy = 6;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1.0;
        add(Box.createVerticalGlue(), c);
    }


    private void createWidgets() {
        agentLabel = new JLabel("Agent");
        minCostLabel = new JLabel("Min cost");
        maxCostLabel = new JLabel("Max cost");
        countLabel = new JLabel("Count");
        timeLabel = new JLabel("Delay (ms)");

        agentLabel.setToolTipText("Agent to which job requests shall "
                + "be issued");
        minCostLabel.setToolTipText("Minimal cost of requested job");
        maxCostLabel.setToolTipText("Maximal cost of requested job");
        countLabel.setToolTipText("Amount of job requests to issue");
        timeLabel.setToolTipText("Amount of milliseconds between " + 
                "consecutive requests");
        
        // Create the widgets
        agentBox = new JComboBox<>(new String[] { "Res", "Other" });
        agentBox.setEditable(true);
        rangeModel = new RangeSpinnerModel(1, Integer.MAX_VALUE, 80, 120);
        lowerSpinner = new JSpinner(rangeModel.getLowerModel());
        upperSpinner = new JSpinner(rangeModel.getUpperModel());
        countSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 1000, 1));
        
        indefinitelyCheckbox = new JCheckBox("Indefinitely");
        indefinitelyCheckbox.setToolTipText("If checked, requests are " + 
                "sent indefinitely");
        
        startButton = new JButton("Send");
        startButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                issueRequests();
            }
        });
        
        timeSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 
                Integer.MAX_VALUE, 1));
    }

    
    private void issueRequests() {
        // Collect the data from widgets
        int randMin = rangeModel.getLowerValue();
        int randMax = rangeModel.getUpperValue();
        int count = (int) countSpinner.getValue();
        
        String name = (String) agentBox.getSelectedItem();
        AID agent = new AID(name , AID.ISLOCALNAME);

        for (int i = 0; i < count; ++ i) {
            long id = System.currentTimeMillis() ^ hashCode();
            id ^= random.nextLong();
            int cputime = randMin + random.nextInt(randMax - randMin + 1);
            client.postJob(agent, new Job(id, new JobParameters(cputime)));
        }
    }

}