package hesp.gui;

import hesp.agents.ClientAgent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class ClientWindow extends JFrame {
    
    public interface Listener {
        void command(String text);
    }

    private ClientAgent client;
    
    private JTextArea commandText;
    private LogPanel logPanel;
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(300, 200));
        setPreferredSize(new Dimension(400, 400));
        
        logPanel = new LogPanel();
        commandText = new JTextArea();
        Font bigger = commandText.getFont().deriveFont(12f);
        commandText.setFont(bigger);
        JScrollPane commandArea = new JScrollPane(commandText);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                commandArea, logPanel);
        
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
    
}
