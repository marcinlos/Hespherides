package hesp.gui;

import hesp.agents.ClientAgent;
import hesp.protocol.Job;
import hesp.traffic.ExpJobGenerator;
import hesp.traffic.FixedDelay;
import hesp.traffic.JobGenerator;
import hesp.traffic.JobGeneratorProvider;
import hesp.traffic.PoissonProces;
import hesp.traffic.TimeDistribution;
import hesp.traffic.TimeDistributionProvider;
import hesp.traffic.UniformJobGenerator;
import jade.core.AID;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class ClientControlPanel extends JPanel {

    private ClientAgent client;

    private GridBagLayout layout = new GridBagLayout();

    // General widgets
    private JLabel agentLabel;
    private JLabel jobLabel;
    private JLabel timeLabel;
    private JLabel countLabel;

    private JComboBox<String> agentBox;
    private JSpinner countSpinner;
    private JCheckBox indefinitelyCheckbox;
    private JButton startButton;

    private JComboBox<String> jobBox;
    private JComboBox<String> timeBox;

    private JobParamsPanel jobParamPanel;
    private TimeParamsPanel timeParameterPanel;

    public ClientControlPanel(ClientAgent client) {
        this.client = client;
        setLayout(layout);
        createWidgets();
        fillLayout();
    }

    private void fillLayout() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(25, 10, 10, 5);
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        add(agentLabel, c);

        c.insets.top = 5;
        c.insets.bottom = 5;
        c.gridy = 1;
        add(jobLabel, c);

        c.gridy = 3;
        add(timeLabel, c);
        
        c.gridy = 6;
        add(countLabel, c);

        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.7;

        add(jobParamPanel, c);

        c.gridy = 4;
        add(timeParameterPanel, c);

        c.insets.right = 10;
        c.insets.left = 5;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 3;
        c.ipadx = 40;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        add(agentBox, c);

        c.insets.top = 0;
        c.insets.bottom = 0;
        c.ipady = 0;
        c.gridy = 1;
        add(jobBox, c);
        c.gridy = 3;
        add(timeBox, c);
        
        c.gridy = 6;
        c.gridx = 1;
        add(countSpinner, c);
        c.gridy = 7;
        c.insets.right = 0;
        c.ipadx = 5;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        add(indefinitelyCheckbox, c);

        c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 3;
        c.weightx = 1.2;
        c.insets = new Insets(0, 0, 0, 10);
        add(startButton, c);

        c = new GridBagConstraints();
        c.gridy = 8;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1.0;
        add(Box.createVerticalGlue(), c);
    }

    private void createWidgets() {
        agentLabel = new JLabel("Agent");
        jobLabel = new JLabel("Job generator");
        countLabel = new JLabel("Count");
        timeLabel = new JLabel("Time distribution");

        agentLabel.setToolTipText("Agent to which job requests shall "
                + "be issued");

        jobParamPanel = new JobParamsPanel();
        timeParameterPanel = new TimeParamsPanel();

        // Create the widgets
        agentBox = new JComboBox<>(new String[] { "Res", "Other..." });
        agentBox.setEditable(true);

        indefinitelyCheckbox = new JCheckBox("Indefinitely");
        indefinitelyCheckbox.setToolTipText("If checked, requests are "
                + "sent indefinitely");

        jobBox = new JComboBox<>(new String[] { "Exponential", "Uniform" });
        jobBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String type = (String) jobBox.getSelectedItem();
                jobParamPanel.switchToCard(type);
            }
        });
        
        timeBox = new JComboBox<>(new String[] { "Poisson", "Fixed" });
        timeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String type = (String) timeBox.getSelectedItem();
                timeParameterPanel.switchToCard(type);
            }
        });
        
        countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 
                Integer.MAX_VALUE, 1));

        startButton = new JButton("Send");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                issueRequests();
            }
        });
    }

    private void issueRequests() {
        JobGenerator jobGen = jobParamPanel.getJobGenerator();
        TimeDistribution timeGen = timeParameterPanel.getTimeDistribution();
        int count = (int) countSpinner.getValue();
        if (indefinitelyCheckbox.isSelected()) {
            count = JobRequestTask.INDEFINITELY;
        }

        String name = (String) agentBox.getSelectedItem();
        final AID agent = new AID(name, AID.ISLOCALNAME);

        JobRequestTask task = new JobRequestTask(count, jobGen, timeGen, 
            new JobRequestListener() {
                @Override public void requestIssued(Job job) {
                    client.postJob(agent, job);
                }
            });
        task.start();
    }

    /*
     * GUI for job parameters panel
     */

    private class ExponentialParamsPanel extends JPanel implements
            JobGeneratorProvider {

        private GridBagLayout layout = new GridBagLayout();
        private SpinnerNumberModel model;

        public ExponentialParamsPanel() {
            setLayout(layout);
            JLabel avgLabel = new JLabel("Average");
            model = new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 1);
            JSpinner spinner = new JSpinner(model);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 10, 10, 5);
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.gridy = 0;
            add(avgLabel, c);
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0.3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.insets.top = 0;
            c.insets.bottom = 0;
            c.ipady = 0;
            add(spinner, c);
        }

        @Override
        public JobGenerator getGenerator() {
            int value = (int) model.getValue();
            return new ExpJobGenerator(value);
        }
    }

    private class UniformParamsPanel extends JPanel implements
            JobGeneratorProvider {

        private GridBagLayout layout = new GridBagLayout();
        private RangeSpinnerModel model;

        public UniformParamsPanel() {
            setLayout(layout);

            JLabel uniformMinLabel = new JLabel("Min");
            JLabel uniformMaxLabel = new JLabel("Max");
            uniformMinLabel.setToolTipText("Minimal cost of requested job");
            uniformMaxLabel.setToolTipText("Maximal cost of requested job");
            model = new RangeSpinnerModel(1, Integer.MAX_VALUE, 80, 120);
            JSpinner minSpinner = new JSpinner(model.getLowerModel());
            JSpinner maxSpinner = new JSpinner(model.getUpperModel());

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 10, 10, 5);
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            add(uniformMinLabel, c);
            c.gridy = 1;
            add(uniformMaxLabel, c);

            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0.3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.insets.top = 0;
            c.insets.bottom = 0;
            c.ipady = 0;
            add(minSpinner, c);
            c.gridy = 1;
            add(maxSpinner, c);
        }

        @Override
        public JobGenerator getGenerator() {
            int min = model.getLowerValue();
            int max = model.getUpperValue();
            return new UniformJobGenerator(min, max);
        }
    }

    private class JobParamsPanel extends JPanel {

        public static final String EXPONENTIAL = "Exponential";
        public static final String UNIFORM = "Uniform";
        private CardLayout layout;

        private ExponentialParamsPanel expCard = new ExponentialParamsPanel();
        private UniformParamsPanel uniformCard = new UniformParamsPanel();

        private Map<String, JobGeneratorProvider> providers = new HashMap<>();
        private JobGeneratorProvider current;

        public JobParamsPanel() {
            layout = new CardLayout();
            setLayout(layout);
            Border expBorder = new TitledBorder("Job parameters");
            setBorder(expBorder);
            add(expCard, EXPONENTIAL);
            add(uniformCard, UNIFORM);

            current = expCard;
            providers.put(EXPONENTIAL, expCard);
            providers.put(UNIFORM, uniformCard);
        }

        public void switchToCard(String name) {
            layout.show(this, name);
            current = providers.get(name);
        }

        public JobGenerator getJobGenerator() {
            return current.getGenerator();
        }

    }

    /*
     * GUI for time distribution parameters panel
     */

    private class PoissonParamsPanel extends JPanel implements
            TimeDistributionProvider {

        private GridBagLayout layout = new GridBagLayout();
        private SpinnerNumberModel model;

        public PoissonParamsPanel() {
            setLayout(layout);
            JLabel lambdaLabel = new JLabel("Lambda");
            lambdaLabel.setToolTipText("Intensity parameter - amount of "
                    + "requests per second (unit: 1/s)");
            model = new SpinnerNumberModel(1.0, 0.1, 1000.0, 0.1);
            JSpinner spinner = new JSpinner(model);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 10, 10, 5);
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.gridy = 0;
            add(lambdaLabel, c);
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0.3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.insets.top = 0;
            c.insets.bottom = 0;
            c.ipady = 0;
            add(spinner, c);
        }

        @Override
        public TimeDistribution getDistribution() {
            double lambda = (double) model.getValue();
            return new PoissonProces(lambda);
        }

    }

    private class FixedParamsPanel extends JPanel implements
            TimeDistributionProvider {

        private GridBagLayout layout = new GridBagLayout();
        private SpinnerNumberModel model;

        public FixedParamsPanel() {
            setLayout(layout);
            JLabel gapLabel = new JLabel("Gap");
            gapLabel.setToolTipText("Time (in seconds) between consecutive "
                    + "requests");
            model = new SpinnerNumberModel(0.2, 0.1, 1000.0, 0.1);
            JSpinner spinner = new JSpinner(model);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 10, 10, 5);
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.gridy = 0;
            add(gapLabel, c);
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0.3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.insets.top = 0;
            c.insets.bottom = 0;
            c.ipady = 0;
            add(spinner, c);
        }

        @Override
        public TimeDistribution getDistribution() {
            double delay = (double) model.getValue();
            return new FixedDelay(delay);
        }

    }

    private class TimeParamsPanel extends JPanel {

        public static final String POISSON = "Poisson";
        public static final String FIXED = "Fixed";
        private CardLayout layout;

        private PoissonParamsPanel poissonCard = new PoissonParamsPanel();
        private FixedParamsPanel fixedCard = new FixedParamsPanel();

        private Map<String, TimeDistributionProvider> providers = new HashMap<>();
        private TimeDistributionProvider current;

        public TimeParamsPanel() {
            layout = new CardLayout();
            setLayout(layout);
            Border expBorder = new TitledBorder("Time distribution parameters");
            setBorder(expBorder);
            add(poissonCard, POISSON);
            add(fixedCard, FIXED);

            current = poissonCard;
            providers.put(POISSON, poissonCard);
            providers.put(FIXED, fixedCard);
        }

        public void switchToCard(String name) {
            layout.show(this, name);
            current = providers.get(name);
        }

        public TimeDistribution getTimeDistribution() {
            return current.getDistribution();
        }

    }

}