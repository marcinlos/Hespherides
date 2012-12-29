package hesp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class ClientWindow extends JFrame {
    
    public interface Listener {
        void command(String text);
    }

    private Listener listener;
    
    private JTextArea commandText;
    private JTextArea logArea;
    private JScrollPane scroll;
    
    private void setupUI() {
        setLayout(new BorderLayout());
        logArea = new JTextArea("Event log");
        logArea.setEditable(false);
        scroll = new JScrollPane(logArea);
        commandText = new JTextArea();
        JScrollPane commandArea = new JScrollPane(commandText);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                commandArea, scroll);
        
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        add(split, BorderLayout.CENTER);
        JButton exec = new JButton("Execute");
        
        exec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.command(commandText.getText());
                }
            }
        });
        add(exec, BorderLayout.PAGE_END);
        
        split.setResizeWeight(0.7);
        setPreferredSize(new Dimension(400, 400));
    }
    
    public void addMessage(String message) {
        logArea.append("\n" + message);
    }
    
    public ClientWindow(String name, Listener listener) {
        super(name);
        this.listener = listener;
        setupUI();
    }
    
}
