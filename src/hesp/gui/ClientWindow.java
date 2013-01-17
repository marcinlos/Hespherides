package hesp.gui;

import hesp.agents.ClientAgent;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

/**
 * Window controlling actions, parameters and behaviours of Client Agent.
 * 
 * @author marcinlos
 */
public class ClientWindow extends JFrame {

    private ClientAgent client;
    private LogPanel logPanel;
    private ClientControlPanel controlPanel;
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(300, 200));
        setPreferredSize(new Dimension(400, 450));

        logPanel = new LogPanel();

        controlPanel = new ClientControlPanel(client);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                controlPanel, logPanel);

        add(split, BorderLayout.CENTER);

        split.setResizeWeight(0.0);
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

}
