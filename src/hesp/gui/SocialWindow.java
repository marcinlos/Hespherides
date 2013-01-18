package hesp.gui;

import hesp.agents.SocialAgent;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

public class SocialWindow extends JFrame {

    private SocialAgent agent;
    private LogPanel logPanel;
    //private ClientControlPanel controlPanel;
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(300, 200));
        setPreferredSize(new Dimension(400, 450));

        logPanel = new LogPanel();

        //controlPanel = new ClientControlPanel(client);

        //JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        //        controlPanel, logPanel);

        //add(split, BorderLayout.CENTER);

        //split.setResizeWeight(0.0);
        //split.setDividerSize(5);
    }

    public LogPanel getLogger() {
        return logPanel;
    }

    public SocialWindow(SocialAgent agent) {
        super(agent.getLocalName());
        this.agent = agent;
        setupUI();
    }
    
}
