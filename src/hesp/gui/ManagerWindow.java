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
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;


public class ManagerWindow extends JFrame {

    private MetagridManager manager;
    
    private SpringLayout layout = new SpringLayout();
    private JMenuBar menubar;
    private JToolBar toolbar;
    private JPanel statusbar;
    private JPanel content;
    private JTree tree;
    private JPanel body;
    
    
    private void setupUI() {
        setMinimumSize(new Dimension(400, 200));
        // known swing bug
        //setMaximumSize(new Dimension(800, 500));
        setPreferredSize(new Dimension(500, 350));
        setLocationByPlatform(true);
        
        menubar = new JMenuBar();
        menubar.add(new JMenu("Lasers"));
        menubar.add(new JMenu("Bitches"));
        menubar.add(new JMenu("Shit"));
        setJMenuBar(menubar);
        
        setLayout(new BorderLayout());
        
        statusbar = new JPanel();
        statusbar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusbar, BorderLayout.SOUTH);
        
//        toolbar = new JToolBar("Grid toolbar");
//        toolbar.add(new JButton("Motherfuckin' button"));
//        add(toolbar, BorderLayout.PAGE_START);
        
        body = new JPanel(layout);
        add(body, BorderLayout.CENTER);
        body.setLayout(new BorderLayout());
        
        tree = new JTree();
        tree.setMinimumSize(new Dimension(70, 100));
        content = new JPanel();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree), content);
        split.setResizeWeight(0.2);
        body.add(split);
        
    }
    
    public ManagerWindow(MetagridManager manager) {
        super("Grid Manager Utility");
        this.manager = manager;
        setupUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
}
