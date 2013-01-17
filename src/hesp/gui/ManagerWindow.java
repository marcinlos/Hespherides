package hesp.gui;

import hesp.agents.MetagridManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


public class ManagerWindow extends JFrame {

    private MetagridManager manager;
    
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
        JMenu menu = new JMenu("File...");
        menubar.add(menu);
        menu.add(new AbstractAction("Create bank") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                manager.createBank();
            }
        });
        setJMenuBar(menubar);
        
        setLayout(new BorderLayout());
        
        statusbar = new JPanel();
        statusbar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusbar, BorderLayout.SOUTH);
        
        body = new JPanel(layout);
        add(body, BorderLayout.CENTER);
        body.setLayout(new BorderLayout());
        
        tree = new JTree(new Model());
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
        this.manager = manager;
        setupUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private class Model implements TreeModel {
        
        private List<TreeModelListener> listeners = new ArrayList<>();

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public Object getChild(Object parent, int index) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getRoot() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isLeaf(Object node) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
}
