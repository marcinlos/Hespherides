package hesp.gui;

import hesp.agents.ClientAgent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;

public class ClientWindow extends JFrame {

    public interface Listener {
        void command(String text);
    }

    private ClientAgent client;

    private JTextArea commandText;
    private LogPanel logPanel;
    private ClientControlPanel controlPanel;

    private void setupUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(300, 200));
        setPreferredSize(new Dimension(400, 400));

        logPanel = new LogPanel();
        commandText = new JTextArea();
        Font bigger = commandText.getFont().deriveFont(12f);
        commandText.setFont(bigger);

        controlPanel = new ClientControlPanel();

        //JScrollPane commandArea = new JScrollPane(commandText);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        /* commandArea */controlPanel, logPanel);

        add(split, BorderLayout.CENTER);
        JButton exec = new JButton("Execute");

        exec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.executeCommand(commandText.getText());
            }
        });
        add(exec, BorderLayout.PAGE_END);

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

        public ClientControlPanel() {
            setLayout(layout);

            JLabel agentLabel = new JLabel("Agent");
            // agentLabel.setBorder(new LineBorder(Color.BLACK));
            agentLabel.setToolTipText("Agent to which job request shall "
                    + "be issued");
            JComboBox<String> agentBox = new JComboBox<>(new String[] { "Res",
                    "Other" });
            // agentBox.setBorder(new LineBorder(Color.BLACK));

            JLabel minCostLabel = new JLabel("Min cost");
            JLabel maxCostLabel = new JLabel("Max cost");
            JLabel countLabel = new JLabel("Count");
            
            rangeModel = new RangeSpinnerModel(40, 150, 80, 120);
            lowerSpinner = new JSpinner(rangeModel.getLowerModel());
            //lowerSpinner.setBorder(new LineBorder(Color.GREEN));
            lowerSpinner.setEditor(new JSpinner.NumberEditor(lowerSpinner));
            upperSpinner = new JSpinner(rangeModel.getUpperModel());
            //upperSpinner.setBorder(new LineBorder(Color.GREEN));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(15, 10, 10, 5);
            c.anchor = GridBagConstraints.BASELINE_LEADING;
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
             * Wiele lepiej już nie będzie,
             * coś się stanie i coś będzie
             */

            c.anchor = GridBagConstraints.BASELINE_LEADING;
            c.insets.right = 30;
            c.insets.left = 5;
            c.gridx = 1;
            c.gridy = 0;
            c.ipadx = 40;
            c.weightx = 0.3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_END;
            add(agentBox, c);

            c.insets.top = 0;
            c.insets.bottom = 0;
            c.ipady = 0;
            c.gridy = 1;
            add(lowerSpinner, c);
            c.gridy = 2;
            add(upperSpinner, c);
            //c.gridy = 3;
            //add(countLabel, c);
            
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.gridheight = GridBagConstraints.REMAINDER;
            c.weightx = 0.7;
            JPanel panel = new JPanel();
            //panel.setBackground(Color.RED);
            add(panel, c);

            c = new GridBagConstraints();
            c.gridy = 4;
            c.fill = GridBagConstraints.VERTICAL;
            c.weighty = 1.0;
            add(Box.createVerticalGlue(), c);

        }

    }

}
