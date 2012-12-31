package hesp.gui;

import hesp.agents.MetagridManager;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;


public class ManagerWindow extends JFrame {

    //private MetagridManager manager;
    
    private SpringLayout layout = new SpringLayout();
    private JMenuBar menubar;
    private JPanel statusbar;
    private JPanel content;
    private JTree tree;
    private JPanel body;
    
    private void setupUI() {
        setMinimumSize(new Dimension(400, 200));
        setPreferredSize(new Dimension(500, 350));
        setLocationByPlatform(true);
        
        menubar = new JMenuBar();
        menubar.add(new JMenu("File..."));
        setJMenuBar(menubar);
        
        setLayout(new BorderLayout());
        
        statusbar = new JPanel();
        statusbar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusbar, BorderLayout.SOUTH);
        
        body = new JPanel(layout);
        add(body, BorderLayout.CENTER);
        body.setLayout(new BorderLayout());
        
        tree = new JTree();
        JScrollPane scroll = new JScrollPane(tree);
        scroll.setMinimumSize(new Dimension(100, 100));        
        content = new JPanel();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scroll, content);
        split.setResizeWeight(0.2);
        split.setDividerSize(5);
        body.add(split);
        
    }
    
    public ManagerWindow(MetagridManager manager) {
        super("Grid Manager Utility");
        //this.manager = manager;
        setupUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
}
