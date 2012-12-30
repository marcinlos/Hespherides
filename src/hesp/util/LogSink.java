package hesp.util;

import hesp.gui.LogPanel;
import hesp.util.LogItem.Level;

/**
 * @author marcinlos
 * 
 * Logging interface for agents. Created to easily plug in {@link LogPanel}
 * without adding dependence on log4j or other 3rd party library.
 */
public interface LogSink {

    void log(LogItem item);

    void log(String text, Level level);

    void info(String text);
    
    void success(String text);
    
    void error(String text);
    
}
