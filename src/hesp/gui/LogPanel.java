package hesp.gui;

import hesp.util.AbstractLogSink;
import hesp.util.LogItem;
import hesp.util.LogItem.Level;
import hesp.util.LogSink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author marcinlos
 *
 * Graphical implementation of logger, displaying log messages as a list of 
 * string labels.
 */
public class LogPanel extends JPanel {

    private JList<LogItem> list;
    private JScrollPane scroller;
    private DefaultListModel<LogItem> model;
    
    /** Mapping of log levels to colors */
    private static final Map<Level, Color> colors = new EnumMap<>(Level.class);
    
    static {
        colors.put(Level.ERROR, new Color(255, 135, 135));
        colors.put(Level.INFO, Color.WHITE);
        colors.put(Level.SUCCESS, new Color(135, 255, 135));
    }

    /**
     * Dedicated renderer, displaying items as text labels with background
     * of appropriate color.
     */
    private class LogItemRenderer extends JComponent implements
            ListCellRenderer<LogItem> {

        private JLabel text;

        public LogItemRenderer() {
            setLayout(new BorderLayout());
            text = new JLabel();
            text.setOpaque(true);
            text.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                    Color.BLACK));
            add(text);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends LogItem> list, LogItem value, int index,
                boolean isSelected, boolean cellHasFocus) {
            text.setText(value.getText());
            Color color = colors.get(value.getLevel());
            text.setBackground(color);
            return this;
        }

    }

    private void setupUI() {
        setLayout(new BorderLayout());
        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setCellRenderer(new LogItemRenderer());
        scroller = new JScrollPane(list);
        add(scroller);
    }

    public LogPanel() {
        setupUI();
    }
    
    /**
     * @return log message sink corresponding to this component
     */
    public LogSink logSink() {
        return sink;
    }

    private LogSink sink = new AbstractLogSink() {
        @Override
        public void log(final LogItem item) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    model.addElement(item);
                    list.ensureIndexIsVisible(model.getSize() - 1);
                }
            });
        }
    };
    
}
