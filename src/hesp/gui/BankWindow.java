package hesp.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableModel;

public class BankWindow extends JFrame {

    private void setupUI(TableModel model) {
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        add(scrollPane);
        
        JTextArea logArea = new JTextArea();
        JScrollPane commandArea = new JScrollPane(logArea);
        logArea.setEditable(false);
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                scrollPane, commandArea);
        
        split.setResizeWeight(0.7);
        split.setDividerSize(5);
        add(split);
        setPreferredSize(new Dimension(500, 300));
    }

    
    public BankWindow(String name, TableModel model) {
        super(name);
        setupUI(model);
    }
    
}
