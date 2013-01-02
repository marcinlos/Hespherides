package hesp.gui;

import hesp.agents.ClientAgent;
import hesp.protocol.Job;

import jade.core.AID;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class ClientWindow extends JFrame {

    public interface Listener {
        void command(String text);
    }

    private ClientAgent client;

    private JTextArea commandText;
    private LogPanel logPanel;
    private ClientControlPanel controlPanel;
    
    private Random random = new Random();

    private void setupUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(300, 200));
        setPreferredSize(new Dimension(400, 400));

        logPanel = new LogPanel();
        commandText = new JTextArea();
        Font bigger = commandText.getFont().deriveFont(12f);
        commandText.setFont(bigger);

        controlPanel = new ClientControlPanel();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                controlPanel, logPanel);

        add(split, BorderLayout.CENTER);

        split.setResizeWeight(0.7);
        split.setDividerSize(5);
    }

    public LogPanel getLogger() {
        return logPanel;
    }

    public ClientWindow(ClientAgent client) {
        super(client.getLocalName());
        this.client = client;
        setupUI();
    }

    private class ClientControlPanel extends JPanel {

        private GridBagLayout layout = new GridBagLayout();
        private RangeSpinnerModel rangeModel;
        private JSpinner lowerSpinner;
        private JSpinner upperSpinner;
        private JSpinner countSpinner;
        private JCheckBox indefinitelyCheckbox;
        private JComboBox<String> agentBox;
        private JLabel agentLabel;
        private JLabel minCostLabel;
        private JLabel maxCostLabel;
        private JLabel countLabel;
        private JButton button;

        public ClientControlPanel() {
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
            //c.ipady = 5;
            c.gridy = 1;
            add(minCostLabel, c);
            c.gridy = 2;
            add(maxCostLabel, c);
            c.gridy = 3;
            add(countLabel, c);
            
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
            
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.gridheight = 3;
            c.weightx = 0.7;
            c.insets = new Insets(0, 0, 0, 10);
            add(button, c);

            c = new GridBagConstraints();
            c.gridy = 4;
            c.fill = GridBagConstraints.VERTICAL;
            c.weighty = 1.0;
            add(Box.createVerticalGlue(), c);
        }


        private void createWidgets() {
            agentLabel = new JLabel("Agent");
            minCostLabel = new JLabel("Min cost");
            maxCostLabel = new JLabel("Max cost");
            countLabel = new JLabel("Count");

            agentLabel.setToolTipText("Agent to which job requests shall "
                    + "be issued");
            minCostLabel.setToolTipText("Minimal cost of requested job");
            maxCostLabel.setToolTipText("Maximal cost of requested job");
            countLabel.setToolTipText("Amount of job requests to issue");
            
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
            
            button = new JButton("Send");
            button.addActionListener(new ActionListener() {
                @Override 
                public void actionPerformed(ActionEvent e) {
                    issueRequests();
                }
            });
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
                client.postJob(agent, new Job(id, cputime));
            }
        }

    }

}
